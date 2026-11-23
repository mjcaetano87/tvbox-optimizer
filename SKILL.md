---
name: tvbox
description: "TV Box / Android TV debloater and optimizer for IPTV: forces Root/Admin mode first, installs embedded KingoRoot/iRoot if needed, disables bloatware and chinese droppers, forces 4-slot DNS matrix (1.1.1.1, 9.9.9.10, 8.8.8.8, 1.0.0.1), syncs Brazil NTP, turns off animations (0x) and enforces zero background processes."
---

# TVBOX - Android TV & TV Box Optimizer for IPTV (v3.0-ROOT)

A skill and standalone utility to automate deep cleaning and performance optimization on **Android TV, TV Box and Fire TV Stick** devices via **ADB** (Android Debug Bridge) or directly on the device using the compiled **APK**.

---

## 🎯 2-Phase Execution Workflow

1. **Phase 1: Force Administrator & ROOT Mode (First):**
   - Audits system permissions: `WRITE_SECURE_SETTINGS`, battery optimization whitelist, `appops`.
   - Checks native Superuser access (`su uid=0`).
   - If not rooted: extracts and provides 1-click root installers (`Kingo ROOT` & `iRoot`) bundled directly in APK assets.
   - Loopback ADB detection (`127.0.0.1:5555`) and silent elevation.

2. **Phase 2: Full IPTV Tuning & BadBox Purge (Only Then):**
   - **Quadruple DNS Anti-Block Matrix:** `1.1.1.1`, `9.9.9.10`, `8.8.8.8`, `1.0.0.1`.
   - **Chinese Firmware Dropper & Spyware Purge:** Triple-kill (`force-stop`, `appops ignore`, `disable-user`, `hide`, `uninstall`).
   - **Developer Settings Tuning:** Window, Transition, Animator scales at `0x (Off)`.
   - **Max Free RAM:** `Always Finish Activities = 1`, Background process limit = `0`.
   - **NTP Brazil & Clock Sync:** Fuso fixo GMT-03:00, NTP em `pool.ntp.br`.
   - **Streaming Video Hardware Acceleration:** `debug.sf.hw = 1`, 2MB TCP socket buffers.

---

## 🚀 Execution

### Direct Device Execution (APK)
Open `TVBOX_Optimizer_IPTV.apk` on Android TV and use DPAD:
1. `[ 👑 1. ROOT / ADMIN ]`
2. `[ 🚀 2. OTIMIZAR ]`
3. `[ 🔄 3. REINICIAR ]`

### Remote ADB Execution (PC)
```powershell
powershell -ExecutionPolicy Bypass -File ".\scripts\optimize_tvbox.ps1" -DeviceIp <TVBOX_IP> -Reboot
```
