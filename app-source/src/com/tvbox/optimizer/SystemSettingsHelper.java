package com.tvbox.optimizer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class SystemSettingsHelper {

    public static boolean isPackageInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean canRequestPackageInstalls(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                return context.getPackageManager().canRequestPackageInstalls();
            } catch (Throwable ignored) {
                return false;
            }
        }
        return true;
    }

    public static boolean isOverlayGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                return Settings.canDrawOverlays(context);
            } catch (Throwable ignored) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSystemWriteGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                return Settings.System.canWrite(context);
            } catch (Throwable ignored) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBatteryOptimizationIgnored(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (pm != null) {
                    return pm.isIgnoringBatteryOptimizations(context.getPackageName());
                }
            } catch (Throwable ignored) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasAllAdminPermissions(Context context) {
        return canRequestPackageInstalls(context) &&
               isBatteryOptimizationIgnored(context) &&
               isSystemWriteGranted(context);
    }

    public static boolean openActivitySafely(Context context, List<Intent> intents, String fallbackToast) {
        for (Intent intent : intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            } catch (Throwable ignored) {}
        }
        if (fallbackToast != null && !fallbackToast.isEmpty()) {
            try {
                Toast.makeText(context, fallbackToast, Toast.LENGTH_SHORT).show();
            } catch (Throwable ignored) {}
        }
        return false;
    }

    public static boolean requestUnknownSourcesPermission(Context context) {
        List<Intent> intents = new ArrayList<Intent>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intentWithData = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intentWithData.setData(Uri.parse("package:" + context.getPackageName()));
            intents.add(intentWithData);
            intents.add(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));
        }
        intents.add(new Intent(Settings.ACTION_SECURITY_SETTINGS));
        intents.add(new Intent(Settings.ACTION_SETTINGS));
        return openActivitySafely(context, intents, "Ative 'Fontes desconhecidas' nas Configurações da TV Box.");
    }

    public static boolean requestIgnoreBatteryOptimization(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;

        List<Intent> intents = new ArrayList<Intent>();
        Intent direct = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        direct.setData(Uri.parse("package:" + context.getPackageName()));
        intents.add(direct);
        intents.add(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
        intents.add(new Intent(Settings.ACTION_SETTINGS));

        return openActivitySafely(context, intents, "Ajuste de bateria indisponível nesta TV Box.");
    }

    public static boolean requestWriteSettingsPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;

        List<Intent> intents = new ArrayList<Intent>();
        Intent direct = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        direct.setData(Uri.parse("package:" + context.getPackageName()));
        intents.add(direct);
        intents.add(new Intent(Settings.ACTION_SETTINGS));

        return openActivitySafely(context, intents, "Permita modificar configurações do sistema.");
    }

    public static boolean openTargetApp(Context context, String packageName) {
        try {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
                return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }
}
