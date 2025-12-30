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

import l1j.server.server.ActionCodes;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.controllers.WarTimeController;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.encryptions.IdFactory;
import l1j.server.server.model.L1Attack;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1War;
import l1j.server.server.model.L1WarSpawn;
import l1j.server.server.model.L1World;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_NPCPack;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.utils.L1SpawnUtil;

public class L1TowerInstance extends L1NpcInstance {
	private static final long serialVersionUID = 1L;

	public L1TowerInstance(L1Npc template) {
		super(template);
	}

	private L1Character _lastattacker;
	private int _castle_id;
	private int _crackStatus;
	
	private boolean _regenBuffActive = false;
	private long _lastRegenTick = 0;

	public static final int REGEN_RADIUS = 4;
	private static final int REGEN_INTERVAL_MS = 1000;
	private static final double REGEN_PERCENT = 0.03;

	private static final int HIDDEN_X = 33333;
	private static final int HIDDEN_Y = 33333;
	private static final short HIDDEN_MAP = 3;
	private static final int REGEN_NPC_ID = 101006;

	// 0 = tower 1, 1 = tower 2, 2 = tower 3
	private static final L1NpcInstance[] _regenNpcs = new L1NpcInstance[3];

	public static void initializeRegenNpcs() {
		for (int i = 0; i < _regenNpcs.length; i++) {
			try {
				
				L1NpcInstance npc = NpcTable.getInstance().newNpcInstance(REGEN_NPC_ID);
				npc.setId(IdFactory.getInstance().nextId());
				npc.setX(HIDDEN_X + i);
				npc.setY(HIDDEN_Y + i);
				npc.setMap(HIDDEN_MAP);
				npc.setHomeX(HIDDEN_X + i);
				npc.setHomeY(HIDDEN_Y + i);
				npc.setHeading(0);

				L1World.getInstance().storeObject(npc);
				L1World.getInstance().addVisibleObject(npc);

				_regenNpcs[i] = npc;
			} catch (Exception e) {
				System.out.println("❌ Failed to spawn regen NPC #" + i);
				e.printStackTrace();
			}
		}
	}

	public void updateRoyalRegenBuff(int towerId, L1PcInstance triggeringPlayer) {
		if (towerId < 1 || towerId > 3) return;

		int npcIndex = towerId - 1;
		L1NpcInstance npc = _regenNpcs[npcIndex];
		if (npc == null) return;

		if (triggeringPlayer == null) {
			if (_regenBuffActive) {
				_regenBuffActive = false;
				teleportRegenNpc(npcIndex, HIDDEN_X, HIDDEN_Y, HIDDEN_MAP);
			}
			return;
		}

		if (!_regenBuffActive) {
			_regenBuffActive = true;

			int[] loc;
			if (towerId == 1) loc = L1CastleLocation.getHeineTower1Loc();
			else if (towerId == 2) loc = L1CastleLocation.getHeineTower2Loc();
			else loc = L1CastleLocation.getHeineTower3Loc();

			teleportRegenNpc(npcIndex, loc[0], loc[1], (short) loc[2]);
		}

		if (System.currentTimeMillis() - _lastRegenTick >= REGEN_INTERVAL_MS) {
		    int maxHp = getMaxHp();
		    int capHp = (int) (maxHp * 0.74);
		    int heal = (int) (maxHp * REGEN_PERCENT);
		    int newHp = Math.min(getCurrentHp() + heal, capHp);
		    setCurrentHp(newHp);
		    _lastRegenTick = System.currentTimeMillis();

			if (newHp == capHp) {
			    if (_crackStatus != 1) {
			        broadcastPacket(new S_DoActionGFX(getId(), ActionCodes.ACTION_TowerCrack1));
			        setStatus(ActionCodes.ACTION_TowerCrack1);
			        _crackStatus = 1;
			    }
			} else if (newHp > (maxHp * 2 / 4)) {
			    if (_crackStatus != 2) {
			        broadcastPacket(new S_DoActionGFX(getId(), ActionCodes.ACTION_TowerCrack2));
			        setStatus(ActionCodes.ACTION_TowerCrack2);
			        _crackStatus = 2;
			    }
			} else if (newHp > (maxHp * 1 / 4)) {
			    if (_crackStatus != 3) {
			        broadcastPacket(new S_DoActionGFX(getId(), ActionCodes.ACTION_TowerCrack3));
			        setStatus(ActionCodes.ACTION_TowerCrack3);
			        _crackStatus = 3;
			    }
			}
		}
	}
	public static void resetAllRegenNpcs() {
	    for (int i = 0; i < _regenNpcs.length; i++) {
	        teleportRegenNpc(i, HIDDEN_X + i, HIDDEN_Y + i, HIDDEN_MAP);
	    }
	}

