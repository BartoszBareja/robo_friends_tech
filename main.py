from ultralytics import YOLO
import cv2
import os

# model
model = YOLO("yolo-Weights/yolo26n_new.pt")

# object classes
classNames = ["fallen", "falling", "sitting", "stand"]

test_images = []

for test_image in os.listdir("test_images"):
    test_images.append("test_images/" + test_image)

results = model(test_images)

for i in range(len(results)):
    results[i].save(filename=f"results/result{i}.jpg")