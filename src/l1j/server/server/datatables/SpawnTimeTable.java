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
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.templates.L1SpawnTime;
import l1j.server.server.utils.SQLUtil;

public class SpawnTimeTable {
	private static Logger _log = LoggerFactory.getLogger(SpawnTimeTable.class.getName());
	private static SpawnTimeTable _instance;
	private Map<Integer, L1SpawnTime> _times = new HashMap<Integer, L1SpawnTime>();

	public static SpawnTimeTable getInstance() {
		if (_instance == null) {
			_instance = new SpawnTimeTable();
		}
		return _instance;
	}

	private SpawnTimeTable() {
		fillSpawnTimeTable();
	}

	private void fillSpawnTimeTable() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM spawnlist_time");
			rs = pstm.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("spawn_id");
				Time timeStart = rs.getTime("time_start");
				Time timeEnd = rs.getTime("time_end");
				
				L1SpawnTime.L1SpawnTimeBuilder builder = new L1SpawnTime.L1SpawnTimeBuilder(id);
				builder.setTimeStart(timeStart);
				builder.setTimeEnd(timeEnd);
				_times.put(id, builder.build());
			}
		} catch (SQLException e) {
			_log.error("Error loading spawn time table", e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public L1SpawnTime get(int id) {
		return _times.get(id);
	}
} 