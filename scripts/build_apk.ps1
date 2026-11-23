[CmdletBinding()]
param(
    [string]$AndroidSdkPath = "",
    [string]$JdkPath = ""
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   BUILD SCRIPT - TVBOX OPTIMIZER APK (v3.1-COMPAT)       " -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
$projectRoot = Split-Path -Parent $scriptRoot
$appDir = Join-Path $projectRoot "app-source"
$outputApk = Join-Path $projectRoot "TVBOX_Optimizer_IPTV.apk"

# 1. Detect Java JDK (Prefer JDK 17 for D8 compatibility)
if ([string]::IsNullOrWhiteSpace($JdkPath) -or -not (Test-Path $JdkPath)) {
    $possibleJdks = @(
        "C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot",
        "C:\Users\xeda\.gemini\antigravity\scratch\jdk-17",
        "$env:JAVA_HOME",
        "$env:ProgramFiles\Java\jdk*",
        "$env:ProgramFiles\Microsoft\jdk*"
    )
    foreach ($p in $possibleJdks) {
        if (-not [string]::IsNullOrWhiteSpace($p)) {
            $found = Get-Item $p -ErrorAction SilentlyContinue | Select-Object -Last 1
            if ($found -and (Test-Path "$($found.FullName)\bin\javac.exe")) {
                $JdkPath = $found.FullName
                break
            }
        }
    }
}

if (-not (Test-Path "$JdkPath\bin\javac.exe")) {
    Write-Error "JDK not found! Please specify -JdkPath or set JAVA_HOME."
    exit 1
}

# 2. Detect Android SDK
if ([string]::IsNullOrWhiteSpace($AndroidSdkPath) -or -not (Test-Path $AndroidSdkPath)) {
    $possibleSdks = @(
        "C:\Users\xeda\.gemini\antigravity\scratch\android-sdk",
        "$env:ANDROID_HOME",
        "$env:LOCALAPPDATA\Android\Sdk",
        "$env:ProgramFiles\Android\Sdk"
    )
    foreach ($p in $possibleSdks) {
        if (-not [string]::IsNullOrWhiteSpace($p) -and (Test-Path $p)) {
            $AndroidSdkPath = $p
            break
        }
    }
}

$androidJar = Get-ChildItem -Path "$AndroidSdkPath\platforms" -Recurse -Filter "android.jar" -ErrorAction SilentlyContinue | Select-Object -Last 1
$aapt = Get-ChildItem -Path "$AndroidSdkPath\build-tools" -Recurse -Filter "aapt.exe" -ErrorAction SilentlyContinue | Select-Object -Last 1
$zipalign = Get-ChildItem -Path "$AndroidSdkPath\build-tools" -Recurse -Filter "zipalign.exe" -ErrorAction SilentlyContinue | Select-Object -Last 1
$apksigner = Get-ChildItem -Path "$AndroidSdkPath\build-tools" -Recurse -Filter "apksigner.bat" -ErrorAction SilentlyContinue | Select-Object -Last 1
$d8jar = Get-ChildItem -Path "$AndroidSdkPath\build-tools" -Recurse -Filter "d8.jar" -ErrorAction SilentlyContinue | Select-Object -Last 1

if (-not $androidJar -or -not $aapt -or -not $zipalign -or -not $apksigner) {
    Write-Error "Android build tools or platforms not found in $AndroidSdkPath!"
    exit 1
}

Write-Host "[+] Using JDK: $JdkPath" -ForegroundColor Green
Write-Host "[+] Using Android SDK: $AndroidSdkPath" -ForegroundColor Green
Write-Host "[+] Using android.jar: $($androidJar.FullName)" -ForegroundColor Green

# Prepare build dirs
$binDir = Join-Path $appDir "bin"
$genDir = Join-Path $appDir "gen"
New-Item -ItemType Directory -Path $binDir, $genDir -Force | Out-Null
Get-ChildItem -Path $binDir -Recurse | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue

# Step A: aapt generate R.java
Write-Host "[*] [1/5] Generating R.java..." -ForegroundColor Cyan
& $aapt.FullName package -f -m -J $genDir -M "$appDir\AndroidManifest.xml" -S "$appDir\res" -I $androidJar.FullName
if ($LASTEXITCODE -ne 0) { Write-Error "aapt package failed"; exit 1 }

# Step B: Compile Java source files
Write-Host "[*] [2/5] Compiling Java classes..." -ForegroundColor Cyan
$javaSources = @(
    (Get-ChildItem -Path "$genDir" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }),
    (Get-ChildItem -Path "$appDir\src" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName })
)
$sourcesFile = Join-Path $binDir "sources.txt"
$javaSources | Out-File -FilePath $sourcesFile -Encoding ascii

