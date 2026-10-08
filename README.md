# Overlay Notes 📝

A floating sticky notes app for Android designed for effortless multitasking—take notes without switching apps.

> **Note:** An experimental project built with the assistance of Google AI Studio.

---

## ✨ Features

- 📌 **Floating Overlay**: Keep notes pinned on top of any app (videos, games, browser).
- ✋ **Drag & Resize**: Freely move and adjust note window dimensions on the fly.
- 🎨 **Material You & Dark Mode**: Supports system themes, dark mode, and custom note colors.
- ⚙️ **Customizable**: Adjustable background opacity and a Compact Mode.
- 💾 **Offline-First**: All data is stored locally via Room Database for maximum privacy.

---

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Database**: Room (SQLite)
- **Core**: Android `WindowManager` & `ForegroundService`

---

## 📱 Requirements & Permissions

- **Min SDK**: Android 7.0 (API 24)
- **Required Permission**: `SYSTEM_ALERT_WINDOW` (*"Appear on top"*)

---

## 🚀 How to Build

1. Clone the repository:
   ```bash
   git clone [https://github.com/Aazeth/Overlay-Notes.git](https://github.com/Aazeth/Overlay-Notes.git)
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle and click **Run**.
4. Grant the **"Appear on top"** permission when prompted on your device.

---

## 📄 License

Distributed under the [MIT License](LICENSE).
