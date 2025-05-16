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
package l1j.server.server.model.Instance;

import java.util.List;

import l1j.server.server.ActionCodes;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.datatables.ClanTable;
import l1j.server.server.datatables.DoorTable;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.encryptions.IdFactory;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1War;
import l1j.server.server.model.L1WarSpawn;
import l1j.server.server.model.L1World;
import l1j.server.server.serverpackets.S_CastleMaster;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.utils.L1SpawnUtil;

public class L1CrownInstance extends L1NpcInstance {

	private static final long serialVersionUID = 1L;

	public L1CrownInstance(L1Npc template) {
		super(template);
	}

	@Override
	public void onAction(L1PcInstance player) {
		boolean in_war = false;
		System.out.println("Crown capture attempt by: " + player.getName());
		System.out.println("ClanId: " + player.getClanid());
		System.out.println("IsCrown: " + player.isCrown());
		System.out.println("TempCharGfx: " + player.getTempCharGfx());
		System.out.println("CheckRange: " + checkRange(player));

		if (player.getClanid() == 0) {
			return;
		}
		String playerClanName = player.getClanname();
		L1Clan clan = L1World.getInstance().getClan(playerClanName);

		System.out.println("PlayerId: " + player.getId() + " vs LeaderId: " + clan.getLeaderId());
		System.out.println("Clan has castle: " + clan.getCastleId());
		if (clan == null) {
			return;
		}
		//Old Siege Code
		//if (!player.isCrown()) {
		//	return;
		//}
		//if (player.getTempCharGfx() != 0 && player.getTempCharGfx() != 1) {
		//	return;
		//}
		//if (player.getId() != clan.getLeaderId()) {
		//	return;
		//}
		// REMOVE class/graphic/leader checks — allow any clan member
		if (!checkRange(player)) {
			return;
		}
		//if (clan.getCastleId() != 0) {
		//	player.sendPackets(new S_ServerMessage(474));
		//	return;
		//}

		int castle_id = L1CastleLocation
				.getCastleId(getX(), getY(), getMapId());

		boolean existDefenseClan = false;
		L1Clan defence_clan = null;
		for (L1Clan defClan : L1World.getInstance().getAllClans()) {
			if (castle_id == defClan.getCastleId()) {
				defence_clan = L1World.getInstance().getClan(
						defClan.getClanName());
				existDefenseClan = true;
				break;
			}
		}

		System.out.println("ExistDefenseClan: " + existDefenseClan + ", InWar: " + in_war);
		List<L1War> wars = L1World.getInstance().getWarList();
		for (L1War war : wars) {
			if (castle_id == war.GetCastleId()) {
				in_war = war.CheckClanInWar(playerClanName);
				break;
			}
		}
		if (existDefenseClan && in_war == false) {
			return;
		}
		if (existDefenseClan && defence_clan != null) {
			defence_clan.setCastleId(0);
			ClanTable.getInstance().updateClan(defence_clan);
			L1PcInstance defence_clan_member[] = defence_clan
					.getOnlineClanMember();
			for (int m = 0; m < defence_clan_member.length; m++) {
				if (defence_clan_member[m].getId() == defence_clan
						.getLeaderId()) {
					defence_clan_member[m].sendPackets(new S_CastleMaster(0,
							defence_clan_member[m].getId()));
					defence_clan_member[m].broadcastPacket(new S_CastleMaster(
							0, defence_clan_member[m].getId()));
					break;
				}
			}
		}
		//clan.setCastleId(castle_id);
		//ClanTable.getInstance().updateClan(clan);
		//player.sendPackets(new S_CastleMaster(castle_id, player.getId()));
		//player.broadcastPacket(new S_CastleMaster(castle_id, player.getId()));

		//int[] loc = new int[3];
		//Old Castle Logic - Teleport players out on capture crown
		//
		//for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
		//	if (pc.getClanid() != player.getClanid() && !pc.isGm()) {
		//
		//		if (L1CastleLocation.checkInWarArea(castle_id, pc)) {
		//			loc = L1CastleLocation.getGetBackLoc(castle_id);
		//			int locx = loc[0];
		//			int locy = loc[1];
		//			short mapid = (short) loc[2];
		//			L1Teleport.teleport(pc, locx, locy, mapid, 5, true);
		//		}
		//	}
		//}

		//for (L1War war : wars) {
		//	if (war.CheckClanInWar(playerClanName) && existDefenseClan) {
		//		war.WinCastleWar(playerClanName);
		//		break;
		//	}
		//}

		L1PcInstance[] clanMember = clan.getOnlineClanMember();

		if (clanMember.length > 0) {
			S_ServerMessage s_serverMessage = new S_ServerMessage(643);
			for (L1PcInstance pc : clanMember) {
				pc.sendPackets(s_serverMessage);
			}
		}
		
		// Replace glow NPCs under towers for new owner
		int team = 7; // default to black
		for (L1War war : wars) {
		    if (war.CheckClanInWar(playerClanName)) {
		        team = war.getClanTeam(playerClanName);
		        break;
		    }
		}

		int towerId = getHeineTowerIdAtLocation(getX(), getY(), getMapId());	
		
		// Announce globally
		String towerName = getHeineTowerName(towerId);
		String msg = towerName + " has been captured by " + playerClanName + "!";
		L1World.getInstance().broadcastServerMessage(msg);

		// Delete the crown
		deleteMe();

		// Kill old glow NPC at this tower (play death animation)
		for (L1Object obj : L1World.getInstance().getObject()) {
		    if (obj instanceof L1NpcInstance) {
		        L1NpcInstance npc = (L1NpcInstance) obj;
		        if (isGlowNpc(npc.getNpcTemplate().get_npcId()) &&
		            npc.getX() == getTowerSpawnX(towerId) &&
		            npc.getY() == getTowerSpawnY(towerId) &&
		            npc.getMapId() == getTowerMapId(towerId)) {

		            // Simulate a death animation and schedule cleanup
		            npc.setCurrentHp(0);
		            npc.setDead(true);
		            npc.setStatus(ActionCodes.ACTION_Die);
		            npc.broadcastPacket(new S_DoActionGFX(npc.getId(), ActionCodes.ACTION_Die));
		            npc.allTargetClear();

		            npc.startDeleteTimer(); // cleanly removes after a delay (default ~5s)
		        }
		    }
		}

		// Spawn new glow immediately
		spawnGlowNpcForTower(towerId, team, player);
		GeneralThreadPool.getInstance().schedule(() -> {
		    for (L1Object l1object : L1World.getInstance().getObject()) {
		        if (l1object instanceof L1TowerInstance) {
		            L1TowerInstance oldTower = (L1TowerInstance) l1object;
		            if (oldTower.getX() == getTowerSpawnX(towerId) &&
		                oldTower.getY() == getTowerSpawnY(towerId) &&
		                oldTower.getMapId() == getTowerMapId(towerId)) {

		                int npcId = 81111; // Heine tower NPC
		                L1Npc l1npc = NpcTable.getInstance().getTemplate(npcId);

		                // 1️⃣ Spawn new tower first
		                SpawnWarObject(l1npc, getTowerSpawnX(towerId), getTowerSpawnY(towerId), getTowerMapId(towerId));

		                // 2️⃣ Then delete the old one (this avoids the visible gap)
		                oldTower.deleteMe();
		            }
		        }
		    }
		}, 5000);
	}
	
