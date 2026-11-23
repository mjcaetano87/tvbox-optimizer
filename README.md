# 🇧🇷 TVBOX Optimizer v3.0-ROOT para IPTV | 🇺🇸 TVBOX Optimizer v3.0-ROOT for IPTV

> **Desenvolvido por / Developed by:** `jesséx10`  
> **Repositório Oficial / Official Repo:** [https://github.com/mjcaetano87/tvbox-optimizer](https://github.com/mjcaetano87/tvbox-optimizer)  
> **Download APK Direto / Direct APK:** [`TVBOX_Optimizer_IPTV.apk`](./TVBOX_Optimizer_IPTV.apk)

---

## 🇧🇷 PORTUGUÊS

O **TVBOX Optimizer v3.0-ROOT** é uma ferramenta profissional de código aberto, debloater e otimizador de baixo nível desenvolvido para **Android TV, TV Boxes (chips Allwinner, Rockchip, Amlogic) e Fire TV Stick**.

Nesta versão **v3.0-ROOT**, o projeto foi atualizado com uma **Arquitetura em Duas Fases**:
1. **Primeiramente:** Verifica, instala utilitários de superusuário e força o **Modo Administrador / ROOT** (`WRITE_SECURE_SETTINGS`, Whitelist de Bateria, detecção de `su uid=0`, Kingo ROOT e iRoot embutidos).
2. **Somente Depois:** Executa a suíte de otimização completa do **TVBOX-OPTIMIZER** com permissão máxima de superusuário, garantindo que nenhum adware ou dropper de fábrica consiga resistir à remoção.

---

### 👑 FASE 1: Instalação & Forçar Modo Administrador / ROOT (Obrigatório / Pré-requisito)
Antes de aplicar as alterações no sistema, o app executa:
* **Auditoria de Privilégios em Tempo Real:** Badge no topo indicando se o aparelho possui Root ativo (`su uid=0`), Modo Desenvolvedor/ADB ou se está em modo usuário comum.
* **Auto-Concessão Silenciosa de Administrador:** Se o aparelho já tiver Root ou ADB loopback (`127.0.0.1:5555`), auto-concede `WRITE_SECURE_SETTINGS` e permissões de `appops` silenciosamente.
* **Utilitários de Root Embutidos nos Assets (`root_bundle`):**
  - **`Kingo ROOT`** (`com.kingo.root`): Ferramenta de 1-clique para root em TV Boxes com Android 4.4 até Android 9.
  - **`iRoot`** (`com.mgyun.shua.su`): Motor alternativo para chips Rockchip/Allwinner.
  - O app extrai direto da memória e dispara a instalação (silenciosa se houver suporte, ou assistida na tela da TV com controle remoto).
* **Atalho Rápido para Configurações do Desenvolvedor:** Acesso direto para ativar Depuração ADB caso precise parear externamente.

---

### 🚀 FASE 2: TVBOX-OPTIMIZER (Executado com Poder Total de Superusuário)
Uma vez estabelecido o Modo Administrador / Root, o motor de otimização entra em ação:
1. **Controle Remoto (DPAD 100% Funcional):**
   - Teclas com realce de foco Neon (`[ 👑 1. ROOT/ADMIN ]`, `[ 🚀 2. OTIMIZAR ]`, `[ 🔄 3. REINICIAR ]`), desenhados para navegar usando apenas as setas do controle remoto.
2. **Matriz Quádrupla de DNS Anti-Bloqueio:**
   - **`DNS 1 (1.1.1.1)`**: Cloudflare Anycast (menor latência do Brasil).
   - **`DNS 2 (9.9.9.10)`**: **Quad9 Unfiltered** (Fundação suíça, sem censura, sem filtros e com estrito DNSSEC para não bloquear streams).
   - **`DNS 3 (8.8.8.8)`**: Google Public DNS (estabilidade de rota global).
   - **`DNS 4 (1.0.0.1)`**: Cloudflare Secundário (redundância ultra-rápida).
   - Gravado em nível de sistema em `net.dns1..4`, `persist.net.dns1..4`, `dhcp.eth0` e `dhcp.wlan0`.
3. **Purga Heurística Profunda de Bloatwares, Droppers Chineses & Mineradores BadBox:**
   - **Motor Heurístico Dinâmico:** Escaneia identificadores de pacote e rótulos visuais na tela da TV (`ASSISTANT`, `BACKUP`, `MOVAPLAY`, `NETSERVICE`, `NOVA TV`, `RSSHUB`, `SETTING ASSISTANT`, `SNF LAUNCHER`, `SYSTEM GUARDUPDATED`, etc.).
   - **Técnica de Bloqueio Triplo com Root:** Mata processos em execução (`am force-stop`), revoga `appops RUN_IN_BACKGROUND ignore`, desativa (`pm disable-user --user 0`), oculta (`pm hide`) e desinstala (`pm uninstall -k --user 0`).
   - Elimina da raiz instaladores silenciosos de firmware chinês (`com.adups.fota`, `com.android.ad.daemon`, `com.rockchip.adservice`, `com.allwinner.service`, etc.).
   - Desativa telemetria pesada em segundo plano (`Google Play Services`, `Play Store`, navegadores pesados e stubs concorrentes).
4. **Tuning de Hardware & Rede para IPTV:**
   - Força aceleração de vídeo por GPU (`debug.sf.hw = 1` e `video.accelerate.hw = 1`).
   - Aumenta os buffers de rede TCP (2MB) para eliminar travamentos e buffering.
5. **Memória RAM Máxima:**
   - Desliga as escalas de animação (`0x`).
   - Ativa `always_finish_activities = 1` (destrói tarefas em segundo plano ao sair).
   - Limita os processos em segundo plano a `0`.
6. **Reinicialização Limpa:**
   - Botão dedicado que executa reinicialização privilegiada via `su`, `svc power reboot` ou `PowerManager`.

---

## 🇺🇸 ENGLISH

**TVBOX Optimizer v3.0-ROOT** is an advanced open-source system debloater and low-level performance tuner tailored for **Android TV, TV Boxes (Allwinner, Rockchip, Amlogic chips), and Fire TV Stick**.

### ⚡ 2-Phase Execution Architecture:
1. **First (Root & Administrator Mode):** Audits system privileges, forces Administrator Mode (`WRITE_SECURE_SETTINGS`, battery optimization whitelist, `appops`), validates `su` superuser status (`uid=0`), and embeds 1-click root installers (`Kingo ROOT` and `iRoot`) inside the APK assets.
2. **Only Then (TVBOX-OPTIMIZER Execution):** Runs the comprehensive IPTV tuning suite with superuser rights, guaranteeing that firmware droppers, adwares, and persistent miners are permanently eliminated without permission errors.

---

## 🛠️ Como Executar / How to Run

### Opção 1: Direto na TV Box (Instalação via APK)
1. Instale o APK [`TVBOX_Optimizer_IPTV.apk`](./TVBOX_Optimizer_IPTV.apk) na sua TV Box.
2. Abra o app usando o controle remoto.
3. Clique em **`[ 👑 1. ROOT / ADMIN ]`**: O app verificará o Root e aplicará o modo administrador. Se o aparelho não for roteado, ele instalará as ferramentas de Root embutidas.
4. Clique em **`[ 🚀 2. OTIMIZAR ]`**: Aplica todas as otimizações de IPTV, 4 DNS e purga do BadBox.
5. Clique em **`[ 🔄 3. REINICIAR ]`**: Reinicia a TV Box para consolidar as alterações de hardware.

### Opção 2: Via Computador / Terminal ADB
Conecte sua TV Box via Wi-Fi ou cabo USB e execute no PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File ".\scripts\optimize_tvbox.ps1" -DeviceIp <IP_DA_TVBOX> -Reboot
```

---

## 🏗️ Como Compilar / How to Build from Source

```powershell
powershell -ExecutionPolicy Bypass -File ".\scripts\build_apk.ps1"
```

---

## 📄 Licença / License
Distribuído sob a licença MIT / Distributed under the MIT License.  
**Assinatura / Signature:** `by: jesséx10`
