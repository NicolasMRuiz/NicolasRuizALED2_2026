$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$portableJdk = Get-ChildItem '.tools/jdk21' -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
if ($portableJdk) { $env:JAVA_HOME = $portableJdk.FullName }
$mavenCommand = Get-Command mvn.cmd -ErrorAction SilentlyContinue
if ($mavenCommand) {
    & $mavenCommand.Source -B -ntp clean package
} elseif (Test-Path '.tools/apache-maven-3.9.9/bin/mvn.cmd') {
    & '.tools/apache-maven-3.9.9/bin/mvn.cmd' -B -ntp '-Dmaven.repo.local=.tools/m2' clean package
} else {
    throw 'Instale Maven 3.9 y JDK 21, configure PATH/JAVA_HOME y vuelva a ejecutar.'
}
exit $LASTEXITCODE
