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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.Config;
import l1j.server.server.ActionCodes;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.encryptions.IdFactory;
import l1j.server.server.model.Instance.L1DoorInstance;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.gametime.L1GameTime;
import l1j.server.server.model.gametime.L1GameTimeAdapter;
import l1j.server.server.model.gametime.L1GameTimeClock;
import l1j.server.server.model.map.L1Map;
import l1j.server.server.model.map.L1WorldMap;
import l1j.server.server.random.RandomGenerator;
import l1j.server.server.random.RandomGeneratorFactory;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.templates.L1SpawnTime;
import l1j.server.server.types.Point;
import java.util.concurrent.ConcurrentHashMap;

public class L1Spawn extends L1GameTimeAdapter {
	private static Logger _log = Logger.getLogger(L1Spawn.class.getName());
	private final L1Npc _template;

	private int _id; // just to find this in the spawn table
	private String _location;
	private int _maximumCount;
	private int _npcid;
	private int _groupId;
	private int _locx;
	private int _locy;
	private int _randomx;
	private int _randomy;
	private int _locx1;
	private int _locy1;
	private int _locx2;
	private int _locy2;
	private int _heading;
	private int _minRespawnDelay;
	private int _maxRespawnDelay;
	private short _mapid;
	private boolean _respaenScreen;
	private int _movementDistance;
	private boolean _rest;
	private int _spawnType;
	private int _delayInterval;
	private L1SpawnTime _time;
	private HashMap<Integer, Point> _homePoint = null; // initでspawnした個々のオブジェクトのホームポイント
	private List<L1NpcInstance> _mobs = new ArrayList<L1NpcInstance>();

	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	private String _name;

	private class SpawnTask implements Runnable {
		private int _spawnNumber;
		private int _objectId;

		private SpawnTask(int spawnNumber, int objectId) {
			_spawnNumber = spawnNumber;
			_objectId = objectId;
		}

		@Override
		public void run() {
			doSpawn(_spawnNumber, _objectId);
		}
	}

