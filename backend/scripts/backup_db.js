const { exec } = require('child_process');
const path = require('path');
const fs = require('fs');
const dotenv = require('dotenv');

dotenv.config({ path: '../.env' });

const backupDir = path.join(__dirname, '../backups');

if (!fs.existsSync(backupDir)) {
    fs.mkdirSync(backupDir);
}

const timestamp = new Date().toISOString().replace(/:/g, '-').split('.')[0];
const backupPath = path.join(backupDir, `backup-${timestamp}`);

const uri = process.env.MONGO_URI;

// Sensitive: masking URI in logs
console.log(`Starting backup at ${backupPath}...`);

// Validating URI
if (!uri) {
    console.error('MONGO_URI is missing in .env');
    process.exit(1);
}

// Construct command
// Note: This requires 'mongodump' to be in system PATH.
// If using Atlas, passing URI directly usually works with recent tools.
const cmd = `mongodump --uri="${uri}" --out="${backupPath}"`;

exec(cmd, (error, stdout, stderr) => {
    if (error) {
        console.error(`Backup Failed: ${error.message}`);
        console.error(`Stderr: ${stderr}`);
        return;
    }
    console.log(`Backup Successful! Stored in: ${backupPath}`);
});