	private static void teleportRegenNpc(int index, int x, int y, short mapId) {
		L1NpcInstance npc = _regenNpcs[index];
		if (npc == null) return;

		L1World.getInstance().removeVisibleObject(npc);

		npc.setX(x);
		npc.setY(y);
		npc.setMap(mapId);
		npc.setHomeX(x);
		npc.setHomeY(y);
		
		L1World.getInstance().addVisibleObject(npc);
		npc.broadcastPacket(new S_RemoveObject(npc));
		npc.broadcastPacket(new S_NPCPack(npc));
	}




	@Override
	public void onPerceive(L1PcInstance perceivedFrom) {
		perceivedFrom.addKnownObject(this);
		perceivedFrom.sendPackets(new S_NPCPack(this));
	}

	@Override
	public void onAction(L1PcInstance player) {
		if (getCurrentHp() > 0 && !isDead()) {
			L1Attack attack = new L1Attack(player, this);
			if (attack.calcHit()) {
				attack.calcDamage();
				attack.addPcPoisonAttack(player, this);
				attack.addChaserAttack();
			}
			attack.action();
			attack.commit();
		}
	}

	@Override
	public void receiveDamage(L1Character attacker, int damage) {
		if (_castle_id == 0) {
			if (isSubTower()) {
				_castle_id = L1CastleLocation.ADEN_CASTLE_ID;
			} else {
				_castle_id = L1CastleLocation.getCastleIdByTowerLocation(getX(), getY(), getMapId());

			}
		}

		long siegeStartTime = WarTimeController.getInstance().getWarStartTime(_castle_id - 1).getTimeInMillis();
		long currentTime = System.currentTimeMillis();
		long elapsedTime;

		if (currentTime >= siegeStartTime) {
		    elapsedTime = currentTime - siegeStartTime;
		} else {
		    elapsedTime = 0;
		}

		if (_castle_id > 0 
				&& (WarTimeController.getInstance().isNowWar(_castle_id) || elapsedTime > 10 * 60 * 1000)) {
			if (_castle_id == L1CastleLocation.ADEN_CASTLE_ID && !isSubTower()) {
				int subTowerDeadCount = 0;
				for (L1Object l1object : L1World.getInstance().getObject()) {
					if (l1object instanceof L1TowerInstance) {
						L1TowerInstance tower = (L1TowerInstance) l1object;
						if (tower.isSubTower() && tower.isDead()) {
							subTowerDeadCount++;
							if (subTowerDeadCount == 4) {
								break;
							}
						}
					}
				}
				if (subTowerDeadCount < 3) {
					return;
				}
			}

			L1PcInstance pc = null;
			if (attacker instanceof L1PcInstance) {
				pc = (L1PcInstance) attacker;
			} else if (attacker instanceof L1PetInstance) {
				pc = (L1PcInstance) ((L1PetInstance) attacker).getMaster();
			} else if (attacker instanceof L1SummonInstance) {
				pc = (L1PcInstance) ((L1SummonInstance) attacker).getMaster();
			}
			if (pc == null) {
				return;
			}

			// Check if attacker is on the same team as the tower
			for (L1War war : L1World.getInstance().getWarList()) {
			    if (war.GetCastleId() == _castle_id) {
			        int attackerTeam = war.getClanTeam(pc.getClanname());
			        int towerId = getHeineTowerIdAtLocation(this.getX(), this.getY(), this.getMapId());
			        int towerTeam = war.getTowerOwner(towerId);


			        if (attackerTeam == towerTeam && towerTeam != -1) {
			            pc.sendPackets(new S_SystemMessage("You cannot attack your own team's tower."));
			            return;
			        }
			    }
			}

			boolean warIsActive = WarTimeController.getInstance().isNowWar(_castle_id);
			L1Clan attackerClan = L1World.getInstance().getClan(pc.getClanname());
			boolean isCastleOwner = (attackerClan != null && attackerClan.getCastleId() == _castle_id);

			boolean isAtWar = false;
			for (L1War war : L1World.getInstance().getWarList()) {
			    if (war.GetCastleId() == _castle_id && war.CheckClanInWar(pc.getClanname())) {
			        isAtWar = true;
			        break;
			    }
			}

			// Prevent attacking towers before 10 minutes unless a war is active
			if (elapsedTime < 10 * 60 * 1000 && !isAtWar) {
			    return;
			}

			// After 10 minutes or if war is active, only owner or war participants can attack
			if (!(isCastleOwner || isAtWar)) {
			    pc.sendPackets(new S_SystemMessage("You are not part of this siege."));
			    return;
			}
			
			// After 10 minutes, auto-assign owner clan to team 7 if not already assigned
			if (elapsedTime >= 10 * 60 * 1000 && isCastleOwner) {
			    boolean hasWar = false;

			    for (L1War war : L1World.getInstance().getWarList()) {
			        if (war.GetCastleId() == _castle_id) {
			            hasWar = true;
			            int teamId = war.getClanTeam(pc.getClanname());
			            if (teamId == -1) {
			                war.AddAttackClan(pc.getClanname()); // Assign team 7
			            }
			            break;
			        }
			    }

			    if (!hasWar) {
			        // 💥 Create a fake war object for this castle
			    	new L1War().handleCommands(1, pc.getClanname(), pc.getClanname());
			        System.out.println("[Siege] Created solo siege war for castleId=" + _castle_id);
			    }
			}


			if (getCurrentHp() > 0 && !isDead()) {
				int newHp = getCurrentHp() - damage;
				if (newHp <= 0 && !isDead()) {
					setCurrentHpDirect(0);
					setDead(true);
					setStatus(ActionCodes.ACTION_TowerDie);
					_lastattacker = attacker;
					_crackStatus = 0;
					Death death = new Death();
					GeneralThreadPool.getInstance().execute(death);
				}
				if (newHp > 0) {
					setCurrentHp(newHp);
					if ((getMaxHp() * 1 / 4) > getCurrentHp()) {
						if (_crackStatus != 3) {
							broadcastPacket(new S_DoActionGFX(getId(),
									ActionCodes.ACTION_TowerCrack3));
							setStatus(ActionCodes.ACTION_TowerCrack3);
							_crackStatus = 3;
						}
					} else if ((getMaxHp() * 2 / 4) > getCurrentHp()) {
						if (_crackStatus != 2) {
							broadcastPacket(new S_DoActionGFX(getId(),
									ActionCodes.ACTION_TowerCrack2));
							setStatus(ActionCodes.ACTION_TowerCrack2);
							_crackStatus = 2;
						}
					} else if ((getMaxHp() * 3 / 4) > getCurrentHp()) {
						if (_crackStatus != 1) {
							broadcastPacket(new S_DoActionGFX(getId(),
									ActionCodes.ACTION_TowerCrack1));
							setStatus(ActionCodes.ACTION_TowerCrack1);
							_crackStatus = 1;
						}
					}
				}
			} else if (!isDead()) {
				setDead(true);
				setStatus(ActionCodes.ACTION_TowerDie);
				_lastattacker = attacker;
				Death death = new Death();
				GeneralThreadPool.getInstance().execute(death);
			}
		}
	}

