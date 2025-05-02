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
package l1j.server.server.model;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.server.datatables.NpcTable;
import l1j.server.server.encryptions.IdFactory;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.templates.L1Npc;

public class L1UbSpawn implements Comparable<L1UbSpawn> {
	private static Logger _log = LoggerFactory.getLogger(L1UbSpawn.class.getName());
	private static Random _random = new Random();
	private int _id;
	private int _ubId;
	private int _pattern;
	private int _group;
	private int _npcTemplateId;
	private int _amount;
	private int _spawnDelay;
	private int _sealCount;
	private String _name;

	public L1UbSpawn(int id, int ubId, int pattern, int group, int npcTemplateId,
			int amount, int spawnDelay, int sealCount, String name) {
		_id = id;
		_ubId = ubId;
		_pattern = pattern;
		_group = group;
		_npcTemplateId = npcTemplateId;
		_amount = amount;
		_spawnDelay = spawnDelay;
		_sealCount = sealCount;
		_name = name;
	}

	public int getId() {
		return _id;
	}

	public int getUbId() {
		return _ubId;
	}

	public int getPattern() {
		return _pattern;
	}

	public int getGroup() {
		return _group;
	}

	public int getNpcTemplateId() {
		return _npcTemplateId;
	}

	public int getAmount() {
		return _amount;
	}

	public int getSpawnDelay() {
		return _spawnDelay;
	}

	public int getSealCount() {
		return _sealCount;
	}

	public String getName() {
		return _name;
	}

	@Override
	public int compareTo(L1UbSpawn rhs) {
		if (getId() < rhs.getId()) {
			return -1;
		}
		if (getId() > rhs.getId()) {
			return 1;
		}
		return 0;
	}

	public void spawnAll(L1UltimateBattle ub) {
		for (int i = 0; i < getAmount(); i++) {
			spawnOne(ub);
		}
	}

	public void spawnOne(L1UltimateBattle _ub) {
		L1Npc template = NpcTable.getInstance().getTemplate(getNpcTemplateId());
		if (template == null) {
			_log.warn("UB NPC template not found for ID: {}", getNpcTemplateId());
			return;
		}
		try {
		    L1NpcInstance npc = NpcTable.getInstance().newNpcInstance(template);
		    npc.setId(IdFactory.getInstance().nextId());
		    npc.setHeading(5); // Or maybe a random heading

		    // --- START FIX ---
		    // Get the map and coordinate boundaries from the L1UltimateBattle instance (_ub)
		    int mapId = _ub.getMapId(); // Get map ID from the UB instance
		    int locX1 = _ub.getLocX1();
		    int locY1 = _ub.getLocY1();
		    int locX2 = _ub.getLocX2();
		    int locY2 = _ub.getLocY2();

		    // Calculate random coordinates within the UB boundaries
		    int spawnX = _random.nextInt(locX2 - locX1) + locX1;
		    int spawnY = _random.nextInt(locY2 - locY1) + locY1;

		    // TODO: Add a check/loop here to ensure spawnX, spawnY is on a passable tile on mapId
		    // (e.g., using L1World.getInstance().getMap(mapId).isPassable(spawnX, spawnY))
		    // If not passable, try getting new random coordinates a few times.

		    npc.setX(spawnX);
		    npc.setY(spawnY);
		    npc.setMap((short) mapId); // Use the map ID from the UB instance
		     // --- END FIX ---


		    if (npc instanceof L1MonsterInstance) {
		        ((L1MonsterInstance) npc).set_storeDroped(true);
		    }

		    L1World.getInstance().storeObject(npc);
		    L1World.getInstance().addVisibleObject(npc);

		    npc.turnOnOffLight();
		    npc.startChat(L1NpcInstance.CHAT_TIMING_APPEARANCE);
		} catch (Exception e) {
		    _log.error("Error spawning UB NPC: {}", template.get_name(), e);
		}
	}
} 