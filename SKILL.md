---
name: tvbox
description: "Otimiza e limpa TV Box / Android TV via ADB para IPTV: desativa bloatware, serviços do Google, navegadores, Netflix e apps pesados, força DNS 1.1.1.1 e 8.8.8.8, sincronização de horário NTP Brasil (Brasília), desliga animações (0x) e ativa 'não manter atividades' com limite zero de processos em segundo plano."
---

# TVBOX - Otimizador de TV Box / Android TV para IPTV

Esta skill automatiza a configuração, limpeza profunda e otimização agressiva de dispositivos **TV Box / Android TV / Fire Stick** conectados via depuração **ADB** (USB ou Wi-Fi/Rede). O objetivo é liberar o máximo de memória RAM e processamento livre com foco exclusivo em aplicativos de IPTV e reprodutores de vídeo.

---

## 🎯 Otimizações Aplicadas

1. **Ajustes de Opções do Desenvolvedor (Performance Máxima & Zero Lag):**
   - **Escala de animação da janela:** DESLIGADA (`0`)
   - **Escala de animação de transição:** DESLIGADA (`0`)
   - **Escala de duração do animator:** DESLIGADA (`0`)
   - **Não manter atividades:** ATIVADO (`always_finish_activities = 1`) — destrói as atividades assim que o usuário sai da tela.
   - **Limite do processamento em 2° plano:** SEM PROCESSOS EM SEGUNDO PLANO (`app_process_limit = 0`).

2. **Remoção de Bloatware & Desativação de Serviços Pesados:**
   - **Google Services:** Google Play Store, Google Play Services, Google Framework, Google Partner Setup, Google Feedback, TTS, Setup Wizard.
   - **Navegadores:** Chrome, AOSP Browser, Opera, Firefox.
   - **Streaming Concorrente:** Netflix (`com.netflix.ninja`, `com.netflix.mediaclient`), YouTube oficial, Prime Video, Disney+, Max/HBO.

3. **DNS Forçado de Alta Performance:**
   - Define fallback e servidores DNS para `1.1.1.1` (Cloudflare) e `8.8.8.8` (Google).
   - Contorna eventuais bloqueios de rota da operadora e melhora a velocidade de requisição dos canais.

4. **Sincronização de Horário NTP Brasil (Horário de Brasília):**
   - Define o fuso horário para `America/Sao_Paulo` (GMT-3).
   - Configura servidores NTP do Brasil (`pool.ntp.br`, `a.st1.ntp.br`, `b.st1.ntp.br`).
   - Força `auto_time = 1` e `auto_time_zone = 1` para garantir que o EPG (Guia de Programação) e certificados SSL não expirem.

5. **Reinicialização Automática:**
   - Envia o comando `adb reboot` ao final da otimização para consolidar todos os parâmetros do sistema.

---

## 🚀 Como Executar

### Conectar via Cabo ou Wi-Fi:
```powershell
# Se for via rede local:
adb connect <IP_DA_TVBOX>:5555

# Verificar se está detectado:
adb devices
```

### Executar a Otimização com Reinício Automático:
```powershell
powershell -ExecutionPolicy Bypass -File "C:\Users\xeda\.gemini\config\skills\tvbox\scripts\optimize_tvbox.ps1" -Reboot
```

Ou especificando o IP diretamente:
```powershell
powershell -ExecutionPolicy Bypass -File "C:\Users\xeda\.gemini\config\skills\tvbox\scripts\optimize_tvbox.ps1" -DeviceIp <IP_DA_TVBOX> -Reboot
```
