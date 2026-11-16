# 🇧🇷 TVBOX Optimizer para IPTV | 🇺🇸 TVBOX Optimizer for IPTV

> **Desenvolvido por / Developed by:** `jesséx10`  
> **Repositório Oficial / Official Repo:** [https://github.com/mjcaetano87/tvbox-optimizer](https://github.com/mjcaetano87/tvbox-optimizer)  
> **Download APK Direto / Direct APK:** [`TVBOX_Optimizer_IPTV.apk`](./TVBOX_Optimizer_IPTV.apk)

---

## 🇧🇷 PORTUGUÊS

O **TVBOX Optimizer** é uma ferramenta de código aberto e debloater profissional desenvolvido especialmente para **Android TV, TV Boxes (chips Allwinner, Rockchip e Amlogic) e Fire TV Stick**. 

O objetivo do projeto é transformar qualquer TV Box em uma máquina veloz, limpa e dedicada para reprodução de IPTV (TiviMate, XCIPTV, IPTV Smarters Pro, OTT Navigator, etc.), removendo processos espiões, otimizando o buffer de vídeo e garantindo que os canais abram sem travamentos ou bloqueios de operadoras.

### ⚠️ AVISO IMPORTANTE:
> **Antes de executar o aplicativo na sua TV Box, certifique-se de que o MODO DESENVOLVEDOR (e a DEPURACAO USB) esteja ATIVO nas configurações do aparelho!**  
> *(Vá em Configurações > Sobre > Clique 7 vezes em 'Número da Versão' até liberar o menu Opções do Desenvolvedor).*

### 🚀 Principais Recursos:
1. **Controle Remoto (DPAD 100% Funcional):**
   - Os botões `[ OTIMIZAR ]` e `[ REINICIAR ]` possuem realce visual de foco (borda Neon), permitindo navegar suavemente usando as setas do controle remoto da TV Box.
2. **Matriz Quádrupla de DNS Anti-Bloqueio:**
   - **`DNS 1 (1.1.1.1)`**: Cloudflare Anycast (menor latência do Brasil).
   - **`DNS 2 (9.9.9.10)`**: **Quad9 Unfiltered** (Fundação suíça, sem censura, sem filtros e com estrito DNSSEC para não bloquear streams).
   - **`DNS 3 (8.8.8.8)`**: Google Public DNS (estabilidade de rota global).
   - **`DNS 4 (1.0.0.1)`**: Cloudflare Secundário (redundância ultra-rápida).
3. **Purga de Bloatware, Droppers Chineses & Mineradores BadBox (Anti-Reinstalação):**
   - **Motor Heurístico Dinâmico:** Escaneia tanto o nome do pacote quanto o rótulo visível na tela da TV para detectar e aniquilar invasores persistentes (`ASSISTANT`, `BACKUP`, `MOVAPLAY`, `NETSERVICE`, `NOVA TV`, `RSSHUB`, `SETTING ASSISTANT`, `SNF LAUNCHER`, `SYSTEM GUARDUPDATED`, etc.).
   - **Técnica de Bloqueio Triplo:** Além do `pm disable-user` e `pm hide`, revoga permissões de execução via `cmd appops set <pkg> RUN_IN_BACKGROUND ignore` e `RUN_ANY_IN_BACKGROUND ignore` para que, mesmo se o firmware da Box tentar reviver o APK, o processo não consiga rodar em segundo plano ou minerar.
   - Elimina da raiz instaladores silenciosos de firmware chinês (`com.adups.fota`, `com.android.ad.daemon`, `com.rockchip.adservice`, etc.).
   - Desativa serviços pesados do Google em segundo plano (`Google Play Services`, `Play Store`, `SetupWizard`, etc.).
   - Desativa navegadores e apps pesados concorrentes de streaming.
4. **Tuning de Hardware & Rede:**
   - Força aceleração de vídeo por GPU (`debug.sf.hw = 1` e `video.accelerate.hw = 1`).
   - Aumenta os buffers de rede TCP para evitar travamentos de buffer (*buffering*).
5. **Memória RAM Máxima:**
   - Desliga as escalas de animação (`0x`).
   - Ativa `always_finish_activities = 1` (destrói tarefas em segundo plano).
   - Limita os processos em segundo plano a `0`.
6. **Interface Terminal Retrô & Diálogo de Conclusão:**
   - Visual hacker (Fundo preto com tipografia Verde Claro / Neon).
   - Auto-scroll automático do log.
   - Pergunta ao final: *"FINALIZADO - Deseja reiniciar agora?"*.

---

## 🇺🇸 ENGLISH

**TVBOX Optimizer** is an open-source performance tuner and debloater designed specifically for **Android TV, TV Boxes (Allwinner, Rockchip, Amlogic chips), and Fire TV Stick** devices. It prepares devices for smooth, lag-free IPTV streaming (TiviMate, XCIPTV, IPTV Smarters Pro, OTT Navigator, etc.) by eliminating background resource hogs, optimizing network routes, and securing system privacy.

### ⚠️ IMPORTANT NOTICE:
> **Before running the application on your TV Box, ensure that DEVELOPER OPTIONS (and USB DEBUGGING) is ENABLED in your device settings!**  
> *(Go to Settings > About > Click 7 times on 'Build Number' to unlock Developer Options).*

### ⚡ Key Features:
1. **Full DPAD / TV Remote Control Support:**
   - Both `[ OPTIMIZE ]` and `[ REBOOT ]` buttons have visual focus outlines (Neon highlights) for seamless navigation using only the TV remote control arrows.
2. **Quadruple DNS Anti-Censorship Matrix:**
   - **`DNS 1 (1.1.1.1)`**: Cloudflare Anycast — ultra-low latency.
   - **`DNS 2 (9.9.9.10)`**: **Quad9 Unfiltered** — Swiss privacy foundation, zero domain filtering, strict DNSSEC, and immunity to regional blocks.
   - **`DNS 3 (8.8.8.8)`**: Google Public DNS — global reliability and routing stability.
   - **`DNS 4 (1.0.0.1)`**: Cloudflare Secondary — instant fallback redundancy.
3. **Chinese Firmware Dropper, Miner & BadBox Purge (Anti-Persistence):**
   - **Dynamic Heuristic Engine:** Analyzes both package identifiers and UI display labels to eliminate stubborn malware and miners (`ASSISTANT`, `BACKUP`, `MOVAPLAY`, `NETSERVICE`, `NOVA TV`, `RSSHUB`, `SETTING ASSISTANT`, `SNF LAUNCHER`, `SYSTEM GUARDUPDATED`, etc.).
   - **Triple-Kill Strategy:** Applies `am force-stop`, revokes background execution via `cmd appops set <pkg> RUN_IN_BACKGROUND ignore` and `RUN_ANY_IN_BACKGROUND ignore`, disables via `pm disable-user --user 0`, and uninstalls/hides the package so Chinese ROM watchdogs cannot resurrect it.
   - Purges silent background APK droppers and firmware adwares (`com.adups.fota`, `com.android.ad.daemon`, `com.rockchip.adservice`, `com.allwinner.service`, etc.).
   - Disables heavy Google background services (`Google Play Services`, `Play Store`, `Google Services Framework`, `SetupWizard`, `TTS`).
   - Disables unused stock browsers and competing heavy streaming apps.
4. **Hardware & Network Tuning:**
   - Forces direct GPU hardware video acceleration (`debug.sf.hw = 1` and `video.accelerate.hw = 1`).
   - Expands TCP network socket window buffers to minimize stream buffering.
5. **Extreme Free RAM:**
   - Disables all animation scales (`0x`).
   - Enables `always_finish_activities = 1` (frees RAM immediately upon exit).
   - Sets background process limit to `0`.
6. **Retro Terminal UI & Finish Dialog:**
   - Black background (`#000000`) with Neon Green (`#39FF14`) monospace typography.
   - Real-time auto-scrolling log console.
   - Completion dialog prompt: *"FINISHED - Would you like to restart now?"*.

---

## 🛠️ Como Compilar / How to Build from Source

Qualquer pessoa pode compilar o APK assinado diretamente via PowerShell sem precisar abrir uma IDE pesada / Anyone can compile the signed APK directly using PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File ".\scripts\build_apk.ps1"
```

---

## 📄 Licença / License
Distribuído sob a licença MIT / Distributed under the MIT License.  
**Assinatura / Signature:** `by: jesséx10`
