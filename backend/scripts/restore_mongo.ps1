# MongoDB Restore Script
# Usage: .\restore_mongo.ps1 -BackupFolder "C:\ai_franchise\backups\backup_2026_01_01_00_00_00"
# Set MONGO_URI in your environment or update it below

param(
    [Parameter(Mandatory=$true)]
    [string]$BackupFolder
)

$ErrorActionPreference = "Stop"

$MONGO_URI = $env:MONGO_URI
if (-not $MONGO_URI) {
    Write-Host "MONGO_URI environment variable is not set. Trying default localhost..."
    $MONGO_URI = "mongodb://localhost:27017/ai_franchise"
}

if (-not (Test-Path $BackupFolder)) {
    Write-Error "Backup folder not found: $BackupFolder"
    exit 1
}

Write-Host "Starting MongoDB restore from $BackupFolder..."

# Run mongorestore
# Note: assumes mongorestore is in the system PATH
try {
    mongorestore --uri="$MONGO_URI" --drop "$BackupFolder"
    Write-Host "Restore completed successfully."
} catch {
    Write-Error "Restore failed: $_"
}
