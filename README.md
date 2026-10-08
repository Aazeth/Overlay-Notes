# Overlay Notes 📝

A floating sticky notes application for Android built with modern **Jetpack Compose**, **Kotlin Coroutines**, and **Room Database**. Overlay Notes allows users to write, pin, resize, and manage sticky notes on top of any active application.

---

## ✨ Fitur Utama (Key Features)

- 📌 **Floating Sticky Notes (Overlay Window)**: Buka dan sematkan catatan langsung melayang di atas aplikasi lain menggunakan `WindowManager`.
- ✋ **Interactive Drag & Resize**: Geser jendela ke posisi mana pun di layar atau ubah ukurannya sesuka hati melalui handle resize di sudut kanan bawah.
- 🎨 **Kustomisasi Tampilan & Tema**:
  - Pilihan **Theme Mode**: *Follow System*, *Light Mode*, atau *Dark Mode*.
  - Dukungan **Material You / Dynamic Colors** (Android 12+).
  - Pilihan warna kartu catatan (*color tags*).
- ⚙️ **Pengaturan Overlay Fleksibel**:
  - Atur transparansi / opacity overlay.
  - Mode Ringkas (*Compact Mode*) untuk menghemat ruang layar.
  - Sembunyikan atau tampilkan tanggal dan tombol *close* di header overlay.
- 💾 **Penyimpanan Lokal Andal (Offline-First)**: Disimpan aman di perangkat menggunakan SQLite melalui **Android Room Database**.
- 🔍 **Pencarian & Manajemen Cepat**: Cari dan kelola catatan dengan cepat dari halaman utama.

---

## 🛠️ Tech Stack & Arsitektur

- **Bahasa**: Kotlin
- **UI Framework**: Jetpack Compose & Material Design 3 (M3)
- **Arsitektur**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Database**: Android Jetpack Room
- **Asynchronous**: Kotlin Coroutines & StateFlow / SharedFlow
- **System Services**: Android `WindowManager`, `ForegroundService`

---

## 📱 Persyaratan Sistem (Prerequisites)

- **Minimum SDK**: Android 7.0 (API Level 24)
- **Target SDK**: Android 15 / 16 (API Level 36)
- **Izin Khusus**: `SYSTEM_ALERT_WINDOW` (*Tampilkan di atas aplikasi lain / Appear on top*)

---

## 🚀 Cara Menjalankan Project (Build & Run)

### 1. Menggunakan Android Studio
1. Clone repositori ini ke komputer lokal:
   ```bash
   git clone https://github.com/<username>/<repository-name>.git
   ```
2. Buka **Android Studio** (disarankan Jellyfish / Koala / Ladybug atau yang lebih baru).
3. Pilih **Open** dan arahkan ke folder project yang baru di-clone.
4. Tunggu proses **Gradle Sync** selesai.
5. Hubungkan perangkat Android fisik atau jalankan Android Emulator.
6. Klik tombol **Run 'app'** (`Shift + F10`).
7. Saat pertama kali membuka fitur overlay, berikan izin **"Appear on top"** / **"Display over other apps"** pada menu pengaturan Android.

### 2. Build APK via CLI
Jika menggunakan Gradle wrapper / CLI:
```bash
# Debug APK
gradle assembleDebug

# Output APK akan berada di:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 Lisensi (License)

Didistribusikan di bawah lisensi MIT atau lisensi terbuka pilihan Anda. Lihat file `LICENSE` untuk rincian lebih lanjut.
