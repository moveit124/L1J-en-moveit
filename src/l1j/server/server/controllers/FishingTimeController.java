/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package l1j.server.server.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import l1j.server.server.random.RandomGenerator;
import l1j.server.server.random.RandomGeneratorFactory;

import l1j.server.server.datatables.ItemTable;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_CharVisualUpdate;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_ServerMessage;

public class FishingTimeController implements Runnable {
	private static Logger _log = Logger.getLogger(FishingTimeController.class.getName());

	private static FishingTimeController _instance;
	private final List<L1PcInstance> _fishingList = new ArrayList<L1PcInstance>();
	
	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	public static FishingTimeController getInstance() {
		if (_instance == null) {
			_instance = new FishingTimeController();
		}
		return _instance;
	}

	@Override
	public void run() {
		try {
			while (true) {
				Thread.sleep(300);
				fishing();
			}
		} catch (Exception e1) {
		}
	}

	public void addMember(L1PcInstance pc) {
		if (pc == null || _fishingList.contains(pc)) {
			return;
		}
		_fishingList.add(pc);
	}

	public void removeMember(L1PcInstance pc) {
		if (pc == null || !_fishingList.contains(pc)) {
			return;
		}
		_fishingList.remove(pc);
	}

	private void fishing() {
		if (_fishingList.size() > 0) {
			long currentTime = System.currentTimeMillis();
			for (int i = 0; i < _fishingList.size(); i++) {
				L1PcInstance pc = _fishingList.get(i);
				if (pc.isFishing()) {
					long time = pc.getFishingTime();
					if (currentTime <= (time + 500) && currentTime >= (time - 500)
							&& !pc.isFishingReady()) {
						pc.setFishingReady(true);
						finishFishing(pc);
					}
				}
			}
		}
	}

	private void finishFishing(L1PcInstance pc) {
		int chance = _random.nextInt(300) + 1; // Adjusted to 1-300 range

		if (chance < 75) {
		    successFishing(pc, 41298); // 25%
		} else if (chance < 98) {
		    successFishing(pc, 41300); // 7.5%
		} else if (chance < 121) {
		    successFishing(pc, 41299); // 7.5%
		} else if (chance < 136) {
		    successFishing(pc, 41296); // 5%
		} else if (chance < 151) {
		    successFishing(pc, 41297); // 5%
		} else if (chance < 159) {
		    successFishing(pc, 41301); // 2.5%
		} else if (chance < 167) {
		    successFishing(pc, 41302); // 2.5%
		} else if (chance < 175) {
		    successFishing(pc, 41303); // 2.5%
		} else if (chance < 183) {
		    successFishing(pc, 41304); // 2.5%
		} else if (chance < 188) {
		    successFishing(pc, 41306); // 1.5%
		} else if (chance < 193) {
		    successFishing(pc, 41307); // 1.5%
		} else if (chance < 198) {
		    successFishing(pc, 41305); // 1.5%
		} else if (chance < 206) {
		    successFishing(pc, 21051); // 2.5%
		} else if (chance < 214) {
		    successFishing(pc, 21052); // 2.5%
		} else if (chance < 222) {
		    successFishing(pc, 21053); // 2.5%
		} else if (chance < 237) {
		    successFishing(pc, 21054); // 5%
		} else if (chance < 240) {
		    successFishing(pc, 41294); // 1.0%
		} else if (chance < 248) {
		    successFishing(pc, 21055); // 2.5%
		} else if (chance < 256) {
		    successFishing(pc, 21056); // 2.5%
		} else if (chance < 257) {
		    successFishing(pc, 41252); // 0.33%
		} else {
			if (pc.isFishingReady()) {
				restartFishing(pc);
			}
		}
	}

	private void successFishing(L1PcInstance pc, int itemId) {
		L1ItemInstance item = ItemTable.getInstance().createItem(itemId);
		if (item != null) {
			pc.sendPackets(new S_ServerMessage(403, item.getItem().getName()));
			pc.sendPackets(new S_OwnCharStatus(pc));
			item.setCount(1);
			if (pc.getInventory().checkAddItem(item, 1) == L1Inventory.OK) {
				pc.getInventory().storeItem(item);
			} else { // インベントリが一杯の場合
				stopFishing(pc);
				item.startItemOwnerTimer(pc);
				L1World.getInstance().getInventory(pc.getX(), pc.getY(), pc.getMapId()).storeItem(item);
				return;
			}
		} else {
			pc.sendPackets(new S_ServerMessage(1136)); // 釣りに失敗しました。
			stopFishing(pc);
			return;
		}

		if (pc.isFishingReady()) {
			if (itemId == 41294) {
				pc.sendPackets(new S_ServerMessage(1136));  // Failed to catch a fish.
				stopFishing(pc);
				return;
			}
			restartFishing(pc);
		}
	}

	private void restartFishing(L1PcInstance pc) {
		if (pc.getInventory().consumeItem(41295, 1)) {
			long fishTime = System.currentTimeMillis() + 10000 + _random.nextInt(5) * 1000;
			pc.setFishingTime(fishTime);
			pc.setFishingReady(false);
		} else {
			pc.sendPackets(new S_ServerMessage(1137)); // 釣りをするためにはエサが必要です。
			stopFishing(pc);
		}
	}

	private void stopFishing(L1PcInstance pc) {
		pc.setFishingTime(0);
		pc.setFishingReady(false);
		pc.setFishing(false);
		pc.sendPackets(new S_CharVisualUpdate(pc));
		pc.broadcastPacket(new S_CharVisualUpdate(pc));
		FishingTimeController.getInstance().removeMember(pc);
	}
}