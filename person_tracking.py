from __future__ import print_function
from imutils.video import VideoStream
from imutils.object_detection import non_max_suppression
import numpy as np
import argparse
import imutils
import time
import cv2
import serial
import pynmea2
from firebase import firebase

print("[INFO] open arduino serial port...")
ser = serial.Serial("/dev/ttyACM0",9600)
ser.flushOutput()
time.sleep(0.1)
print("OK")

print("[INFO] connect gps...")
port = "/dev/ttyUSB0"
serialPort = serial.Serial(port, baudrate = 9600, timeout = 0.5)
time.sleep(0.1)
print("OK")

print("[INFO] connect firebase...")
firebase = firebase.FirebaseApplication('https://fir-a8117.firebaseio.com', None)
firebase.put('User/1', 'latitude', '')
firebase.put('User/1', 'longitude', '')
time.sleep(0.1)
print("OK")

def parseGPS(str):
	if str.find('GGA') > 0:
		msg = pynmea2.parse(str)
		firebase.put('User/1', 'latitude', '%s' % msg.latitude)
		firebase.put('User/1', 'longitude', '%s' % msg.longitude)

hog = cv2.HOGDescriptor()
hog.setSVMDetector(cv2.HOGDescriptor_getDefaultPeopleDetector())
print("[INFO] start video stream...")
vs = VideoStream(usePiCamera=True).start()
time.sleep(1.0)

while True:	
	frame = vs.read()
	frame = imutils.resize(frame, width=min(400, frame.shape[1]))
	orig = frame.copy()
	
	(rects, weights) = hog.detectMultiScale(frame, winStride=(4, 4),
		padding=(8, 8), scale=1.05)
		
	for (x, y, w, h) in rects:
		cv2.rectangle(orig, (x, y), (x + w, y + h), (0, 0, 255), 2)
		
	rects = np.array([[x, y, x + w, y + h] for (x, y, w, h) in rects])
	pick = non_max_suppression(rects, probs=None, overlapThresh=0.65)
	
	a = 0;
	b = 0;
	c = 0;
	for (xA, yA, xB, yB) in pick:
		cv2.rectangle(frame, (xA, yA), (xB, yB), (0, 255, 0), 2)
		if xA >= 0 and xA < 120 :
			a = a + 1
		elif xA >= 120 and xA <= 199 :
			b = b + 1
		elif xA > 199 and xA <= 319 :
			c = c + 1
	
	if a == 0 and b == 0 and c == 0 :
		ser.write(b'4')
		most = 0
	elif a <= b :
		if b >= c :
			ser.write(b'2')
			most = 2
		elif b < c :
			ser.write(b'3')
			most = 3
	elif a > b :
		if a >= c :
			ser.write(b'1')
			most = 1
		elif a < c :
			ser.write(b'3')
			most = 3
	
	cv2.imshow("Frame", frame)
	print("[INFO] {} person detected.\n       {} {} {} located.\n       {} is most.\n".format(len(pick), a, b, c, most))
	
    str = serialPort.readline()
	parseGPS(str)
    
	key = cv2.waitKey(1) & 0xFF
	if key == ord("q"):
		ser.write(b'5')
		break

ser.close()
serialPort.close()
cv2.destroyAllWindows()
vs.stop()
