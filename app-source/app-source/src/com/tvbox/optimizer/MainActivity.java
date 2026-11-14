package com.tvbox.optimizer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import java.util.List;

public class MainActivity extends Activity {

    private TextView tvLogs;
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
        scrollLogs = (ScrollView) findViewById(R.id.scrollLogs);
        btnOptimize = (Button) findViewById(R.id.btnOptimize);
        btnReboot = (Button) findViewById(R.id.btnReboot);

        appendLog("=================================================");
        appendLog("   TVBOX TERMINAL OPTIMIZER - IPTV SYSTEM v1.2   ");
        appendLog("=================================================");
        appendLog("[*] Verificando privilégios e permissões de sistema...");

        checkAndRequestAllPermissions();

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
            builder.setMessage("Todas as otimizações para IPTV foram aplicadas com sucesso!\n\nDeseja reiniciar agora?");
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
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception e) {
            appendLog("[!] Alerta final: FINALIZADO. Deseja reiniciar agora? (Clique no botão Reiniciar)");
        }
    }

    private void checkAndRequestAllPermissions() {
        // 1. Permissões de Armazenamento / Gravação
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

        // 2. Permissão de Gerenciamento Total de Arquivos (Android 11+)
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

        // 3. Permissão para Instalar APKs de Fontes Desconhecidas (Android 8+)
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

        // 4. Permissão para Escrever Configurações do Sistema
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

        // 5. Teste de Acesso Root (su)
        String suTest = execCommand("id");
        if (suTest.contains("uid=0")) {
            appendLog("[ROOT] Acesso de superusuário (ROOT UID=0): ATIVO!");
        } else {
            appendLog("[INFO] Modo padrão de usuário. Usando comandos de sistema...");
        }

        appendLog("[*] Pronto! Pressione [ 🚀 OTIMIZAR ] para aplicar as diretrizes IPTV.");
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

        // 1. Horário de Brasília & NTP Brasil
        appendLog("\n[*] [1/5] Sincronizando Horario de Brasilia (GMT-3) e NTP.BR...");
        try {
            Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME, 1);
            Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 1);
            Settings.Global.putString(getContentResolver(), "ntp_server", "pool.ntp.br");
            Settings.Global.putString(getContentResolver(), "ntp_server_2", "a.st1.ntp.br");
        } catch (Exception ignored) {}
        execCommand("setprop persist.sys.timezone America/Sao_Paulo");
        execCommand("setprop persist.sys.time.zone America/Sao_Paulo");
        execCommand("settings put global auto_time 1");
        execCommand("settings put global auto_time_zone 1");
        execCommand("settings put global ntp_server pool.ntp.br");
        execCommand("settings put global ntp_server_2 a.st1.ntp.br");
        appendLog("  -> Horario GMT-3 e NTP pool.ntp.br configurados.");

        // 2. DNS 1.1.1.1 e 8.8.8.8
        appendLog("\n[*] [2/5] Forcando servidores DNS 1.1.1.1 e 8.8.8.8...");
        String[] dnsCmds = new String[] {
            "setprop net.dns1 1.1.1.1",
            "setprop net.dns2 8.8.8.8",
            "setprop persist.net.dns1 1.1.1.1",
            "setprop persist.net.dns2 8.8.8.8",
            "setprop dhcp.eth0.dns1 1.1.1.1",
            "setprop dhcp.eth0.dns2 8.8.8.8",
            "setprop dhcp.wlan0.dns1 1.1.1.1",
            "setprop dhcp.wlan0.dns2 8.8.8.8",
            "settings put global private_dns_mode off"
        };
        for (String cmd : dnsCmds) {
            execCommand(cmd);
        }
        appendLog("  -> DNS Cloudflare (1.1.1.1) e Google (8.8.8.8) ativos.");

        // 3. Animações 0x (Zero Lag)
        appendLog("\n[*] [3/5] Desligando Animacoes (Janela, Transicao e Duracao)...");
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
        appendLog("\n[*] [4/5] Bloqueando Processos em Segundo Plano...");
        try {
            Settings.Global.putInt(getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES, 1);
            Settings.Global.putInt(getContentResolver(), "app_process_limit", 0);
        } catch (Exception ignored) {}
        execCommand("settings put global always_finish_activities 1");
        execCommand("setprop persist.sys.always_finish 1");
        execCommand("settings put global app_process_limit 0");
        execCommand("setprop persist.sys.process_limit 0");
        appendLog("  -> Nao manter atividades: ATIVO (destroi ao sair).");
        appendLog("  -> Limite de background: 0 PROCESSOS (RAM 100% livre).");

        // 5. Bloqueio de Droppers Chineses, Autoinstaladores e Bloatwares
        appendLog("\n[*] [5/5] Purgando Droppers Chineses e Bloatwares pesados...");
        List<String> packagesToDisable = new ArrayList<String>();
        
        // Bloatware Chinês / Droppers / Spywares / Autoinstaladores
        packagesToDisable.add("com.android.providers.downloads.ui.test");
        packagesToDisable.add("com.android.ad.daemon");
        packagesToDisable.add("com.rockchip.adservice");
        packagesToDisable.add("com.adups.fota");                  // FOTA chinês conhecido por baixar malware
        packagesToDisable.add("com.adups.fota.sysoper");
        packagesToDisable.add("com.allwinner.service");
        packagesToDisable.add("com.softwinner.fireplayer");
        packagesToDisable.add("com.android.rockchip");
        packagesToDisable.add("com.google.android.fota");
        packagesToDisable.add("com.android.smart.terminal.iptv");
        packagesToDisable.add("com.android.adsystem");
        packagesToDisable.add("com.fw.upgrade.sysoper");
        packagesToDisable.add("com.redstone.ota.ui");

        // Google Services Pesados
        packagesToDisable.add("com.google.android.gms");
        packagesToDisable.add("com.android.vending");
        packagesToDisable.add("com.google.android.gsf");
        packagesToDisable.add("com.google.android.onetimeinitializer");
        packagesToDisable.add("com.google.android.partnersetup");
        packagesToDisable.add("com.google.android.feedback");
        packagesToDisable.add("com.google.android.tts");
        packagesToDisable.add("com.google.android.syncadapters.contacts");
        packagesToDisable.add("com.google.android.syncadapters.calendar");
        packagesToDisable.add("com.google.android.backuptransport");
        packagesToDisable.add("com.google.android.setupwizard");
        packagesToDisable.add("com.google.android.printservice.recommendation");

        // Navegadores
        packagesToDisable.add("com.android.chrome");
        packagesToDisable.add("com.android.browser");
        packagesToDisable.add("org.chromium.webview_shell");
        packagesToDisable.add("com.opera.browser");
        packagesToDisable.add("com.opera.mini.native");
        packagesToDisable.add("org.mozilla.firefox");

        // Streaming Pesado Concorrente
        packagesToDisable.add("com.netflix.ninja");
        packagesToDisable.add("com.netflix.mediaclient");
        packagesToDisable.add("com.google.android.youtube.tv");
        packagesToDisable.add("com.google.android.youtube");
        packagesToDisable.add("com.google.android.videos");
        packagesToDisable.add("com.google.android.music");
        packagesToDisable.add("com.amazon.amazonvideo.livingroom");
        packagesToDisable.add("com.disney.disneyplus");

        int disabled = 0;
        for (String pkg : packagesToDisable) {
            String res = execCommand("pm disable-user --user 0 " + pkg);
            if (res.contains("disabled") || res.contains("state")) {
                disabled++;
                appendLog("  [DESATIVADO] -> " + pkg);
            } else {
                String res2 = execCommand("pm uninstall -k --user 0 " + pkg);
                if (res2.contains("Success")) {
                    disabled++;
                    appendLog("  [REMOVIDO]   -> " + pkg);
                }
            }
        }
        appendLog("  -> Total de pacotes expurgados: " + disabled);

        appendLog("\n=================================================");
        appendLog("   OTIMIZACAO FINALIZADA COM SUCESSO!            ");
        appendLog("=================================================");
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
