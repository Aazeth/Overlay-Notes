# Overlay Notes 📝

> **An experimental Android project created via Google AI Studio, born out of frustration with conventional note-taking apps while multitasking due to limited coding skills.**

---

## 💡 The Story Behind the Project

**Overlay Notes** is an experimental project that sparked from a simple, everyday annoyance: **how painful it is to use regular note-taking apps when trying to multitask on Android.**

Whenever you are watching a video lecture, referencing a recipe, comparing prices while shopping, or copying verification codes and math formulas, standard note apps force you to constantly switch back and forth between screens or rely on cumbersome split-screen modes that shrink your view. 

This project was built to test a frictionless alternative: **floating, persistent sticky notes that sit on top of any active application**, allowing quick note-taking, reading, and drafting without ever leaving your current workflow.

---

## ✨ Key Features

- 📌 **True Floating Overlay Window**: Keep your notes pinned directly on top of games, browsers, video players, or reading apps via Android's `WindowManager`.
- ✋ **Interactive Drag & Resize**: Smoothly reposition your note anywhere across the screen, or drag the resize handle at the bottom-right corner to adjust its size on the fly.
- 🎨 **Adaptive Theming & Material You**:
  - Full support for **Follow System**, **Light Mode**, and **Dark Mode**.
  - Dynamic Monet color palettes on Android 12+ devices.
  - Custom color tags for organizing notes visually.
- ⚙️ **Tailored Overlay Customization**:
  - Configurable background transparency / opacity.
  - **Compact Mode** to minimize screen clutter.
  - Toggle date display and quick close actions on the overlay header.
- 💾 **Reliable Local Storage (Offline-First)**: Powered by Android Jetpack **Room Database** (SQLite)—your notes stay on your device with zero cloud dependency.
- 🔍 **Instant Search & Management**: Easily organize, search, edit, and delete notes through the full dashboard screen.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (M3)
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Local Persistence**: Jetpack [Room Database](https://developer.android.com/training/data-storage/room)
- **Concurrency & State**: Kotlin Coroutines & `StateFlow` / `SharedFlow`
- **System Windowing**: Android `WindowManager` & `ForegroundService`

---

## 📱 Permissions & Requirements

- **Minimum SDK**: Android 7.0 (API Level 24)
- **Target SDK**: Android 15 / 16 (API Level 36)
- **Key Permission**:
  - `SYSTEM_ALERT_WINDOW` (*"Display over other apps"* / *"Appear on top"*): Required to render the floating sticky note window over other applications.

---

## 🚀 How to Build & Run

### 1. With Android Studio
1. Clone this repository:
   ```bash
   git clone https://github.com/Aazeth/Overlay-Notes.git
   ```
2. Open **Android Studio** (Jellyfish, Koala, Ladybug, or newer recommended).
3. Select **Open** and choose the cloned directory.
4. Allow Gradle to sync dependencies.
5. Connect your Android device or start an emulator.
6. Click **Run 'app'** (`Shift + F10`).
7. When prompted, grant the **"Appear on top"** permission in Android system settings.

### 2. With Command Line (CLI)
```bash
# Build Debug APK
./gradlew assembleDebug
# (or 'gradle assembleDebug' if gradlew wrapper is not installed)

# The generated APK will be located at:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🧪 Project Status & Disclaimer

This is an **experimental exploration**. It was developed to experiment with custom window overlay managers, gesture detection inside Android floating services, and seamless Compose embedding into floating window hierarchies. Feel free to fork, experiment, or adapt the overlay architecture for your own ideas!

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
