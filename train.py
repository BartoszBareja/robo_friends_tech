from ultralytics import YOLO

if __name__ == "__main__":
    model = YOLO("yolo-Weights/yolo26n.pt")
    model.train(data="datasets/dataset4/data.yaml", epochs=15, imgsz=640, device="cuda")