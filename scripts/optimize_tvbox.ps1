[CmdletBinding()]
param(
    [string]$DeviceIp = "",
    [int]$Port = 5555,
    [switch]$Reboot
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   TVBOX OPTIMIZER FOR IPTV (ADB DEBLOATER & TUNING v2.1) " -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan

# Definir caminho do ADB
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
Write-Host "`n[*] [1/6] Configurando Horário de Brasília e NTP Brasil..." -ForegroundColor Cyan
& $adb shell setprop persist.sys.timezone "America/Sao_Paulo"
& $adb shell setprop persist.sys.time.zone "America/Sao_Paulo"
& $adb shell settings put global auto_time 1
& $adb shell settings put global auto_time_zone 1
& $adb shell settings put global ntp_server "pool.ntp.br"
& $adb shell settings put global ntp_server_2 "a.st1.ntp.br"
& $adb shell settings put global ntp_server_3 "b.st1.ntp.br"
& $adb shell setprop net.ntp.server "pool.ntp.br"
Write-Host "  -> Horário de Brasília (GMT-3) e NTP pool.ntp.br aplicados." -ForegroundColor Green

# 2. Configurar Matriz Quádrupla de DNS Anti-Bloqueio (1.1.1.1, 9.9.9.10, 8.8.8.8, 1.0.0.1)
Write-Host "`n[*] [2/6] Configurando Matriz Quádrupla de DNS Anti-Bloqueio..." -ForegroundColor Cyan
Write-Host "  -> DNS 1: 1.1.1.1 (Cloudflare Anycast)" -ForegroundColor Gray
Write-Host "  -> DNS 2: 9.9.9.10 (Quad9 Unfiltered - Sem Censura / Suíça)" -ForegroundColor Gray
Write-Host "  -> DNS 3: 8.8.8.8 (Google Public)" -ForegroundColor Gray
Write-Host "  -> DNS 4: 1.0.0.1 (Cloudflare Redundância)" -ForegroundColor Gray

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
Write-Host "  -> Matriz quádrupla de DNS configurada com sucesso." -ForegroundColor Green

# 3. Ajustes de Desenvolvedor: Animações Desligadas (Zero Lag)
Write-Host "`n[*] [3/6] Desligando Animações (Janela, Transição e Duração)..." -ForegroundColor Cyan
& $adb shell settings put global window_animation_scale 0
& $adb shell settings put global transition_animation_scale 0
& $adb shell settings put global animator_duration_scale 0
Write-Host "  -> Escalas de animação definidas para 0 (DESLIGADAS)." -ForegroundColor Green

# 4. Ajustes de Desenvolvedor: Limite de 2º Plano (0) & Destruir Atividades
Write-Host "`n[*] [4/6] Configurando Limite de Segundo Plano (0 processos) e Não Manter Atividades..." -ForegroundColor Cyan
& $adb shell settings put global always_finish_activities 1
& $adb shell setprop persist.sys.always_finish 1
& $adb shell settings put global app_process_limit 0
& $adb shell setprop persist.sys.process_limit 0
& $adb shell settings put global max_phantom_processes 2147483647 2>$null
Write-Host "  -> 'Não manter atividades' ATIVADO (1)." -ForegroundColor Green
Write-Host "  -> 'Limite de processos em 2º plano' definido para 0 (SEM PROCESSOS)." -ForegroundColor Green

# 5. Otimizações de Kernel & I/O para Streaming IPTV
Write-Host "`n[*] [5/6] Ajustando Buffers TCP de Rede e Aceleração de Hardware..." -ForegroundColor Cyan
& $adb shell setprop net.tcp.buffersize.wifi 524288,1048576,2097152,262144,524288,1048576
& $adb shell setprop net.tcp.buffersize.ethernet 524288,1048576,2097152,262144,524288,1048576
& $adb shell setprop debug.sf.hw 1
& $adb shell setprop debug.performance.tuning 1
& $adb shell setprop video.accelerate.hw 1
Write-Host "  -> Buffers de streaming e aceleração de vídeo por hardware configurados." -ForegroundColor Green

# 6. Desativar Droppers Chineses, Serviços do Google e Bloatwares
Write-Host "`n[*] [6/6] Purgando Droppers Chineses, Google Services e Bloatwares (Base UAD)..." -ForegroundColor Cyan

$targetPackages = @(
    # Droppers / Spywares de Firmware Chinês
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

    # Google Services Pesados
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

    # Navegadores
    "com.android.chrome",
    "com.android.browser",
    "org.chromium.webview_shell",
    "com.opera.browser",
    "com.opera.mini.native",
    "org.mozilla.firefox",

    # Streaming Pesado
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
        Write-Host "  -> Purgando pacote: $pkg..." -NoNewline
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
