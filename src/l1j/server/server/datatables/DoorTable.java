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

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.server.ActionCodes;
import l1j.server.server.encryptions.IdFactory;
import l1j.server.server.model.L1Location;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1DoorInstance;
import l1j.server.server.templates.L1DoorGfx;
import l1j.server.server.templates.L1DoorSpawn;
import l1j.server.server.utils.PerformanceTimer;
import l1j.server.server.utils.collections.Lists;
import l1j.server.server.utils.collections.Maps;

public class DoorTable {
	private static Logger _log = LoggerFactory.getLogger(DoorTable.class.getName());
	
	private static DoorTable _instance;

	private static Map<L1Location, L1DoorInstance> _doors = Maps.newConcurrentHashMap();
	
	private static Map<L1Location, L1DoorInstance> _doorDirections = Maps.newConcurrentHashMap();

	public static DoorTable getInstance() {
		if (_instance == null) {
			_instance = new DoorTable();
		}
		return _instance;
	}

	private DoorTable() {
		load();
	}

	/**
	 * Check if a door ID is in a range of known duplicate doors
	 * @param id Door ID to check
	 * @return true if the door is in a known duplicate range
	 */
	private boolean isInKnownDuplicateRange(int id) {
		return (id >= 0 && id <= 4) || 
			   (id >= 100 && id <= 107) || 
			   (id >= 200 && id <= 205) || 
			   (id >= 300 && id <= 310) || 
			   (id >= 400 && id <= 407) || 
			   (id >= 500 && id <= 511) || 
			   (id >= 600 && id <= 635) || 
			   (id >= 700 && id <= 812) || 
			   (id >= 1000 && id <= 1269) || 
			   (id >= 1300 && id <= 1328) || 
			   (id >= 3001 && id <= 3045) || 
			   (id >= 4001 && id <= 4004) || 
			   (id >= 5001 && id <= 5010) || 
			   (id >= 6001 && id <= 6032) || 
			   (id >= 7001 && id <= 7216) || 
			   (id >= 9000 && id <= 9001);
	}

	private void loadDoors(Map<L1Location, L1DoorInstance> doors,
			Map<L1Location, L1DoorInstance> doorDirections) {
		PerformanceTimer timer = new PerformanceTimer();
		for (L1DoorSpawn spawn : L1DoorSpawn.all()) {
			L1Location loc = spawn.getLocation();
			if (doors.containsKey(loc)) {
				// Only log warnings for doors that aren't in our known duplicate ranges
				if (!isInKnownDuplicateRange(spawn.getId())) {
					_log.warn(
							String.format("Duplicate door location: id = %d",
									spawn.getId()));
				}
				continue;
			}
			createDoor(spawn.getId(), spawn.getGfx(), loc, spawn.getHp(),
					spawn.getKeeper());
		}
		System.out.println("loading doors...OK! " + timer.elapsedTimeMillis() + "ms");
	}

	private void load() {
		loadDoors(_doors, _doorDirections);
	}
	
	public void reload() {
		Map<L1Location, L1DoorInstance> doors = Maps.newConcurrentHashMap();
		Map<L1Location, L1DoorInstance> doorDirections = Maps.newConcurrentHashMap();
		loadDoors(doors, doorDirections);
		_doors = doors;
		_doorDirections = doorDirections;
	}
	
	private void putDirections(Map<L1Location, L1DoorInstance> doorDirections, L1DoorInstance door) {
		for (L1Location key : makeDirectionsKeys(door)) {
			doorDirections.put(key, door);
		}
	}

	private void removeDirections(Map<L1Location, L1DoorInstance> doorDirections, L1DoorInstance door) {
		for (L1Location key : makeDirectionsKeys(door)) {
			doorDirections.remove(key);
		}
	}

	private List<L1Location> makeDirectionsKeys(L1DoorInstance door) {
		List<L1Location> keys = Lists.newArrayList();
		int left = door.getLeftEdgeLocation();
		int right = door.getRightEdgeLocation();
		if (door.getDirection() == 0) {
			for (int x = left; x <= right; x++) {
				keys.add(new L1Location(x, door.getY(), door.getMapId()));
			}
		} else {
			for (int y = left; y <= right; y++) {
				keys.add(new L1Location(door.getX(), y, door.getMapId()));
			}
		}
		return keys;
	}

	public L1DoorInstance createDoor(int doorId, L1DoorGfx gfx, L1Location loc,
			int hp, int keeper) {
		return createDoor(_doors, _doorDirections, doorId, gfx, loc, hp, keeper);
	}
	
	public L1DoorInstance createDoor(Map<L1Location, L1DoorInstance> doors,
			Map<L1Location, L1DoorInstance> doorDirections, int doorId,
			L1DoorGfx gfx, L1Location loc, int hp, int keeper) {
		if (doors.containsKey(loc)) {
			return null;
		}
		L1DoorInstance door = new L1DoorInstance(doorId, gfx, loc, hp, keeper);
		door.setId(IdFactory.getInstance().nextId());
		L1World.getInstance().storeObject(door);
		L1World.getInstance().addVisibleObject(door);
		doors.put(door.getLocation(), door);
		putDirections(doorDirections, door);
		return door;
	}

	public void deleteDoorByLocation(L1Location loc) {
		L1DoorInstance door = _doors.remove(loc);
		if (door != null) {
			removeDirections(_doorDirections, door);
			door.deleteMe();
		}
	}

	public int getDoorDirection(L1Location loc) {
		L1DoorInstance door = _doorDirections.get(loc);
		if (door == null || door.getOpenStatus() == ActionCodes.ACTION_Open) {
			return -1;
		}
		return door.getDirection();
	}

	public L1DoorInstance findByDoorId(int doorId) {
		for (L1DoorInstance door : _doors.values()) {
			if (door.getDoorId() == doorId) {
				return door;
			}
		}
		return null;
	}

	public L1DoorInstance[] getDoorList() {
		return _doors.values().toArray(new L1DoorInstance[_doors.size()]);
	}
}