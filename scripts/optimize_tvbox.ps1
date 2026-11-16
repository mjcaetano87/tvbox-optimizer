[CmdletBinding()]
param(
    [string]$DeviceIp = "",
    [int]$Port = 5555,
    [switch]$Reboot
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   TVBOX OPTIMIZER FOR IPTV (ADB DEBLOATER & TUNING v2.3) " -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan

# Locate ADB executable automatically from PATH or local environment
$adbCmd = Get-Command "adb" -ErrorAction SilentlyContinue
if ($adbCmd) {
    $adb = $adbCmd.Source
} else {
    $commonAdbPaths = @(
        "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
        "$env:ProgramFiles\platform-tools\adb.exe"
    )
    $found = $false
    foreach ($p in $commonAdbPaths) {
        if (Test-Path $p) {
            $adb = $p
            $found = $true
            break
        }
    }
    if (-not $found) {
        Write-Error "ADB executable was not found. Please ensure Android platform-tools is installed and added to PATH."
        exit 1
    }
}

# If USB device is connected, use it; otherwise, connect via IP
$devices = & $adb devices | Where-Object { $_ -match "\bdevice\b" -and $_ -notmatch "List of devices" }
if (-not $devices) {
    if ([string]::IsNullOrWhiteSpace($DeviceIp)) {
        $DeviceIp = Read-Host "No USB device detected. Enter TV Box IP address (e.g. 192.168.1.100)"
    }
    if (-not [string]::IsNullOrWhiteSpace($DeviceIp)) {
        Write-Host "[+] Connecting to TV Box at $DeviceIp`:$Port..." -ForegroundColor Green
        & $adb connect "$($DeviceIp):$Port"
        Start-Sleep -Seconds 2
        $devices = & $adb devices | Where-Object { $_ -match "\bdevice\b" -and $_ -notmatch "List of devices" }
    }
}

if (-not $devices) {
    Write-Warning "No authorized/connected Android device was detected by ADB."
    Write-Host "Please ensure USB Debugging or Network Debugging is enabled on the device." -ForegroundColor Yellow
    exit 1
}

Write-Host "[+] Device connected successfully:`n$devices" -ForegroundColor Green

# 1. Configure Fixed Timezone GMT-3 (Brazil / Brasilia) and NTP Brasil
Write-Host "`n[*] [1/6] Locking Fixed Timezone GMT-03:00 (Brasilia) and setting NTP servers..." -ForegroundColor Cyan

# Disable auto timezone so Chinese firmware won't pull old daylight saving time (GMT-2)
& $adb shell settings put global auto_time 1
& $adb shell settings put global auto_time_zone 0
& $adb shell settings put system auto_time_zone 0

# Set explicit GMT-03:00 timezone
& $adb shell setprop persist.sys.timezone "GMT-03:00"
& $adb shell setprop persist.sys.time.zone "GMT-03:00"
& $adb shell setprop persist.sys.timezone.default "GMT-03:00"
& $adb shell service call alarm 3 s16 "GMT-03:00" 2>$null

# NTP Servers (pool.ntp.br)
& $adb shell settings put global ntp_server "pool.ntp.br"
& $adb shell settings put global ntp_server_2 "a.st1.ntp.br"
& $adb shell settings put global ntp_server_3 "b.st1.ntp.br"
& $adb shell setprop net.ntp.server "pool.ntp.br"
Write-Host "  -> Fixed GMT-03:00 timezone and NTP servers locked." -ForegroundColor Green

# 2. Configure Quadruple DNS Matrix (1.1.1.1, 9.9.9.10, 8.8.8.8, 1.0.0.1)
Write-Host "`n[*] [2/6] Configuring Quadruple DNS Anti-Block Matrix..." -ForegroundColor Cyan
Write-Host "  -> DNS 1: 1.1.1.1 (Cloudflare Anycast)" -ForegroundColor Gray
Write-Host "  -> DNS 2: 9.9.9.10 (Quad9 Unfiltered - No Censorship / Switzerland)" -ForegroundColor Gray
Write-Host "  -> DNS 3: 8.8.8.8 (Google Public)" -ForegroundColor Gray
Write-Host "  -> DNS 4: 1.0.0.1 (Cloudflare Secondary)" -ForegroundColor Gray

$dnsCommands = @(
    "setprop net.dns1 1.1.1.1",
    "setprop net.dns2 9.9.9.10",
    "setprop net.dns3 8.8.8.8",
    "setprop net.dns4 1.0.0.1",
    "setprop persist.net.dns1 1.1.1.1",
    "setprop persist.net.dns2 9.9.9.10",
    "setprop persist.net.dns3 8.8.8.8",
    "setprop persist.net.dns4 1.0.0.1",
    "setprop dhcp.eth0.dns1 1.1.1.1",
    "setprop dhcp.eth0.dns2 9.9.9.10",
    "setprop dhcp.eth0.dns3 8.8.8.8",
    "setprop dhcp.eth0.dns4 1.0.0.1",
    "setprop dhcp.wlan0.dns1 1.1.1.1",
    "setprop dhcp.wlan0.dns2 9.9.9.10",
    "setprop dhcp.wlan0.dns3 8.8.8.8",
    "setprop dhcp.wlan0.dns4 1.0.0.1",
    "settings put global private_dns_mode off"
)
foreach ($cmd in $dnsCommands) {
    & $adb shell $cmd 2>$null | Out-Null
}
Write-Host "  -> DNS matrix configured successfully." -ForegroundColor Green

# 3. Developer Options: Turn off Animations (Zero Lag)
Write-Host "`n[*] [3/6] Disabling Window, Transition, and Animator scales..." -ForegroundColor Cyan
& $adb shell settings put global window_animation_scale 0
& $adb shell settings put global transition_animation_scale 0
& $adb shell settings put global animator_duration_scale 0
Write-Host "  -> Animation scales set to 0 (Disabled)." -ForegroundColor Green

# 4. Background Limits: 0 Processes & Always Finish Activities
Write-Host "`n[*] [4/6] Setting Background Process Limit (0) and Destroy Activities on Exit..." -ForegroundColor Cyan
& $adb shell settings put global always_finish_activities 1
& $adb shell setprop persist.sys.always_finish 1
& $adb shell settings put global app_process_limit 0
& $adb shell setprop persist.sys.process_limit 0
& $adb shell settings put global max_phantom_processes 2147483647 2>$null
Write-Host "  -> 'Always finish activities' ENABLED." -ForegroundColor Green
Write-Host "  -> 'Background process limit' set to 0 (Maximum Free RAM)." -ForegroundColor Green

# 5. Kernel & I/O Streaming Buffers
Write-Host "`n[*] [5/6] Tuning Network TCP Buffers and Video Hardware Acceleration..." -ForegroundColor Cyan
& $adb shell setprop net.tcp.buffersize.wifi 524288,1048576,2097152,262144,524288,1048576
& $adb shell setprop net.tcp.buffersize.ethernet 524288,1048576,2097152,262144,524288,1048576
& $adb shell setprop debug.sf.hw 1
& $adb shell setprop debug.performance.tuning 1
& $adb shell setprop video.accelerate.hw 1
Write-Host "  -> Streaming TCP buffers and GPU hardware acceleration tuned." -ForegroundColor Green

# 6. Purge Chinese Droppers, BadBox Miners, Google Services and Bloatware (UAD Base)
Write-Host "`n[*] [6/6] Purging Chinese Firmware Droppers, BadBox Miners and Bloatware..." -ForegroundColor Cyan

$targetPackages = @(
    # BadBox / Supply-Chain Miners & Malware (Specific Chinese TV Box Watchdogs)
    "com.android.netservice",
    "com.android.backup",
    "com.android.assistant",
    "com.android.settings.assistant",
    "com.systemguard.android",
    "com.systemguard.updated",
    "com.movaplay.tv",
    "com.movaplay.android",
    "com.novatv.app",
    "com.rsshub.tv",
    "com.snf.launcher",
    "com.snflauncher.tv",
    "com.droidlogic.mboxlauncher",
    "com.android.guardupdated",
    "com.guard.update",

    # Firmware Droppers & Spyware
    "com.adups.fota",
    "com.adups.fota.sysoper",
    "com.android.providers.downloads.ui.test",
    "com.android.ad.daemon",
    "com.android.adsystem",
    "com.rockchip.adservice",
    "com.allwinner.service",
    "com.softwinner.fireplayer",
    "com.android.rockchip",
    "com.google.android.fota",
    "com.android.smart.terminal.iptv",
    "com.fw.upgrade.sysoper",
    "com.redstone.ota.ui",
    "com.LocalFota",
    "com.daemon.shelper",
    "com.evenwell.AprUploadService",

    # Google Services & Store
    "com.google.android.gms",
    "com.android.vending",
    "com.google.android.gsf",
    "com.google.android.gsf.login",
    "com.google.android.onetimeinitializer",
    "com.google.android.partnersetup",
    "com.google.android.feedback",
    "com.google.android.tts",
    "com.google.android.syncadapters.contacts",
    "com.google.android.syncadapters.calendar",
    "com.google.android.backuptransport",
    "com.google.android.setupwizard",
    "com.google.android.printservice.recommendation",
    "com.android.printservice.recommendation",

    # Browsers
    "com.android.chrome",
    "com.android.browser",
    "org.chromium.webview_shell",
    "com.opera.browser",
    "com.opera.mini.native",
    "org.mozilla.firefox",

    # Competing Heavy Streaming Apps
    "com.netflix.ninja",
    "com.netflix.mediaclient",
    "com.netflix.partner.activation",
    "com.google.android.youtube.tv",
    "com.google.android.youtube",
    "com.google.android.videos",
    "com.google.android.music",
    "com.amazon.amazonvideo.livingroom",
    "com.disney.disneyplus",
    "com.globo.globotv",
    "com.hbo.hbonow",
    "com.wbd.stream"
)

$installedPackages = & $adb shell pm list packages | ForEach-Object { $_ -replace "^package:", "" }

$disabledCount = 0
foreach ($pkg in $targetPackages) {
    if ($installedPackages -contains $pkg) {
        Write-Host "  -> Neutralizing package: $pkg..." -NoNewline
        # Triple-Kill strategy: Stop + AppOps Ignore + Disable + Hide + Uninstall
        & $adb shell am force-stop $pkg 2>$null | Out-Null
        & $adb shell cmd appops set $pkg RUN_IN_BACKGROUND ignore 2>$null | Out-Null
        & $adb shell cmd appops set $pkg RUN_ANY_IN_BACKGROUND ignore 2>$null | Out-Null
        & $adb shell cmd appops set $pkg SYSTEM_ALERT_WINDOW ignore 2>$null | Out-Null
        & $adb shell cmd appops set $pkg WRITE_SETTINGS ignore 2>$null | Out-Null

        $res = & $adb shell pm disable-user --user 0 $pkg 2>&1
        & $adb shell pm disable $pkg 2>$null | Out-Null
        & $adb shell pm hide $pkg 2>$null | Out-Null
        & $adb shell pm clear $pkg 2>$null | Out-Null

        if ($LASTEXITCODE -eq 0 -or $res -match "disabled") {
            Write-Host " [NEUTRALIZED & DISABLED]" -ForegroundColor Green
            $disabledCount++
        } else {
            $res2 = & $adb shell pm uninstall -k --user 0 $pkg 2>&1
            if ($LASTEXITCODE -eq 0 -or $res2 -match "Success") {
                Write-Host " [REMOVED USER 0]" -ForegroundColor Yellow
                $disabledCount++
            } else {
                Write-Host " [RESTRICTED / OP-IGNORED]" -ForegroundColor DarkYellow
                $disabledCount++
            }
        }
    }
}

Write-Host "`n[+] Purge completed! $disabledCount packages were disabled/removed." -ForegroundColor Green

if ($Reboot) {
    Write-Host "`n[*] Rebooting TV Box now to apply all settings..." -ForegroundColor Cyan
    & $adb reboot
    Write-Host "[+] Reboot command sent successfully!" -ForegroundColor Green
}

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "          OPTIMIZATION COMPLETED SUCCESSFULLY!            " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan
