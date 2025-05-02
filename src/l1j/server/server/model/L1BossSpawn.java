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

import java.util.Calendar;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.Config;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.templates.L1Npc;

import java.util.HashSet;
import java.util.Set;

public class L1BossSpawn extends L1Spawn {
    private static final Set<Integer> BOSS_NPC_IDS = new HashSet<>();
	private static Logger _log = LoggerFactory.getLogger(L1BossSpawn.class.getName());
	private String _cycleType;
	private int _percentage;
	private L1BossCycle _cycle;
	private int _spawnCount;
	private Calendar _activeSpawnTime;
	private static Random _rnd = new Random();
	private int _spawnNumber;
	private int _objectId;
    static {
        // Add all boss NPC IDs from your list
        int[] bossIds = {
            45668, 45650, 45513, 90009, 45675, 45752, 45606, 45684, 45640, 45640, 45640, 45313, 
            46123, 46124, 45666, 45612, 45685, 45956, 45960, 45961, 45955, 45958, 45959, 45957, 
            45962, 45941, 45931, 45458, 45795, 45648, 45547, 45228, 45963, 45772, 45617, 45574,
            46026, 45534, 45535, 45802, 45681, 45672, 45600, 45492, 90010, 45618, 45680, 45943,
            45548, 45711, 45711, 45711, 45711, 45711, 45711, 45711, 45654, 45516, 45516, 45516, 
            45516, 45609, 46141, 46142, 45673, 45584, 45644, 45644, 45644, 45643, 45643, 45643, 
            45645, 45645, 45645, 45642, 45642, 45642, 45734, 45944, 45916, 45683, 45923, 45923, 
            45923, 45607, 45602, 81081, 81081, 90005, 45863, 45677, 45651, 45844, 45529, 45529, 
            45529, 45529, 45546, 45588, 45801, 45649, 45601, 45674, 45545, 45545, 45935, 45942, 
            90004, 45338, 46025, 45652, 45735, 45625, 45488, 45577, 45583, 45829, 45573, 45370, 
            45370, 45370, 45370, 45370, 90003, 45585, 45615, 45676, 45682, 45614, 45610, 45646, 
            45671, 45653, 45497, 45473
        };
        for (int id : bossIds) {
            BOSS_NPC_IDS.add(id);
        }
    }

    public static boolean isBoss(int npcId) {
        return BOSS_NPC_IDS.contains(npcId);
    }

	private class SpawnTask implements Runnable {

		private String originalThreadName;

		private SpawnTask(int spawnNumber, int objectId) {
			_spawnNumber = spawnNumber;
			_objectId = objectId;
		}

		@Override
		public void run() {
			try {
				originalThreadName = Thread.currentThread().getName();
				Thread.currentThread().setName("L1BossSpawn");
				doSpawn(_spawnNumber, _objectId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				_log.error("",e);
			} finally {
				Thread.currentThread().setName(originalThreadName);
			}
		}
	}

	public L1BossSpawn(L1Npc mobTemplate) {
		super(mobTemplate);
	}

	/**
	 * SpawnTask To start.
	 * 
	 * @param spawnNumber
	 *            L1Spawn Which is administered by the numbers. Home to specify
	 *            what point does not exist and it's good.
	 */
	@Override
	public void executeSpawnTask(int spawnNumber, int objectId) {
		// count Decrement to check the entire death
		if (subAndGetCount() != 0) {
			return; // Not all dead
		}
		// Last appearance time that the next time to calculate the emergence
		Calendar spawnTime;
		Calendar now = Calendar.getInstance();
		Calendar latestStart = _cycle.getLatestStartTime(now);
		Calendar activeStart = _cycle.getSpawnStartTime(_activeSpawnTime);
		// Period of time and was active in the recent period started started
		// the same time, the emergence of
		if (!activeStart.before(latestStart)) {
			spawnTime = calcNextSpawnTime(activeStart);
		} else {
			// If different from the one before the emergence of a period of
			latestStart.add(Calendar.SECOND, -1);
			spawnTime = calcNextSpawnTime(_cycle
					.getLatestStartTime(latestStart));
		}
		spawnBoss(spawnTime, objectId);
	}

	private synchronized int subAndGetCount() {
		return --_spawnCount;
	}

	public void setCycleType(String type) {
		_cycleType = type;
	}

	public void setPercentage(int percentage) {
		_percentage = percentage;
	}
	

	@Override
	public void init() {
		if (_percentage <= 0) {
			return;
		}
		_cycle = L1BossCycle.getBossCycle(_cycleType);
		if (_cycle == null) {
			throw new RuntimeException(_cycleType + " not found");
		}
		Calendar now = Calendar.getInstance();
		// Appearance time
		Calendar spawnTime;
		if (Config.INIT_BOSS_SPAWN && _percentage > _rnd.nextInt(100)) {
			spawnTime = _cycle.calcSpawnTime(now);

		} else {
			spawnTime = calcNextSpawnTime(now);
		}
		spawnBoss(spawnTime, 0);
	}

	// Next calculate the probability of the emergence of time to calculate
	private Calendar calcNextSpawnTime(Calendar cal) {
		do {
			cal = _cycle.nextSpawnTime(cal);
		} while (!(_percentage > _rnd.nextInt(100)));
		return cal;
	}

	// The boss at the time specified in the emergence of schedule
	private void spawnBoss(Calendar spawnTime, int objectId) {
		// The emergence of time to keep. When re-emergence.
		_activeSpawnTime = spawnTime;
		long delay = spawnTime.getTimeInMillis() - System.currentTimeMillis();
		if (Config.RANDOMIZE_BOSS_SPAWNS) {
			double adjustment = _rnd.nextGaussian();
			// Bound the possible values to keep the boss spawns reasonable.
			if (adjustment > 2.5)
				adjustment = 2.5;
			else if (adjustment < -2.5)
				adjustment = -2.5;
			delay = delay
					+ (long) (delay * adjustment * Config.RANDOMIZED_BOSS_SPAWN_FACTOR);
		}
		int cnt = _spawnCount;
		_spawnCount = getAmount();
		while (cnt < getAmount()) {
			cnt++;
			GeneralThreadPool.getInstance().schedule(
					new SpawnTask(0, objectId), delay);
		}
		_log.trace(toString());
	}

	/**
	 * The boss of the current active period of time and appearance,
	 * respectively.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[MOB]npcid:" + getNpcId());
		builder.append(" name:" + getName());
		builder.append("[Type]" + _cycle.getName());
		builder.append("[The current cycle]");
		builder.append(_cycle.getSpawnStartTime(_activeSpawnTime).getTime());
		builder.append(" - ");
		builder.append(_cycle.getSpawnEndTime(_activeSpawnTime).getTime());
		builder.append("[Appearance time]");
		builder.append(_activeSpawnTime.getTime());
		return builder.toString();
	}
}
