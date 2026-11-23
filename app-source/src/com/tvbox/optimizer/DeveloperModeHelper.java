package com.tvbox.optimizer;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.provider.Settings;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeveloperModeHelper {

    public interface LogCallback {
        void log(String message);
    }

    public static boolean isDeveloperOptionsEnabled(Context context) {
        try {
            return Settings.Global.getInt(context.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isAdbEnabled(Context context) {
        try {
            return Settings.Global.getInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, 0) == 1;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void openDeveloperSettings(Context context) {
        List<Intent> intents = Arrays.asList(
            new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
            new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS),
            new Intent(Settings.ACTION_SETTINGS)
        );
        SystemSettingsHelper.openActivitySafely(
            context,
            intents,
            "Abra Configurações -> Opções do Desenvolvedor manualmente."
        );
    }

    public static boolean configureAdminPersistence(Context context, LogCallback callback) {
        String pkg = context.getPackageName();
        if (callback != null) callback.log("[RUN] Forçando Modo Administrador e privilégios de sistema...");

        List<String> commands = Arrays.asList(
            "pm grant " + pkg + " android.permission.WRITE_SECURE_SETTINGS",
            "dumpsys deviceidle whitelist +" + pkg,
            "cmd appops set " + pkg + " SYSTEM_ALERT_WINDOW allow",
            "cmd appops set " + pkg + " WRITE_SETTINGS allow"
        );

        boolean shellSuccess = ShellExecutor.runPrivilegedOrShell(commands, true, 2000L);
        if (shellSuccess) {
            if (callback != null) callback.log("[OK] Diretivas administrativas aplicadas (WRITE_SECURE_SETTINGS / Whitelist).");
        } else {
            if (callback != null) callback.log("[INFO] Comandos enviados ao subsistema Android.");
        }

        // Teste de conexão local via socket ADB (127.0.0.1:5555)
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", 5555), 1000);
            if (socket.isConnected()) {
                socket.close();
                if (callback != null) callback.log("[OK] Daemon ADB Local (127.0.0.1:5555) detectado e online.");
            }
        } catch (Throwable ignored) {
            if (callback != null) callback.log("[INFO] ADB loopback local em standby.");
        }

        return shellSuccess;
    }

    public static boolean rebootDevice(Context context, LogCallback callback) {
        if (callback != null) callback.log("[SYS] Solicitando reinicialização privilegiada...");

        List<String> rebootCmds = Arrays.asList(
            "svc power reboot",
            "cmd power reboot",
            "reboot"
        );

        boolean useSu = ShellExecutor.hasSuBinary();
        for (String cmd : rebootCmds) {
            if (useSu) {
                ShellExecutor.CommandResult rootRes = ShellExecutor.runWithTimeout("su", cmd, 1500L);
                if (rootRes.success) return true;
            }
            ShellExecutor.CommandResult shRes = ShellExecutor.runWithTimeout("sh", cmd, 1500L);
            if (shRes.success) return true;
        }

        // Fallback via PowerManager
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                pm.reboot(null);
                return true;
            }
        } catch (Throwable e) {
            if (callback != null) callback.log("[WARN] Reinicialização por hardware restrita: " + e.getMessage());
        }

        return false;
    }
}
