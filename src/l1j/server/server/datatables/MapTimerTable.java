package l1j.server.server.datatables;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Set;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.utils.SQLUtil;

public class MapTimerTable {
	private static Logger _log = LoggerFactory.getLogger(MapsTable.class.getName());
	
	private int _charId;
	private int _mapId;
	private int _areaId;
	private int _enterTime;

	public MapTimerTable() { }

	public MapTimerTable(int charId, int mapId, int areaId, int enterTime) {
		_charId = charId;
		_mapId = mapId;
		_areaId = areaId;
		_enterTime = enterTime;
	}

	public int getCharObjId() {
		return _charId;
	}
	
	public void setCharId(int charObjId) {
		_charId = charObjId;
	}
	
	public int getMapId() {
		return _mapId;
	}
	
	public void setMapId(int mapId) {
		_mapId = mapId;
	}

	public int getAreaId() {
		return _areaId;
	}
	
	public void setAreaId(int areaId) {
		_areaId = areaId;
	}
		
	public int getEnterTime() {
		return _enterTime;
	}
	
	public void setEnterTime(int enterTime) {
		_enterTime = enterTime;
	}

	/**
	 * Gets the current time in the server's configured timezone
	 */
	private static Calendar getServerTime() {
		return Calendar.getInstance(TimeZone.getTimeZone(Config.TIME_ZONE));
	}

	public static MapTimerTable find(int charId, int areaId) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM map_timers WHERE char_id = ? AND area_id = ?");
			pstm.setInt(1, charId);
			pstm.setInt(2, areaId);
			rs = pstm.executeQuery();
			if(rs.next()) {
				return new MapTimerTable(rs.getInt("char_id"), rs.getInt("map_id"), rs.getInt("area_id"), rs.getInt("enter_time"));
			}
			
			return null;
		} catch (SQLException e) {
			_log.error(e.getLocalizedMessage(), e);
			
			return null;
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	private void store(Connection con) {
		PreparedStatement pstm = null;

		try {
			pstm = con.prepareStatement("INSERT INTO map_timers SET char_id=?, map_id=?, area_id=?, enter_time=?, last_updated=?");
			pstm.setInt(1, _charId);
			pstm.setInt(2, _mapId);
			pstm.setInt(3, _areaId);
			pstm.setInt(4, _enterTime);
			pstm.setTimestamp(5, new java.sql.Timestamp(getServerTime().getTimeInMillis()));
			
			pstm.execute();
		} catch (SQLException e) {
			_log.error(e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
		}
	}

	public void save() {
		Connection con = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			delete(con, _charId, _areaId);
			store(con);
		} catch (SQLException e) {
			_log.error(e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(con);
		}
	}

	private static void delete(Connection con, int charId, int areaId) {
		PreparedStatement pstm = null;

		try {
			pstm = con.prepareStatement("DELETE FROM map_timers WHERE char_id = ? AND area_id = ?");
			pstm.setInt(1, charId);
			pstm.setInt(2, areaId);
			pstm.execute();
		} catch (SQLException e) {
			_log.error(e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
		}
	}

	private static void delete(Connection con, int areaId) {
		PreparedStatement pstm = null;

		try {
			pstm = con.prepareStatement("DELETE FROM map_timers WHERE area_id = ?");
			pstm.setInt(1, areaId);
			pstm.execute();
		} catch (SQLException e) {
			_log.error(e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
		}
	}

	public static void remove(int areaId) {
		Connection con = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			delete(con, areaId);
		} catch (SQLException e) {
			_log.error(e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(con);
		}
	}

	public static void reset(int areaId) {
		_log.info("Initiating timer reset for area " + areaId + " at " + getServerTime().getTime() + " " + Config.TIME_ZONE);

		// Step 1: Get IDs of players with active timers for this area
		Set<Integer> playersToReset = getPlayersWithActiveTimer(areaId);
		if (playersToReset.isEmpty()) {
			_log.info("No active timers found for area " + areaId + ". Skipping reset steps.");
			return; // Nothing to do if no players had timers
		}
		_log.info("Found " + playersToReset.size() + " players with active timers for area " + areaId);

		// Step 2: Stop limiter TASK for targeted players (if online) - NO SAVE
		for (int charId : playersToReset) {
			l1j.server.server.model.L1Object obj = L1World.getInstance().findObject(charId);
			if (obj != null && obj instanceof L1PcInstance) {
				L1PcInstance pc = (L1PcInstance) obj;
				if (pc.getMapLimiter() != null && pc.getMapLimiter().getAreaId() == areaId) {
					_log.debug("Cancelling map limiter task for player {} ({}) in area {}", pc.getName(), charId, areaId);
					pc.cancelMapLimiterTask();
				}
			}
		}

		// Step 3: Remove database entries (efficient)
		_log.info("Removing map timer entries for area {}", areaId);
		remove(areaId); // This executes: DELETE FROM map_timers WHERE area_id = ?

		// Step 4: Start limiter logic for targeted players (if online)
		// The startMapLimiter method itself should handle checks if they are on the right map etc.
		for (int charId : playersToReset) {
			l1j.server.server.model.L1Object obj = L1World.getInstance().findObject(charId);
			if (obj != null && obj instanceof L1PcInstance) {
				L1PcInstance pc = (L1PcInstance) obj;
				_log.debug("Attempting to start map limiter for player {} ({}) after reset of area {}", pc.getName(), charId, areaId);
				pc.startMapLimiter();
			}
		}
		_log.info("Finished resetting timers for area " + areaId);
	}

	// Helper method to get players with active timers
	private static Set<Integer> getPlayersWithActiveTimer(int areaId) {
		Set<Integer> playerIds = new HashSet<>();
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT DISTINCT char_id FROM map_timers WHERE area_id = ?");
			pstm.setInt(1, areaId);
			rs = pstm.executeQuery();
			while (rs.next()) {
				playerIds.add(rs.getInt("char_id"));
			}
		} catch (SQLException e) {
			_log.error("Failed to retrieve players with active timers for areaId: " + areaId, e);
		} finally {
			// Ensure proper resource closure
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		return playerIds;
	}
}
