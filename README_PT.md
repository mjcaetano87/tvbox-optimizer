# TVBOX Optimizer para IPTV 🚀

**TVBOX Optimizer** é uma ferramenta de código aberto e debloater projetado especificamente para dispositivos **Android TV, TV Boxes (Allwinner, Rockchip, Amlogic) e Fire TV Stick**. Ele prepara aparelhos para reprodução limpa, estável e sem engasgos em aplicativos de IPTV (TiviMate, XCIPTV, IPTV Smarters Pro, OTT Navigator, etc.), eliminando processos ocultos, otimizando rotas de rede e garantindo máxima privacidade.

Disponível tanto como **Aplicativo Android nativo (APK)** quanto como **script de automação ADB**.

---

## ⚡ Destaques da Versão 2.1

### 1. Matriz Quádrupla de DNS Anti-Bloqueio
Configura os 4 slots nativos de resolução do Android para contornar bloqueios e traffic shaping de operadoras sem falsos positivos:
* **`DNS 1` (`1.1.1.1`)**: Cloudflare Anycast — menor latência no Brasil.
* **`DNS 2` (`9.9.9.10`)**: **Quad9 Unfiltered** — fundação suíça de privacidade, sem censura, estrito DNSSEC e sem listas de bloqueio.
* **`DNS 3` (`8.8.8.8`)**: Google Public DNS — alta estabilidade global.
* **`DNS 4` (`1.0.0.1`)**: Cloudflare Secundário — redundância ultra-rápida.

### 2. Purga de Droppers Chineses de Firmware e Bloatware
Baseado na inteligência do **[Universal Android Debloater Next Generation (UAD-NG)](https://github.com/Universal-Debloater-Alliance/universal-android-debloater-next-generation)**:
* Remove instaladores ocultos de firmware chinês que instalam APKs sem permissão (`com.adups.fota`, `com.android.ad.daemon`, `com.rockchip.adservice`, etc.).
* Desativa todos os serviços pesados do Google em segundo plano (`Google Play Services`, `Play Store`, `Framework`, `SetupWizard`, etc.).
* Desativa navegadores não utilizados e apps pesados de streaming concorrentes.

### 3. Otimizações de Hardware & Rede para IPTV
* **Aceleração de Vídeo por Hardware**: Força renderização direta pela GPU (`debug.sf.hw = 1` e `video.accelerate.hw = 1`), eliminando queda de quadros em 4K/FHD.
* **Buffers TCP de Streaming**: Expande o buffer de recepção nas interfaces Ethernet e Wi-Fi, prevenindo congelamentos de imagem (*buffering*).

### 4. Ajustes de Opções do Desenvolvedor e RAM Livre
* Escalas de animação (janela, transição e duração) definidas para **`0x` (Desligadas)**.
* **Não Manter Atividades** ativado (`always_finish_activities = 1`) para destruir atividades ao sair e liberar RAM na hora.
* **Limite de Processos em 2º Plano** definido para **`0`** (Sem processos em segundo plano).

### 5. Horário de Brasília & NTP Brasil
* Ajusta o fuso para `America/Sao_Paulo` (GMT-3) e servidores `pool.ntp.br`, evitando falhas de guias EPG e certificados SSL expirados.

### 6. Interface Terminal Retrô
* Layout estilo console: Fundo Preto (`#000000`) com fonte Monospace em **Verde Claro / Neon** (`#39FF14`).
* Auto-scroll em tempo real acompanhando cada linha do log.
* Diálogo ao concluir: *"FINALIZADO - Deseja reiniciar agora?"*.

---

## 📲 Como Usar

### Opção 1: Direto na TV Box (APK)
1. Baixe o arquivo **`TVBOX_Optimizer_IPTV.apk`** deste repositório.
2. Instale na TV Box via Pen Drive ou gerenciador de arquivos.
3. Abra o app, conceda as permissões na primeira inicialização.
4. Clique em **`[ 🚀 OTIMIZAR ]`**.
5. Ao concluir, confirme para reiniciar a TV Box.

### Opção 2: Remoto via Computador (ADB)
```powershell
powershell -ExecutionPolicy Bypass -File ".\scripts\optimize_tvbox.ps1" -DeviceIp <IP_DA_TVBOX> -Reboot
```

---

## 🛠️ Como Compilar o APK a partir do Código Fonte

Qualquer pessoa pode compilar o APK assinado via linha de comando:

```powershell
powershell -ExecutionPolicy Bypass -File ".\scripts\build_apk.ps1"
```
O script compilará os arquivos Java (`javac`), gerará o bytecode Dalvik (`classes.dex` via D8), empacotará os recursos, alinhará (`zipalign`) e assinará o APK final em `TVBOX_Optimizer_IPTV.apk`.
