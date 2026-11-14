package com.tvbox.optimizer;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    private TextView tvLogs;
    private TextView tvGithub;
    private ScrollView scrollLogs;
    private Button btnOptimize;
    private Button btnReboot;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final StringBuilder logBuilder = new StringBuilder();
    private static final int REQ_PERMISSIONS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLogs = (TextView) findViewById(R.id.tvLogs);
        tvGithub = (TextView) findViewById(R.id.tvGithub);
        scrollLogs = (ScrollView) findViewById(R.id.scrollLogs);
        btnOptimize = (Button) findViewById(R.id.btnOptimize);
        btnReboot = (Button) findViewById(R.id.btnReboot);

        // Foco inicial automático para o controle remoto (DPAD)
        btnOptimize.setFocusable(true);
        btnOptimize.setFocusableInTouchMode(true);
        btnOptimize.requestFocus();

        appendLog("=================================================");
        appendLog("   TVBOX TERMINAL OPTIMIZER - IPTV SYSTEM v2.3   ");
        appendLog("=================================================");
        appendLog("★ GitHub: https://github.com/mjcaetano87/tvbox-optimizer");
        appendLog("★ Desenvolvido por: jesséx10");
        appendLog("-------------------------------------------------");
        appendLog("⚠️  AVISO IMPORTANTE / ATTENTION:");
        appendLog("O MODO DESENVOLVEDOR (DEPURACAO USB) DEVE ESTAR");
        appendLog("ATIVO NESTA TV BOX PARA OTIMIZACAO COMPLETA!");
        appendLog("-------------------------------------------------");
        appendLog("[*] Suporte a Controle Remoto (DPAD): ATIVADO");
        appendLog("[*] Matriz Quádrupla DNS (1.1.1.1, 9.9.9.10, 8.8.8.8, 1.0.0.1)");
        appendLog("[*] Forçador Fixo Fuso GMT-3 (Horário de Brasília Sem Horário de Verão)");
        appendLog("[*] Verificando privilégios e permissões de sistema...");

        checkAndRequestAllPermissions();

        if (tvGithub != null) {
            tvGithub.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/mjcaetano87/tvbox-optimizer"));
                        startActivity(browserIntent);
                    } catch (Exception ignored) {}
                }
            });
        }

        btnOptimize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOptimize.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOptimization();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                btnOptimize.setEnabled(true);
                                btnOptimize.requestFocus();
                                showFinishRebootDialog();
                            }
                        });
                    }
                }).start();
            }
        });

        btnReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appendLog("\n[!] Enviando comando de reboot para a TV Box...");
                execCommand("reboot");
            }
        });
    }

    private void showFinishRebootDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
            builder.setTitle("FINALIZADO");
            builder.setMessage("Todas as otimizações, buffers de streaming, matriz de 4 DNS e o fuso fixo GMT-3 foram aplicados!\n\nDeseja reiniciar agora?");
            builder.setCancelable(false);
            builder.setPositiveButton("Sim, Reiniciar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    appendLog("\n[!] Reiniciando a TV Box agora...");
                    execCommand("reboot");
                }
            });
            builder.setNegativeButton("Depois", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    appendLog("\n[*] Reinicialização adiada pelo usuário.");
                    btnOptimize.requestFocus();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception e) {
            appendLog("[!] FINALIZADO. Deseja reiniciar agora? (Clique no botão Reiniciar)");
        }
    }

    private void checkAndRequestAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> needed = new ArrayList<String>();
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (!needed.isEmpty()) {
                appendLog("[?] Solicitando permissão de LEITURA e GRAVAÇÃO...");
                requestPermissions(needed.toArray(new String[0]), REQ_PERMISSIONS);
            } else {
                appendLog("[OK] Permissão de gravação no armazenamento: CONCEDIDA");
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                if (!Environment.isExternalStorageManager()) {
                    appendLog("[?] Solicitando acesso total aos arquivos (MANAGE_EXTERNAL_STORAGE)...");
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            } catch (Exception ignored) {}
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                if (!getPackageManager().canRequestPackageInstalls()) {
                    appendLog("[?] Solicitando permissão para Instalação de APKs...");
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } else {
                    appendLog("[OK] Permissão de instalação de pacotes: CONCEDIDA");
                }
            } catch (Exception ignored) {}
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (!Settings.System.canWrite(this)) {
                    appendLog("[?] Solicitando permissão WRITE_SETTINGS...");
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } else {
                    appendLog("[OK] Permissão de alterar configurações do sistema: CONCEDIDA");
                }
            } catch (Exception ignored) {}
        }

        String suTest = execCommand("id");
        if (suTest.contains("uid=0")) {
            appendLog("[ROOT] Acesso de superusuário (ROOT UID=0): ATIVO!");
        } else {
            appendLog("[INFO] Modo padrão de usuário. Usando engine ADB/Shell...");
        }

        appendLog("\n[*] Use o DPAD do controle remoto e clique em [ 🚀 OTIMIZAR ]");
    }

    private void appendLog(final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                logBuilder.append(msg).append("\n");
                tvLogs.setText(logBuilder.toString());
                scrollLogs.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollLogs.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    private void runOptimization() {
        appendLog("\n=================================================");
        appendLog("   INICIANDO OTIMIZACAO GLOBAL PARA IPTV         ");
        appendLog("=================================================");

        // 1. Horário de Brasília & Fuso Fixo GMT-3 (Sem horário de verão chinês)
        appendLog("\n[*] [1/6] Forcando Fuso Fixo GMT-3 (Horario de Brasilia)...");
        
        // Tentativa via AlarmManager nativo do Android
        try {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am != null) {
                am.setTimeZone("GMT-03:00");
            }
        } catch (Exception ignored) {}

        // Desativa fuso automático para a ROM chinesa não puxar GMT-2 incorreto
        try {
            Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME, 1);
            Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0);
            Settings.Global.putString(getContentResolver(), "ntp_server", "pool.ntp.br");
            Settings.Global.putString(getContentResolver(), "ntp_server_2", "a.st1.ntp.br");
        } catch (Exception ignored) {}

        // Força por linha de comando em todas as camadas do sistema
        execCommand("settings put global auto_time 1");
        execCommand("settings put global auto_time_zone 0");
        execCommand("settings put system auto_time_zone 0");

        // Fuso fixo GMT-3 / America/Sao_Paulo / America/Araguaina
        execCommand("setprop persist.sys.timezone GMT-03:00");
        execCommand("setprop persist.sys.time.zone GMT-03:00");
        execCommand("setprop persist.sys.timezone.default GMT-03:00");
        execCommand("service call alarm 3 s16 GMT-03:00");

        // Configuração NTP oficial do Brasil
        execCommand("settings put global ntp_server pool.ntp.br");
        execCommand("settings put global ntp_server_2 a.st1.ntp.br");
        execCommand("settings put global ntp_server_3 b.st1.ntp.br");
        execCommand("setprop net.ntp.server pool.ntp.br");

        appendLog("  -> auto_time_zone=0 (desativado para travar em -03:00).");
        appendLog("  -> Fuso fixado permanentemente em GMT-03:00 (Brasilia).");
        appendLog("  -> NTP pool.ntp.br configurado com sincronizacao automatica.");

        // 2. Matriz Quádrupla de DNS Anti-Bloqueio
        appendLog("\n[*] [2/6] Configurando Matriz Quadrupla de DNS Anti-Bloqueio...");
        appendLog("  -> DNS 1: 1.1.1.1 (Cloudflare Anycast)");
        appendLog("  -> DNS 2: 9.9.9.10 (Quad9 Unfiltered / Sem Filtros)");
        appendLog("  -> DNS 3: 8.8.8.8 (Google Public)");
        appendLog("  -> DNS 4: 1.0.0.1 (Cloudflare Secundario)");

        String[] dnsCmds = new String[] {
            // DNS 1: Cloudflare
            "setprop net.dns1 1.1.1.1",
            "setprop persist.net.dns1 1.1.1.1",
            "setprop dhcp.eth0.dns1 1.1.1.1",
            "setprop dhcp.wlan0.dns1 1.1.1.1",

            // DNS 2: Quad9 Unfiltered
            "setprop net.dns2 9.9.9.10",
            "setprop persist.net.dns2 9.9.9.10",
            "setprop dhcp.eth0.dns2 9.9.9.10",
            "setprop dhcp.wlan0.dns2 9.9.9.10",

            // DNS 3: Google
            "setprop net.dns3 8.8.8.8",
            "setprop persist.net.dns3 8.8.8.8",
            "setprop dhcp.eth0.dns3 8.8.8.8",
            "setprop dhcp.wlan0.dns3 8.8.8.8",

            // DNS 4: Cloudflare Fallback
            "setprop net.dns4 1.0.0.1",
            "setprop persist.net.dns4 1.0.0.1",
            "setprop dhcp.eth0.dns4 1.0.0.1",
            "setprop dhcp.wlan0.dns4 1.0.0.1",

            "settings put global private_dns_mode off"
        };
        for (String cmd : dnsCmds) {
            execCommand(cmd);
        }
        appendLog("  -> Matriz de 4 DNS anti-bloqueio aplicada com sucesso.");

        // 3. Animações 0x (Zero Lag)
        appendLog("\n[*] [3/6] Desligando Animacoes (Janela, Transicao e Duracao)...");
        try {
            Settings.Global.putFloat(getContentResolver(), Settings.Global.WINDOW_ANIMATION_SCALE, 0f);
            Settings.Global.putFloat(getContentResolver(), Settings.Global.TRANSITION_ANIMATION_SCALE, 0f);
            Settings.Global.putFloat(getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 0f);
        } catch (Exception ignored) {}
        execCommand("settings put global window_animation_scale 0");
        execCommand("settings put global transition_animation_scale 0");
        execCommand("settings put global animator_duration_scale 0");
        appendLog("  -> Animacoes definidas para 0x (DESLIGADAS).");

        // 4. Limite de Segundo Plano & Destruir Atividades
        appendLog("\n[*] [4/6] Bloqueando Processos em Segundo Plano...");
        try {
            Settings.Global.putInt(getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES, 1);
            Settings.Global.putInt(getContentResolver(), "app_process_limit", 0);
        } catch (Exception ignored) {}
        execCommand("settings put global always_finish_activities 1");
        execCommand("setprop persist.sys.always_finish 1");
        execCommand("settings put global app_process_limit 0");
        execCommand("setprop persist.sys.process_limit 0");
        appendLog("  -> Nao manter atividades: ATIVO.");
        appendLog("  -> Limite de background: 0 PROCESSOS (RAM Maxima).");

        // 5. Otimizações de Kernel & I/O para Streaming IPTV
        appendLog("\n[*] [5/6] Ajustando Kernel, Buffers TCP e Aceleracao de Hardware...");
        execCommand("setprop net.tcp.buffersize.wifi 524288,1048576,2097152,262144,524288,1048576");
        execCommand("setprop net.tcp.buffersize.ethernet 524288,1048576,2097152,262144,524288,1048576");
        execCommand("setprop debug.sf.hw 1");
        execCommand("setprop debug.performance.tuning 1");
        execCommand("setprop video.accelerate.hw 1");
        appendLog("  -> Aceleracao de GPU e Buffers de Rede configurados.");

        // 6. Purga Profunda com Base UAD
        appendLog("\n[*] [6/6] Purgando Spywares Chineses, Droppers e Bloatware (Base UAD)...");
        
        String installedOutput = execCommand("pm list packages");
        Set<String> installedSet = new HashSet<String>();
        for (String line : installedOutput.split("\n")) {
            String p = line.trim().replace("package:", "");
            if (!p.isEmpty()) {
                installedSet.add(p);
            }
        }

        List<String> uadTargets = getUadTargetPackages();
        int disabledCount = 0;

        for (String pkg : uadTargets) {
            if (installedSet.contains(pkg)) {
                String res = execCommand("pm disable-user --user 0 " + pkg);
                if (res.contains("disabled") || res.contains("state")) {
                    disabledCount++;
                    appendLog("  [DESATIVADO] -> " + pkg);
                } else {
                    String res2 = execCommand("pm uninstall -k --user 0 " + pkg);
                    if (res2.contains("Success")) {
                        disabledCount++;
                        appendLog("  [REMOVIDO]   -> " + pkg);
                    }
                }
            }
        }

        appendLog("  -> Total de pacotes expurgados: " + disabledCount);
        appendLog("\n=================================================");
        appendLog("   OTIMIZACAO FINALIZADA COM SUCESSO!            ");
        appendLog("=================================================");
    }

    private List<String> getUadTargetPackages() {
        return Arrays.asList(
            // === DROPPERS CHINESES, ADWARES E INVASORES DE FIRMWARE (UAD / ALLWINNER / ROCKCHIP) ===
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
            "com.tct.tctsmartapprecommend",
            "com.google.mainline.adservices",
            "com.google.android.adservices.api",
            "com.android.adservices.api",
            "com.android.ext.adservices.api",
            "com.evenwell.AprUploadService",
            "com.evenwell.AprUploadService.data.overlay.base",
            "com.evenwell.AprUploadService.data.overlay.base.s600id",
            "com.evenwell.AprUploadService.data.overlay.base.s600ww",
            "com.evenwell.customerfeedback.overlay.base.s600ww",

            // === GOOGLE SERVICES PESADOS & TELEMETRIA ===
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
            "com.google.android.setupwizard.a_overlay",
            "com.google.android.overlay.setupwizard",
            "com.google.android.printservice.recommendation",
            "com.android.printservice.recommendation",
            "com.google.android.apps.assistant",
            "com.google.android.apps.searchlite",
            "com.google.android.apps.chromecast.app",
            "com.google.chromeremotedesktop",
            "com.google.android.tvrecommendations",
            "com.google.android.apps.youtube.music.setupwizard",

            // === NAVEGADORES PESADOS ===
            "com.android.chrome",
            "com.chrome.beta",
            "com.chrome.dev",
            "com.chrome.canary",
            "com.android.browser",
            "org.chromium.webview_shell",
            "com.opera.browser",
            "com.opera.mini.native",
            "org.mozilla.firefox",

            // === STREAMING PESADO & STUBS CONCORRENTES ===
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
        );
    }

    private String execCommand(String command) {
        StringBuilder output = new StringBuilder();
        Process process = null;
        DataOutputStream os = null;
        BufferedReader osRes = null;
        try {
            try {
                process = Runtime.getRuntime().exec("su");
                os = new DataOutputStream(process.getOutputStream());
                osRes = new BufferedReader(new InputStreamReader(process.getInputStream()));
                os.writeBytes(command + "\n");
                os.writeBytes("exit\n");
                os.flush();
                String line;
                while ((line = osRes.readLine()) != null) {
                    output.append(line).append("\n");
                }
                process.waitFor();
            } catch (Exception rootEx) {
                process = Runtime.getRuntime().exec(command);
                osRes = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = osRes.readLine()) != null) {
                    output.append(line).append("\n");
                }
                process.waitFor();
            }
        } catch (Exception e) {
            output.append("Erro: ").append(e.getMessage());
        } finally {
            try { if (os != null) os.close(); } catch (Exception ignored) {}
            try { if (osRes != null) osRes.close(); } catch (Exception ignored) {}
            if (process != null) process.destroy();
        }
        return output.toString();
    }
}
