from ultralytics import YOLO
import cv2
import os

# model
model = YOLO("yolo-Weights/yolo26n_new.pt")

# object classes
classNames = ["fallen", "falling", "sitting", "stand"]

