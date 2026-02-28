# MongoDB Backup Script
# Usage: .\backup_mongo.ps1
# Set MONGO_URI in your environment or update it below

$ErrorActionPreference = "Stop"

$MONGO_URI = $env:MONGO_URI
if (-not $MONGO_URI) {
    Write-Host "MONGO_URI environment variable is not set. Trying default localhost..."
    $MONGO_URI = "mongodb://localhost:27017/ai_franchise"
}

$BACKUP_DIR = "C:\ai_franchise\backups\"
$DATE_STRING = (Get-Date).ToString("yyyy_MM_dd_HH_mm_ss")
$BACKUP_PATH = Join-Path -Path $BACKUP_DIR -ChildPath "backup_$DATE_STRING"

Write-Host "Starting MongoDB backup to $BACKUP_PATH..."

# Create backup directory if it doesn't exist
if (-not (Test-Path $BACKUP_DIR)) {
    New-Item -ItemType Directory -Force -Path $BACKUP_DIR | Out-Null
}

# Run mongodump
# Note: assumes mongodump is in the system PATH
try {
    mongodump --uri="$MONGO_URI" --out="$BACKUP_PATH"
    Write-Host "Backup completed successfully at $BACKUP_PATH"
} catch {
    Write-Error "Backup failed: $_"
}
