package l1j.server.server.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.Config;
import l1j.server.server.GeneralThreadPool;

public class DBBackupManager {
    private static Logger _log = LoggerFactory.getLogger(DBBackupManager.class);
    private static DBBackupManager _instance;
    private static final String BACKUP_DIR = "backups";
    private static final int BACKUP_INTERVAL = 6 * 60 * 60 * 1000; // 6 hours in milliseconds
    private static final int BACKUP_RETENTION_DAYS = 7;
    private static final int MAX_BACKUPS = 10;

    private DBBackupManager() {
        initialize();
    }

    public static DBBackupManager getInstance() {
        if (_instance == null) {
            _instance = new DBBackupManager();
        }
        return _instance;
    }

    private void initialize() {
        // Create backup directory if it doesn't exist
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        // Perform initial backup
        _log.info("Performing initial database backup on server startup...");
        try {
            // performBackup(); // Disabled internal backup
        } catch (Exception e) {
            _log.error("Failed to perform initial backup", e);
        }

        // Schedule regular backups - Disabled
        /*
        GeneralThreadPool.getInstance().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    performBackup();
                } catch (Exception e) {
                    _log.error("Error during scheduled backup", e);
                }
            }
        }, BACKUP_INTERVAL, BACKUP_INTERVAL);
        */

        _log.info("Internal Database backup manager disabled. Backups will NOT run automatically.");
    }

    public void performBackup() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupFile = BACKUP_DIR + "/l1jdb_test_backup_" + timestamp + ".sql";

        try {
            _log.info("Starting database backup...");
            
            // Create backup command
            ProcessBuilder pb = new ProcessBuilder(
                "mysqldump",
                "-u", Config.DB_LOGIN,
                Config.DB_PASSWORD.isEmpty() ? "" : "-p" + Config.DB_PASSWORD,
                "l1jdb_test"
            );

            // Redirect output to backup file
            pb.redirectOutput(new File(backupFile));
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            // Execute backup
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                _log.info("Database backup completed successfully: " + backupFile);
                
                // Compress the backup
                _log.info("Compressing backup file...");
                ProcessBuilder gzipPb = new ProcessBuilder("gzip", backupFile);
                gzipPb.redirectError(ProcessBuilder.Redirect.INHERIT);
                Process gzipProcess = gzipPb.start();
                gzipProcess.waitFor();
                
                cleanupOldBackups();
            } else {
                _log.error("Database backup failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            _log.error("Error during backup", e);
        }
    }

    private void cleanupOldBackups() {
        File backupDir = new File(BACKUP_DIR);
        File[] backupFiles = backupDir.listFiles((dir, name) -> name.endsWith(".sql.gz"));
        
        if (backupFiles == null) return;

        // Delete files older than BACKUP_RETENTION_DAYS
        long cutoffTime = System.currentTimeMillis() - (BACKUP_RETENTION_DAYS * 24 * 60 * 60 * 1000L);
        
        for (File file : backupFiles) {
            if (file.lastModified() < cutoffTime) {
                if (file.delete()) {
                    _log.info("Deleted old backup: " + file.getName());
                }
            }
        }

        // If we still have more than MAX_BACKUPS, delete the oldest ones
        backupFiles = backupDir.listFiles((dir, name) -> name.endsWith(".sql.gz"));
        if (backupFiles != null && backupFiles.length > MAX_BACKUPS) {
            // Sort files by last modified time
            java.util.Arrays.sort(backupFiles, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
            
            // Delete oldest files until we reach MAX_BACKUPS
            for (int i = 0; i < backupFiles.length - MAX_BACKUPS; i++) {
                if (backupFiles[i].delete()) {
                    _log.info("Deleted excess backup: " + backupFiles[i].getName());
                }
            }
        }
    }
} 