from ultralytics import YOLO
import cv2
import math
import os

# model
model = YOLO("yolo-Weights/best.pt")

# object classes — must match the trained model's class order
classNames = ["fall", "person"]
boxColors = [(0, 0, 255), (0, 255, 0)]  # BGR: fall=red, person=green

images = os.listdir("test_data")


for image in images:
    print(image)
    img = cv2.imread(f"test_data/{image}")
    results = model(img, stream=True, conf=0.1)

    detections = []
    for r in results:
        for box in r.boxes:
            x1, y1, x2, y2 = box.xyxy[0]
            x1, y1, x2, y2 = int(x1), int(y1), int(x2), int(y2)
            confidence = math.ceil((box.conf[0]*100))/100
            cls = int(box.cls[0])
            detections.append((confidence, cls, x1, y1, x2, y2))

    detections.sort(key=lambda d: d[0], reverse=True)

    print("\nTop 6 predictions:")
    for i, (confidence, cls, x1, y1, x2, y2) in enumerate(detections[:6]):
        print(f"  {i+1}. {classNames[cls]}: {confidence:.2f}")
        color = boxColors[cls % len(boxColors)]
        cv2.rectangle(img, (x1, y1), (x2, y2), color, 2)
        label = f"{classNames[cls]} {confidence:.2f}"
        (label_w, label_h), _ = cv2.getTextSize(label, cv2.FONT_HERSHEY_SIMPLEX, 0.6, 2)
        cv2.rectangle(img, (x1, y1 - label_h - 8), (x1 + label_w + 4, y1), color, -1)
        cv2.putText(img, label, (x1 + 2, y1 - 5), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 255), 2)

    print("--"*40)

    cv2.imshow(image, img)
    cv2.waitKey(0)
    cv2.destroyAllWindows()
