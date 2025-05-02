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
package l1j.server.server.command.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import l1j.server.server.datatables.NpcSpawnTable;
import l1j.server.server.datatables.SpawnTable;
import l1j.server.server.model.L1Spawn;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;

public class L1ToSpawn implements L1CommandExecutor {
	private static final Map<Integer, Integer> _spawnId = new HashMap<Integer, Integer>();

	private L1ToSpawn() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1ToSpawn();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			if (arg == null) {
				pc.sendPackets(new S_SystemMessage("Please specify a spawn ID."));
				return;
			}
			
			StringTokenizer st = new StringTokenizer(arg);
			int id = Integer.parseInt(st.nextToken());
			
			L1Spawn spawn = null;
			if (_spawnId.containsKey(id)) {
				spawn = NpcSpawnTable.getInstance().getTemplate(_spawnId.get(id));
			}
			if (spawn == null) {
				spawn = SpawnTable.getInstance().getTemplate(id);
				_spawnId.put(id, spawn.getId());
			}
			if (spawn != null) {
				L1Teleport.teleport(pc, spawn.getLocX(), spawn.getLocY(), spawn.getMapId(), 5, false);
				pc.sendPackets(new S_SystemMessage("Teleported to spawn point " + id));
			} else {
				pc.sendPackets(new S_SystemMessage("Spawn point " + id + " not found."));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("." + cmdName + " spawnid"));
		}
	}
} 