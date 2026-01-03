package com.sentinel;

import java.io.File;
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
        try {
            grabber.start();
        } catch (Exception e) {
            System.err.println("Could not start grabber. Check Camera Permissions!");
            return;
        }

        // 2. Load the Brain (Using the local file)
        File faceFile = new File("haarcascade_frontalface_alt.xml");
        if (!faceFile.exists()) {
            // Fallback: Check if it's in the project root specifically for Mac
            faceFile = new File(System.getProperty("user.dir") + "/haarcascade_frontalface_alt.xml");
        }

        if (!faceFile.exists()) {
            System.err.println("XML NOT FOUND! Please ensure the file is in: " + faceFile.getAbsolutePath());
            return;
        }

        CascadeClassifier faceDetector = new CascadeClassifier(faceFile.getAbsolutePath());
        CanvasFrame canvas = new CanvasFrame("Project Sentinel - ACTIVE SECURITY");
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

        System.out.println("Sentinel is active. If you leave the frame, I will lock this Mac.");

        // Security logic variables
        long lastFaceDetectedTime = System.currentTimeMillis();
        int lockDelayMs = 3000; // 3 seconds grace period

        while (canvas.isVisible()) {
            Frame frame = grabber.grab();
            if (frame == null) continue;

            Mat colorImage = converter.convert(frame);
            RectVector faces = new RectVector();
            
            // Detect faces
            faceDetector.detectMultiScale(colorImage, faces);

            if (faces.size() > 0) {
                // We see a face! Reset the timer
                lastFaceDetectedTime = System.currentTimeMillis();
                
                for (long i = 0; i < faces.size(); i++) {
                    Rect face = faces.get(i);
                    rectangle(colorImage, face, new Scalar(0, 255, 0, 0)); // Green box
                }
            } else {
                // No face detected! Check how long it's been empty
                long timeSinceLastFace = System.currentTimeMillis() - lastFaceDetectedTime;
                
                if (timeSinceLastFace > lockDelayMs) {
                    System.out.println("INTRUDER OR EMPTY CHAIR DETECTED! LOCKING SYSTEM...");
                    
                    // GOD TIER TRIGGER: Mac Lock Command
                    Runtime.getRuntime().exec("pmset displaysleepnow"); 
                    
                    // Reset timer so it doesn't spam the command
                    lastFaceDetectedTime = System.currentTimeMillis();
                }
            }

            canvas.showImage(converter.convert(colorImage));
        }

        grabber.stop();
        canvas.dispose();
    }
}