	@Override
	public void setCurrentHp(int i) {
		int currentHp = i;
		if (currentHp >= getMaxHp()) {
			currentHp = getMaxHp();
		}
		setCurrentHpDirect(currentHp);
	}

	class Death implements Runnable {
	    L1Character lastAttacker = _lastattacker;
	    L1Object object = L1World.getInstance().findObject(getId());
	    L1TowerInstance npc = (L1TowerInstance) object;

	    @Override
	    public void run() {
	        try {
	            Thread.currentThread().setName("L1TowerInstance-Death");

	            setCurrentHpDirect(0);
	            setDead(true);
	            setStatus(ActionCodes.ACTION_TowerDie);
	            int targetobjid = npc.getId();

	            npc.getMap().setPassable(npc.getLocation(), true);

	            npc.broadcastPacket(new S_DoActionGFX(targetobjid, ActionCodes.ACTION_TowerDie));

	            // Identify tower + player clan
	            int towerId = getHeineTowerIdAtLocation(npc.getX(), npc.getY(), npc.getMapId());
	            String playerClanName = "Unknown";

	            if (lastAttacker instanceof L1PcInstance) {
	                playerClanName = ((L1PcInstance) lastAttacker).getClanname();
	            } else if (lastAttacker instanceof L1PetInstance) {
	                L1PcInstance master = (L1PcInstance) ((L1PetInstance) lastAttacker).getMaster();
	                if (master != null) {
	                    playerClanName = master.getClanname();
	                }
	            }

	            // Find team color
	            int team = 7; // default black
	            for (L1War war : L1World.getInstance().getWarList()) {
	                if (war.CheckClanInWar(playerClanName)) {
	                    team = war.getClanTeam(playerClanName);
	                    break;
	                }
	            }

	         // Announce tower capture only to players in warzone
	            String towerName = getHeineTowerName(towerId);
	            String msg = towerName + " has been captured by " + playerClanName + "!";

	            for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
	                if (L1CastleLocation.checkInWarArea(_castle_id, pc)) {
	                    pc.sendPackets(new S_SystemMessage(msg));
	                }
	            }

	            // Set Tower Owner and capture points
	            L1War war = null;
	            for (L1War w : L1World.getInstance().getWarList()) {
	                if (w.GetCastleId() == _castle_id) {
	                    war = w;
	                    break;
	                }
	            }

	            if (war == null) {
	                return; // No active war, exit early
	            }

	            war.setTowerOwner(towerId, team);
	            war.setTowerCapturer(towerId, playerClanName); // Optional if doing 5-min bonus later

	            int capturePoints = 1;

	            // Bonus if prince/royal
	            if (lastAttacker instanceof L1PcInstance) {
	                L1PcInstance capturer = (L1PcInstance) lastAttacker;
	                if (capturer.isCrown()) {
	                    capturePoints = 2;
	                }
	            } else if (lastAttacker instanceof L1PetInstance) {
	                L1PcInstance master = (L1PcInstance) ((L1PetInstance) lastAttacker).getMaster();
	                if (master != null && master.isCrown()) {
	                    capturePoints = 2;
	                }
	            }

	            war.addPointsToTeam(team, capturePoints);

	            // Kill old glow NPCs
	            for (L1Object obj : L1World.getInstance().getObject()) {
	                if (obj instanceof L1NpcInstance) {
	                    L1NpcInstance glowNpc = (L1NpcInstance) obj;
	                    if (isGlowNpc(glowNpc.getNpcTemplate().get_npcId()) &&
	                        glowNpc.getX() == getTowerSpawnX(towerId) &&
	                        glowNpc.getY() == getTowerSpawnY(towerId) &&
	                        glowNpc.getMapId() == getTowerMapId(towerId)) {

	                        glowNpc.setCurrentHp(0);
	                        glowNpc.setDead(true);
	                        glowNpc.setStatus(ActionCodes.ACTION_Die);
	                        glowNpc.broadcastPacket(new S_DoActionGFX(glowNpc.getId(), ActionCodes.ACTION_Die));
	                        glowNpc.allTargetClear();

	                        glowNpc.startDeleteTimer();
	                    }
	                }
	            }

	            // Spawn new glow immediately
	            L1PcInstance pc = null;
	            if (lastAttacker instanceof L1PcInstance) {
	                pc = (L1PcInstance) lastAttacker;
	            } else if (lastAttacker instanceof L1PetInstance) {
	                pc = (L1PcInstance) ((L1PetInstance) lastAttacker).getMaster();
	            }
	            spawnGlowNpcForTower(towerId, team, pc);

	         // Wait 9.75 seconds, then delete old tower and respawn a new one
	            GeneralThreadPool.getInstance().schedule(() -> {
	                for (L1Object l1object : L1World.getInstance().getObject()) {
	                    if (l1object instanceof L1TowerInstance) {
	                        L1TowerInstance oldTower = (L1TowerInstance) l1object;
	                        if (oldTower.getX() == getTowerSpawnX(towerId) &&
	                            oldTower.getY() == getTowerSpawnY(towerId) &&
	                            oldTower.getMapId() == getTowerMapId(towerId)) {

	                            oldTower.deleteMe(); // 💥 Delete first

	                            // ✅ Then spawn a new tower shortly after
	                            GeneralThreadPool.getInstance().schedule(() -> {
	                                int npcId = 81111;
	                                L1Npc l1npc = NpcTable.getInstance().getTemplate(npcId);
	                                SpawnWarObject(l1npc, getTowerSpawnX(towerId), getTowerSpawnY(towerId), getTowerMapId(towerId));
	                            }, 250); // Let deletion finish before respawning
	                        }
	                    }
	                }
	            }, 9750);


	        } catch (Exception e) {
	            _log.error("", e);
	        }
	    }
	}

	public static void removeGlowNpcsAtTower(int towerId) {
	    for (L1Object obj : L1World.getInstance().getObject()) {
	        if (obj instanceof L1NpcInstance) {
	            L1NpcInstance npc = (L1NpcInstance) obj;
	            if (isGlowNpc(npc.getNpcTemplate().get_npcId()) &&
	                npc.getX() == getTowerSpawnX(towerId) &&
	                npc.getY() == getTowerSpawnY(towerId) &&
	                npc.getMapId() == getTowerMapId(towerId)) {

	                npc.setCurrentHp(0);
	                npc.setDead(true);
	                npc.setStatus(ActionCodes.ACTION_Die);
	                npc.broadcastPacket(new S_DoActionGFX(npc.getId(), ActionCodes.ACTION_Die));
	                npc.allTargetClear();
	                npc.startDeleteTimer();
	            }
	        }
	    }
	}


	public static void SpawnWarObject(L1Npc npc, int x, int y, short mapId) {
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
	    if (towerId == 1) return "The Central Tower";
	    if (towerId == 2) return "The Eastern Tower";
	    if (towerId == 3) return "The Western Tower";
	    return "Unknown Tower";
	}

	public int getHeineTowerIdAtLocation(int x, int y, short mapId) {
	    if (x == 33524 && y == 33396 && mapId == 4) return 1;
	    if (x == 33547 && y == 33398 && mapId == 4) return 2;
	    if (x == 33497 && y == 33398 && mapId == 4) return 3;
	    return -1;
	}
	
	public static int getTowerSpawnX(int towerId) {
	    if (towerId == 1) return 33524;
	    if (towerId == 2) return 33547;
	    if (towerId == 3) return 33497;
	    return 0;
	}

	public static int getTowerSpawnY(int towerId) {
	    if (towerId == 1) return 33396;
	    if (towerId == 2) return 33398;
	    if (towerId == 3) return 33398;
	    return 0;
	}

	public static short getTowerMapId(int towerId) {
	    return 4;
	}

	
	private static boolean isGlowNpc(int npcId) {
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

	public boolean isSubTower2() {
		return false;
	}

	public boolean isSubTower() {
		return (getNpcTemplate().get_npcId() == 81190
				|| getNpcTemplate().get_npcId() == 81191
				|| getNpcTemplate().get_npcId() == 81192 || getNpcTemplate()
				.get_npcId() == 81193);
	}
	
	public int getCastleId() {
	    return _castle_id;
	}

}