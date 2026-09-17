from ultralytics import YOLO

if __name__ == "__main__":
    model = YOLO("yolo-Weights/yolo26n.pt")
    results = model.train(data="datasets/test_data", epochs=10, imgsz=640)

    model.save("yolo-Weights/yolo26n_new.pt")