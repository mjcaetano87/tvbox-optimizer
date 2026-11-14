[CmdletBinding()]
param(
    [string]$DeviceIp = "",
    [int]$Port = 5555,
    [switch]$Reboot
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   TVBOX OPTIMIZER FOR IPTV (ADB DEBLOATER & TUNING)      " -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan

# Definir caminho do ADB (preferência pelo platform-tools estável)
$adbPath = "C:\Users\xeda\.gemini\antigravity\scratch\platform-tools\adb.exe"
if (-not (Test-Path $adbPath)) {
    $adbCmd = Get-Command "adb" -ErrorAction SilentlyContinue
    if ($adbCmd) {
        $adb = $adbCmd.Source
    } else {
        Write-Error "O executável ADB não foi encontrado."
        exit 1
    }
} else {
    $adb = $adbPath
}

# Se já houver um dispositivo conectado via USB, usa diretamente; caso contrário, conecta via IP
$devices = & $adb devices | Where-Object { $_ -match "\bdevice\b" -and $_ -notmatch "List of devices" }
if (-not $devices) {
    if ([string]::IsNullOrWhiteSpace($DeviceIp)) {
        $DeviceIp = Read-Host "Nenhum aparelho USB detectado. Digite o IP da TV Box (ex: 192.168.15.42)"
    }
    if (-not [string]::IsNullOrWhiteSpace($DeviceIp)) {
        Write-Host "[+] Conectando à TV Box em $DeviceIp`:$Port..." -ForegroundColor Green
        & $adb connect "$($DeviceIp):$Port"
        Start-Sleep -Seconds 2
        $devices = & $adb devices | Where-Object { $_ -match "\bdevice\b" -and $_ -notmatch "List of devices" }
    }
}

if (-not $devices) {
    Write-Warning "Nenhum dispositivo Android autorizado/conectado foi detectado pelo ADB."
    Write-Host "Certifique-se de que a Depuração USB/Rede está ativa." -ForegroundColor Yellow
    exit 1
}

Write-Host "[+] Dispositivo conectado com sucesso:`n$devices" -ForegroundColor Green

# 1. Configurar Fuso Horário e NTP Brasil (Horário de Brasília)
Write-Host "`n[*] [1/5] Configurando Horário de Brasília e NTP Brasil..." -ForegroundColor Cyan
& $adb shell setprop persist.sys.timezone "America/Sao_Paulo"
& $adb shell setprop persist.sys.time.zone "America/Sao_Paulo"
& $adb shell settings put global auto_time 1
& $adb shell settings put global auto_time_zone 1
& $adb shell settings put global ntp_server "pool.ntp.br"
& $adb shell settings put global ntp_server_2 "a.st1.ntp.br"
& $adb shell settings put global ntp_server_3 "b.st1.ntp.br"
& $adb shell setprop net.ntp.server "pool.ntp.br"
Write-Host "  -> Horário de Brasília (GMT-3) e NTP pool.ntp.br aplicados." -ForegroundColor Green

# 2. Configurar DNS 1.1.1.1 e 8.8.8.8
Write-Host "`n[*] [2/5] Forçando DNS 1.1.1.1 (Cloudflare) e 8.8.8.8 (Google)..." -ForegroundColor Cyan
$dnsCommands = @(
    "setprop net.dns1 1.1.1.1",
    "setprop net.dns2 8.8.8.8",
    "setprop persist.net.dns1 1.1.1.1",
    "setprop persist.net.dns2 8.8.8.8",
    "setprop dhcp.eth0.dns1 1.1.1.1",
    "setprop dhcp.eth0.dns2 8.8.8.8",
    "setprop dhcp.wlan0.dns1 1.1.1.1",
    "setprop dhcp.wlan0.dns2 8.8.8.8",
    "settings put global private_dns_mode off"
)
foreach ($cmd in $dnsCommands) {
    & $adb shell $cmd 2>$null | Out-Null
}
Write-Host "  -> DNS 1.1.1.1 e 8.8.8.8 configurados com sucesso." -ForegroundColor Green

# 3. Ajustes de Desenvolvedor: Animações Desligadas (Zero Lag)
Write-Host "`n[*] [3/5] Desligando Animações (Janela, Transição e Duração)..." -ForegroundColor Cyan
& $adb shell settings put global window_animation_scale 0
& $adb shell settings put global transition_animation_scale 0
& $adb shell settings put global animator_duration_scale 0
Write-Host "  -> Escalas de animação definidas para 0 (DESLIGADAS)." -ForegroundColor Green

# 4. Ajustes de Desenvolvedor: Limite de 2º Plano (0) & Destruir Atividades
Write-Host "`n[*] [4/5] Configurando Limite de Segundo Plano (0 processos) e Não Manter Atividades..." -ForegroundColor Cyan
& $adb shell settings put global always_finish_activities 1
& $adb shell setprop persist.sys.always_finish 1
& $adb shell settings put global app_process_limit 0
& $adb shell setprop persist.sys.process_limit 0
& $adb shell settings put global max_phantom_processes 2147483647 2>$null
Write-Host "  -> 'Não manter atividades' ATIVADO (1)." -ForegroundColor Green
Write-Host "  -> 'Limite de processos em 2º plano' definido para 0 (SEM PROCESSOS)." -ForegroundColor Green

# 5. Desativar Serviços do Google, Navegadores e Apps Pesados
Write-Host "`n[*] [5/5] Desativando Serviços do Google, Navegadores e Bloatwares..." -ForegroundColor Cyan

$targetPackages = @(
    # --- Google Services & Store ---
    "com.google.android.gms",                    # Google Play Services
    "com.android.vending",                       # Google Play Store
    "com.google.android.gsf",                     # Google Services Framework
    "com.google.android.onetimeinitializer",
    "com.google.android.partnersetup",
    "com.google.android.feedback",
    "com.google.android.tts",                    # Text-to-speech
    "com.google.android.syncadapters.contacts",
    "com.google.android.syncadapters.calendar",
    "com.google.android.backuptransport",
    "com.google.android.setupwizard",
    "com.google.android.printservice.recommendation",

    # --- Navegadores & Web ---
    "com.android.chrome",                        # Google Chrome
    "com.android.browser",                       # AOSP Browser padrão
    "org.chromium.webview_shell",
    "com.opera.browser",
    "com.opera.mini.native",
    "org.mozilla.firefox",

    # --- Streaming e Mídia Pesados ---
    "com.netflix.ninja",                         # Netflix Android TV
    "com.netflix.mediaclient",                   # Netflix Mobile APK
    "com.google.android.youtube.tv",             # YouTube TV
    "com.google.android.youtube",                # YouTube Standard
    "com.google.android.videos",                 # Google Play Movies
    "com.google.android.music",                  # Google Play Music
    "com.amazon.amazonvideo.livingroom",         # Prime Video
    "com.disney.disneyplus",                     # Disney+
    "com.globo.globotv",
    "com.hbo.hbonow",
    "com.wbd.stream"
)

$installedPackages = & $adb shell pm list packages | ForEach-Object { $_ -replace "^package:", "" }

$disabledCount = 0
foreach ($pkg in $targetPackages) {
    if ($installedPackages -contains $pkg) {
        Write-Host "  -> Desativando pacote: $pkg..." -NoNewline
        $res = & $adb shell pm disable-user --user 0 $pkg 2>&1
        if ($LASTEXITCODE -eq 0 -or $res -match "disabled") {
            Write-Host " [DESATIVADO]" -ForegroundColor Green
            $disabledCount++
        } else {
            $res2 = & $adb shell pm uninstall -k --user 0 $pkg 2>&1
            if ($LASTEXITCODE -eq 0 -or $res2 -match "Success") {
                Write-Host " [REMOVIDO USER 0]" -ForegroundColor Yellow
                $disabledCount++
            } else {
                Write-Host " [FALHA / RESTRITO]" -ForegroundColor Red
            }
        }
    }
}

Write-Host "`n[+] Limpeza concluída! $disabledCount pacotes foram desativados/removidos." -ForegroundColor Green

if ($Reboot) {
    Write-Host "`n[*] Reiniciando a TV Box agora para aplicar todas as configurações..." -ForegroundColor Cyan
    & $adb reboot
    Write-Host "[+] Comando de reinicialização enviado com sucesso!" -ForegroundColor Green
}

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "          OTIMIZAÇÃO CONCLUÍDA COM SUCESSO!               " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan
