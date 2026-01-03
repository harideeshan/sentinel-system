package com.sentinel;

import java.io.File;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.global.opencv_objdetect;

import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;

public class App {
    public static void main(String[] args) throws Exception {
        // 1. Initialize the Camera
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start();

        // 2. Load the "Brain" (The XML file that knows what a face looks like)
        // This magic line finds the file inside your Maven dependencies automatically
        File faceFile = Loader.cacheResource(opencv_objdetect.class, "org/bytedeco/opencv/etc/haarcascades/haarcascade_frontalface_alt.xml");
        
        if (faceFile == null) {
            System.err.println("Could not find the Haar Cascade XML file!");
            return;
        }

        CascadeClassifier faceDetector = new CascadeClassifier(faceFile.getAbsolutePath());
        
        CanvasFrame canvas = new CanvasFrame("Project Sentinel - Scanning...");
        // This lets us draw boxes on the image
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

        System.out.println("Sentinel is active. Looking for faces...");

        while (canvas.isVisible()) {
            Frame frame = grabber.grab();
            if (frame == null) continue;

            // Convert video frame to Mat (Matrix) for processing
            Mat colorImage = converter.convert(frame);

            // 3. The Detection Logic
            RectVector faces = new RectVector();
            faceDetector.detectMultiScale(colorImage, faces);

            // 4. Draw the HUD (Heads-Up Display)
            long totalFaces = faces.size();
            for (long i = 0; i < totalFaces; i++) {
                Rect face = faces.get(i);
                // Draw a green box (BGR color: 0, 255, 0) around the face
                rectangle(colorImage, face, new Scalar(0, 255, 0, 0));
            }

            // Show the result in the window
            canvas.showImage(converter.convert(colorImage));
        }

        grabber.stop();
        canvas.dispose();
    }
}