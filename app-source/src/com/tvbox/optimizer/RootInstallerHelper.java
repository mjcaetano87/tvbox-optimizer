package com.tvbox.optimizer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class RootInstallerHelper {

    public static final String KINGO_PACKAGE = "com.kingo.root";
    public static final String IROOT_PACKAGE = "com.mgyun.shua.su";

    public static final String KINGO_ASSET = "root_bundle/kingoroot.apk";
    public static final String IROOT_ASSET = "root_bundle/iroot.apk";

    public interface LogCallback {
        void log(String message);
    }

    public static boolean isDeviceRooted() {
        if (ShellExecutor.hasSuBinary()) {
            ShellExecutor.CommandResult res = ShellExecutor.runWithTimeout("su", "id", 1500L);
            if (res.success && res.output.contains("uid=0")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isKingoInstalled(Context context) {
        return SystemSettingsHelper.isPackageInstalled(context, KINGO_PACKAGE);
    }

    public static boolean isIRootInstalled(Context context) {
        return SystemSettingsHelper.isPackageInstalled(context, IROOT_PACKAGE);
    }

    public static boolean installRootApp(Context context, String assetPath, String appLabel, LogCallback callback) {
        if (callback != null) callback.log("[RUN] Preparando instalador de Root: " + appLabel + "...");

        try {
            File targetDir = new File(context.getCacheDir(), "root_apks");
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            String fileName = assetPath.substring(assetPath.lastIndexOf('/') + 1);
            File targetFile = new File(targetDir, fileName);

            InputStream input = context.getAssets().open(assetPath);
            FileOutputStream output = new FileOutputStream(targetFile);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
            output.close();
            input.close();

            if (callback != null) {
                callback.log("[OK] " + appLabel + " extraído (" + (targetFile.length() / 1024) + " KB).");
            }

            // 1. Se já houver su, tenta instalação silenciosa em segundo plano
            if (ShellExecutor.hasSuBinary()) {
                if (callback != null) callback.log("[RUN] Tentando instalação silenciosa via Root...");
                ShellExecutor.CommandResult suInstall = ShellExecutor.runWithTimeout(
                    "su",
                    "pm install -r -d \"" + targetFile.getAbsolutePath() + "\"",
                    20000L
                );
                if (suInstall.success || suInstall.output.toLowerCase().contains("success")) {
                    if (callback != null) callback.log("[OK] " + appLabel + " instalado com sucesso via Root!");
                    return true;
                }
            }

            // 2. Fallback com StrictMode desabilitado para suportar file:// em qualquer versão do Android
            try {
                Method disableDeathOnFileUriExposure = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                disableDeathOnFileUriExposure.invoke(null);
            } catch (Throwable ignored) {
                try {
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                } catch (Throwable ignored2) {}
            }

            Uri fileUri = Uri.fromFile(targetFile);
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setDataAndType(fileUri, "application/vnd.android.package-archive");
            viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent installIntent = new Intent("android.intent.action.INSTALL_PACKAGE");
            installIntent.setData(fileUri);
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            List<Intent> intents = Arrays.asList(viewIntent, installIntent);
            boolean opened = SystemSettingsHelper.openActivitySafely(context, intents, null);
            if (opened) {
                if (callback != null) {
                    callback.log("[OK] Tela de instalação do " + appLabel + " iniciada. Confirme no controle!");
                }
                return true;
            } else {
                if (callback != null) {
                    callback.log("[WARN] Não foi possível disparar o instalador padrão do Android.");
                }
                return false;
            }
        } catch (Throwable e) {
            if (callback != null) {
                callback.log("[ERRO] Falha ao instalar " + appLabel + ": " + e.getMessage());
            }
            return false;
        }
    }

    public static boolean openRootApp(Context context, String packageName, String appLabel, LogCallback callback) {
        try {
            boolean opened = SystemSettingsHelper.openTargetApp(context, packageName);
            if (opened) {
                if (callback != null) callback.log("[OK] Abrindo " + appLabel + " (" + packageName + ")...");
            } else {
                if (callback != null) callback.log("[WARN] Não foi possível abrir " + appLabel + " (ainda não instalado).");
            }
            return opened;
        } catch (Throwable e) {
            if (callback != null) callback.log("[ERRO] Falha ao abrir " + appLabel + ": " + e.getMessage());
            return false;
        }
    }
}
