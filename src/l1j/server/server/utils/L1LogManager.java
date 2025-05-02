package l1j.server.server.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class L1LogManager {
    private static Logger _log = LoggerFactory.getLogger(L1LogManager.class.getName());
    private static L1LogManager _instance;
    private static final String LOG_DIR = "log";
    private static final String ARCHIVE_DIR = "log/archive";
    
    private L1LogManager() {
        // Create necessary directories
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
            Files.createDirectories(Paths.get(ARCHIVE_DIR));
        } catch (IOException e) {
            _log.error("Failed to create log directories", e);
        }
    }
    
    public static L1LogManager getInstance() {
        if (_instance == null) {
            _instance = new L1LogManager();
        }
        return _instance;
    }
    
    public void collectLogsOnShutdown() {
        try {
            _log.info("Collecting logs for archival...");
            
            // Force flush any pending log messages
            LogManager.getLogManager().reset();
            
            // Create timestamp for archive folder
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            Path archivePath = Paths.get(ARCHIVE_DIR, "shutdown_" + timestamp);
            Files.createDirectories(archivePath);
            
            // Move current log files to archive
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(LOG_DIR))) {
                for (Path path : stream) {
                    if (Files.isRegularFile(path)) {
                        String fileName = path.getFileName().toString();
                        // Skip archive directory and lock files
                        if (!fileName.startsWith("archive") && !fileName.endsWith(".lck")) {
                            Path targetPath = archivePath.resolve(fileName);
                            try {
                                Files.move(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
                                _log.info("Archived log file: " + fileName);
                            } catch (IOException e) {
                                // If we can't move (file in use), try to copy instead
                                Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
                                _log.info("Copied log file: " + fileName);
                            }
                        }
                    }
                }
            }
            
            // Cleanup old archives (keep last 7 days)
            cleanupOldArchives(7);
            
            _log.info("Log collection complete. Archived to: " + archivePath);
            
        } catch (Exception e) {
            _log.error("Error collecting logs during shutdown", e);
        }
    }
    
    private void cleanupOldArchives(int daysToKeep) {
        try {
            Path archiveDir = Paths.get(ARCHIVE_DIR);
            if (!Files.exists(archiveDir)) {
                return;
            }
            
            long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L);
            
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(archiveDir)) {
                for (Path path : stream) {
                    if (Files.isDirectory(path) && 
                        Files.getLastModifiedTime(path).toMillis() < cutoffTime) {
                        deleteDirectoryRecursively(path);
                        _log.info("Deleted old log archive: " + path.getFileName());
                    }
                }
            }
        } catch (IOException e) {
            _log.error("Error cleaning up old log archives", e);
        }
    }

    private void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path entry : stream) {
                    deleteDirectoryRecursively(entry);
                }
            }
        }
        Files.delete(path);
    }
} 