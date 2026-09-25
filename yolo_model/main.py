import time

import cv2
import requests
from ultralytics import YOLO

JETSON_URL = "http://127.0.0.1:8000/api/yolo"
PREDICTION_INTERVAL_SECONDS = 15

model = YOLO("yolo-Weights/yolo26n.pt")


def make_prediction(img) -> dict:
    """Runs one detection pass and returns the highest-confidence result, if any."""
    result = model.predict(source=img, save=True, verbose=False)[0]

    if len(result.boxes) == 0:
        return {"cls": None, "label": None}

    top_idx = int(result.boxes.conf.argmax())
    cls_id = int(result.boxes.cls[top_idx])
    return {"cls": cls_id, "label": result.names.get(cls_id)}


def send_prediction(prediction: dict) -> None:
    try:
        requests.post(JETSON_URL, json=prediction, timeout=5)
    except requests.RequestException as exc:
        print(f"Nie udało się wysłać predykcji do jetson_app: {exc}")


if __name__ == '__main__':
    while True:
        img = cv2.imread("test_images/test.jpg")
        if img is None:
            print("Nie udało się wczytać obrazu test_images/test.jpg")
        else:
            prediction = make_prediction(img)
            print(prediction)
            send_prediction(prediction)

        time.sleep(PREDICTION_INTERVAL_SECONDS)
