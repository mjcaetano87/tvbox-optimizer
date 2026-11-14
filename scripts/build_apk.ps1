[CmdletBinding()]
param(
    [string]$AndroidSdkPath = $env:ANDROID_HOME,
    [string]$JdkPath = $env:JAVA_HOME
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   BUILD SCRIPT - TVBOX OPTIMIZER APK (STANDALONE)        " -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
$projectRoot = Split-Path -Parent $scriptRoot
$appDir = Join-Path $projectRoot "app-source"
$outputApk = Join-Path $projectRoot "TVBOX_Optimizer_IPTV.apk"

# 1. Detect Java JDK
if ([string]::IsNullOrWhiteSpace($JdkPath) -or -not (Test-Path $JdkPath)) {
    $javac = Get-Command "javac" -ErrorAction SilentlyContinue
    if ($javac) {
        $JdkPath = Split-Path -Parent (Split-Path -Parent $javac.Source)
    } else {
        $possibleJdks = @(
            "$env:ProgramFiles\Java\jdk*",
            "$env:ProgramFiles\Microsoft\jdk*",
            "$env:LOCALAPPDATA\Programs\Common\Java\jdk*"
        )
        foreach ($p in $possibleJdks) {
            $found = Get-Item $p -ErrorAction SilentlyContinue | Select-Object -Last 1
            if ($found) { $JdkPath = $found.FullName; break }
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
        "$env:LOCALAPPDATA\Android\Sdk",
        "$env:ProgramFiles\Android\Sdk",
        "$env:ProgramFiles(x86)\Android\android-sdk"
    )
    foreach ($p in $possibleSdks) {
        if (Test-Path $p) { $AndroidSdkPath = $p; break }
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
& "$JdkPath\bin\javac.exe" -encoding UTF-8 -source 1.8 -target 1.8 -bootclasspath $androidJar.FullName -d $binDir $javaSources
if ($LASTEXITCODE -ne 0) { Write-Error "javac compilation failed"; exit 1 }

# Step C: Convert bytecode to classes.dex with D8
Write-Host "[*] [3/5] Converting bytecode to classes.dex (D8)..." -ForegroundColor Cyan
$classFiles = Get-ChildItem -Path "$binDir" -Recurse -Filter "*.class" | ForEach-Object { $_.FullName }
& "$JdkPath\bin\java.exe" -cp $d8jar.FullName com.android.tools.r8.D8 --output $binDir $classFiles
if ($LASTEXITCODE -ne 0) { Write-Error "D8 dexer failed"; exit 1 }

# Step D: Package APK & add classes.dex
Write-Host "[*] [4/5] Packaging APK resource archive..." -ForegroundColor Cyan
$unalignedApk = Join-Path $binDir "unaligned.apk"
$alignedApk = Join-Path $binDir "aligned.apk"
& $aapt.FullName package -f -M "$appDir\AndroidManifest.xml" -S "$appDir\res" -I $androidJar.FullName -F $unalignedApk
Push-Location $binDir
& $aapt.FullName add "unaligned.apk" "classes.dex" | Out-Null
Pop-Location
& $zipalign.FullName -f 4 $unalignedApk $alignedApk
if ($LASTEXITCODE -ne 0) { Write-Error "zipalign failed"; exit 1 }

# Step E: Sign APK with keystore
Write-Host "[*] [5/5] Signing APK with debug keystore..." -ForegroundColor Cyan
$keystore = Join-Path $binDir "debug.keystore"
if (-not (Test-Path $keystore)) {
    & "$JdkPath\bin\keytool.exe" -genkey -v -keystore $keystore -alias tvbox -keyalg RSA -keysize 2048 -validity 10000 -storepass android -keypass android -dname "CN=TVBox, OU=Optimizer, O=IPTV, C=US" | Out-Null
}
& $apksigner.FullName sign --ks $keystore --ks-pass pass:android --key-pass pass:android --out $outputApk $alignedApk
& $apksigner.FullName verify $outputApk

if ($LASTEXITCODE -eq 0 -and (Test-Path $outputApk)) {
    Write-Host "`n==========================================================" -ForegroundColor Cyan
    Write-Host "   BUILD SUCCESSFUL!                                      " -ForegroundColor Green
    Write-Host "   APK Output: $outputApk                                 " -ForegroundColor Green
    Write-Host "==========================================================" -ForegroundColor Cyan
} else {
    Write-Error "APK signing verification failed!"
    exit 1
}
