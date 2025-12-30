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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1FourthOfJulyEvent;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1Quest;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.model.classes.L1ClassId;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Drop;
import l1j.server.server.utils.SQLUtil;

// Referenced classes of package l1j.server.server.templates:
// L1Npc, L1Item, ItemTable
public class DropTable {
	private static Logger _log = LoggerFactory.getLogger(DropTable.class.getName());
	private static DropTable _instance;
	private final HashMap<Integer, ArrayList<L1Drop>> _droplists;
	
	// Mobids that should bypass adjustChance algorithm
	private static final Set<Integer> ADJUST_CHANCE_EXCEPTIONS = new HashSet<>();
	
	static {
		// Load all boss npc_ids from spawnlist_boss table
		loadBossExceptions();
		
		// Add additional mobids that should use simple rate multiplication instead of adjustChance
		// Example: ADJUST_CHANCE_EXCEPTIONS.add(45001); // Some specific mob
		// TODO: Add mobids as needed
	}
	
	private static void loadBossExceptions() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT DISTINCT npc_id FROM spawnlist_boss");
			rs = pstm.executeQuery();
			int count = 0;
			while (rs.next()) {
				int npcId = rs.getInt("npc_id");
				ADJUST_CHANCE_EXCEPTIONS.add(npcId);
				count++;
			}
			_log.info("Loaded {} boss npc_ids as adjustChance exceptions", count);
		} catch (SQLException e) {
			_log.error("Failed to load boss exceptions: " + e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public static DropTable getInstance() {
		if (_instance == null) {
			_instance = new DropTable();
		}
		return _instance;
	}

	private static Map<Integer, String> _questDrops;

	private DropTable() {
		_droplists = allDropList();
		_questDrops = questDrops();
	}

	private Map<Integer, String> questDrops() {
		Map<Integer, String> questDropsMap = new HashMap<Integer, String>();
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("select * from quest_drops");
			rs = pstm.executeQuery();
			while (rs.next()) {
				questDropsMap.put(rs.getInt("item_id"), rs.getString("class"));
			}
		} catch (SQLException e) {
			_log.error(e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		return questDropsMap;
	}

    private HashMap<Integer, ArrayList<L1Drop>> allDropList() {
        HashMap<Integer, ArrayList<L1Drop>> droplistMap = new HashMap<>();
        try (Connection con = L1DatabaseFactory.getInstance().getConnection();
             PreparedStatement pstm = con.prepareStatement("SELECT * FROM droplist");
             ResultSet rs = pstm.executeQuery()) {

            while (rs.next()) {
                int mobId = rs.getInt("mobId");
                int itemId = rs.getInt("itemId");
                int min = rs.getInt("min");
                int max = rs.getInt("max");
                int chance = rs.getInt("chance");

                // Apply adjustChance only if mobId is not in exceptions
                if (!ADJUST_CHANCE_EXCEPTIONS.contains(mobId)) {
                    chance = (int) adjustChance(chance, Config.RATE_DROP_ITEMS);
                }
                // If mobId is in exceptions, chance remains original value
                // and will be affected by normal rate multipliers at runtime

                L1Drop drop = new L1Drop(mobId, itemId, min, max, chance);
                droplistMap.computeIfAbsent(mobId, k -> new ArrayList<>()).add(drop);
            }
        } catch (SQLException e) {
            _log.error(e.getLocalizedMessage(), e);
        }
        return droplistMap;
    }

    public static double adjustChance(double originalChance, double multiplier) {
													   
        double normalizedChance = originalChance / 1000000.0;

        if (normalizedChance >= 0.5) { // Common items
            return originalChance / multiplier;
        } else if (normalizedChance > 0.002) { // Uncommon items (0.2% to 50%)
            double factor = 1.0 + (multiplier - 1.0) * Math.sqrt((normalizedChance - 0.002) / 0.498);
            return originalChance / factor;
        }
        return originalChance; // Rare items (<= 0.2%) - Keep original chance
    }

    public void dropToGround(L1MonsterInstance mob) {
        try {
            int npcId = mob.getNpcTemplate().get_npcId();
            if (npcId == 45640 && mob.getTempCharGfx() != 2332) return;

            List<L1Drop> dropList = getDrops(npcId);
            if (dropList == null) return;

            L1Inventory groundInv = L1World.getInstance().getInventory(
                mob.getX(), mob.getY(), mob.getMapId());

            for (L1Drop drop : dropList) {
                if (ThreadLocalRandom.current().nextInt(1000000) < drop.getChance()) {
                    int count = drop.getMin() + ThreadLocalRandom.current().nextInt(drop.getMax() - drop.getMin() + 1);
                    if (count <= 0) continue;

                    L1ItemInstance item = ItemTable.getInstance().createItem(drop.getItemid());
                    item.setCount(count);
                    item.setIdentified(true);
                    groundInv.storeItem(item);
                }
            }
        } catch (Exception e) {
            _log.error("dropToGround failed for mob " + mob.getNpcId(), e);
        }
    }
    
	public void setDrop(L1NpcInstance npc, L1Inventory inventory) {
		int mobId = npc.getNpcTemplate().get_npcId();
		ArrayList<L1Drop> dropList = _droplists.get(mobId);
		if (dropList == null) {
			return;
		}

		double droprate = Config.RATE_DROP_ITEMS;
		if (droprate <= 0) {
			droprate = 0;
		}
		double adenarate = Config.RATE_DROP_ADENA;
		if (adenarate <= 0) {
			adenarate = 0;
		}
		if (droprate <= 0 && adenarate <= 0) {
			return;
		}

		int itemId;
		int itemCount;
		int addCount;
		int randomChance;
		L1ItemInstance item;
		new Random();

		for (L1Drop drop : dropList) {
			itemId = drop.getItemid();
			if (adenarate == 0 && itemId == L1ItemId.ADENA) {
				continue;
			}

			randomChance = ThreadLocalRandom.current().nextInt(0xf4240) + 1;
			double rateOfMapId = MapsTable.getInstance().getDropRate(
					npc.getMapId());
			double rateOfItem = DropItemTable.getInstance().getDropRate(itemId);
			if (droprate == 0
					|| drop.getChance() * droprate * rateOfMapId * rateOfItem < randomChance) {
				continue;
			}

			// Changed to prevent adena rates of >1 to always result in even
			// numbers
			double amount = DropItemTable.getInstance().getDropAmount(itemId);
			int min;
			int max;
			if (itemId == L1ItemId.ADENA) {
				min = (int) (drop.getMin() * amount * adenarate);
				max = (int) (drop.getMax() * amount * adenarate);
			} else {
				min = (int) (drop.getMin() * amount);
				max = (int) (drop.getMax() * amount);
			}
			itemCount = min;
			addCount = max - min + 1;
			if (addCount > 1) {
				itemCount += ThreadLocalRandom.current().nextInt(addCount);
			}
			if (itemCount < 0) {
				itemCount = 0;
			}
			if (itemCount > 2000000000) {
				itemCount = 2000000000;
			}
			item = ItemTable.getInstance().createItem(itemId);
			if (item == null) {
				_log.warn(String.format("DropTable::SetDrop: "
						+ "invalid item id %d for npc %d.", itemId, mobId));
				continue;
			}
			item.setCount(itemCount);
			inventory.storeItem(item);
		}
	}

	public void dropShare(L1NpcInstance npc, ArrayList<?> acquisitorList,
			ArrayList<?> hateList) {
		L1Inventory inventory = npc.getInventory();
		if (inventory.getSize() == 0) {
			return;
		}
		if (acquisitorList.size() != hateList.size()) {
			return;
		}
		int totalHate = 0;
		L1Character acquisitor;
		for (int i = hateList.size() - 1; i >= 0; i--) {
			acquisitor = (L1Character) acquisitorList.get(i);
			if ((Config.AUTO_LOOT == 2)
					&& (acquisitor instanceof L1SummonInstance || acquisitor instanceof L1PetInstance)) {
				acquisitorList.remove(i);
				hateList.remove(i);
			} else if (acquisitor != null
					&& !acquisitor.isDead() // added
					&& acquisitor.getMapId() == npc.getMapId()
					&& acquisitor.getLocation().getTileLineDistance(
							npc.getLocation()) <= Config.LOOTING_RANGE) {
				totalHate += (Integer) hateList.get(i);
			} else {
				acquisitorList.remove(i);
				hateList.remove(i);
			}
		}
		L1ItemInstance item;
		L1Inventory targetInventory = null;
		L1PcInstance player;
		L1PcInstance[] partyMember;
		new Random();
		int randomInt;
		int chanceHate;
		int itemId;
		for (int i = inventory.getSize(); i > 0; i--) {
			item = inventory.getItems().get(0);
			itemId = item.getItemId();
			boolean isGround = false;
			if (item.getItem().getType2() == 0 && item.getItem().getType() == 2) {
				item.setNowLighting(false);
			}
			item.setIdentified(false); // changed
			if (((Config.AUTO_LOOT != 0) || itemId == L1ItemId.ADENA)
					&& totalHate > 0) {
				randomInt = ThreadLocalRandom.current().nextInt(totalHate);
				chanceHate = 0;
				for (int j = hateList.size() - 1; j >= 0; j--) {
					chanceHate += (Integer) hateList.get(j);
					if (chanceHate > randomInt) {
						acquisitor = (L1Character) acquisitorList.get(j);
						if (itemId >= 40131 && itemId <= 40135) {
							if (!(acquisitor instanceof L1PcInstance)
									|| hateList.size() > 1) {
								targetInventory = null;
								break;
							}
							player = (L1PcInstance) acquisitor;
							if (player.getQuest().get_step(L1Quest.QUEST_LYRA) != 1) {
								targetInventory = null;
								break;
							}
						}
						if (itemId == 41422 || itemId == 40586) { // royal 45q items that shouldn't drop when partied or with pets
							//there should probably be more itemIDs here. TODO: find out which quests have this req and whether summons are/aren't allowed
							
							// since pets/summons can't pick up items, check to see if the person getting the item is a pc or not
							if(!(acquisitor instanceof L1PcInstance)) {
								inventory.deleteItem(item);
								break;
							}
							
							player = (L1PcInstance) acquisitor;
							if (player.isInParty() || !player.getPetList().isEmpty()) { // no summon check for royal, ignoring dolls for now. thx tricid
								inventory.deleteItem(item);
								break;
							}
						}
						L1PcInstance owner = null;
						int npcId = npc.getNpcTemplate().get_npcId();

						if (acquisitor instanceof L1PcInstance) {
						    owner = (L1PcInstance) acquisitor;
						} else if (acquisitor instanceof L1PetInstance || acquisitor instanceof L1SummonInstance) {
						    L1NpcInstance petOrSummon = (L1NpcInstance) acquisitor;
						    if (petOrSummon.getMaster() instanceof L1PcInstance) {
						        owner = (L1PcInstance) petOrSummon.getMaster();
						    }
						}

						if (owner != null) {
						    // 🎯 Kill count tracking (if pet/summon kills should count)
						    if (itemId == L1ItemId.ADENA) {
						        if (npcId == 101008) {
						            L1FourthOfJulyEvent.addEventKillCountMage();
						        }
						        if (npcId == 101007) {
						            L1FourthOfJulyEvent.addEventKillCountOther();
						        }
						    }

						    // 📢 Rare item announcement
						    if ((npcId == 101007 || npcId == 101008)
						        && getChance(npcId, item.getItemId()) < 500
						        && !item.isGivenToNpc()) {

						        String itemName = item.getItem().getName();
						        String monsterName = npc.getName();
						        String msg = "A player has received a rare item: " + itemName + " from " + monsterName + "! Happy 4th of July!";
						        for (L1PcInstance onlinePlayer : L1World.getInstance().getAllPlayers()) {
						            onlinePlayer.sendPackets(new S_SystemMessage(msg));
						        }
						    }

						 // List of rare item IDs
						    Set<Integer> rareItemIds = Set.of(
						        84, 108, 110, 111, 117, 124, 163, 164, 20017, 20018, 20022, 20025, 20029, 20040, 20049, 20050,
						        20057, 20074, 20077, 20116, 20169, 20200, 20202, 20204, 20218, 20253, 20255, 20279, 20314, 20317,
						        21093, 21094, 21095, 40219, 40222, 40223, 40224, 40393, 40394, 40395, 40396, 41148, 41149, 54, 58
						    );

						    // Rare drop announcement for any monster
						    if (rareItemIds.contains(item.getItemId()) && !item.isGivenToNpc()) {
						        String itemName = item.getItem().getName();
						        String monsterName = npc.getName();
						        String msg = "A player has received a rare item: " + itemName + " from " + monsterName + "!";
						        for (L1PcInstance onlinePlayer : L1World.getInstance().getAllPlayers()) {
						            onlinePlayer.sendPackets(new S_SystemMessage(msg));
						        }
						    }
						    
						    // ✅ Class check for quest items
						    if (_questDrops.containsKey(item.getItemId())) {
						        if (!L1ClassId.classCode(owner).equals(_questDrops.get(item.getItemId()))) {
						            inventory.deleteItem(item);
						            break;
						        }
						    }

						    // 💰 Adena overflow protection
						    L1ItemInstance adena = owner.getInventory().findItemId(L1ItemId.ADENA);
						    if (adena != null && adena.getCount() > 2000000000) {
						        targetInventory = L1World.getInstance().getInventory(acquisitor.getX(), acquisitor.getY(), acquisitor.getMapId());
						        isGround = true;
						        owner.sendPackets(new S_ServerMessage(166, "The limit of the itemcount is 2000000000"));
						    } else {
						        if (owner.isInParty()) {
						            for (L1PcInstance member : owner.getParty().getMembers()) {
						                if (member.getPartyDropMessages()) {
						                    member.sendPackets(new S_ServerMessage(813, npc.getName(), item.getLogName(), owner.getName()));
						                }
						            }
						        } else {
						            if (owner.getDropMessages()) {
						                owner.sendPackets(new S_ServerMessage(143, npc.getName(), item.getLogName()));
						            }
						        }
						    }

						    if (!isGround && owner != null && targetInventory == null) {
						        targetInventory = owner.getInventory(); // ✅ this is what you're missing
						    }
						} else {
							targetInventory = L1World.getInstance()
									.getInventory(acquisitor.getX(),
											acquisitor.getY(),
											acquisitor.getMapId());
							isGround = true;
						}
						break;
					}
				}
			} else {
				List<Integer> dirList = new ArrayList<Integer>();
				for (int j = 0; j < 8; j++) {
					dirList.add(j);
				}
				int x = 0;
				int y = 0;
				int dir = 0;
				do {
					if (dirList.size() == 0) {
						x = 0;
						y = 0;
						break;
					}
					randomInt = ThreadLocalRandom.current().nextInt(dirList.size());
					dir = dirList.get(randomInt);
					dirList.remove(randomInt);
					switch (dir) {
					case 0:
						x = 0;
						y = -1;
						break;
					case 1:
						x = 1;
						y = -1;
						break;
					case 2:
						x = 1;
						y = 0;
						break;
					case 3:
						x = 1;
						y = 1;
						break;
					case 4:
						x = 0;
						y = 1;
						break;
					case 5:
						x = -1;
						y = 1;
						break;
					case 6:
						x = -1;
						y = 0;
						break;
					case 7:
						x = -1;
						y = -1;
						break;
					}
				} while (!npc.getMap().isPassable(npc.getX(), npc.getY(), dir));
				targetInventory = L1World.getInstance().getInventory(
						npc.getX() + x, npc.getY() + y, npc.getMapId());
				isGround = true;
			}
			if (itemId >= 40131 && itemId <= 40135) {
				if (isGround || targetInventory == null) {
					inventory.removeItem(item, item.getCount());
					continue;
				}
			}
			if (item != null) {
				inventory.tradeItem(item, item.getCount(), targetInventory);
			}
		}
		npc.turnOnOffLight();
	}

	public List<L1Drop> getDrops(int mobID) {// New for GMCommands
		return _droplists.get(mobID);
	}
	
	public int getChance(int mobId, int itemId) {
	    ArrayList<L1Drop> drops = _droplists.get(mobId);
	    if (drops == null) {
	        return -1; // Mob not found
	    }

	    for (L1Drop drop : drops) {
	        if (drop.getItemid() == itemId) {
	            return drop.getChance(); // Found exact match
	        }
	    }

	    return -1; // Item not found for this mob
	}

}
