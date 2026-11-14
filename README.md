# TVBOX Optimizer for IPTV 🚀

**TVBOX Optimizer** is an open-source performance tuner and debloater designed specifically for **Android TV, TV Boxes (Allwinner, Rockchip, Amlogic), and Fire TV Stick** devices. It prepares devices for smooth, lag-free IPTV streaming (TiviMate, XCIPTV, IPTV Smarters Pro, OTT Navigator, etc.) by eliminating background resource hogs, optimizing network routes, and securing system privacy.

Available both as a standalone **Android Application (APK)** and as an **ADB automation script**.

---

## ⚡ Key Highlights (v2.1)

### 1. Quadruple DNS Anti-Censorship Matrix
Configures all 4 native Android DNS resolution slots to bypass local ISP throttling, route hijacking, and DNS poisoning without false positives:
* **`DNS 1` (`1.1.1.1`)**: Cloudflare Anycast — ultra-low latency.
* **`DNS 2` (`9.9.9.10`)**: **Quad9 Unfiltered** — Swiss privacy foundation, zero domain filtering, strict DNSSEC, and immunity to regional blocks.
* **`DNS 3` (`8.8.8.8`)**: Google Public DNS — global reliability and routing stability.
* **`DNS 4` (`1.0.0.1`)**: Cloudflare Secondary — instant fallback redundancy.

### 2. Chinese Firmware Dropper & Spyware Purge
Based on the **[Universal Android Debloater Next Generation (UAD-NG)](https://github.com/Universal-Debloater-Alliance/universal-android-debloater-next-generation)** database:
* Purgers silent background APK droppers and firmware adwares commonly found in cheap generic TV Boxes (`com.adups.fota`, `com.android.ad.daemon`, `com.rockchip.adservice`, `com.allwinner.service`, etc.).
* Disables heavy Google background services (`Google Play Services`, `Play Store`, `Google Services Framework`, `SetupWizard`, `TTS`).
* Disables unused stock browsers and competing heavy streaming apps.

### 3. IPTV Hardware & Network Tuning
* **Hardware Video Acceleration**: Forces direct GPU composition (`debug.sf.hw = 1` and `video.accelerate.hw = 1`) to eliminate dropped frames on 4K/FHD 60fps streams.
* **TCP Network Streaming Buffers**: Expands WiFi and Ethernet socket receive/transmit window buffers to reduce stream buffering and stutter.

### 4. Developer Settings & Extreme RAM Freeing
* Window, Transition, and Animator scales set to **`0x` (Disabled)** for instant UI response.
* **Always Finish Activities** enabled (`always_finish_activities = 1`) to free RAM immediately whenever an activity leaves the screen.
* **Background Process Limit** set to **`0`** (No background processes).

### 5. Brazil NTP & Clock Synchronization
* Synchronizes device clock with official Brazilian servers (`pool.ntp.br`, `a.st1.ntp.br`) and timezone `America/Sao_Paulo` (GMT-3) to ensure EPG guides and SSL/TLS certificates never fail.

### 6. Terminal-Style UI
* Sleek retro-terminal layout: Black background (`#000000`) with Neon Green (`#39FF14`) monospace typography.
* Real-time auto-scrolling log console.
* Interactive dialog on completion: *"FINISHED - Would you like to restart now?"*.

---

## 📁 Repository Structure

```
├── TVBOX_Optimizer_IPTV.apk   # Pre-compiled ready-to-install Android APK
├── README.md                  # English documentation & build guide
├── README_PT.md               # Documentação em Português
├── SKILL.md                   # AI Agent Skill manifest
├── scripts/
│   ├── optimize_tvbox.ps1     # ADB remote automation script
│   └── build_apk.ps1          # Standalone APK compilation script
├── app-source/                # Complete native Android Java source code
│   ├── AndroidManifest.xml
│   ├── res/
│   └── src/
└── references/
    └── commands.md            # Quick ADB reference cheatsheet
```

---

## 📲 How to Use

### Method 1: Direct Install on TV Box (APK)
1. Download **`TVBOX_Optimizer_IPTV.apk`** from this repository.
2. Transfer it to your TV Box via USB Flash Drive, browser, or Send Files to TV.
3. Open the app, grant requested permissions on initial launch.
4. Click **`[ 🚀 OPTIMIZE ]`**.
5. Once finished, confirm the prompt to reboot the device.

### Method 2: Remote via Computer (ADB)
Ensure **USB Debugging** or **Network Debugging** is enabled in Developer Options:
```powershell
powershell -ExecutionPolicy Bypass -File ".\scripts\optimize_tvbox.ps1" -DeviceIp <TVBOX_IP> -Reboot
```

---

## 🛠️ How to Build the APK from Source

Anyone can build their own signed APK without opening an IDE.

### Prerequisites:
* **JDK 8 or JDK 17+** (with `javac` accessible or `JAVA_HOME` set).
* **Android SDK** with Platform `android-30` (Android 11) and Build-Tools (`aapt`, `d8`, `zipalign`, `apksigner`).

### Build Command:
Run the automated build script:
```powershell
powershell -ExecutionPolicy Bypass -File ".\scripts\build_apk.ps1"
```
Or specify paths manually if they are not in your environment variables:
```powershell
powershell -ExecutionPolicy Bypass -File ".\scripts\build_apk.ps1" -AndroidSdkPath "C:\path\to\android-sdk" -JdkPath "C:\path\to\jdk"
```

The script will automatically compile Java sources, convert them to Dalvik bytecode (`classes.dex`), package resources, align, and sign the output at `TVBOX_Optimizer_IPTV.apk`.

---

## 📄 License
Open source and distributed under the MIT License. Contributions and suggestions are welcome!
