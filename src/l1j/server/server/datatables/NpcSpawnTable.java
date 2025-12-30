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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.L1Spawn;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.utils.SQLUtil;

public class NpcSpawnTable {
	private static Logger _log = LoggerFactory.getLogger(NpcSpawnTable.class.getName());
	private static NpcSpawnTable _instance;
	private Map<Integer, L1Spawn> _spawntable = new HashMap<Integer, L1Spawn>();
	private int _highestId;
	private static L1Spawn _bugBoard;
	private static L1Spawn _rankingBoard;

	public static NpcSpawnTable getInstance() {
		if (_instance == null) {
			_instance = new NpcSpawnTable();
		}
		return _instance;
	}

	private NpcSpawnTable() {
		fillNpcSpawnTable();
	}

	private void fillNpcSpawnTable() {
		int spawnCount = 0;
		int failedSpawns = 0;
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM spawnlist_npc");
			rs = pstm.executeQuery();
			while (rs.next()) {
				if (Config.ALT_GMSHOP == false) {
					int npcid = rs.getInt(1);
					if ((npcid >= Config.ALT_GMSHOP_MIN_ID) && (npcid <= Config.ALT_GMSHOP_MAX_ID)) {
						continue;
					}
				}
				if (!Config.HALLOWEEN_EVENT) {
					int npcid = rs.getInt("id");
					if (((npcid >= 130852) && (npcid <= 130862)) || ((npcid >= 26656) && (npcid <= 26734)) 
						|| ((npcid >= 89634) && (npcid <= 89644))) {
						continue;
					}
				}
				if (Config.BKLM_EVENT == false ) {
					int npcid = rs.getInt("id");
					if (npcid == 101007 || npcid == 101008) {
						continue;
					}
				}
				if (Config.ALT_JPPRIVILEGED == false) {
					int npcid = rs.getInt("id");
					if ((npcid >= 1310368) && (npcid <= 1310379)) {
						continue;
					}
				}
				if (Config.ALT_TALKINGSCROLLQUEST == false) {
					int npcid = rs.getInt("id");
					if (((npcid >= 87537) && (npcid <= 87551)) || ((npcid >= 1310387) && (npcid <= 1310389))) {
						continue;
					}
				}
				if (Config.ALT_TALKINGSCROLLQUEST == true) {
					int npcid = rs.getInt("id");
					if ((npcid >= 90066) && (npcid <= 90069)) {
						continue;
					}
				}
				int npcTemplateId = rs.getInt("npc_templateid");
				if (NpcTable.getInstance().getTemplate(npcTemplateId) == null) {
					_log.warn("NPC template does not exist. npc_templateid: {} at location ({},{})", 
						npcTemplateId, rs.getInt("locx"), rs.getInt("locy"));
					failedSpawns++;
					continue;
				}

				try {
					L1Spawn spawnDat = new L1Spawn(NpcTable.getInstance().getTemplate(npcTemplateId));
					spawnDat.setId(rs.getInt("id"));
					spawnDat.setAmount(rs.getInt("count"));
					spawnDat.setLocX(rs.getInt("locx"));
					spawnDat.setLocY(rs.getInt("locy"));
					spawnDat.setRandomx(rs.getInt("randomx"));
					spawnDat.setRandomy(rs.getInt("randomy"));
					spawnDat.setHeading(rs.getInt("heading"));
					spawnDat.setMinRespawnDelay(rs.getInt("respawn_delay"));
					spawnDat.setMaxRespawnDelay(rs.getInt("respawn_delay"));
					spawnDat.setMapId(rs.getShort("mapid"));
					spawnDat.setMovementDistance(rs.getInt("movement_distance"));
					spawnDat.setName(rs.getString("location"));
					spawnDat.setLocation(rs.getString("location"));

					if (rs.getInt("id") > _highestId) {
						_highestId = rs.getInt("id");
					}

					int npcId = rs.getInt("npc_templateid");
					if (npcId == 81126) {
						_bugBoard = spawnDat;
					} else if (npcId == 81150) {
						_rankingBoard = spawnDat;
					}

					_spawntable.put(Integer.valueOf(spawnDat.getId()), spawnDat);
					spawnCount += spawnDat.getAmount();

					_log.debug("Initializing spawn - NPC ID: {}, Location: ({},{}) Map: {}", 
						npcTemplateId, spawnDat.getLocX(), spawnDat.getLocY(), spawnDat.getMapId());
					
					try {
						spawnDat.init();
					} catch (Exception e) {
						failedSpawns++;
						_log.error("Failed to initialize spawn for NPC {} at ({},{}) Map: {} - Error: {}", 
							spawnDat.getName(), spawnDat.getLocX(), spawnDat.getLocY(), 
							spawnDat.getMapId(), e.getMessage());
					}
				} catch (Exception e) {
					failedSpawns++;
					_log.error("Failed to create spawn data for NPC template {} - Error: {}", 
						npcTemplateId, e.getMessage());
				}
			}
		} catch (SQLException e) {
			_log.error("Error loading NPC spawn table", e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		_log.info("Loaded {} NPC spawns ({} failed) for {} records", 
			spawnCount, failedSpawns, _spawntable.size() + failedSpawns);
	}

	public static L1Spawn getBugBoard() {
		return _bugBoard;
	}

	public static L1Spawn getRankingBoard() {
		return _rankingBoard;
	}

	public void storeSpawn(L1Npc npc, int count, int locx, int locy, short mapid) {
		Connection con = null;
		PreparedStatement pstm = null;

		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("INSERT INTO spawnlist_npc SET location=?,count=?,npc_templateid=?,locx=?,locy=?,heading=?,mapid=?");
			pstm.setString(1, "");
			pstm.setInt(2, count);
			pstm.setInt(3, npc.get_npcId());
			pstm.setInt(4, locx);
			pstm.setInt(5, locy);
			pstm.setInt(6, 5);
			pstm.setInt(7, mapid);
			pstm.execute();

		} catch (Exception e) {
			_log.error("Error storing NPC spawn", e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public L1Spawn getTemplate(int i) {
		return _spawntable.get(i);
	}

	public void addNewSpawn(L1Spawn spawn) {
		_highestId++;
		spawn.setId(_highestId);
		_spawntable.put(Integer.valueOf(spawn.getId()), spawn);
	}
} 