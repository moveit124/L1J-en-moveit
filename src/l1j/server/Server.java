/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l1j.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import l1j.server.server.GameServer;
import l1j.server.ssh.SSHServer;
import l1j.server.telnet.TelnetServer;

/**
 * l1j-En
 */
public class Server {
	private static Logger _log = LoggerFactory.getLogger(Server.class.getName());
	private static final String LOG_PROP = "./config/log.properties";
	private static final String TEST_LOG_PROP = "./dev/config/log.properties";
	private static boolean isTestServer = false;

	public static void main(final String[] args) throws Exception {
		// Check if we're running in test mode
		if (args.length > 0 && "test".equalsIgnoreCase(args[0])) {
			isTestServer = true;
			_log.info("Starting in TEST SERVER mode");
		}

		File logFolder = new File("log");
		logFolder.mkdir();
		
		// Create test server log folder if needed
		if (isTestServer) {
			File testLogFolder = new File("log/test_server");
			testLogFolder.mkdir();
		}

		// Clean up old log files
		cleanupOldLogFiles(3);

		try {
			String logPropPath = isTestServer ? TEST_LOG_PROP : LOG_PROP;
			InputStream is = new BufferedInputStream(new FileInputStream(logPropPath));
			LogManager.getLogManager().readConfiguration(is);
			is.close();
			_log.info("Loaded logging configuration from: " + logPropPath);
		} catch (IOException e) {
			_log.error("Failed to load logging properties file.", e);
			System.exit(0);
		}
		InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

		try {
			// Load configuration based on server type
			String serverConfigPath = System.getProperty("server.config.path");
			String ratesConfigPath = System.getProperty("rates.config.path");
			
			if (isTestServer) {
				if (serverConfigPath == null) {
					serverConfigPath = "./dev/config/server.properties";
				}
				if (ratesConfigPath == null) {
					ratesConfigPath = "./dev/config/rates.properties";
				}
				_log.info("Loading test server configuration from: " + serverConfigPath);
				_log.info("Loading test server rates from: " + ratesConfigPath);
			}
			
			Config.load(serverConfigPath, ratesConfigPath);
		} catch (Exception e) {
			_log.error(e.getLocalizedMessage(), e);
			System.exit(0);
		}

		// L1DatabaseFactory
		L1DatabaseFactory.setDatabaseSettings(Config.DB_DRIVER, Config.DB_URL,
				Config.DB_LOGIN, Config.DB_PASSWORD);
		L1DatabaseFactory.getInstance();

		// Initialize GameServer
		String serverType = isTestServer ? "TEST" : "PRODUCTION";
		_log.info("Initializing " + serverType + " GameServer...");
		GameServer.getInstance().initialize();
		
		// Telnet Server
		if (Config.TELNET_SERVER) {
			TelnetServer.getInstance().start();
			_log.info("Telnet server initialized on port " + Config.TELNET_SERVER_PORT);
		} else {
			_log.info("Telnet server is currently disabled.");
		}
		
		if(Config.SSH_SERVER) {
			SSHServer.getInstance().start();
			_log.info("SSH server initialized on port " + Config.SSH_PORT +
					" with " + Config.SSH_ALLOWED_USERNAMES.length + " users.");
		}
		
		if (isTestServer) {
			_log.info("TEST SERVER is running on port " + Config.GAME_SERVER_PORT);
		} else {
			_log.info("PRODUCTION SERVER is running on port " + Config.GAME_SERVER_PORT);
		}
	}
	
	/**
	 * Deletes log files that are older than the specified number of days
	 * @param days number of days, files older than this will be deleted
	 */
	private static void cleanupOldLogFiles(int days) {
		try {
			_log.info("Cleaning up log files older than " + days + " days...");
			Path logDir = Paths.get("log");
			Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
			int deletedCount = 0;
			
			Files.walk(logDir)
				.filter(Files::isRegularFile)
				.filter(path -> {
					try {
						BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
						return attrs.creationTime().toInstant().isBefore(cutoff);
					} catch (IOException e) {
						return false;
					}
				})
				.forEach(path -> {
					try {
						String fileName = path.getFileName().toString();
						// Skip lock files
						if (!fileName.endsWith(".lck")) {
							Files.delete(path);
							// Use local variable for logging since we're in a lambda
							_log.info("Deleted old log file: " + path.getFileName());
						}
					} catch (IOException e) {
						_log.warn("Failed to delete old log file: " + path.toString(), e);
					}
				});
				
			_log.info("Log cleanup complete. Removed " + deletedCount + " old log files.");
		} catch (IOException e) {
			_log.error("Error while cleaning up old log files", e);
		}
	}
	
	/**
	 * Returns whether the server is running in test mode
	 * @return true if this is a test server, false otherwise
	 */
	public static boolean isTestServer() {
		return isTestServer;
	}
}