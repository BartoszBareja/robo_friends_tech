from ultralytics import YOLO

model = YOLO("yolo-Weights/yolo26n.pt")
model.train(data="datasets/test_data", epochs=5, imgsz=640)

model.save("yolo26n_new.pth")