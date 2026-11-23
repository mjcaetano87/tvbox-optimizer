package com.tvbox.optimizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ShellExecutor {

    private static final List<String> SU_PATHS = Arrays.asList(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/su/bin/su",
        "/vendor/bin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su"
    );

    public static boolean hasSuBinary() {
        for (String path : SU_PATHS) {
            try {
                if (new File(path).exists()) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }
        return false;
    }

    public static CommandResult runWithTimeout(String binary, String command, long timeoutMs) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{binary, "-c", command});
            
            // Suporte a timeout compatível com Android API 21+
            boolean finished = false;
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                try {
                    process.exitValue();
                    finished = true;
                    break;
                } catch (IllegalThreadStateException e) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {}
                }
            }

            if (!finished) {
                process.destroy();
                return new CommandResult(false, -1, "Timeout");
            }

            StringBuilder out = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    out.append(line).append("\n");
                }
                reader.close();
            } catch (Throwable ignored) {}

            int exitCode = process.exitValue();
            return new CommandResult(exitCode == 0, exitCode, out.toString().trim());
        } catch (Throwable e) {
            if (process != null) {
                try { process.destroy(); } catch (Throwable ignored) {}
            }
            return new CommandResult(false, -1, e.getMessage() != null ? e.getMessage() : "");
        }
    }

    public static boolean runPrivilegedOrShell(List<String> commands, boolean tryRootFirst, long timeoutMs) {
        boolean anySuccess = false;
        boolean useSu = tryRootFirst && hasSuBinary();
        for (String cmd : commands) {
            if (useSu) {
                CommandResult res = runWithTimeout("su", cmd, timeoutMs);
                if (res.success) {
                    anySuccess = true;
                    continue;
                }
            }
            CommandResult res = runWithTimeout("sh", cmd, timeoutMs);
            if (res.success) {
                anySuccess = true;
            }
        }
        return anySuccess;
    }

    public static class CommandResult {
        public final boolean success;
        public final int exitCode;
        public final String output;

        public CommandResult(boolean success, int exitCode, String output) {
            this.success = success;
            this.exitCode = exitCode;
            this.output = output;
        }
    }
}
