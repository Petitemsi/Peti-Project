# Setup Java Environment Variables for PETI Project
# Run this script as Administrator to set permanent environment variables

Write-Host "Setting up Java environment variables..." -ForegroundColor Green

# Set JAVA_HOME
$javaHome = "C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"
[Environment]::SetEnvironmentVariable("JAVA_HOME", $javaHome, "Machine")

# Add Java to PATH
$currentPath = [Environment]::GetEnvironmentVariable("PATH", "Machine")
if ($currentPath -notlike "*$javaHome\bin*") {
    $newPath = "$javaHome\bin;$currentPath"
    [Environment]::SetEnvironmentVariable("PATH", $newPath, "Machine")
}

Write-Host "JAVA_HOME set to: $javaHome" -ForegroundColor Yellow
Write-Host "Java added to PATH" -ForegroundColor Yellow
Write-Host "Environment variables set successfully!" -ForegroundColor Green
Write-Host "Please restart your terminal/PowerShell for changes to take effect." -ForegroundColor Cyan 