	public L1Spawn(L1Npc mobTemplate) {
		_template = mobTemplate;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public short getMapId() {
		return _mapid;
	}

	public void setMapId(short _mapid) {
		this._mapid = _mapid;
	}

	public boolean isRespawnScreen() {
		return _respaenScreen;
	}

	public void setRespawnScreen(boolean flag) {
		_respaenScreen = flag;
	}

	public int getMovementDistance() {
		return _movementDistance;
	}

	public void setMovementDistance(int i) {
		_movementDistance = i;
	}

	public int getAmount() {
		return _maximumCount;
	}

	public int getGroupId() {
		return _groupId;
	}

	public int getId() {
		return _id;
	}

	public String getLocation() {
		return _location;
	}

	public int getLocX() {
		return _locx;
	}

	public int getLocY() {
		return _locy;
	}

	public int getNpcId() {
		return _npcid;
	}

	public int getHeading() {
		return _heading;
	}

	public int getRandomx() {
		return _randomx;
	}

	public int getRandomy() {
		return _randomy;
	}

	public int getLocX1() {
		return _locx1;
	}

	public int getLocY1() {
		return _locy1;
	}

	public int getLocX2() {
		return _locx2;
	}

	public int getLocY2() {
		return _locy2;
	}

	public int getMinRespawnDelay() {
		return _minRespawnDelay;
	}

	public int getMaxRespawnDelay() {
		return _maxRespawnDelay;
	}

	public void setAmount(int amount) {
		_maximumCount = amount;
	}

	public void setId(int id) {
		_id = id;
	}

	public void setGroupId(int i) {
		_groupId = i;
	}

	public void setLocation(String location) {
		_location = location;
	}

	public void setLocX(int locx) {
		_locx = locx;
	}

	public void setLocY(int locy) {
		_locy = locy;
	}

	public void setNpcid(int npcid) {
		_npcid = npcid;
	}

	public void setHeading(int heading) {
		_heading = heading;
	}

	public void setRandomx(int randomx) {
		_randomx = randomx;
	}

	public void setRandomy(int randomy) {
		_randomy = randomy;
	}

	public void setLocX1(int locx1) {
		_locx1 = locx1;
	}

	public void setLocY1(int locy1) {
		_locy1 = locy1;
	}

	public void setLocX2(int locx2) {
		_locx2 = locx2;
	}

	public void setLocY2(int locy2) {
		_locy2 = locy2;
	}

	public void setMinRespawnDelay(int i) {
		_minRespawnDelay = i;
	}

	public void setMaxRespawnDelay(int i) {
		_maxRespawnDelay = i;
	}

	private int calcRespawnDelay() {
		int respawnDelay = _minRespawnDelay * 1000;
		if (_delayInterval > 0) {
			respawnDelay += _random.nextInt(_delayInterval) * 1000;
		}
		L1GameTime currentTime = L1GameTimeClock.getInstance().currentTime();
		if (_time != null && !_time.getTimePeriod().includes(currentTime)) { // 指定時間外なら指定時間までの時間を足す
			long diff = (_time.getTimeStart().getTime() - currentTime.toTime()
					.getTime());
			if (diff < 0) {
				diff += 24 * 1000L * 3600L;
			}
			diff /= 6; // real time to game time
			respawnDelay = (int) diff;
		}
		return respawnDelay;
	}

	/**
	 * SpawnTaskを起動する。
	 * 
	 * @param spawnNumber
	 *            L1Spawnで管理されている番号。ホームポイントが無ければ何を指定しても良い。
	 */
	public void executeSpawnTask(int spawnNumber, int objectId) {
		SpawnTask task = new SpawnTask(spawnNumber, objectId);
		GeneralThreadPool.getInstance().schedule(task, calcRespawnDelay());
	}

	private boolean _initSpawn = false;

	private boolean _spawnHomePoint;

	public void init() {
		if (_time != null && _time.isDeleteAtEndTime()) {
			L1GameTimeClock.getInstance().addListener(this);
		}
		_delayInterval = _maxRespawnDelay - _minRespawnDelay;
		_initSpawn = true;

		if (getSpawnHome() != 0 && getSpawnHomeCount() > 0 && isAreaSpawn()) {
			_spawnHomePoint = true;
			_homePoint = new HashMap<>();

			final int gridSize = 8;
			final int initialMinDistance = 8;
			final int minAllowedDistance = 3;
			Map<String, List<Point>> grid = new HashMap<>();

			for (int i = 0; i < getSpawnHomeCount(); i++) {
				Point chosen = null;
				int minDistance = initialMinDistance;

				for (int attempt = 0; attempt < 500; attempt++) {
					int x = ThreadLocalRandom.current().nextInt(getLocX1(), getLocX2() + 1);
					int y = ThreadLocalRandom.current().nextInt(getLocY1(), getLocY2() + 1);
					Point candidate = new Point(x, y);
					int gridX = x / gridSize;
					int gridY = y / gridSize;

					boolean tooClose = false;
					for (int dx = -1; dx <= 1 && !tooClose; dx++) {
						for (int dy = -1; dy <= 1 && !tooClose; dy++) {
							String key = (gridX + dx) + "," + (gridY + dy);
							List<Point> nearby = grid.getOrDefault(key, List.of());
							for (Point existing : nearby) {
								if (existing.getTileDistance(candidate) < minDistance) {
									tooClose = true;
									break;
								}
							}
						}
					}

					if (!tooClose) {
						chosen = candidate;
						String key = gridX + "," + gridY;
						grid.computeIfAbsent(key, k -> new ArrayList<>()).add(candidate);
						break;
					}

					// Gradually reduce spacing threshold
					if (attempt % 100 == 99 && minDistance > minAllowedDistance) {
						minDistance--;
					}
				}

				if (chosen == null) {
					for (int i1 = 0; i1 < 50; i1++) {
						int x = ThreadLocalRandom.current().nextInt(getLocX1(), getLocX2() + 1);
						int y = ThreadLocalRandom.current().nextInt(getLocY1(), getLocY2() + 1);
						Point candidate = new Point(x, y);

						L1Map map = L1WorldMap.getInstance().getMap(getMapId());
						if (map.isInMap(candidate) && map.isPassable(candidate)) {
							chosen = candidate;
							String key = (x / gridSize) + "," + (y / gridSize);
							grid.computeIfAbsent(key, k -> new ArrayList<>()).add(candidate);
							break;
						}
					}

					// Final safety fallback (in case all 50 attempts fail)
					if (chosen == null) {
						int x = ThreadLocalRandom.current().nextInt(getLocX1(), getLocX2() + 1);
						int y = ThreadLocalRandom.current().nextInt(getLocY1(), getLocY2() + 1);
						chosen = new Point(x, y);
						String key = (x / gridSize) + "," + (y / gridSize);
						grid.computeIfAbsent(key, k -> new ArrayList<>()).add(chosen);
					}
				}


				_homePoint.put(i, chosen);
			}
		}

		int spawnNum = 0;
		while (spawnNum < _maximumCount) {
			doSpawn(++spawnNum);
		}

		_initSpawn = false;
	}






	/**
	 * ホームポイントがある場合は、spawnNumberを基にspawnする。 それ以外の場合は、spawnNumberは未使用。
	 */
	protected void doSpawn(int spawnNumber) { // 初期配置
		// 指定時間外であれば、次spawnを予約して終わる。
		if (_time != null
				&& !_time.getTimePeriod().includes(
						L1GameTimeClock.getInstance().currentTime())) {
			executeSpawnTask(spawnNumber, 0);
			return;
		}
		doSpawn(spawnNumber, 0);
	}
	// Track boss spawn times to prevent announcements on server startup
	private static final Map<Integer, Long> bossSpawnTimes = new HashMap<>();
	// Track bosses that have been announced already
	private static final Set<Integer> announcedBosses = new HashSet<>();

	protected void doSpawn(int spawnNumber, int objectId) {
		L1NpcInstance mob = null;
		try {
			int newlocx = getLocX();
			int newlocy = getLocY();
			int tryCount = 0;

			mob = NpcTable.getInstance().newNpcInstance(_template);
			synchronized (_mobs) {
				_mobs.add(mob);
			}
			mob.setId(objectId == 0 ? IdFactory.getInstance().nextId() : objectId);
			mob.setHeading((0 <= getHeading() && getHeading() <= 7) ? getHeading() : 5);

			int npcId = mob.getNpcTemplate().get_npcId();
			if (npcId == 45488 && getMapId() == 9) mob.setMap((short) (getMapId() + _random.nextInt(2)));
			else if (npcId == 45601 && getMapId() == 11) mob.setMap((short) (getMapId() + _random.nextInt(3)));
			else if (npcId == 45649 && getMapId() == 80) mob.setMap((short) (getMapId() + _random.nextInt(3)));
			else mob.setMap(getMapId());

			mob.setMovementDistance(getMovementDistance());
			mob.setRest(isRest());

			while (tryCount <= 500) {
				switch (getSpawnType()) {
					case SPAWN_TYPE_PC_AROUND:
						if (!_initSpawn) {
							ArrayList<L1PcInstance> players = new ArrayList<>();
							for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
								if (pc.getMapId() == getMapId()) players.add(pc);
							}
							if (!players.isEmpty()) {
								L1PcInstance pc = players.get(_random.nextInt(players.size()));
								L1Location loc = pc.getLocation().randomLocation(PC_AROUND_DISTANCE, false);
								newlocx = loc.getX();
								newlocy = loc.getY();
								break;
							}
						}
						// fall through
					default:
						if (isAreaSpawn()) {
							if (_spawnHomePoint && _homePoint.containsKey(spawnNumber)) {
								Point pt = _homePoint.get(spawnNumber);
								if (pt != null) {
									// Optimized spatial bucketing
									final int gridSize = 8;
									Map<String, List<Point>> grid = new HashMap<>();
									List<L1NpcInstance> mobSnapshot;
									synchronized (_mobs) {
										mobSnapshot = new ArrayList<>(_mobs);
									}

									for (L1NpcInstance other : mobSnapshot) {
										if (other == mob || other.getMapId() != getMapId()) continue;
										Point opt = other.getLocation();
										String key = (opt.getX() / gridSize) + "," + (opt.getY() / gridSize);
										grid.computeIfAbsent(key, k -> new ArrayList<>()).add(opt);
									}


									Point candidate = null;
									for (int i = 0; i < 500; i++) {
										L1Location loc = new L1Location(pt, getMapId()).randomLocation(getSpawnHomeRange(), false);
										int gx = loc.getX() / gridSize;
										int gy = loc.getY() / gridSize;
										boolean tooClose = false;

										for (int dx = -1; dx <= 1; dx++) {
											for (int dy = -1; dy <= 1; dy++) {
												String key = (gx + dx) + "," + (gy + dy);
												List<Point> nearby = grid.getOrDefault(key, List.of());
												for (Point other : nearby) {
													if (other.getTileDistance(loc) < 8) {
														tooClose = true;
														break;
													}
												}
												if (tooClose) break;
											}
											if (tooClose) break;
										}

										if (!tooClose) {
											candidate = loc;
											break;
										}
									}

									if (candidate == null) {
										for (int i = 0; i < 50; i++) {
											L1Location attempt = new L1Location(pt, getMapId()).randomLocation(getSpawnHomeRange(), false);
											if (mob.getMap().isInMap(attempt) && mob.getMap().isPassable(attempt)) {
												candidate = attempt;
												break;
											}
										}
										// Final fallback if all 50 attempts failed
										if (candidate == null) {
											candidate = new L1Location(pt, getMapId()).randomLocation(getSpawnHomeRange(), false);
										}
									}
									newlocx = candidate.getX();
									newlocy = candidate.getY();
								}
							} else {
								int rangeX = getLocX2() - getLocX1();
								int rangeY = getLocY2() - getLocY1();
								newlocx = _random.nextInt(rangeX) + getLocX1();
								newlocy = _random.nextInt(rangeY) + getLocY1();
							}
							if (tryCount > 499) {
								for (int i = 0; i < 50; i++) {
									int x = ThreadLocalRandom.current().nextInt(getLocX1(), getLocX2() + 1);
									int y = ThreadLocalRandom.current().nextInt(getLocY1(), getLocY2() + 1);
									Point pt = new Point(x, y);
									if (mob.getMap().isInMap(pt) && mob.getMap().isPassable(pt)) {
										newlocx = x;
										newlocy = y;
										break;
									}
								}
							}

						} else if (isRandomSpawn()) {
							newlocx = getLocX() + (_random.nextInt(getRandomx() * 2 + 1) - getRandomx());
							newlocy = getLocY() + (_random.nextInt(getRandomy() * 2 + 1) - getRandomy());
						} else {
							newlocx = getLocX();
							newlocy = getLocY();
						}
				}

				mob.setX(newlocx);
				mob.setHomeX(newlocx);
				mob.setY(newlocy);
				mob.setHomeY(newlocy);

				if (mob.getMap().isInMap(mob.getLocation()) && mob.getMap().isPassable(mob.getLocation())) {
					if (mob instanceof L1MonsterInstance) {
						if (isRespawnScreen()) break;
						if (L1World.getInstance().getVisiblePlayer((L1MonsterInstance) mob).isEmpty()) break;
						GeneralThreadPool.getInstance().schedule(new SpawnTask(spawnNumber, mob.getId()), 3000L);
						return;
					}
				}
				tryCount++;
			}

			if (mob instanceof L1MonsterInstance) {
				((L1MonsterInstance) mob).initHide();
			}

			mob.setSpawn(this);
			mob.setreSpawn(true);
			mob.setSpawnNumber(spawnNumber);

			if (_initSpawn && _spawnHomePoint) {
				_homePoint.put(spawnNumber, new Point(mob.getX(), mob.getY()));
			}

			if (mob instanceof L1MonsterInstance && mob.getMapId() == 666) {
				((L1MonsterInstance) mob).set_storeDroped(true);
			}

			if (npcId == 45573 && mob.getMapId() == 2) {
				for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
					if (pc.getMapId() == 2) {
						L1Teleport.teleport(pc, 32664, 32797, (short) 2, 0, true);
					}
				}
			}

			if ((npcId == 46142 && mob.getMapId() == 73) || (npcId == 46141 && mob.getMapId() == 74)) {
				for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
					if (pc.getMapId() >= 72 && pc.getMapId() <= 74) {
						L1Teleport.teleport(pc, 32840, 32833, (short) 72, pc.getHeading(), true);
					}
				}
			}