& "$JdkPath\bin\javac.exe" -encoding UTF-8 -source 1.8 -target 1.8 -bootclasspath $androidJar.FullName -d $binDir "@$sourcesFile"
if ($LASTEXITCODE -ne 0) { Write-Error "javac compilation failed"; exit 1 }

# Step C: Convert bytecode to classes.dex with D8
Write-Host "[*] [3/5] Converting bytecode to classes.dex (D8)..." -ForegroundColor Cyan
$classFiles = Get-ChildItem -Path "$binDir" -Recurse -Filter "*.class" | ForEach-Object { $_.FullName }
& "$JdkPath\bin\java.exe" -cp $d8jar.FullName com.android.tools.r8.D8 --output $binDir @classFiles
if ($LASTEXITCODE -ne 0) { Write-Error "D8 dexer failed"; exit 1 }

# Step D: Package APK & add classes.dex and assets (root_bundle)
Write-Host "[*] [4/5] Packaging APK resource and asset archive..." -ForegroundColor Cyan
$unalignedApk = Join-Path $binDir "unaligned.apk"
$alignedApk = Join-Path $binDir "aligned.apk"
$assetsDir = Join-Path $appDir "assets"

$aaptArgs = @("package", "-f", "-M", "$appDir\AndroidManifest.xml", "-S", "$appDir\res", "-I", $androidJar.FullName)
if (Test-Path $assetsDir) {
    $aaptArgs += @("-A", $assetsDir)
}
$aaptArgs += @("-F", $unalignedApk)

& $aapt.FullName @aaptArgs
if ($LASTEXITCODE -ne 0) { Write-Error "aapt package unaligned failed"; exit 1 }

Push-Location $binDir
& $aapt.FullName add "unaligned.apk" "classes.dex" | Out-Null
Pop-Location

& $zipalign.FullName -f 4 $unalignedApk $alignedApk
if ($LASTEXITCODE -ne 0) { Write-Error "zipalign failed"; exit 1 }

# Step E: Sign APK with keystore with full v1/v2/v3 support for Android 4.4+
Write-Host "[*] [5/5] Signing APK with debug keystore (v1 + v2 + v3 enabled)..." -ForegroundColor Cyan
$keystore = Join-Path $binDir "debug.keystore"
if (-not (Test-Path $keystore)) {
    & "$JdkPath\bin\keytool.exe" -genkey -v -keystore $keystore -alias tvbox -keyalg RSA -keysize 2048 -validity 10000 -storepass android -keypass android -dname "CN=TVBox, OU=Optimizer, O=IPTV, C=US" | Out-Null
}
& $apksigner.FullName sign --ks $keystore --ks-pass pass:android --key-pass pass:android --min-sdk-version 19 --v1-signing-enabled true --v2-signing-enabled true --v3-signing-enabled true --out $outputApk $alignedApk
& $apksigner.FullName verify --verbose $outputApk

if ($LASTEXITCODE -eq 0 -and (Test-Path $outputApk)) {
    $apkItem = Get-Item $outputApk
    $sizeMb = [math]::Round($apkItem.Length / 1MB, 2)
    Write-Host "`n==========================================================" -ForegroundColor Cyan
    Write-Host "   BUILD SUCCESSFUL! (v3.1-COMPAT Standalone)             " -ForegroundColor Green
    Write-Host "   APK Output: $outputApk ($sizeMb MB)                   " -ForegroundColor Green
    Write-Host "==========================================================" -ForegroundColor Cyan
} else {
    Write-Error "APK signing verification failed!"
    exit 1
}
