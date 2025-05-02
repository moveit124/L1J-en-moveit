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
package l1j.server.server.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.utils.SQLUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChatLogTable {
	private static Logger _log = LoggerFactory.getLogger(ChatLogTable.class.getName());
	/*
	 * HashMap in the code should be used, but the performance problems might
	 * have used an array compromise. HashMap to consider changes to the
	 * performance or the lack of sufficient attention to the problem.
	 */
	private final boolean[] loggingConfig = new boolean[15];

	// Create a single-threaded executor for sequential chat logging
	private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
		Thread thread = Executors.defaultThreadFactory().newThread(runnable);
		thread.setName("ChatLogThread"); // Give the thread a meaningful name
		thread.setDaemon(true); // Allow JVM to exit if this is the only thread running
		return thread;
	});

	private ChatLogTable() {
		loadConfig();
	}

	private void loadConfig() {
		loggingConfig[0] = Config.LOGGING_CHAT_NORMAL;
		loggingConfig[1] = Config.LOGGING_CHAT_WHISPER;
		loggingConfig[2] = Config.LOGGING_CHAT_SHOUT;
		loggingConfig[3] = Config.LOGGING_CHAT_WORLD;
		loggingConfig[4] = Config.LOGGING_CHAT_CLAN;
		loggingConfig[11] = Config.LOGGING_CHAT_PARTY;
		loggingConfig[13] = Config.LOGGING_CHAT_COMBINED;
		loggingConfig[14] = Config.LOGGING_CHAT_CHAT_PARTY;
	}

	private static ChatLogTable _instance;

	public static ChatLogTable getInstance() {
		if (_instance == null) {
			_instance = new ChatLogTable();
		}
		return _instance;
	}

	private boolean isLoggingTarget(int type) {
		return !executor.isShutdown() && loggingConfig[type];
	}

	public void storeChat(L1PcInstance pc, L1PcInstance target, String text,
			int type) {
		if (!isLoggingTarget(type)) {
			return;
		}
		// type
		// 0: Normal chat
		// 1: Whisper
		// 2: The Scream
		// 3: the entire chat
		// 4: clan chat
		// 11: party chat
		// 13: Union chat
		// 14: party chat
		// Capture data synchronously before submitting the task
		final String accountName = pc.getAccountName();
		final int charId = pc.getId();
		final String charName = pc.isGm() ? "******" : pc.getName();
		final int clanId = pc.getClanid();
		final String clanName = pc.getClanname();
		final int locX = pc.getX();
		final int locY = pc.getY();
		final int mapId = pc.getMapId();

		final String targetAccountName = (target != null) ? target.getAccountName() : null;
		final int targetId = (target != null) ? target.getId() : 0;
		final String targetName = (target != null) ? target.getName() : null;
		final int targetClanId = (target != null) ? target.getClanid() : 0;
		final String targetClanName = (target != null) ? target.getClanname() : null;
		final int targetLocX = (target != null) ? target.getX() : 0;
		final int targetLocY = (target != null) ? target.getY() : 0;
		final int targetMapId = (target != null) ? target.getMapId() : 0;

		// Submit the database operation to the executor service
		executor.submit(() -> {
			Connection con = null;
			PreparedStatement pstm = null;
			try {
				con = L1DatabaseFactory.getInstance().getConnection();
				if (targetName != null) { // Check if target existed when data was captured
					pstm = con
							.prepareStatement("INSERT INTO log_chat (account_name, char_id, name, clan_id, clan_name, locx, locy, mapid, type, target_account_name, target_id, target_name, target_clan_id, target_clan_name, target_locx, target_locy, target_mapid, content, datetime) VALUE (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE())");
					pstm.setString(1, accountName);
					pstm.setInt(2, charId);
					pstm.setString(3, charName);
					pstm.setInt(4, clanId);
					pstm.setString(5, clanName);
					pstm.setInt(6, locX);
					pstm.setInt(7, locY);
					pstm.setInt(8, mapId);
					pstm.setInt(9, type);
					pstm.setString(10, targetAccountName);
					pstm.setInt(11, targetId);
					pstm.setString(12, targetName);
					pstm.setInt(13, targetClanId);
					pstm.setString(14, targetClanName);
					pstm.setInt(15, targetLocX);
					pstm.setInt(16, targetLocY);
					pstm.setInt(17, targetMapId);
					pstm.setString(18, text);
				} else {
					pstm = con
							.prepareStatement("INSERT INTO log_chat (account_name, char_id, name, clan_id, clan_name, locx, locy, mapid, type, content, datetime) VALUE (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE())");
					pstm.setString(1, accountName);
					pstm.setInt(2, charId);
					pstm.setString(3, charName);
					pstm.setInt(4, clanId);
					pstm.setString(5, clanName);
					pstm.setInt(6, locX);
					pstm.setInt(7, locY);
					pstm.setInt(8, mapId);
					pstm.setInt(9, type);
					pstm.setString(10, text);
				}
				pstm.execute();
			} catch (SQLException e) {
				_log.error("Error storing chat log asynchronously: " + e.getLocalizedMessage(), e);
			} finally {
				SQLUtil.close(pstm);
				SQLUtil.close(con);
			}
		});
	}

	/**
	 * Shuts down the chat log executor service gracefully.
	 */
	public void shutdown() {
		try {
			_log.info("Shutting down ChatLog executor service...");
			executor.shutdown(); // Disable new tasks from being submitted
			if (!executor.awaitTermination(60, TimeUnit.SECONDS)) { // Wait a while for existing tasks to terminate
				_log.warn("ChatLog executor service did not terminate in 60s. Forcing shutdown...");
				executor.shutdownNow(); // Cancel currently executing tasks
				if (!executor.awaitTermination(60, TimeUnit.SECONDS)) // Wait again for tasks to respond to being cancelled
					_log.error("ChatLog executor service did not terminate after forced shutdown.");
			}
			_log.info("ChatLog executor service shutdown complete.");
		} catch (InterruptedException ie) {
			_log.error("ChatLog executor service shutdown interrupted.", ie);
			executor.shutdownNow(); // Re-cancel if current thread also interrupted
			Thread.currentThread().interrupt(); // Preserve interrupt status
		}
	}
}