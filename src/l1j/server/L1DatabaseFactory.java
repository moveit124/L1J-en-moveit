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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.BoneCPDataSource;

public class L1DatabaseFactory {
	BoneCPDataSource _source;

	private static L1DatabaseFactory _instance;
	//private ComboPooledDataSource _source;
	private static Logger _log = LoggerFactory.getLogger(L1DatabaseFactory.class);
	private static String _driver;
	private static String _url;
	private static String _user;
	private static String _password;

	public static void setDatabaseSettings(final String driver,
			final String url, final String user, final String password) {
		_driver = driver;
		_url = url;
		_user = user;
		_password = password;
	}

	public L1DatabaseFactory() throws SQLException {
		try {
			// DatabaseFactory
			_source = new BoneCPDataSource();
			_source.setDriverClass(_driver);
			_source.setJdbcUrl(_url);
			_source.setUser(_user);
			_source.setPassword(_password);
			_source.setPartitionCount(3);
			_source.setIdleConnectionTestPeriodInSeconds(60);
			_source.setConnectionTestStatement("/* ping */ SELECT 1");
			_source.setTransactionRecoveryEnabled(true);
			_source.setDisableConnectionTracking(true);
			
			// --- Performance Tuning ---
			// Set min/max connections per partition (adjust based on monitoring)
			_source.setMinConnectionsPerPartition(5); // Baseline connections per partition
			_source.setMaxConnectionsPerPartition(20); // Max connections per partition (Total potential max = 3 * 20 = 60)
			// Optional: Enable statement caching (consumes more heap memory)
			// _source.setStatementsCacheSize(50); 
			// Optional: Set a timeout for acquiring connections (e.g., 10 seconds)
			// _source.setConnectionTimeoutInMs(10000);
			// --- End Performance Tuning ---

			// Test the connection and database access
			Connection testCon = null;
			PreparedStatement testPstm = null;
			ResultSet testRs = null;
			try {
				testCon = _source.getConnection();
				testPstm = testCon.prepareStatement("SELECT COUNT(*) FROM spawnlist_npc");
				testRs = testPstm.executeQuery();
				if (testRs.next()) {
					_log.info("Database connection test successful - Found {} NPC spawn records", testRs.getInt(1));
				}
			} catch (SQLException e) {
				_log.error("Database connection test failed", e);
				throw e;
			} finally {
				if (testRs != null) try { testRs.close(); } catch (SQLException e) {}
				if (testPstm != null) try { testPstm.close(); } catch (SQLException e) {}
				if (testCon != null) try { testCon.close(); } catch (SQLException e) {}
			}
		} catch (Exception e) {
			_log.error("Database Connection FAILED");
			throw new SQLException("could not init DB connection:" + e);
		}
	}

	public void shutdown() {
		try {
			_source.close();
		} catch (Exception e) {
			_log.error("",e);
		}
		try {
			_source = null;
		} catch (Exception e) {
			_log.error("", e);
		}
	}

	public static L1DatabaseFactory getInstance() throws SQLException {
		if (_instance == null) {
			_instance = new L1DatabaseFactory();
		}
		return _instance;
	}

	public Connection getConnection() {
		Connection con = null;
		while (con == null) {
			try {
				con = _source.getConnection();
			} catch (SQLException e) {
				_log.warn("L1DatabaseFactory: getConnection() failed, trying again "
						+ e);
			}
		}
		return con;
	}
}
