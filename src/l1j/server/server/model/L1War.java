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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.controllers.WarTimeController;
import l1j.server.server.datatables.CastleTable;
import l1j.server.server.datatables.ClanTable;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.model.Instance.L1TowerInstance;
import l1j.server.server.serverpackets.S_CastleMaster;
import l1j.server.server.serverpackets.S_OwnCharAttrDef;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_SPMR;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_War;
import l1j.server.server.templates.L1Castle;
import l1j.server.server.templates.L1Npc;

// Referenced classes of package l1j.server.server.model:
// L1War
public class L1War {
	private String _param1 = null;
	private String _param2 = null;
	private final ArrayList<String> _attackClanList = new ArrayList<String>();
	private String _defenceClanName = null;
	private int _warType = 0;
	private int _castleId = 0;
	private L1Castle _castle = null;
	private Calendar _warEndTime;
	private boolean _isWarTimerDelete = false;
	// Total points per team
	private final Map<Integer, Integer> _teamPoints = new HashMap<>();

	// Optional: Which player captured which tower
	private final Map<Integer, String> _towerCapturer = new HashMap<>();
	
	private final Map<Integer, Long> _castleWarStartTimes = new HashMap<>();

	public L1War() {
	}
	class CastleWarTimer implements Runnable {
	    public CastleWarTimer() {
	    }

