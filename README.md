# TVBOX Optimizer for IPTV 🚀

Ferramenta de automação e aplicativo APK para otimização extrema de dispositivos **TV Box, Android TV e Fire Stick** via **ADB** ou direto no aparelho via **APK**, com foco exclusivo em performance máxima para reprodutores IPTV (TiviMate, Smarters Pro, XCIPTV, OTT Navigator, etc.).

---

## ⚡ Novidades da Versão 2.1 (Anti-Bloqueio & UAD Base)

1. **Matriz Quádrupla de DNS Anti-Bloqueio:**
   Configura os 4 slots nativos de resolução do Android para garantir abertura instantânea dos canais e imunidade contra ordens judiciais e bloqueios de operadoras locais:
   - **`DNS 1` (1.1.1.1):** Cloudflare Anycast (menor latência do Brasil).
   - **`DNS 2` (9.9.9.10):** Quad9 Unfiltered (Fundação suíça, sem filtros, sem censura e com DNSSEC).
   - **`DNS 3` (8.8.8.8):** Google Public (maior disponibilidade do mundo).
   - **`DNS 4` (1.0.0.1):** Cloudflare Secundário (redundância ultra-rápida).

2. **Purga de Bloatware & Droppers Chineses (Base UAD Next Generation):**
   - Elimina os instaladores ocultos de firmwares Allwinner/Rockchip que instalam APKs indesejados (`com.adups.fota`, `com.android.ad.daemon`, `com.rockchip.adservice`, etc.).
   - Desativa todos os serviços pesados do Google em segundo plano (`Google Play Services`, `Play Store`, `Framework`, `SetupWizard`, etc.).
   - Desativa navegadores e aplicativos de streaming concorrentes.

3. **Otimizações de Streaming & Aceleração de Hardware:**
   - Buffers TCP de recepção de rede aumentados para evitar travamentos de buffer (*buffering*).
   - Aceleração forçada por GPU (`debug.sf.hw = 1` e `video.accelerate.hw = 1`).

4. **Interface Hacker / Terminal v2.1 no APK:**
   - Fundo 100% Preto e tipografia **Verde Claro / Neon**.
   - Auto-scroll acompanhando cada linha do log em tempo real.
   - Solicitação de permissões completas na 1ª abertura.
   - Caixa de diálogo ao final: *"FINALIZADO, deseja reiniciar agora?"*.

---

## 📦 Estrutura do Repositório

```
├── TVBOX_Optimizer_IPTV.apk  # APK pronto para instalação direta na TV Box
├── SKILL.md                  # Definição e documentação da Skill para agentes de IA
├── README.md                 # Documentação detalhada
├── scripts/
│   └── optimize_tvbox.ps1    # Script de automação remota via ADB
├── app-source/               # Código-fonte completo do aplicativo Android
└── references/
    └── commands.md           # Guia de comandos rápidos ADB
```

---

## 🚀 Como Usar

### Opção 1: Direto na TV Box (Via APK)
1. Baixe o arquivo **`TVBOX_Optimizer_IPTV.apk`** e instale na TV Box (via pen drive ou gerenciador de arquivos).
2. Abra o aplicativo, conceda as permissões solicitadas na primeira execução.
3. Clique em **`[ 🚀 OTIMIZAR ]`**.
4. Ao concluir, confirme em **`[ Sim, Reiniciar ]`**.

### Opção 2: Remoto via Computador (PowerShell / ADB)
Execute o script passando o IP da TV Box:
```powershell
powershell -ExecutionPolicy Bypass -File ".\scripts\optimize_tvbox.ps1" -DeviceIp 192.168.15.42 -Reboot
```
