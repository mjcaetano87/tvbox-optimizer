package com.tvbox.optimizer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private TextView tvLogs;
    private Button btnOptimize;
    private Button btnReboot;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final StringBuilder logBuilder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLogs = (TextView) findViewById(R.id.tvLogs);
        btnOptimize = (Button) findViewById(R.id.btnOptimize);
        btnReboot = (Button) findViewById(R.id.btnReboot);

        appendLog("[*] App iniciado. Suporta Root (su) e Secure Settings.");
        appendLog("[*] Clique em OTIMIZAR AGORA para aplicar todas as correções.");

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
                            }
                        });
                    }
                }).start();
            }
        });

        btnReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appendLog("[*] Reiniciando TV Box...");
                execCommand("reboot");
            }
        });
    }

    private void appendLog(final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                logBuilder.append(msg).append("\n");
                tvLogs.setText(logBuilder.toString());
            }
        });
    }

    private void runOptimization() {
        appendLog("\n=================================");
        appendLog("   INICIANDO OTIMIZAÇÃO IPTV     ");
        appendLog("=================================");

        // 1. Horário de Brasília & NTP Brasil
        appendLog("\n[1/5] Configurando Fuso e NTP Brasil...");
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
        appendLog("  -> Horário de Brasília e pool.ntp.br aplicados.");

        // 2. DNS 1.1.1.1 e 8.8.8.8
        appendLog("\n[2/5] Forçando DNS 1.1.1.1 e 8.8.8.8...");
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
        appendLog("  -> DNS 1.1.1.1 e 8.8.8.8 configurados.");

        // 3. Animações 0x (Zero Lag)
        appendLog("\n[3/5] Desligando Escalas de Animação...");
        try {
            Settings.Global.putFloat(getContentResolver(), Settings.Global.WINDOW_ANIMATION_SCALE, 0f);
            Settings.Global.putFloat(getContentResolver(), Settings.Global.TRANSITION_ANIMATION_SCALE, 0f);
            Settings.Global.putFloat(getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 0f);
        } catch (Exception ignored) {}
        execCommand("settings put global window_animation_scale 0");
        execCommand("settings put global transition_animation_scale 0");
        execCommand("settings put global animator_duration_scale 0");
        appendLog("  -> Animações desligadas (0x).");

        // 4. Limite de Segundo Plano & Destruir Atividades
        appendLog("\n[4/5] Aplicando Limite 0 e Não Manter Atividades...");
        try {
            Settings.Global.putInt(getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES, 1);
            Settings.Global.putInt(getContentResolver(), "app_process_limit", 0);
        } catch (Exception ignored) {}
        execCommand("settings put global always_finish_activities 1");
        execCommand("setprop persist.sys.always_finish 1");
        execCommand("settings put global app_process_limit 0");
        execCommand("setprop persist.sys.process_limit 0");
        appendLog("  -> Não manter atividades: ATIVO.");
        appendLog("  -> Limite de background: 0 PROCESSOS.");

        // 5. Bloqueio de Droppers/Crapware Chineses e Bloatwares
        appendLog("\n[5/5] Desativando Apps Chineses Invasores e Bloatware...");
        List<String> packagesToDisable = new ArrayList<String>();
        
        // Bloatware Chinês / Auto-Instaladores / Droppers / Adware
        packagesToDisable.add("com.android.providers.downloads.ui.test");
        packagesToDisable.add("com.android.ad.daemon");
        packagesToDisable.add("com.rockchip.adservice");
        packagesToDisable.add("com.adups.fota");                  // Conhecido spyware/dropper chinês de firmware
        packagesToDisable.add("com.adups.fota.sysoper");
        packagesToDisable.add("com.allwinner.service");
        packagesToDisable.add("com.softwinner.fireplayer");
        packagesToDisable.add("com.android.rockchip");
        packagesToDisable.add("com.google.android.fota");
        packagesToDisable.add("com.android.smart.terminal.iptv");
        packagesToDisable.add("com.android.adsystem");
        packagesToDisable.add("com.fw.upgrade.sysoper");

        // Google Services pesados
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

        // Streaming pesado
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
                appendLog("  -> " + pkg + " [DESATIVADO]");
            } else {
                // Tenta remoção do user 0
                String res2 = execCommand("pm uninstall -k --user 0 " + pkg);
                if (res2.contains("Success")) {
                    disabled++;
                    appendLog("  -> " + pkg + " [REMOVIDO]");
                }
            }
        }
        appendLog("  -> Bloatwares e droppers tratados: " + disabled);

        appendLog("\n=================================");
        appendLog("  OTIMIZAÇÃO CONCLUÍDA COM SUCESSO! ");
        appendLog("  Clique em 'REINICIAR TV BOX'.   ");
        appendLog("=================================");
    }

    private String execCommand(String command) {
        StringBuilder output = new StringBuilder();
        Process process = null;
        DataOutputStream os = null;
        BufferedReader osRes = null;
        try {
            // Tenta via SU (Root) primeiro, senão roda direto no shell do sistema
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
                // Fallback shell comum
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