	private void SpawnWarObject(L1Npc npc, int x, int y, short mapId) {
	    try {
	        L1NpcInstance tower = NpcTable.getInstance().newNpcInstance(npc.get_npcId());
	        tower.setId(IdFactory.getInstance().nextId());
	        tower.setMap(mapId);
	        tower.setX(x);
	        tower.setY(y);
	        tower.setHomeX(x);
	        tower.setHomeY(y);
	        tower.setHeading(0);
	        L1World.getInstance().storeObject(tower);
	        L1World.getInstance().addVisibleObject(tower);
	    } catch (Exception e) {
	        _log.error(e.getLocalizedMessage(), e);
	    }
	}
	
	private String getHeineTowerName(int towerId) {
	    if (towerId == 1) return "Maphr Tower";
	    if (towerId == 2) return "Eva Tower";
	    if (towerId == 3) return "Sayha Tower";
	    return "Unknown Tower";
	}

	private int getHeineTowerIdAtLocation(int x, int y, short mapId) {
	    if (x == 33524 && y == 33396 && mapId == 4) return 1;
	    if (x == 33547 && y == 33398 && mapId == 4) return 2;
	    if (x == 33497 && y == 33398 && mapId == 4) return 3;
	    return -1;
	}
	
	private int getTowerSpawnX(int towerId) {
	    if (towerId == 1) return 33524;
	    if (towerId == 2) return 33547;
	    if (towerId == 3) return 33497;
	    return 0;
	}