	    @Override
	    public void run() {
	        Thread.currentThread().setName("L1War-CastleWarTimer");

	        try {
	        	int castleId = GetCastleId();
	        	long warStartMillis = 0;
	        	try (Connection con = L1DatabaseFactory.getInstance().getConnection();
	        	     PreparedStatement ps = con.prepareStatement("SELECT war_time FROM castle WHERE castle_id = ?")) {
	        	    ps.setInt(1, castleId);
	        	    try (ResultSet rs = ps.executeQuery()) {
	        	        if (rs.next()) {
	        	            Timestamp warTime = rs.getTimestamp("war_time");
	        	            if (warTime != null) {
	        	                warStartMillis = warTime.getTime();
	        	            }
	        	        }
	        	    }
	        	} catch (SQLException e) {
	        	    System.out.println("Failed to load war start time from DB: " + e.getMessage());
	        	}

	            GeneralThreadPool.getInstance().scheduleAtFixedRate(() -> {
	                try {
	                    long now = System.currentTimeMillis();

	                    for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
	                        if (!pc.isCrown() || pc.getClanid() == 0 || pc.isDead()) continue;

	                        L1Clan clan = pc.getClan();
	                        if (clan == null) continue;

	                        boolean inWar = L1World.getInstance().getWarList().stream()
	                            .anyMatch(war -> war.CheckClanInWar(clan.getClanName()));
	                        if (!inWar) continue;

	                        //Inspiration Buff Logic for Wars
	                        for (L1PcInstance nearby : L1World.getInstance().getVisiblePlayer(pc, 4)) {
	                            if (nearby.isDead()) continue;
	                            if (nearby.getClanid() != pc.getClanid()) continue;

	                            // If not already buffed, apply it
	                            if (!nearby.isInspired()) {
	                                nearby.addAc(-5);
	                                nearby.addMr(10);
	                                nearby.setInspired(true);
	            					nearby.sendPackets(new S_OwnCharAttrDef(nearby));
	            					nearby.sendPackets(new S_OwnCharStatus(nearby));
	            					nearby.sendPackets(new S_SPMR(nearby));
	                            }

	                            nearby.setLastInspiredTimestamp(System.currentTimeMillis());
	                        }
	                        
	                     // 🏰 Tower Regen Buff Logic
	                        for (L1Object obj : L1World.getInstance().getObject()) {
	                            if (!(obj instanceof L1TowerInstance)) continue;

	                            L1TowerInstance tower = (L1TowerInstance) obj;
	                            int towerId = tower.getHeineTowerIdAtLocation(tower.getX(), tower.getY(), tower.getMapId());

	                            if (tower.isDead()) {
	                                tower.updateRoyalRegenBuff(towerId, null); // Force despawn if dead
	                                continue;
	                            }

	                            if (towerId == -1) continue;

	                            // 🔍 Find war for this castle
	                            L1War activeWar = null;
	                            for (L1War w : L1World.getInstance().getWarList()) {
	                                if (w.GetCastleId() == tower.getCastleId()) {
	                                    activeWar = w;
	                                    break;
	                                }
	                            }

	                            boolean validRoyalNearby = false;

	                            for (L1PcInstance pc1 : L1World.getInstance().getVisiblePlayer(tower, L1TowerInstance.REGEN_RADIUS)) {
	                                if (!pc1.isCrown() || pc1.isDead()) continue;

	                                int ownerTeam = getTowerOwner(towerId);
	                                int playerTeam = getClanTeam(pc1.getClanname());

	                                // ✅ Only allow regen from war-participating royals on the owning team
	                                if (ownerTeam != -1 && ownerTeam == playerTeam && activeWar != null && activeWar.CheckClanInWar(pc1.getClanname())) {
	                                    tower.updateRoyalRegenBuff(towerId, pc1); // heal + spawn
	                                    validRoyalNearby = true;
	                                    break;
	                                }
	                            }

	                            if (!validRoyalNearby) {
	                                tower.updateRoyalRegenBuff(towerId, null); // will trigger despawn
	                            }
	                        }
	                    }
	                    
		                 //Pet/Summon warzone check (teleport owner if pet is in warzone)
	                    for (L1Object obj : L1World.getInstance().getObject()) {
	                        if (obj instanceof L1PetInstance || obj instanceof L1SummonInstance) {
	                            L1Character summon = (L1Character) obj;

	                            if (!(summon instanceof L1NpcInstance)) {
	                                continue;
	                            }

	                            L1PcInstance owner = null;
	                            try {
	                                owner = (L1PcInstance) ((L1NpcInstance) summon).getMaster();
	                            } catch (Exception e) {
	                                continue;
	                            }

	                            if (owner == null) {
	                                continue;
	                            }

	                            if (owner.isGm()) {
	                                continue;
	                            }

	                            if (!WarTimeController.getInstance().isNowWar(castleId)) {
	                                continue;
	                            }

	                            if (L1CastleLocation.checkInWarArea(castleId, summon)) {
	                                int[] loc = L1CastleLocation.getGetBackLoc(castleId);
	                                owner.sendPackets(new S_SystemMessage("Pets and summons are not allowed in the war area! You have been relocated."));
	                                L1Teleport.teleport(owner, loc[0], loc[1], (short) loc[2], owner.getHeading(), true);
	                            } else {
	                            }
	                        }
	                    }

	                    // Remove buffs when expired
	                    for (L1PcInstance player : L1World.getInstance().getAllPlayers()) {
	                        if (!player.isInspired()) continue;

	                        if (System.currentTimeMillis() - player.getLastInspiredTimestamp() > 5000) {
	                            player.addAc(5);
	                            player.addMr(-10);
	                            player.setInspired(false);
	                            player.sendPackets(new S_OwnCharAttrDef(player));
	                            player.sendPackets(new S_OwnCharStatus(player));
	                            player.sendPackets(new S_SPMR(player));
	                            player.setLastInspiredTimestamp(0);
	                        }
	                    }
	                } catch (Exception e) {
	                    System.out.println("[INSPIRATION ERROR] " + e.getMessage());
	                    e.printStackTrace();
	                }
	            }, 0, 1000);
	            
	            Map<String, Integer> towerCapturesByClan = new HashMap<>();

	            for (int i = 1; i <= 12; i++) {
	                long nextTick = warStartMillis + (i * 5 * 60 * 1000L);
	                long delay = nextTick - System.currentTimeMillis();

	                if (delay > 0) {
	                    Thread.sleep(delay);
	                }

	                Map<String, Integer> clanPointMap = new LinkedHashMap<>();

	                for (int towerId = 1; towerId <= 3; towerId++) {
	                    int teamId = getTowerOwner(towerId);
	                    if (teamId == -1) continue;

	                    int bonusPoints = 10;
	                    String capturer = getTowerCapturer(towerId);
	                    String clanName = "Unknown";

	                    if (capturer != null) {
	                        L1Clan capturerClan = L1World.getInstance().getClan(capturer);
	                        if (capturerClan != null && getClanTeam(capturer) == teamId) {
	                            L1PcInstance leader = L1World.getInstance().getPlayer(capturerClan.getLeaderName());
	                            if (leader != null && leader.isCrown()) {
	                                bonusPoints = 15;
	                            }
	                            clanName = capturer;
	                        }
	                    }

	                    addPointsToTeam(teamId, bonusPoints);

	                    if (!clanName.equals("Unknown")) {
	                        clanPointMap.put(clanName, clanPointMap.getOrDefault(clanName, 0) + bonusPoints);
	                        towerCapturesByClan.put(clanName, towerCapturesByClan.getOrDefault(clanName, 0) + 1);
	                    }

	                    setTowerOwner(towerId, -1);
	                    setTowerCapturer(towerId, null);
	                    L1TowerInstance.removeGlowNpcsAtTower(towerId);
	                    L1TowerInstance.resetAllRegenNpcs();
	                }

	                if (i != 12 && !clanPointMap.isEmpty()) {
	                    List<Map.Entry<String, Integer>> sorted = new ArrayList<>(clanPointMap.entrySet());
	                    sorted.removeIf(e -> e.getValue() == 0);

	                    // Sort by total score, descending
	                    sorted.sort((a, b) -> {
	                        int totalA = getPointsForTeam(getClanTeam(a.getKey()));
	                        int totalB = getPointsForTeam(getClanTeam(b.getKey()));
	                        return Integer.compare(totalB, totalA);
	                    });

	                    StringBuilder captureLine = new StringBuilder("Captured this round: ");
	                    StringBuilder rankingsLine = new StringBuilder("Current Rankings: ");

	                    int addedCaptures = 0;
	                    for (Map.Entry<String, Integer> entry : sorted) {
	                        if (towerCapturesByClan.containsKey(entry.getKey()) && addedCaptures < 3) {
	                            if (addedCaptures > 0) captureLine.append(", ");
	                            captureLine.append(entry.getKey()).append(" x").append(towerCapturesByClan.get(entry.getKey()));
	                            addedCaptures++;
	                        }
	                    }

	                    for (int rank = 0; rank < Math.min(3, sorted.size()); rank++) {
	                        String clan = sorted.get(rank).getKey();
	                        int totalPoints = getPointsForTeam(getClanTeam(clan));
	                        if (rank > 0) rankingsLine.append(" | ");
	                        rankingsLine.append((rank + 1)).append("st: ").append(clan).append(" (").append(totalPoints).append(" pts)");
	                    }

	                    for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
	                        if (L1CastleLocation.checkInWarArea(GetCastleId(), pc)) {
	                            if (addedCaptures > 0)
	                                pc.sendPackets(new S_SystemMessage(captureLine.toString()));
	                            pc.sendPackets(new S_SystemMessage(rankingsLine.toString()));
	                        }
	                    }
	                }

	            
	                if (_isWarTimerDelete) return;
	            }


