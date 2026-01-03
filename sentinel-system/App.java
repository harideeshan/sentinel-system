package com.sentinel;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;

public class App {
    public static void main(String[] args) throws Exception {
        // 1. Initialize the Camera
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start();

        // 2. Load the "Brain" (Face Detection Model)
        // This file is built into the OpenCV library we downloaded
        CascadeClassifier faceDetector = new CascadeClassifier();
        String classifierPath = "C:/path/to/haarcascade_frontalface_alt.xml"; 
        // Note: For now, JavaCV often finds this automatically, but we will refine this!
        
        CanvasFrame canvas = new CanvasFrame("Project Sentinel - Scanning...");
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

        while (canvas.isVisible()) {
            Frame frame = grabber.grab();
            if (frame == null) continue;

            // Convert video frame to OpenCV "Mat" (Matrix of pixels)
            Mat colorImage = converter.convert(frame);

            // 3. Detect Faces
            RectVector faces = new RectVector();
            // faceDetector.detectMultiScale(colorImage, faces); // We'll uncomment this once we verify the XML path

            // 4. Draw Rectangles (The "Sentinel" HUD)
            for (long i = 0; i < faces.size(); i++) {
                Rect face = faces.get(i);
                rectangle(colorImage, face, new Scalar(0, 255, 0, 0)); // Green Box
            }

            canvas.showImage(converter.convert(colorImage));
        }

        grabber.stop();
        canvas.dispose();
    }
}