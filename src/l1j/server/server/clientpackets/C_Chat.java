/*
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
package l1j.server.server.clientpackets;

import static l1j.server.server.model.skill.L1SkillId.AREA_OF_SILENCE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_GLOBAL_CHAT_PROHIBITED;
import static l1j.server.server.model.skill.L1SkillId.SILENCE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_POISON_SILENCE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import l1j.server.Config;
import l1j.server.server.GMCommands;
import l1j.server.server.PCommands;
import l1j.server.server.datatables.ChatLogTable;
import l1j.server.server.datatables.ExcludeTable;
import l1j.server.server.encryptions.Opcodes;
import l1j.server.server.model.BotCheckActivityManager;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1War;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.network.Client;
import l1j.server.server.serverpackets.S_ChatPacket;
import l1j.server.server.serverpackets.S_NpcChatPacket;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_RawStringDialog;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;

public class C_Chat extends ClientBasePacket {
	private static final String C_CHAT = "[C] C_Chat";
	private static final int SHOUT_RANGE = 50;

	private static void sendChatPacket(final L1PcInstance sender,
			final Iterable<L1PcInstance> receivers, final String text,
			final int type, final int opcode) {
		ChatLogTable.getInstance().storeChat(sender, null, text, type);
		S_ChatPacket packet = new S_ChatPacket(sender, text, opcode, type);
		for (L1PcInstance receiver : filterForIgnored(sender, receivers))
			receiver.sendPackets(packet);
	}

	private static Iterable<L1PcInstance> filterForIgnored(
			final L1PcInstance sender, final Iterable<L1PcInstance> receivers) {
		ExcludeTable excludeTable =  ExcludeTable.getInstance();
		List<L1PcInstance> filtered = new ArrayList<L1PcInstance>();
		for (L1PcInstance receiver : receivers)
			if (!excludeTable.isExcluded(receiver, sender))
				filtered.add(receiver);
		return filtered;
	}

	public C_Chat(byte abyte0[], Client clientThread) {
		super(abyte0);

		L1PcInstance pc = clientThread.getActiveChar();
		
		if (pc == null) {
		    return; // Connection was lost or not properly established
		}
		
		int chatType = readC();
		System.out.println("Chat Case readC: " + chatType);
		String chatText = readS();
		// 🛡️ Handle Bot Check Answer
		if (pc.isAwaitingBotCheck()) {
		    String expectedAnswer = pc.getBotCheckQuestion();
		    if (expectedAnswer != null && chatText != null) {
		    	String playerAnswer = chatText.trim().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
		    	String correctAnswer = expectedAnswer.trim().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
		    if (playerAnswer.equals(correctAnswer)) {
		            // ✅ Correct Answer
		            BotCheckActivityManager.handleBotCheckSuccess(pc); // <<< ADD THIS
		            pc.setAwaitingBotCheck(false);
		            pc.setBotCheckQuestion(null);
		            pc.setBotCheckStartTime(0);
		        } else {
		            pc.sendPackets(new S_SystemMessage("Incorrect answer. Please try again."));
			        // 🛡️ If window was closed but they type something, resend question
			    	pc.sendPackets(new S_RawStringDialog(pc.getId(), "Bot Check", pc.getBotCheckQuestionText()));
			    	pc.sendPackets(new S_SystemMessage("Please answer the bot check question."));
		        }
		    }
		    return; // Do not process their chat normally if they are mid-bot-check
		}


		System.out.println("Chat Case readS: " + chatText);
		if (pc.hasSkillEffect(SILENCE) || pc.hasSkillEffect(AREA_OF_SILENCE)
				|| pc.hasSkillEffect(STATUS_POISON_SILENCE)
				|| pc.hasSkillEffect(AREA_OF_SILENCE)) {
			return;
		}
		if (pc.hasSkillEffect(1005)) { // Chat prohibited
			pc.sendPackets(new S_ServerMessage(242)); // Chat is currently prohibited.
			return;
		}

		// Restrict chat in Deathmatch map (map ID: 5153) for non-GM players
		if (pc.getMapId() == 5153 && !pc.isGm()) {
			return;
		}

		if (chatType == 0 && !chatText.startsWith("!")) { // Normal chat
			if (pc.isGhost() && !(pc.isGm())) {
				return;
			}
			// GM commands
			if (chatText.startsWith(".") && chatText.length() > 1) {
				String cmd = chatText.substring(1);
				GMCommands.getInstance().handleCommands(pc, cmd);
				return;
			}
			
			// Player commands
			else if (chatText.startsWith("-")) {
				String cmd = chatText.substring(1);
				PCommands.getInstance().handleCommands(pc, cmd);
				return;
			}
			
			// Trade chat
			// It should be chatType==12, but the leading $ is not sent
			if (chatText.startsWith("$")) {
				String text = chatText.substring(1);
				chatWorld(pc, text, 12);
				if (!pc.isGm()) {
					pc.checkChatInterval();
				}
				return;
			}

			ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
			S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,
					Opcodes.S_OPCODE_NPCSHOUT, 0);
			if (!ExcludeTable.getInstance().isExcluded(pc, pc)) {
				pc.sendPackets(s_chatpacket);
			}
			for (L1PcInstance listner : L1World.getInstance()
					.getRecognizePlayer(pc)) {
				if (listner.getMapId() > 10000
						&& pc.getMapId() <= 10000) { // Inn check
					break;
				} else if (!ExcludeTable.getInstance().isExcluded(listner, pc)) {
					listner.sendPackets(s_chatpacket);
				}
			}
			// Doppelganger processing
			for (L1Object obj : pc.getKnownObjects()) {
				if (obj instanceof L1MonsterInstance) {
					L1MonsterInstance mob = (L1MonsterInstance) obj;
					if (mob.getNpcTemplate().is_doppel()
							&& mob.getName().equals(pc.getName())) {
						mob.broadcastPacket(new S_NpcChatPacket(mob, chatText,
								0));
					}
				}
			}
		} else if (chatType == 0 && chatText.startsWith("!")) { // Normal chat (Shout)
		    if (pc.isGhost()) {
		        return;
		    }

		    chatText = chatText.substring(1); // 🔹 Remove the first "!"

		    ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
		    S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_NORMALCHAT, 2);

		    if (!ExcludeTable.getInstance().isExcluded(pc, pc)) {
		        pc.sendPackets(s_chatpacket);
		    }
		    for (L1PcInstance listner : L1World.getInstance().getVisiblePlayer(pc, 50)) {
		        if (listner.getMapId() < 16384 || listner.getMapId() > 25088 || pc.getMapId() <= 10000) {
		            continue; // Skip if conditions apply
		        }
		        if (!ExcludeTable.getInstance().isExcluded(listner, pc)) {
		            listner.sendPackets(s_chatpacket);
		        }
		    }

		    // Doppelganger NPC Chat (if needed)
		    for (L1Object obj : pc.getKnownObjects()) {
		        if (obj instanceof L1MonsterInstance) {
		            L1MonsterInstance mob = (L1MonsterInstance) obj;
		            if (mob.getNpcTemplate().is_doppel() && mob.getName().equals(pc.getName()) && !mob.isDead()) {
		                for (L1PcInstance listner : L1World.getInstance().getVisiblePlayer(mob, 50)) {
		                    listner.sendPackets(new S_NpcChatPacket(mob, chatText, 2));
		                }
		            }
		        }
		    }
		} else if (chatType == 2) { // Shout
			if (pc.isGhost()) {
				return;
			}
			ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
			S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,
					Opcodes.S_OPCODE_NORMALCHAT, 2);
			if (!ExcludeTable.getInstance().isExcluded(pc, pc)) {
				pc.sendPackets(s_chatpacket);
			}
			for (L1PcInstance listner : L1World.getInstance().getVisiblePlayer(
					pc, 50)) {
				if (listner.getMapId() > 10000
						&& pc.getMapId() <= 10000) { // Inn check
					break;
				} else if (!ExcludeTable.getInstance().isExcluded(listner, pc)) {
					listner.sendPackets(s_chatpacket);
				}
			}

			// Doppelganger processing
			for (L1Object obj : pc.getKnownObjects()) {
				if (obj instanceof L1MonsterInstance) {
					L1MonsterInstance mob = (L1MonsterInstance) obj;
					if (mob.getNpcTemplate().is_doppel()
							&& mob.getName().equals(pc.getName())) {
						for (L1PcInstance listner : L1World.getInstance()
								.getVisiblePlayer(mob, 50)) {
							listner.sendPackets(new S_NpcChatPacket(mob,
									chatText, 2));
						}
					}
				}
			}
		} else if (chatType == 3) { // Global chat
			chatWorld(pc, chatText, chatType);
		} else if (chatType == 4) { // Clan chat
			if (pc.getClanid() != 0) { // Member of a clan
				L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
				int rank = pc.getClanRank();
				if (clan != null
						&& (rank == L1Clan.CLAN_RANK_PROBATION
						|| rank == L1Clan.CLAN_RANK_PUBLIC
						|| rank == L1Clan.CLAN_RANK_GUARDIAN
						|| rank == L1Clan.CLAN_RANK_PRINCE)) {
					ChatLogTable.getInstance().storeChat(pc, null, chatText,
							chatType);
					S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,
							Opcodes.S_OPCODE_GLOBALCHAT, 4);
					L1PcInstance[] clanMembers = clan.getOnlineClanMember();
					for (L1PcInstance listner : clanMembers) {
						if (!ExcludeTable.getInstance().isExcluded(listner, pc)) {
							if (Config.CLAN_CHAT)
								listner.sendPackets(s_chatpacket);
						}
					}
				}
			}
		} else if (chatType == 11) { // Party chat
			if (pc.isInParty()) { // In party
				ChatLogTable.getInstance().storeChat(pc, null, chatText,
						chatType);
				S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,
						Opcodes.S_OPCODE_GLOBALCHAT, 11);
				L1PcInstance[] partyMembers = pc.getParty().getMembers();
				for (L1PcInstance listner : partyMembers) {
					if (!ExcludeTable.getInstance().isExcluded(listner, pc)) {
						if (Config.PARTY_CHAT)
							listner.sendPackets(s_chatpacket);
					}
				}
			}
		} else if (chatType == 12) { // Trade chat
			chatWorld(pc, chatText, chatType);
		} else if (chatType == 13) { // Alliance chat
			if (pc.getClanid() != 0) { // Member of a clan
				L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
				int rank = pc.getClanRank();
				if (clan != null
						&& (rank == L1Clan.CLAN_RANK_GUARDIAN
						|| rank == L1Clan.CLAN_RANK_GUARDIAN
						|| rank == L1Clan.CLAN_RANK_PRINCE)) {
					ChatLogTable.getInstance().storeChat(pc, null, chatText,
							chatType);
					S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,
							Opcodes.S_OPCODE_GLOBALCHAT, 13);
					L1PcInstance[] clanMembers = clan.getOnlineClanMember();
					for (L1PcInstance listner : clanMembers) {
						int listnerRank = listner.getClanRank();
						if (!ExcludeTable.getInstance().isExcluded(listner, pc)
								&& (listnerRank == L1Clan.CLAN_RANK_GUARDIAN
								|| listnerRank == L1Clan.CLAN_RANK_GUARDIAN
								|| listnerRank == L1Clan.CLAN_RANK_PRINCE)) {
							listner.sendPackets(s_chatpacket);
						}
					}
				}
			}
		} else if (chatType == 14) { // Chat party
			if (pc.isInChatParty()) { // In chat party
				ChatLogTable.getInstance().storeChat(pc, null, chatText,
						chatType);
				S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,
						Opcodes.S_OPCODE_GLOBALCHAT, 14);
				L1PcInstance[] partyMembers = pc.getChatParty().getMembers();
				for (L1PcInstance listner : partyMembers) {
					if (!ExcludeTable.getInstance().isExcluded(listner, pc)) {
						listner.sendPackets(s_chatpacket);
					}
				}
			}
		} else if (chatType == 15) { // Alliance chat
			if (pc.getClanid() != 0) { // Member of a clan
				L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
				int rank = pc.getClanRank();
				if (clan != null
						&& (rank == L1Clan.CLAN_RANK_PROBATION
								|| rank == L1Clan.CLAN_RANK_PUBLIC
								|| rank == L1Clan.CLAN_RANK_GUARDIAN || rank == L1Clan.CLAN_RANK_PRINCE)) {
					ChatLogTable.getInstance().storeChat(pc, null, chatText,
							chatType);
					S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,
							Opcodes.S_OPCODE_GLOBALCHAT, 15);

					for (L1Clan alliClan : L1World.getInstance().getAllClans()) {
						if (clan.getClanId() == alliClan.getClanId()) {
							continue;
						}
						for (L1War war : L1World.getInstance().getWarList()) {
							if (war.CheckClanInWar(clan.getClanName())) {
								if (war.CheckClanInSameWar(clan.getClanName(),
										alliClan.getClanName())) {
									L1PcInstance[] alliClanMembers = alliClan
											.getOnlineClanMember();
									for (L1PcInstance alliMember : alliClanMembers) {
										if (!ExcludeTable.getInstance().isExcluded(alliMember, pc)) {
											if (Config.CLAN_CHAT
													&& chatType == 15)
												alliMember
														.sendPackets(s_chatpacket);
										}
									}
								}
							}
						}
					}
					L1PcInstance[] clanMembers = clan.getOnlineClanMember();
					for (L1PcInstance listner : clanMembers) {
						if (!ExcludeTable.getInstance().isExcluded(listner, pc)) {
							if (Config.CLAN_CHAT && chatType == 15)// Clan
								listner.sendPackets(s_chatpacket);
						}
					}
				}
			}
		}

		if (!pc.isGm()) {
			pc.checkChatInterval();
		}
	}

	private void chatWorld(L1PcInstance pc, String chatText, int chatType) {
	    if (pc.hasSkillEffect(STATUS_GLOBAL_CHAT_PROHIBITED)) {
	    	pc.sendPackets(new S_SystemMessage("You are banned from using global chat."));
	        return;
	    }

	    if (pc.isGm()) {
	        ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
	        S_ChatPacket packet = new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_GLOBALCHAT, chatType);
	        for (L1PcInstance listner : L1World.getInstance().getAllPlayers()) {
	            if (!ExcludeTable.getInstance().isExcluded(listner, pc)) {
	                listner.sendPackets(packet);
	            }
	        }
	    } else if (pc.getLevel() >= Config.GLOBAL_CHAT_LEVEL) {
	        if (L1World.getInstance().isWorldChatElabled()) {
	            if (pc.get_food() >= 6) {
	                pc.sendPackets(new S_PacketBox(S_PacketBox.FOOD, pc.get_food()));
	                ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
	                S_ChatPacket packet = new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_GLOBALCHAT, chatType);
	                for (L1PcInstance listner : L1World.getInstance().getAllPlayers()) {
	                    if (!ExcludeTable.getInstance().isExcluded(listner, pc)) {
	                        listner.sendPackets(packet);
	                    }
	                }
	            } else {
	                pc.sendPackets(new S_ServerMessage(462)); // Not enough satiety to speak far away.
	            }
	        } else {
	            pc.sendPackets(new S_ServerMessage(510)); // World chat is currently suspended.
	        }
	    } else {
	        pc.sendPackets(new S_ServerMessage(195, String.valueOf(Config.GLOBAL_CHAT_LEVEL))); // Characters below level %0 cannot use global chat.
	    }
	}



	@Override
	public String getType() {
		return C_CHAT;
	}
}
