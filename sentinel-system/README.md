# 🛡️ Sentinel: AI-Powered Autonomous Security

Sentinel is a real-time computer vision security agent for macOS. It monitors physical presence using biometric detection and executes autonomous defensive protocols if the owner is not detected.

## 🚀 Features
- **Biometric Presence Monitoring:** Uses OpenCV Haar Cascade classifiers to verify human presence in real-time.
- **Autonomous Lockdown:** Automatically triggers macOS system sleep/lock commands (`pmset`) within a 3-second grace period of target loss.
- **Smart Alarm System:** Integrated Text-to-Speech (TTS) engine that provides audible security warnings to potential intruders.
- **Forensic Evidence Capture:** Automatically captures and timestamps `.jpg` evidence of the last frame before system lockdown.
- **IoT Notifications:** Instant security alerts with visual evidence pushed to mobile via Discord Webhooks.
- **Persistence Log:** Generates a local HTML-based activity log for security auditing.
- **Sticky HUD:** A non-intrusive, "Always-on-Top" heads-up display for real-time monitoring.

## 🛠️ Tech Stack
- **Java 17+**
- **JavaCV / OpenCV** (Computer Vision)
- **macOS System API** (Hardware Management)
- **Discord API** (Remote Alerts)
- **Maven** (Build Tool)

## 📦 Setup & Execution
1. **Clone the Repo:**
   ```bash
   git clone [https://github.com/harideeshan/sentinel-system.git](https://github.com/harideeshan/sentinel-system.git)