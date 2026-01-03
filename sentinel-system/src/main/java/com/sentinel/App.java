package com.sentinel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFrame;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.global.opencv_imgcodecs;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class App {

    // SAFE FOR GITHUB: This pulls from your local settings
    private static final String WEBHOOK_URL = System.getenv("SENTINEL_WEBHOOK");
    private static final String LOG_FILE = "sentinel_activity_log.html";

    public static void main(String[] args) throws Exception {
        // --- HEALTH CHECK ---
        System.out.println("Checking Environment...");
        if (WEBHOOK_URL == null) {
            System.err.println("❌ CRITICAL ERROR: SENTINEL_WEBHOOK not found! Check launch.json");
            // If you are running via terminal, make sure to: export SENTINEL_WEBHOOK="url"
        } else {
            System.out.println("✅ Webhook Loaded Successfully.");
        }

        initLog();
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        try {
            grabber.start();
        } catch (Exception e) {
            System.err.println("Could not start grabber. Check Camera Permissions!");
            return;
        }

        File faceFile = new File("haarcascade_frontalface_alt.xml");
        if (!faceFile.exists()) {
            faceFile = new File(System.getProperty("user.dir") + "/haarcascade_frontalface_alt.xml");
        }

        CascadeClassifier faceDetector = new CascadeClassifier(faceFile.getAbsolutePath());
        CanvasFrame canvas = new CanvasFrame("Project Sentinel - ACTIVE");
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas.setAlwaysOnTop(true); 
        canvas.setCanvasSize(320, 240); 
        
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        canvas.setLocation((int)screenSize.getWidth() - 340, 50);

        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        long lastFaceDetectedTime = System.currentTimeMillis();
        int lockDelayMs = 3000; 
        boolean systemIsLocked = false; 

        while (canvas.isVisible()) {
            Frame frame = grabber.grab();
            if (frame == null) continue;
            Mat colorImage = converter.convert(frame);
            
            RectVector faces = new RectVector();
            faceDetector.detectMultiScale(colorImage, faces, 1.1, 8, 0, new Size(100, 100), new Size(1000, 1000));

            if (faces.size() > 0) {
                if (systemIsLocked) {
                    speak("Identity confirmed.");
                    writeToLog("Owner returned to desk.");
                }
                lastFaceDetectedTime = System.currentTimeMillis();
                systemIsLocked = false; 
                for (long i = 0; i < faces.size(); i++) {
                    rectangle(colorImage, faces.get(i), new Scalar(0, 255, 0, 0), 2, LINE_AA, 0);
                }
            } else {
                long timeSinceLastFace = System.currentTimeMillis() - lastFaceDetectedTime;
                if (timeSinceLastFace > lockDelayMs && !systemIsLocked) {
                    speak("Unauthorized. Locking.");
                    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    File evidenceFile = new File("evidence_" + timestamp + ".jpg");
                    opencv_imgcodecs.imwrite(evidenceFile.getAbsolutePath(), colorImage);
                    
                    String detailedMsg = "🔴 **SENTINEL SECURITY ALERT**\nTarget Lost at: " + new Date() + "\n📷 *Evidence attached.*";
                    sendDiscordAlertWithImage(detailedMsg, evidenceFile);
                    
                    Runtime.getRuntime().exec("pmset displaysleepnow"); 
                    systemIsLocked = true; 
                }
            }
            canvas.showImage(converter.convert(colorImage));
        }
        grabber.stop();
        canvas.dispose();
    }

    private static void speak(String text) {
        try { Runtime.getRuntime().exec(new String[]{"say", "-r", "250", text}); } catch (Exception e) {}
    }

    private static void initLog() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            File f = new File(LOG_FILE);
            if (f.length() == 0) {
                writer.write("<html><body style='background:#181818; color:#00FF00;'><h1>🛡️ Sentinel Logs</h1><hr>");
            }
        } catch (Exception e) {}
    }

    private static void writeToLog(String event) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write("<p>[" + new Date() + "] " + event + "</p>");
        } catch (Exception e) {}
    }

    private static void sendDiscordAlertWithImage(String message, File imageFile) {
        try {
            if (WEBHOOK_URL == null) return;
            ProcessBuilder pb = new ProcessBuilder("curl", "-X", "POST", "-F", "content=" + message, "-F", "file=@" + imageFile.getAbsolutePath(), WEBHOOK_URL);
            pb.start();
        } catch (Exception e) {}
    }
}