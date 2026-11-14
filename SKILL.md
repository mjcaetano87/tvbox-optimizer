---
name: tvbox
description: "TV Box / Android TV debloater and optimizer for IPTV: disables bloatware and chinese droppers, forces 4-slot DNS matrix (1.1.1.1, 9.9.9.10, 8.8.8.8, 1.0.0.1), syncs Brazil NTP, turns off animations (0x) and enforces zero background processes."
---

# TVBOX - Android TV & TV Box Optimizer for IPTV

A skill and standalone utility to automate deep cleaning and performance optimization on **Android TV, TV Box and Fire TV Stick** devices via **ADB** (Android Debug Bridge) or directly on the device using the compiled **APK**.

---

## 🎯 Key Features

1. **Quadruple DNS Anti-Block Matrix:**
   - `DNS 1 (1.1.1.1)`: Cloudflare Anycast (Ultra-low latency).
   - `DNS 2 (9.9.9.10)`: Quad9 Unfiltered (Swiss privacy, no censorship, zero domain blocking to prevent false positives).
   - `DNS 3 (8.8.8.8)`: Google Public DNS.
   - `DNS 4 (1.0.0.1)`: Cloudflare Secondary.

2. **Chinese Firmware Dropper & Spyware Purge:**
   - Disables silent background droppers (`com.adups.fota`, `com.android.ad.daemon`, `com.rockchip.adservice`, etc.).
   - Disables background Google telemetry, store and competing heavy media services.

3. **Developer Settings Tuning:**
   - Window, Transition, and Animator scales set to **0x (Off)**.
   - **Always Finish Activities** enabled (destroys activities immediately upon exit).
   - **Background Process Limit** set to **0** for maximum free RAM.

4. **NTP Brazil & Clock Sync:**
   - Timezone set to `America/Sao_Paulo` (GMT-3).
   - NTP servers pointed to `pool.ntp.br` and `a.st1.ntp.br`.

---

## 🚀 Execution

```powershell
powershell -ExecutionPolicy Bypass -File ".\scripts\optimize_tvbox.ps1" -DeviceIp <TVBOX_IP> -Reboot
```
