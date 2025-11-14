### Real-Time Edge Detection Pipeline + TypeScript Web Viewer  
Technical Assessment — Android + NDK + OpenCV + OpenGL + Web

---

## Overview

This project implements a real-time camera processing pipeline on Android using:

- Camera2 API (YUV_420_888 capture)
- JNI + NDK (Java/Kotlin ↔ C++)
- OpenCV (C++) for Canny Edge Detection
- OpenGL ES 2.0 for texture rendering
- TypeScript Web Viewer (debug interface)

The goal is to demonstrate the ability to integrate Android-native components, native C++, computer vision, real-time rendering, and web tooling in one clean, modular project.

---

## Features

### Android App
- Real-time camera frames captured using Camera2  
- Y-plane extracted and passed into C++ via JNI  
- C++ OpenCV performs **Canny Edge Detection**  
- Results converted to RGBA and returned to Kotlin  
- Rendered in real-time using **OpenGL ES 2.0**  
- Runs at **~14 FPS** at **640 × 480** resolution  

### C++ / OpenCV
- Efficient grayscale → edge detection pipeline  
- Zero-copy Mat wrapping where possible  
- Minimal memory overhead  

### OpenGL Renderer
- Custom GLSurfaceView renderer  
- Fullscreen quad with texture coordinates  
- Dynamic texture upload each frame  

### TypeScript Web Debug Viewer
- Displays a static processed frame  
- Shows FPS + resolution  
- Clean TypeScript setup with `tsc`  
- Simple and professional debug interface  

---

## 🧠 Architecture Diagram

┌──────────────┐     ┌───────────────┐     ┌──────────────────┐     ┌──────────────────┐
│ Camera2 API  │ --> │ Y Plane (NV21)│ --> │ JNI Bridge (C++) │ --> │ OpenCV Canny     │
└──────────────┘     └───────────────┘     └──────────────────┘     └──────────────────┘
                                                                              │
                                                                              ▼
                                                                        RGBA ByteArray
                                                                              │
                                                                              ▼
                                                                  ┌──────────────────────────┐
                                                                  │ OpenGL ES Renderer       │
                                                                  └──────────────────────────┘
                                                                              │
                                                                              ▼
                                                                  Displayed via GLSurfaceView



##  Sample Output (Edge Detection)

> Resolution: **640 × 480**  
> FPS: **12 FPS (average)** 

<img width="1917" height="925" alt="image" src="https://github.com/user-attachments/assets/3fbb17e5-6b41-4e87-a3bb-54e2fc2a1013" />

##   Output
![WhatsApp Image 2025-11-14 at 5 58 06 AM](https://github.com/user-attachments/assets/2dbec445-d3cb-4b1f-a469-d837e47b72f3)
![WhatsApp Image 2025-11-14 at 5 59 36 AM](https://github.com/user-attachments/assets/3f0770fd-0bef-4416-9e46-d7689a2518da)

---

## 📁 Project Structure

android-opencv-gl/
├── app/
│ ├── src/main/
│ │ ├── java/com/example/edgeviewer/
│ │ │ ├── MainActivity.kt
│ │ │ ├── camera/CameraController.kt
│ │ │ ├── gl/EdgeGLSurfaceView.kt
│ │ │ └── gl/EdgeRenderer.kt
│ │ ├── cpp/
│ │ │ ├── native-lib.cpp
│ │ │ └── CMakeLists.txt
│ │ ├── res/layout/activity_main.xml
│ │ └── AndroidManifest.xml
│ └── build.gradle.kts
│
├── web/
│ ├── src/index.ts
│ ├── dist/index.js
│ ├── index.html
│ └── tsconfig.json
│
└── README.md


## ⚙️ Setup Instructions

### 1. Clone Repository
```bash
git clone https://github.com/<your-username>/android-opencv-gl.git
cd android-opencv-gl
2. Android Setup
Requirements
Android Studio (Otter 2025+)

NDK + CMake installed

OpenCV Android SDK downloaded

Configure CMakeLists.txt
Update the path in:

set(OpenCV_DIR "C:/Users/<you>/OpenCV-android-sdk/sdk/native/jni")
Then rebuild:

mathematica
Build → Rebuild Project  
Run → Run 'app'
3. Web Viewer Setup
bash
cd web
npm install
npx tsc
Open:

bash
web/index.html
🧪 Evaluation Checklist
Native C++ Integration — ✔

OpenCV Canny Implementation — ✔

OpenGL Rendering — ✔

Camera2 YUV pipeline — ✔

TypeScript Web Debug UI — ✔

Clean Modular Project Structure — ✔

Proper Git Commit History — ✔

🛠 Technologies Used
Kotlin / Android SDK

Camera2 API

OpenGL ES 2.0

JNI / NDK

C++17

OpenCV 4.x

TypeScript

HTML / CSS

👨‍💻 Author
Amogh Tolagi
Android R&D — Native Camera, OpenCV, OpenGL, TypeScript
GitHub: https://github.com/AmoghT32
