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
package l1j.server.server.model.map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.Config;
import l1j.server.server.utils.PerformanceTimer;

public class L1WorldMap {
	private static Logger _log = Logger.getLogger(L1WorldMap.class.getName());

	private static L1WorldMap _instance;
	private Map<Integer, L1Map> _maps;

	public static L1WorldMap getInstance() {
		if (_instance == null) {
			_instance = new L1WorldMap();
		}
		return _instance;
	}

	private L1WorldMap() {

		try {
			PerformanceTimer timer = new PerformanceTimer();
			_maps = MapReader.getDefaultReader().read();
			if (_maps == null) {
				throw new RuntimeException("マップの読み込みに失敗");
			}
			System.out.println("loading maps...OK! " + timer.elapsedTimeMillis() + "ms");
		} catch (FileNotFoundException e) {
			handleException(e, "maps.zipを展開し忘れていませんか？");
		} catch (Exception e) {
			handleException(e, null);
		}
	}

	private void handleException(Exception e, String hint) {
		System.out.println("ERROR!");
		_log.log(Level.SEVERE, e.getLocalizedMessage(), e);

		if (hint != null) {
			System.out.println("Hint: " + hint);
		}

		System.exit(0);
	}

	/**
	 * 指定されたマップの情報を保持するL1Mapを返す。
	 * 
	 * @param mapId
	 *            マップID
	 * @return マップ情報を保持する、L1Mapオブジェクト。
	 */
	public L1Map getMap(short mapId) {
		L1Map map = _maps.get((int) mapId);
		if (map == null) { // マップ情報が無い
			map = L1Map.newNull(); // 何もしないMapを返す。
		}
		return map;
	}

	/**
	 * 指定されたマップを新たに追加する
	 * 
	 * @param map
	 */
	public synchronized void addMap(L1Map map) {
		_maps.put(map.getId(), map);
	}

	/**
	 * 指定されたマップを削除する
	 * 
	 * @param mapId
	 */
	public synchronized void removeMap(int mapId) {
		_maps.remove(mapId);
	}
}