# TVBOX Optimizer for IPTV 🚀

Skill e ferramenta de automação para otimização extrema de dispositivos **TV Box, Android TV e Fire Stick** via **ADB** (Android Debug Bridge), focada em performance máxima para reprodutores e listas de IPTV.

---

## ⚡ O que este projeto faz?

1. **Ajustes de Desenvolvedor (Performance Máxima & Zero Lag):**
   - **Escala de Animações:** Todas desligadas (`0x` para janela, transição e duração).
   - **Não manter atividades:** Ativado (`always_finish_activities = 1`) — destrói as atividades da memória assim que o usuário sai.
   - **Limite de Processamento em 2º Plano:** `0` (Sem processos em segundo plano).

2. **Limpeza Profunda de Bloatwares:**
   - Desativa serviços pesados em background do Google: *Google Play Services, Play Store, Framework, Setup Wizard, TTS, Feedback, sincronizadores*.
   - Desativa navegadores pesados (*Chrome, Opera, Firefox, navegadores padrão*).
   - Desativa apps de streaming concorrentes/pesados (*Netflix Android TV, Netflix Mobile, YouTube oficial, Prime Video, Disney+, etc.*).
   - Mantém intactos componentes de mídia e reprodutores IPTV (*TiviMate, XCIPTV, Smarters Pro, OTT Navigator, etc.*).

3. **DNS Forçado de Baixa Latência:**
   - Configura servidores DNS **1.1.1.1** (Cloudflare) e **8.8.8.8** (Google) no sistema e interfaces Ethernet/Wi-Fi, evitando bloqueios de operadoras e acelerando abertura dos canais.

4. **Horário de Brasília & NTP Brasil:**
   - Seta fuso horário oficial para `America/Sao_Paulo` (GMT-3).
   - Aponta servidores NTP para `pool.ntp.br` e `a.st1.ntp.br`, corrigindo falhas de EPG (Guia de Programação) e certificados SSL/TLS expirados por data/hora errada.

5. **Reinicialização Automática:**
   - Reinicia o aparelho para consolidar todas as novas diretrizes do sistema operacional.

---

## 📦 Estrutura do Repositório

```
├── SKILL.md                 # Definição e documentação da Skill para agentes de IA
├── README.md                # Este documento explicativo
├── scripts/
│   └── optimize_tvbox.ps1   # Script de automação em PowerShell
└── references/
    └── commands.md          # Guia de comandos rápidos ADB
```

---

## 🚀 Como Executar Manualmente

### 1. Pré-requisitos
- Ter o **ADB** instalado no computador.
- Estar conectado na mesma rede que a TV Box (ou conectado via cabo USB com Depuração USB ativada).

### 2. Executando o Script
Abra o PowerShell e execute:

```powershell
powershell -ExecutionPolicy Bypass -File ".\scripts\optimize_tvbox.ps1"
```
O script solicitará interativamente o **IP da TV Box** (ou você pode passar via parâmetro):

```powershell
powershell -ExecutionPolicy Bypass -File ".\scripts\optimize_tvbox.ps1" -DeviceIp 192.168.15.41 -Reboot
```

---

## 🔄 Reversão de Pacotes
Caso precise reativar algum app posteriormente:
```bash
adb shell pm enable <nome_do_pacote>
adb shell cmd package install-existing <nome_do_pacote>
```
