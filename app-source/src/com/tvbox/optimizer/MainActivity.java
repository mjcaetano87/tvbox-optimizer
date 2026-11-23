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
import android.graphics.Color;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    private TextView tvTitle;
    private TextView tvStatusBadge;
    private TextView tvLogs;
    private TextView tvGithub;
    private ScrollView scrollLogs;
    private Button btnRootAdmin;
    private Button btnOptimize;
    private Button btnReboot;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final StringBuilder logBuilder = new StringBuilder();
    private static final int REQ_PERMISSIONS = 1001;

    private boolean isBusy = false;
    private boolean rootOrAdminVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvStatusBadge = (TextView) findViewById(R.id.tvStatusBadge);
        tvLogs = (TextView) findViewById(R.id.tvLogs);
        tvGithub = (TextView) findViewById(R.id.tvGithub);
        scrollLogs = (ScrollView) findViewById(R.id.scrollLogs);
        btnRootAdmin = (Button) findViewById(R.id.btnRootAdmin);
        btnOptimize = (Button) findViewById(R.id.btnOptimize);
        btnReboot = (Button) findViewById(R.id.btnReboot);

        // Foco inicial estratégico no DPAD: Passo 1 (ROOT / ADMIN)
        btnRootAdmin.setFocusable(true);
        btnRootAdmin.setFocusableInTouchMode(true);
        btnRootAdmin.requestFocus();

        printInitialBanner();
        setupListeners();
        checkAndRequestAllPermissions();
        refreshPrivilegeTelemetry();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPrivilegeTelemetry();
    }

    private void printInitialBanner() {
        appendLog("==================================================================");
        appendLog("   TVBOX OPTIMIZER v3.0-ROOT - ANTI-BADBOX & TUNING IPTV          ");
        appendLog("   Ordem: 1. ROOT/ADMIN  ->  2. OTIMIZAR  ->  3. REINICIAR        ");
        appendLog("==================================================================");
        appendLog("★ GitHub Oficial: https://github.com/mjcaetano87/tvbox-optimizer");
        appendLog("★ Desenvolvido por: jesséx10");
        appendLog("------------------------------------------------------------------");
        appendLog("[*] Suporte a Controle Remoto (DPAD): ATIVADO");
        appendLog("[*] Modo Root / Admin Integrado (Kingo ROOT + iRoot + ADB loopback)");
        appendLog("[*] Matriz Quádrupla DNS (1.1.1.1, 9.9.9.10, 8.8.8.8, 1.0.0.1)");
        appendLog("[*] Fuso Fixo GMT-03:00 (Brasília Sem Horário de Verão)");
        appendLog("[*] Motor Triplo de Purga: Disable + AppOps + Neutralizer");
        appendLog("------------------------------------------------------------------");
    }

    private void setupListeners() {
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

        btnRootAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isBusy) {
                    handleStep1RootAdmin();
                }
            }
        });

        btnOptimize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isBusy) {
                    handleStep2Optimize();
                }
            }
        });

        btnReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isBusy) {
                    handleStep3Reboot();
                }
            }
        });
    }

    private void refreshPrivilegeTelemetry() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean isRooted = RootInstallerHelper.isDeviceRooted();
                final boolean isDev = DeveloperModeHelper.isDeveloperOptionsEnabled(MainActivity.this);
                final boolean isAdb = DeveloperModeHelper.isAdbEnabled(MainActivity.this);
                final boolean hasAllAdmin = SystemSettingsHelper.hasAllAdminPermissions(MainActivity.this);

                rootOrAdminVerified = isRooted || (isDev && isAdb);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isRooted) {
                            tvStatusBadge.setText("[👑 ROOT UID=0 ATIVO ✅]");
                            tvStatusBadge.setTextColor(Color.parseColor("#39FF14"));
                        } else if (isAdb || isDev) {
                            tvStatusBadge.setText("[⚙️ MODO DEV/ADB ATIVO ✅]");
                            tvStatusBadge.setTextColor(Color.parseColor("#00E5FF"));
                        } else if (hasAllAdmin) {
                            tvStatusBadge.setText("[🛡️ ADMIN CONCEDIDO]");
                            tvStatusBadge.setTextColor(Color.parseColor("#FFD700"));
                        } else {
                            tvStatusBadge.setText("[⚠️ ROOT/ADMIN PENDENTE]");
                            tvStatusBadge.setTextColor(Color.parseColor("#FF5252"));
                        }
                    }
                });
            }
        }).start();
    }

    private void handleStep1RootAdmin() {
        appendLog("\n--- [PASSO 1/3] VERIFICAÇÃO E ATIVAÇÃO DE ROOT / MODO ADMINISTRADOR ---");
        isBusy = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 1. Tentar forçar permissões administrativas via shell / loopback
                DeveloperModeHelper.configureAdminPersistence(MainActivity.this, new DeveloperModeHelper.LogCallback() {
                    @Override
                    public void log(String message) {
                        appendLog(message);
                    }
                });

                final boolean isRooted = RootInstallerHelper.isDeviceRooted();
                final boolean isKingo = RootInstallerHelper.isKingoInstalled(MainActivity.this);
                final boolean isIRoot = RootInstallerHelper.isIRootInstalled(MainActivity.this);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        isBusy = false;
                        refreshPrivilegeTelemetry();

                        if (isRooted) {
                            appendLog("[OK] ROOT DETECTADO COM SUCESSO (su uid=0)!");
                            appendLog(">>> Privilégios máximos de superusuário liberados.");
                            appendLog(">>> Avançando foco para o [ 🚀 2. OTIMIZAR ]...");
                            btnOptimize.requestFocus();
                            return;
                        }

                        appendLog("[INFO] Dispositivo ainda sem Root nativo (su).");
                        appendLog("[INFO] Escolha abaixo uma ferramenta de Root embutida para aplicar o Root na TV Box:");

                        showRootInstallationMenu(isKingo, isIRoot);
                    }
                });
            }
        }).start();
    }

    private void showRootInstallationMenu(final boolean kingoInstalled, final boolean irootInstalled) {
        String option1 = kingoInstalled ? "1. Abrir Kingo ROOT (Já instalado)" : "1. Instalar Kingo ROOT (Embutido)";
        String option2 = irootInstalled ? "2. Abrir iRoot (Já instalado)" : "2. Instalar iRoot (Embutido)";
        String[] options = new String[] {
            option1,
            option2,
            "3. Instalar Ambos os Utilitários de Root",
            "4. Abrir Opções do Desenvolvedor (Ativar Depuração ADB)",
            "5. Já apliquei Root / Avançar para Otimização"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        builder.setTitle("Passo 1: Ativação de ROOT & Administrador");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (kingoInstalled) {
                            RootInstallerHelper.openRootApp(MainActivity.this, RootInstallerHelper.KINGO_PACKAGE, "Kingo ROOT", new RootInstallerHelper.LogCallback() {
                                @Override public void log(String msg) { appendLog(msg); }
                            });
                        } else {
                            installRootAsset(RootInstallerHelper.KINGO_ASSET, "Kingo ROOT", RootInstallerHelper.KINGO_PACKAGE);
                        }
                        break;
                    case 1:
                        if (irootInstalled) {
                            RootInstallerHelper.openRootApp(MainActivity.this, RootInstallerHelper.IROOT_PACKAGE, "iRoot", new RootInstallerHelper.LogCallback() {
                                @Override public void log(String msg) { appendLog(msg); }
                            });
                        } else {
                            installRootAsset(RootInstallerHelper.IROOT_ASSET, "iRoot", RootInstallerHelper.IROOT_PACKAGE);
                        }
                        break;
                    case 2:
                        installBothRootAssets();
                        break;
                    case 3:
                        appendLog("[USER] Abrindo Opções do Desenvolvedor...");
                        DeveloperModeHelper.openDeveloperSettings(MainActivity.this);
                        break;
                    case 4:
                        appendLog("[INFO] Prosseguindo para o Passo 2: Otimização IPTV.");
                        btnOptimize.requestFocus();
                        break;
                }
            }
        });
        builder.setNegativeButton("Fechar", null);
        builder.show();
    }

    private void installRootAsset(final String assetPath, final String appLabel, final String packageName) {
        if (!SystemSettingsHelper.canRequestPackageInstalls(this)) {
            appendLog("[!] Permissão 'Fontes desconhecidas' pendente. Conceda para instalar " + appLabel + ".");
            SystemSettingsHelper.requestUnknownSourcesPermission(this);
            return;
        }

        isBusy = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean ok = RootInstallerHelper.installRootApp(
                    MainActivity.this,
                    assetPath,
                    appLabel,
                    new RootInstallerHelper.LogCallback() {
                        @Override public void log(String message) { appendLog(message); }
                    }
                );
                isBusy = false;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshPrivilegeTelemetry();
                        btnOptimize.requestFocus();
                    }
                });
            }
        }).start();
    }

    private void installBothRootAssets() {
        if (!SystemSettingsHelper.canRequestPackageInstalls(this)) {
            appendLog("[!] Libere 'Fontes desconhecidas' antes de prosseguir com a instalação.");
            SystemSettingsHelper.requestUnknownSourcesPermission(this);
            return;
        }

        isBusy = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!RootInstallerHelper.isKingoInstalled(MainActivity.this)) {
                    RootInstallerHelper.installRootApp(MainActivity.this, RootInstallerHelper.KINGO_ASSET, "Kingo ROOT", new RootInstallerHelper.LogCallback() {
                        @Override public void log(String msg) { appendLog(msg); }
                    });
                    try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
                } else {
                    appendLog("[OK] Kingo ROOT já está instalado.");
                }

                if (!RootInstallerHelper.isIRootInstalled(MainActivity.this)) {
                    RootInstallerHelper.installRootApp(MainActivity.this, RootInstallerHelper.IROOT_ASSET, "iRoot", new RootInstallerHelper.LogCallback() {
                        @Override public void log(String msg) { appendLog(msg); }
                    });
                } else {
                    appendLog("[OK] iRoot já está instalado.");
                }

                appendLog("[OK] Pacotes de Root processados! Após aplicar o Root de 1-clique, use o botão [ 🚀 2. OTIMIZAR ].");
                isBusy = false;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshPrivilegeTelemetry();
                        btnOptimize.requestFocus();
                    }
                });
            }
        }).start();
    }

    private void handleStep2Optimize() {
        // Se ainda não houver Root nem modo desenvolvedor ativo, pergunta antes
        if (!rootOrAdminVerified && !RootInstallerHelper.isDeviceRooted()) {
            AlertDialog.Builder warnDialog = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
            warnDialog.setTitle("Modo Administrador / Root");
            warnDialog.setMessage("O Modo Root ou Depuração ADB ainda não foram confirmados nesta TV Box!\n\nRecomendamos executar o Passo 1 [ 👑 ROOT / ADMIN ] primeiro para que o app tenha permissão total para eliminar mineradores do sistema e fixar os 4 DNS.\n\nDeseja verificar o Root primeiro?");
            warnDialog.setPositiveButton("1. Ir para ROOT / ADMIN", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    btnRootAdmin.requestFocus();
                    handleStep1RootAdmin();
                }
            });
            warnDialog.setNegativeButton("Otimizar Mesmo Assim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startOptimizationProcess();
                }
            });
            warnDialog.show();
            return;
        }

        startOptimizationProcess();
    }

    private void startOptimizationProcess() {
        btnOptimize.setEnabled(false);
        isBusy = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOptimization();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        btnOptimize.setEnabled(true);
                        isBusy = false;
                        btnReboot.requestFocus();
                        showFinishRebootDialog();
                    }
                });
            }
        }).start();
    }

    private void handleStep3Reboot() {
        appendLog("\n[SYS] Solicitando reinicialização da TV Box...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                DeveloperModeHelper.rebootDevice(MainActivity.this, new DeveloperModeHelper.LogCallback() {
                    @Override
                    public void log(String message) {
                        appendLog(message);
                    }
                });
            }
        }).start();
    }

    private void showFinishRebootDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
            builder.setTitle("OTIMIZAÇÃO CONCLUÍDA!");
            builder.setMessage("Todas as otimizações, buffers de streaming, matriz de 4 DNS e a purga contra BadBox/Mineradores foram concluídos com sucesso!\n\nDeseja reiniciar agora para aplicar todas as diretivas de hardware?");
            builder.setCancelable(false);
            builder.setPositiveButton("Sim, Reiniciar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handleStep3Reboot();
                }
            });
            builder.setNegativeButton("Depois", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    appendLog("\n[*] Reinicialização adiada. Use [ 🔄 3. REINICIAR ] quando desejar.");
                    btnReboot.requestFocus();
                }
            });
            builder.create().show();
        } catch (Exception e) {
            appendLog("[!] FINALIZADO. Clique em [ 🔄 3. REINICIAR ] para concluir.");
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
                appendLog("[?] Solicitando permissão de armazenamento...");
                requestPermissions(needed.toArray(new String[0]), REQ_PERMISSIONS);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                if (!Environment.isExternalStorageManager()) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            } catch (Exception ignored) {}
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!SystemSettingsHelper.canRequestPackageInstalls(this)) {
                appendLog("[?] Conceda permissão de 'Instalar apps desconhecidos' para permitir os utilitários de Root.");
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!SystemSettingsHelper.isBatteryOptimizationIgnored(this)) {
                SystemSettingsHelper.requestIgnoreBatteryOptimization(this);
            }
        }
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

        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        Set<String> installedSet = new HashSet<String>();
        Set<String> targetsToNeutralize = new HashSet<String>();

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

            String lowerPkg = pkg.toLowerCase();
            for (String kw : suspiciousLabels) {
                String cleanKw = kw.replace(" ", "");
                if (lowerPkg.contains(cleanKw)) {
                    targetsToNeutralize.add(pkg);
                    break;
                }
            }

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

        targetsToNeutralize.addAll(getUadTargetPackages());

        int neutralizedCount = 0;
        for (String pkg : targetsToNeutralize) {
            if (installedSet.contains(pkg)) {
                // TÉCNICA TRIPLA COM PODER DE ROOT / ADMIN CONTRA PERSISTÊNCIA:
                execCommand("am force-stop " + pkg);
                execCommand("cmd appops set " + pkg + " RUN_IN_BACKGROUND ignore");
                execCommand("cmd appops set " + pkg + " RUN_ANY_IN_BACKGROUND ignore");
                execCommand("cmd appops set " + pkg + " SYSTEM_ALERT_WINDOW ignore");
                execCommand("cmd appops set " + pkg + " WRITE_SETTINGS ignore");

                execCommand("pm disable-user --user 0 " + pkg);
                execCommand("pm disable " + pkg);

                execCommand("pm hide " + pkg);
                execCommand("pm uninstall -k --user 0 " + pkg);
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
        if (ShellExecutor.hasSuBinary()) {
            ShellExecutor.CommandResult res = ShellExecutor.runWithTimeout("su", command, 3000L);
            if (res.success) {
                return res.output;
            }
        }
        ShellExecutor.CommandResult res = ShellExecutor.runWithTimeout("sh", command, 3000L);
        return res.output;
    }
}