	            // Wait for war end (default: 60 min from war start)
	            while (!_warEndTime.before(WarTimeController.getRealTime())) {
	                Thread.sleep(1000);
	                if (_isWarTimerDelete) return;
	            }

	            String winner = assignCastleToHighestScoringTeam();
	            if (winner != null) {
	                WinCastleWar(winner);
	            } else {
	                CeaseCastleWar();
	            }


	        } catch (Exception e) {
	        }
	    }
	}
	
	public L1War(int castleId) {
	    _castleId = castleId;
	    // Init any lists if needed
	}

	public void startCastleWar(int castleId) {
	    if (!_castleWarStartTimes.containsKey(castleId)) {
	        _castleWarStartTimes.put(castleId, System.currentTimeMillis());
	    }
	}


	public String assignCastleToHighestScoringTeam() {
	    int highestPoints = -1;
	    int winningTeamId = -1;
	    int secondHighestPoints = -1;

	    // Find top two teams by score
	    for (Map.Entry<Integer, Integer> entry : _teamPoints.entrySet()) {
	        int teamId = entry.getKey();
	        int points = entry.getValue();

	        if (points > highestPoints) {
	            secondHighestPoints = highestPoints;
	            highestPoints = points;
	            winningTeamId = teamId;
	        } else if (points > secondHighestPoints) {
	            secondHighestPoints = points;
	        }
	    }

	    // 🟨 Handle tie
	    int castleId = GetCastleId();
	    String currentOwner = null;
	    for (L1Clan c : L1World.getInstance().getAllClans()) {
	        if (c.getCastleId() == castleId) {
	            currentOwner = c.getClanName();
	            break;
	        }
	    }

	    if (highestPoints == secondHighestPoints) {
	        boolean ownerInTie = false;
	        for (Map.Entry<String, Integer> entry : _clanTeamMap.entrySet()) {
	            if (entry.getValue() == winningTeamId && entry.getKey().equalsIgnoreCase(currentOwner)) {
	                ownerInTie = true;
	                break;
	            }
	        }

	        if (ownerInTie) {
	            L1World.getInstance().broadcastServerMessage("The war ends in a draw! The current owner retains the castle.");
	        } else {
	            L1World.getInstance().broadcastServerMessage("The war ends in a draw! No clan takes the castle.");
	        }
	        return null;
	    }

	    // Get clan name of winner
	    String winningClanName = null;
	    for (Map.Entry<String, Integer> entry : _clanTeamMap.entrySet()) {
	        if (entry.getValue() == winningTeamId) {
	            winningClanName = entry.getKey();
	            break;
	        }
	    }

	    if (winningClanName == null) return null;
	    L1Clan winningClan = L1World.getInstance().getClan(winningClanName);
	    if (winningClan == null) return null;

	    // Assign castle
	    for (L1Clan c : L1World.getInstance().getAllClans()) {
	        if (c.getCastleId() == castleId) {
	            c.setCastleId(0);
	            ClanTable.getInstance().updateClan(c);
	        }
	    }
	    winningClan.setCastleId(castleId);
	    ClanTable.getInstance().updateClan(winningClan);
	    
	    WarTimeController.getInstance().clearCastle(castleId);

	    for (L1PcInstance pc : winningClan.getOnlineClanMember()) {
	        if (pc.getId() == winningClan.getLeaderId()) {
	            pc.sendPackets(new S_CastleMaster(castleId, pc.getId()));
	            pc.broadcastPacket(new S_CastleMaster(castleId, pc.getId()));
	        }
	    }

	    // ✨ Flavor based on gap
	    String flavor;
	    if (highestPoints - secondHighestPoints > 50) {
	        flavor = winningClanName + " stomped the opposition into the dirt.";
	    } else if (highestPoints - secondHighestPoints > 30) {
	        flavor = "Crushed. Shattered. Humiliated. " + winningClanName + " made it look easy.";
	    } else if (highestPoints - secondHighestPoints > 15) {
	        flavor = "Absolutely DOMINATED. The rest never stood a chance.";
	    } else if (highestPoints - secondHighestPoints >= 1) {
	        flavor = winningClanName + " bled for this one and they earned every inch of it.";
	    } else {
	        flavor = "The war ends in a draw. No clan stood above the rest this time.";
	    }

	    // 🧾 Final broadcast
	    L1World.getInstance().broadcastServerMessage(winningClanName + " has claimed the castle with " + highestPoints + " points.");
	    L1World.getInstance().broadcastServerMessage(flavor);
	    L1World.getInstance().broadcastServerMessage("Final scores:");

	    List<Map.Entry<String, Integer>> entries = new ArrayList<>(_clanTeamMap.entrySet());
	    entries.sort((a, b) -> _teamPoints.getOrDefault(b.getValue(), 0) - _teamPoints.getOrDefault(a.getValue(), 0));

	    for (int i = 0; i < entries.size(); i += 2) {
	        StringBuilder line = new StringBuilder();

	        Map.Entry<String, Integer> first = entries.get(i);
	        int score1 = _teamPoints.getOrDefault(first.getValue(), 0);
	        line.append(first.getKey()).append(": ").append(score1);

	        if (i + 1 < entries.size()) {
	            Map.Entry<String, Integer> second = entries.get(i + 1);
	            int score2 = _teamPoints.getOrDefault(second.getValue(), 0);
	            line.append("  |  ").append(second.getKey()).append(": ").append(score2);
	        }

	        L1World.getInstance().broadcastServerMessage(line.toString());
	    }
	    for (L1War war : L1World.getInstance().getWarList()) {
	        if (war.GetCastleId() == castleId) {
	            war.endWar();
	        }
	    }
	    return winningClanName;
	}
	
	class SimWarTimer implements Runnable {
		public SimWarTimer() {
		}

		@Override
		public void run() {
			Thread.currentThread().setName("L1War-SimWarTimer");
			for (int loop = 0; loop < 240; loop++) {
				try {
					Thread.sleep(60000);
				} catch (Exception exception) {
					break;
				}
				if (_isWarTimerDelete) {
					return;
				}
			}
			CeaseWar(_param1, _param2);
			delete();
		}
	}

	// Add this at the top of L1War class (with other fields)
	private final Map<String, Integer> _clanTeamMap = new HashMap<>();

	// Updated handleCommands()
	public void handleCommands(int war_type, String attack_clan_name,
	        String defence_clan_name) {
	    SetWarType(war_type);
	    DeclareWar(attack_clan_name, defence_clan_name);
	    _param1 = attack_clan_name;
	    _param2 = defence_clan_name;
	    InitAttackClan();
	    SetDefenceClanName(defence_clan_name);

	    int castleId = GetCastleId();
	    WarTimeController.getInstance().clearCastle(castleId);

	    // Defender is always black (team 7)
	    _clanTeamMap.put(defence_clan_name, 7);

	    // Assign the first attacker to team 0 (Orange)
	    AddAttackClan(attack_clan_name);

	    if (war_type == 1) {
	        _castle = GetCastle();
	        if (_castle != null) {
	            Calendar cal = (Calendar) _castle.getWarTime().clone();
	            cal.add(Config.ALT_WAR_TIME_UNIT, Config.ALT_WAR_TIME);
	            _warEndTime = cal;
	        }
	        CastleWarTimer castle_war_timer = new CastleWarTimer();
	        GeneralThreadPool.getInstance().execute(castle_war_timer);
	    } else if (war_type == 2) {
	        SimWarTimer sim_war_timer = new SimWarTimer();
	        GeneralThreadPool.getInstance().execute(sim_war_timer);
	    }

	    L1World.getInstance().addWar(this);
	}

	// Helper: Get the next available team index (0–6)
	private int getNextAvailableTeamIndex() {
	    for (int i = 0; i < 7; i++) { // skip 7, which is black
	        if (!_clanTeamMap.containsValue(i)) {
	            return i;
	        }
	    }
	    return 0; // fallback, though ideally you never exceed 7 clans
	}

	// Getter: Retrieve a clan's team
	public int getClanTeam(String clanName) {
	    return _clanTeamMap.getOrDefault(clanName, -1);
	}


	private void RequestCastleWar(int type, String clan1_name, String clan2_name) {
		if (clan1_name == null || clan2_name == null) {
			return;
		}

		L1Clan clan1 = L1World.getInstance().getClan(clan1_name);
		if (clan1 != null) {
			L1PcInstance clan1_member[] = clan1.getOnlineClanMember();
			for (int cnt = 0; cnt < clan1_member.length; cnt++) {
				clan1_member[cnt].sendPackets(new S_War(type, clan1_name,
						clan2_name));
			}
		}

		int attack_clan_num = GetAttackClanListSize();

		if (type == 1 || type == 2 || type == 3) {
			L1Clan clan2 = L1World.getInstance().getClan(clan2_name);
			if (clan2 != null) {
				L1PcInstance clan2_member[] = clan2.getOnlineClanMember();
				for (int cnt = 0; cnt < clan2_member.length; cnt++) {
					if (type == 1) {
						clan2_member[cnt].sendPackets(new S_War(type,
								clan1_name, clan2_name));
					} else if (type == 2) {
						clan2_member[cnt].sendPackets(new S_War(type,
								clan1_name, clan2_name));
						if (attack_clan_num == 1) {
							clan2_member[cnt].sendPackets(new S_War(4,
									clan2_name, clan1_name));
						} else {
							clan2_member[cnt].sendPackets(new S_ServerMessage(
									228, clan1_name, clan2_name));
							RemoveAttackClan(clan1_name);
						}
					} else if (type == 3) {
						clan2_member[cnt].sendPackets(new S_War(type,
								clan1_name, clan2_name));
						if (attack_clan_num == 1) {
							clan2_member[cnt].sendPackets(new S_War(4,
									clan2_name, clan1_name));
						} else {
							clan2_member[cnt].sendPackets(new S_ServerMessage(
									227, clan1_name, clan2_name));
							RemoveAttackClan(clan1_name);
						}
					}
				}
			}
		}
		if ((type == 2 || type == 3) && attack_clan_num == 1) {
			_isWarTimerDelete = true;
			delete();
		}
	}

	private void RequestSimWar(int type, String clan1_name, String clan2_name) {
		if (clan1_name == null || clan2_name == null) {
			return;
		}

		L1Clan clan1 = L1World.getInstance().getClan(clan1_name);
		if (clan1 != null) {
			L1PcInstance clan1_member[] = clan1.getOnlineClanMember();
			for (int cnt = 0; cnt < clan1_member.length; cnt++) {
				clan1_member[cnt].sendPackets(new S_War(type, clan1_name,
						clan2_name));
			}
		}

		if (type == 1 || type == 2 || type == 3) {
			L1Clan clan2 = L1World.getInstance().getClan(clan2_name);
			if (clan2 != null) {
				L1PcInstance clan2_member[] = clan2.getOnlineClanMember();
				for (int cnt = 0; cnt < clan2_member.length; cnt++) {
					if (type == 1) {
						clan2_member[cnt].sendPackets(new S_War(type,
								clan1_name, clan2_name));
					} else if (type == 2 || type == 3) {
						clan2_member[cnt].sendPackets(new S_War(type,
								clan1_name, clan2_name));
						clan2_member[cnt].sendPackets(new S_War(4, clan2_name,
								clan1_name));
					}
				}
			}
		}
		if (type == 2 || type == 3) {
			_isWarTimerDelete = true;
			delete();
		}
	}

	public void WinCastleWar(String clan_name) {
	    String defence_clan_name = GetDefenceClanName();

	    L1Clan defence_clan = L1World.getInstance().getClan(defence_clan_name);
	    if (defence_clan != null) {
	        L1PcInstance[] defence_clan_member = defence_clan.getOnlineClanMember();
	        for (L1PcInstance member : defence_clan_member) {
	            for (String attacker : GetAttackClanList()) {
	                member.sendPackets(new S_War(3, defence_clan_name, attacker));
	            }
	        }
	    }

	    String[] clanList = GetAttackClanList();
	    for (String attacker : clanList) {
	        if (attacker != null) {
	            L1Clan clan = L1World.getInstance().getClan(attacker);
	            if (clan != null) {
	                L1PcInstance[] clan_member = clan.getOnlineClanMember();
	                for (L1PcInstance member : clan_member) {
	                    member.sendPackets(new S_War(3, attacker, defence_clan_name));
	                }
	            }
	        }
	    }

	    _isWarTimerDelete = true;
	    delete();
	}
	public void CeaseCastleWar() {
	    String defence_clan_name = GetDefenceClanName();
	    String[] clanList = GetAttackClanList();

	    L1Clan defence_clan = L1World.getInstance().getClan(defence_clan_name);
	    if (defence_clan != null) {
	        L1PcInstance[] defence_clan_member = defence_clan.getOnlineClanMember();
	        for (L1PcInstance member : defence_clan_member) {
	            member.sendPackets(new S_War(4, defence_clan_name, clanList[0]));
	        }
	    }

	    for (String attacker : clanList) {
	        if (attacker != null) {
	            L1Clan clan = L1World.getInstance().getClan(attacker);
	            if (clan != null) {
	                L1PcInstance[] clan_member = clan.getOnlineClanMember();
	                for (L1PcInstance member : clan_member) {
	                    member.sendPackets(new S_War(3, attacker, defence_clan_name));
	                }
	            }
	        }
	    }

	    _isWarTimerDelete = true;
	    delete();
	}


	public void DeclareWar(String clan1_name, String clan2_name) {
		if (GetWarType() == 1) {
			RequestCastleWar(1, clan1_name, clan2_name);
		} else {
			RequestSimWar(1, clan1_name, clan2_name);
		}
	}

	public void SurrenderWar(String clan1_name, String clan2_name) {
		if (GetWarType() == 1) {
			RequestCastleWar(2, clan1_name, clan2_name);
		} else {
			RequestSimWar(2, clan1_name, clan2_name);
		}
	}

	public void CeaseWar(String clan1_name, String clan2_name) {
		if (GetWarType() == 1) {
			RequestCastleWar(3, clan1_name, clan2_name);
		} else {
			RequestSimWar(3, clan1_name, clan2_name);
		}
	}

	public void WinWar(String clan1_name, String clan2_name) {
		if (GetWarType() == 1) {
			RequestCastleWar(4, clan1_name, clan2_name);
		} else {
			RequestSimWar(4, clan1_name, clan2_name);
		}
	}

	public boolean CheckClanInWar(String clan_name) {
		boolean ret;
		if (GetDefenceClanName().toLowerCase().equals(clan_name.toLowerCase())) {
			ret = true;
		} else {
			ret = CheckAttackClan(clan_name);
		}
		return ret;
	}

	public boolean CheckClanInSameWar(String player_clan_name,
			String target_clan_name) {
		boolean player_clan_flag;
		boolean target_clan_flag;

		if (GetDefenceClanName().toLowerCase().equals(
				player_clan_name.toLowerCase())) {
			player_clan_flag = true;
		} else {
			player_clan_flag = CheckAttackClan(player_clan_name);
		}

		if (GetDefenceClanName().toLowerCase().equals(
				target_clan_name.toLowerCase())) {
			target_clan_flag = true;
		} else {
			target_clan_flag = CheckAttackClan(target_clan_name);
		}
		if (player_clan_flag == true && target_clan_flag == true) {
			return true;
		} else {
			return false;
		}
	}

	public String GetEnemyClanName(String player_clan_name) {
		String enemy_clan_name = null;
		if (GetDefenceClanName().toLowerCase().equals(
				player_clan_name.toLowerCase())) {
			String clanList[] = GetAttackClanList();
			for (int cnt = 0; cnt < clanList.length; cnt++) {
				if (clanList[cnt] != null) {
					enemy_clan_name = clanList[cnt];
					return enemy_clan_name;
				}
			}
		} else {
			enemy_clan_name = GetDefenceClanName();
			return enemy_clan_name;
		}
		return enemy_clan_name;
	}

	public void delete() {
		L1World.getInstance().removeWar(this);
	}

	public int GetWarType() {
		return _warType;
	}

	public void SetWarType(int war_type) {
		_warType = war_type;
	}

	public String GetDefenceClanName() {
		return _defenceClanName;
	}

	public void SetDefenceClanName(String defence_clan_name) {
		_defenceClanName = defence_clan_name;
	}

	public void InitAttackClan() {
		_attackClanList.clear();
	}
	
	private final Map<Integer, Integer> _towerTeamMap = new HashMap<>();

	public void setTowerOwner(int towerId, int teamId) {
	    _towerTeamMap.put(towerId, teamId);

	    if (teamId == -1) {
	        int x = L1TowerInstance.getTowerSpawnX(towerId);
	        int y = L1TowerInstance.getTowerSpawnY(towerId);
	        short mapId = L1TowerInstance.getTowerMapId(towerId);

	        // Remove old tower
	        for (L1Object obj : L1World.getInstance().getObject()) {
	            if (obj instanceof L1TowerInstance) {
	                L1TowerInstance tower = (L1TowerInstance) obj;
	                if (tower.getX() == x && tower.getY() == y && tower.getMapId() == mapId) {
	                    tower.deleteMe();
	                }
	            }
	        }

	        // Respawn tower
	        L1Npc npc = NpcTable.getInstance().getTemplate(81111); // Tower NPC ID
	        if (npc != null) {
	            L1TowerInstance.SpawnWarObject(npc, x, y, mapId);
	        }
	    }
	}


	public int getTowerOwner(int towerId) {
	    return _towerTeamMap.getOrDefault(towerId, -1); // -1 = unowned
	}

	

	public void AddAttackClan(String attack_clan_name) {
	    if (_attackClanList.contains(attack_clan_name)) {
	        return; // already added, nothing to do
	    }
	    // If this clan owns the castle, assign team 7
	    L1Clan ownerClan = L1World.getInstance().getClan(attack_clan_name);
	    if (ownerClan != null && ownerClan.getCastleId() == 5) {
	        _attackClanList.add(attack_clan_name);
	        _clanTeamMap.put(attack_clan_name, 7); // Force team 7
	        return;
	    }

	    // Check if we’ve hit the max (8 teams: index 0–7)
	    if (_clanTeamMap.size() >= 8) {
	        for (L1Clan clan : L1World.getInstance().getAllClans()) {
	            if (clan.getClanName().equals(attack_clan_name)) {
	                L1PcInstance clanLeader = L1World.getInstance().getPlayer(clan.getLeaderName());
	                if (clanLeader != null) {
	                    clanLeader.sendPackets(new S_SystemMessage("Too many clans are already participating in this war."));
	                }
	                break;
	            }
	        }
	        return;
	    }

	    _attackClanList.add(attack_clan_name);

	    // Only assign if not already assigned
	    if (!_clanTeamMap.containsKey(attack_clan_name)) {
	        int assignedTeam = getNextAvailableTeamIndex();
	        _clanTeamMap.put(attack_clan_name, assignedTeam);
	    }
	}


	public void RemoveAttackClan(String attack_clan_name) {
		if (_attackClanList.contains(attack_clan_name)) {
			_attackClanList.remove(attack_clan_name);
		}
	}

	public boolean CheckAttackClan(String attack_clan_name) {
		if (_attackClanList.contains(attack_clan_name)) {
			return true;
		}
		return false;
	}

	public String[] GetAttackClanList() {
		return _attackClanList.toArray(new String[_attackClanList.size()]);
	}

	public int GetAttackClanListSize() {
		return _attackClanList.size();
	}

	public int GetCastleId() {
		int castle_id = 0;
		if (GetWarType() == 1) {
			L1Clan clan = L1World.getInstance().getClan(GetDefenceClanName());
			if (clan != null) {
				castle_id = clan.getCastleId();
			}
		}
		return castle_id;
	}

	public L1Castle GetCastle() {
		L1Castle l1castle = null;
		if (GetWarType() == 1) {
			L1Clan clan = L1World.getInstance().getClan(GetDefenceClanName());
			if (clan != null) {
				int castle_id = clan.getCastleId();
				l1castle = CastleTable.getInstance().getCastleTable(castle_id);
			}
		}
		return l1castle;
	}
	
	public void addPointsToTeam(int teamId, int points) {
	    _teamPoints.put(teamId, _teamPoints.getOrDefault(teamId, 0) + points);
	}

	public int getPointsForTeam(int teamId) {
	    return _teamPoints.getOrDefault(teamId, 0);
	}

	public void setTowerCapturer(int towerId, String playerName) {
	    _towerCapturer.put(towerId, playerName);
	}

	public String getTowerCapturer(int towerId) {
	    return _towerCapturer.getOrDefault(towerId, null);
	}
	
	private boolean _ended = false;

	public void endWar() {
	    if (_ended) return;
	    _ended = true;

	    L1World.getInstance().removeWar(this);

	    // Optional: reset team state if needed
	    _clanTeamMap.clear();
	    _teamPoints.clear();
	}


}
