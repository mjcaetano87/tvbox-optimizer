# TVBOX Skill - Guia de Referência Rápida

## Comandos Úteis do ADB

### Conectar via Rede (Wi-Fi / Ethernet):
```bash
adb connect <IP_DO_APARELHO>:5555
```

### Configurar Horário de Brasília & NTP:
```bash
adb shell setprop persist.sys.timezone America/Sao_Paulo
adb shell settings put global auto_time 1
adb shell settings put global auto_time_zone 1
adb shell settings put global ntp_server pool.ntp.br
adb shell settings put global ntp_server_2 a.st1.ntp.br
```

### Forçar DNS 1.1.1.1 e 8.8.8.8:
```bash
adb shell setprop net.dns1 1.1.1.1
adb shell setprop net.dns2 8.8.8.8
adb shell setprop persist.net.dns1 1.1.1.1
adb shell setprop persist.net.dns2 8.8.8.8
```

### Reverter/Reativar um aplicativo desativado:
```bash
adb shell pm enable <nome_do_pacote>
adb shell cmd package install-existing <nome_do_pacote>
```