	private int getTowerSpawnY(int towerId) {
	    if (towerId == 1) return 33396;
	    if (towerId == 2) return 33398;
	    if (towerId == 3) return 33398;
	    return 0;
	}

	private short getTowerMapId(int towerId) {
	    return 4;
	}

	
	private boolean isGlowNpc(int npcId) {
	    return npcId == 101000 || npcId == 101001 || npcId == 101002 || npcId == 101003 || npcId == 101004;
	}
	private void spawnGlowNpcForTower(int towerId, int team, L1PcInstance player) {
	    int[] glowNpcIds;

	    switch (team) {
	        case 0: glowNpcIds = new int[]{101000}; break; // Orange
	        case 1: glowNpcIds = new int[]{101001}; break; // Green
	        case 2: glowNpcIds = new int[]{101002}; break; // Yellow
	        case 3: glowNpcIds = new int[]{101003}; break; // Blue
	        case 4: glowNpcIds = new int[]{101003, 101000}; break; // Blue + Orange
	        case 5: glowNpcIds = new int[]{101003, 101001}; break; // Blue + Green
	        case 6: glowNpcIds = new int[]{101003, 101002}; break; // Blue + Yellow
	        case 7: glowNpcIds = new int[]{101004}; break; // Black
	        default: glowNpcIds = new int[]{101004}; break; // fallback to Black
	    }

	    int spawnX = 0;
	    int spawnY = 0;
	    short mapId = 4; // Heine map

	    // Set X/Y based on towerId
	    if (towerId == 1) {
	        int[] loc = L1CastleLocation.getHeineTower1Loc();
	        spawnX = loc[0];
	        spawnY = loc[1];
	        mapId = (short) loc[2];
	    } else if (towerId == 2) {
	        int[] loc = L1CastleLocation.getHeineTower2Loc();
	        spawnX = loc[0];
	        spawnY = loc[1];
	        mapId = (short) loc[2];
	    } else if (towerId == 3) {
	        int[] loc = L1CastleLocation.getHeineTower3Loc();
	        spawnX = loc[0];
	        spawnY = loc[1];
	        mapId = (short) loc[2];
	    }

	    for (int npcId : glowNpcIds) {
	        L1SpawnUtil.spawn(player, npcId, spawnX, spawnY, 0); // 0 = don’t auto-delete
	    }
	}

	
	@Override
	public void deleteMe() {
		_destroyed = true;
		if (getInventory() != null) {
			getInventory().clearItems();
		}
		allTargetClear();
		_master = null;
		L1World.getInstance().removeVisibleObject(this);
		L1World.getInstance().removeObject(this);
		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(this)) {
			pc.removeKnownObject(this);
			pc.sendPackets(new S_RemoveObject(this));
		}
		removeAllKnownObjects();
	}

	private boolean checkRange(L1PcInstance pc) {
		return (getX() - 1 <= pc.getX() && pc.getX() <= getX() + 1
				&& getY() - 1 <= pc.getY() && pc.getY() <= getY() + 1);
	}
}
