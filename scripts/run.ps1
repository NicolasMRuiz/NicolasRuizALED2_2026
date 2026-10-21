$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not (Test-Path 'target/testdesk-1.0.0.jar')) { throw 'Primero ejecute scripts/build.ps1 o mvn clean package.' }
$portableJdk = Get-ChildItem '.tools/jdk21' -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
if ($portableJdk) {
    $javaCommand = Join-Path $portableJdk.FullName 'bin/java.exe'
} elseif ($env:JAVA_HOME) {
    $javaCommand = Join-Path $env:JAVA_HOME 'bin/java.exe'
} else {
    $javaCommand = (Get-Command java -ErrorAction Stop).Source
}
& $javaCommand -jar target/testdesk-1.0.0.jar
exit $LASTEXITCODE