			doCrystalCave(npcId);

			L1World.getInstance().storeObject(mob);
			L1World.getInstance().addVisibleObject(mob);

			if (mob instanceof L1MonsterInstance && !_initSpawn && ((L1MonsterInstance) mob).getHiddenStatus() == 0) {
				((L1MonsterInstance) mob).onNpcAI();
			}

			if (getGroupId() != 0) {
				L1MobGroupSpawn.getInstance().doSpawn(mob, getGroupId(), isRespawnScreen(), _initSpawn);
			}

			mob.turnOnOffLight();
			mob.startChat(L1NpcInstance.CHAT_TIMING_APPEARANCE);
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}



	public void setRest(boolean flag) {
		_rest = flag;
	}

	public boolean isRest() {
		return _rest;
	}

	// TODO private static final int SPAWN_TYPE_NORMAL = 0;
	private static final int SPAWN_TYPE_PC_AROUND = 1;

	private static final int PC_AROUND_DISTANCE = 30;

	private int getSpawnType() {
		return _spawnType;
	}

	public void setSpawnType(int type) {
		_spawnType = type;
	}

	private boolean isAreaSpawn() {
		return getLocX1() != 0 && getLocY1() != 0 && getLocX2() != 0
				&& getLocY2() != 0;
	}

	private boolean isRandomSpawn() {
		return getRandomx() != 0 || getRandomy() != 0;
	}

	public L1SpawnTime getTime() {
		return _time;
	}

	public void setTime(L1SpawnTime time) {
		_time = time;
	}

	@Override
	public void onMinuteChanged(L1GameTime time) {
		if (_time.getTimePeriod().includes(time)) {
			return;
		}
		synchronized (_mobs) {
			if (_mobs.isEmpty()) {
				return;
			}
			// 指定時間外になっていれば削除
			for (L1NpcInstance mob : _mobs) {
				mob.setCurrentHpDirect(0);
				mob.setDead(true);
				mob.setStatus(ActionCodes.ACTION_Die);
				mob.deleteMe();
			}
			_mobs.clear();
		}
	}

	public static void doCrystalCave(int npcId) {
		int[] npcId2 = { 46143, 46144, 46145, 46146, 46147, 46148, 46149,
				46150, 46151, 46152 };
		int[] doorId = { 5001, 5002, 5003, 5004, 5005, 5006, 5007, 5008, 5009,
				5010 };

		for (int i = 0; i < npcId2.length; i++) {
			if (npcId == npcId2[i]) {
				closeDoorInCrystalCave(doorId[i]);
			}
		}
	}

	private static void closeDoorInCrystalCave(int doorId) {
		for (L1Object object : L1World.getInstance().getObject()) {
			if (object instanceof L1DoorInstance) {
				L1DoorInstance door = (L1DoorInstance) object;
				if (door.getDoorId() == doorId) {
					door.close();
				}
			}
		}
	}
	private int _spawnHome;
	private int _spawnHomeRange;
	private int _spawnHomeCount;

	public int getSpawnHome() {
	    return _spawnHome;
	}

	public void setSpawnHome(int spawnHome) {
	    _spawnHome = spawnHome;
	}

	public int getSpawnHomeRange() {
	    return _spawnHomeRange;
	}

	public void setSpawnHomeRange(int range) {
	    _spawnHomeRange = range;
	}

	public int getSpawnHomeCount() {
	    return _spawnHomeCount;
	}

	public void setSpawnHomeCount(int count) {
	    _spawnHomeCount = count;
	}

}