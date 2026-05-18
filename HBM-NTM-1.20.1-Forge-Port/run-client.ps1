$ErrorActionPreference = "Stop"

Set-Location -LiteralPath $PSScriptRoot

$env:JAVA_OPTS = "-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7890 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890 -Dnet.minecraftforge.gradle.check.certs=false"

Write-Host "Starting HBM NTM Forge 1.20.1 client..."
Write-Host "Project: $PWD"
Write-Host "Proxy: 127.0.0.1:7890"
Write-Host ""

.\gradlew.bat runClient --no-daemon
