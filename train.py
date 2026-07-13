from ultralytics import YOLO
model = YOLO("yolo-Weights/yolo26n.pt")

model.train(data="datasets/dataset4/data.yaml", epochs=50, imgsz=640)