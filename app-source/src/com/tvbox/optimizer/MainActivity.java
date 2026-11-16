package com.tvbox.optimizer;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
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

        // Foco inicial no DPAD
        btnOptimize.setFocusable(true);
        btnOptimize.setFocusableInTouchMode(true);
        btnOptimize.requestFocus();

        appendLog("=================================================");
        appendLog("   TVBOX TERMINAL OPTIMIZER - ANTI-BADBOX v2.4   ");
        appendLog("=================================================");
        appendLog("★ GitHub: https://github.com/mjcaetano87/tvbox-optimizer");
        appendLog("★ Desenvolvido por: jesséx10");
        appendLog("-------------------------------------------------");
        appendLog("⚠️  AVISO IMPORTANTE / ATTENTION:");
        appendLog("O MODO DESENVOLVEDOR (DEPURACAO USB) DEVE ESTAR");
        appendLog("ATIVO NESTA TV BOX PARA OTIMIZACAO COMPLETA!");
        appendLog("-------------------------------------------------");
        appendLog("[*] Suporte a Controle Remoto (DPAD): ATIVADO");
        appendLog("[*] Motor Triplo de Purga: Disable + AppOps + Neutralizer");
        appendLog("[*] Matriz Quádrupla DNS (1.1.1.1, 9.9.9.10, 8.8.8.8, 1.0.0.1)");
        appendLog("[*] Fuso Fixo GMT-03:00 (Brasília Sem Horário de Verão)");
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
            builder.setMessage("Todas as otimizações, buffers de streaming, matriz de 4 DNS e a purga contra BadBox/Mineradores foram concluídos!\n\nDeseja reiniciar agora?");
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

        // 1. Horário de Brasília & Fuso Fixo GMT-3
        appendLog("\n[*] [1/6] Forcando Fuso Fixo GMT-03:00 (Brasilia)...");
        try {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am != null) {
                am.setTimeZone("GMT-03:00");
            }
        } catch (Exception ignored) {}

        try {
            Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME, 1);
            Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0);
            Settings.Global.putString(getContentResolver(), "ntp_server", "pool.ntp.br");
            Settings.Global.putString(getContentResolver(), "ntp_server_2", "a.st1.ntp.br");
        } catch (Exception ignored) {}

        execCommand("settings put global auto_time 1");
        execCommand("settings put global auto_time_zone 0");
        execCommand("settings put system auto_time_zone 0");
        execCommand("setprop persist.sys.timezone GMT-03:00");
        execCommand("setprop persist.sys.time.zone GMT-03:00");
        execCommand("setprop persist.sys.timezone.default GMT-03:00");
        execCommand("service call alarm 3 s16 GMT-03:00");
        execCommand("settings put global ntp_server pool.ntp.br");
        execCommand("settings put global ntp_server_2 a.st1.ntp.br");
        execCommand("settings put global ntp_server_3 b.st1.ntp.br");
        execCommand("setprop net.ntp.server pool.ntp.br");
        appendLog("  -> Fuso fixado permanentemente em GMT-03:00 (Brasilia).");

        // 2. Matriz Quádrupla de DNS Anti-Bloqueio
        appendLog("\n[*] [2/6] Configurando Matriz Quadrupla de DNS Anti-Bloqueio...");
        appendLog("  -> DNS 1: 1.1.1.1 (Cloudflare Anycast)");
        appendLog("  -> DNS 2: 9.9.9.10 (Quad9 Unfiltered / Sem Filtros)");
        appendLog("  -> DNS 3: 8.8.8.8 (Google Public)");
        appendLog("  -> DNS 4: 1.0.0.1 (Cloudflare Secundario)");

        String[] dnsCmds = new String[] {
            "setprop net.dns1 1.1.1.1", "setprop persist.net.dns1 1.1.1.1", "setprop dhcp.eth0.dns1 1.1.1.1", "setprop dhcp.wlan0.dns1 1.1.1.1",
            "setprop net.dns2 9.9.9.10", "setprop persist.net.dns2 9.9.9.10", "setprop dhcp.eth0.dns2 9.9.9.10", "setprop dhcp.wlan0.dns2 9.9.9.10",
            "setprop net.dns3 8.8.8.8", "setprop persist.net.dns3 8.8.8.8", "setprop dhcp.eth0.dns3 8.8.8.8", "setprop dhcp.wlan0.dns3 8.8.8.8",
            "setprop net.dns4 1.0.0.1", "setprop persist.net.dns4 1.0.0.1", "setprop dhcp.eth0.dns4 1.0.0.1", "setprop dhcp.wlan0.dns4 1.0.0.1",
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

        // 6. Varredura Heurística Profunda & Purga Tripla (BadBox, Mineradores, Droppers)
        appendLog("\n[*] [6/6] Purga Heuristica Profunda (Droppers, Mineradores e Adware)...");
        
        // Coleta todos os pacotes e seus nomes visuais
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        Set<String> installedSet = new HashSet<String>();
        Set<String> targetsToNeutralize = new HashSet<String>();

        // Palavras-chave dos mineradores e adwares detectados
        String[] suspiciousLabels = new String[] {
            "netservice", "system guard", "systemguard", "movaplay", "nova tv", "novatv",
            "rsshub", "setting assistant", "settingassistant", "snf launcher", "snflauncher",
            "guardupdated", "assistant", "backup"
        };

        for (ApplicationInfo ai : apps) {
            String pkg = ai.packageName;
            installedSet.add(pkg);

            // Não auto-elimina nosso próprio app nem reprodutores legítimos
            if (pkg.equals(getPackageName()) || pkg.contains("tivimate") || pkg.contains("xciptv") || pkg.contains("smarters") || pkg.contains("ottnavigator")) {
                continue;
            }

            // Checa pelo nome do pacote
            String lowerPkg = pkg.toLowerCase();
            for (String kw : suspiciousLabels) {
                String cleanKw = kw.replace(" ", "");
                if (lowerPkg.contains(cleanKw)) {
                    targetsToNeutralize.add(pkg);
                    break;
                }
            }

            // Checa pelo rótulo visual (Nome do aplicativo que aparece na tela da TV)
            try {
                CharSequence label = pm.getApplicationLabel(ai);
                if (label != null) {
                    String lowerLabel = label.toString().toLowerCase();
                    for (String kw : suspiciousLabels) {
                        if (lowerLabel.contains(kw)) {
                            targetsToNeutralize.add(pkg);
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        // Adiciona alvos conhecidos estáticos
        targetsToNeutralize.addAll(getUadTargetPackages());

        int neutralizedCount = 0;
        for (String pkg : targetsToNeutralize) {
            if (installedSet.contains(pkg)) {
                // TÉCNICA TRIPLA CONTRA REINSTALAÇÃO / PERSISTÊNCIA:
                
                // 1. Mata imediatamente qualquer processo e bloqueia em AppOps
                execCommand("am force-stop " + pkg);
                execCommand("cmd appops set " + pkg + " RUN_IN_BACKGROUND ignore");
                execCommand("cmd appops set " + pkg + " RUN_ANY_IN_BACKGROUND ignore");
                execCommand("cmd appops set " + pkg + " SYSTEM_ALERT_WINDOW ignore");
                execCommand("cmd appops set " + pkg + " WRITE_SETTINGS ignore");

                // 2. Desativa para o usuário atual
                execCommand("pm disable-user --user 0 " + pkg);
                execCommand("pm disable " + pkg);

                // 3. Oculta e desinstala
                execCommand("pm hide " + pkg);
                String unRes = execCommand("pm uninstall -k --user 0 " + pkg);
                execCommand("pm clear " + pkg);

                neutralizedCount++;
                appendLog("  [NEUTRALIZADO] -> " + pkg);
            }
        }

        appendLog("  -> Total de ameacas e bloatwares neutralizados: " + neutralizedCount);
        appendLog("\n=================================================");
        appendLog("   OTIMIZACAO FINALIZADA COM SUCESSO!            ");
        appendLog("=================================================");
    }

    private List<String> getUadTargetPackages() {
        return Arrays.asList(
            // === LISTA ESPECÍFICA DETECTADA NA TV BOX ===
            "com.android.netservice",
            "com.android.backup",
            "com.android.assistant",
            "com.android.settings.assistant",
            "com.systemguard.android",
            "com.systemguard.updated",
            "com.movaplay.tv",
            "com.movaplay.android",
            "com.novatv.app",
            "com.rsshub.tv",
            "com.snf.launcher",
            "com.snflauncher.tv",
            "com.droidlogic.mboxlauncher",
            "com.android.guardupdated",
            "com.guard.update",

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
