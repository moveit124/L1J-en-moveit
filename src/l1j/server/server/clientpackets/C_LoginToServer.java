/* This program is free software; you can redistribute it and/or modify
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
package l1j.server.server.clientpackets;

import static l1j.server.server.model.skill.L1SkillId.COOKING_1_0_N;
import static l1j.server.server.model.skill.L1SkillId.PLAYERSTATUS_HPBAR;
import static l1j.server.server.model.skill.L1SkillId.COOKING_1_0_S;
import static l1j.server.server.model.skill.L1SkillId.COOKING_1_6_N;
import static l1j.server.server.model.skill.L1SkillId.COOKING_1_6_S;
import static l1j.server.server.model.skill.L1SkillId.COOKING_2_0_N;
import static l1j.server.server.model.skill.L1SkillId.COOKING_2_0_S;
import static l1j.server.server.model.skill.L1SkillId.COOKING_2_6_N;
import static l1j.server.server.model.skill.L1SkillId.COOKING_2_6_S;
import static l1j.server.server.model.skill.L1SkillId.COOKING_3_0_N;
import static l1j.server.server.model.skill.L1SkillId.COOKING_3_0_S;
import static l1j.server.server.model.skill.L1SkillId.COOKING_3_6_N;
import static l1j.server.server.model.skill.L1SkillId.COOKING_3_6_S;
import static l1j.server.server.model.skill.L1SkillId.SHAPE_CHANGE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_BLUE_POTION;
import static l1j.server.server.model.skill.L1SkillId.STATUS_BRAVE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CHAT_PROHIBITED;
import static l1j.server.server.model.skill.L1SkillId.STATUS_ELFBRAVE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HASTE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_UNDERWATER_BREATH;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CHAT_PROHIBITED;
import static l1j.server.server.model.skill.L1SkillId.STATUS_POISON;
import static l1j.server.server.model.skill.L1SkillId.STATUS_POISON_SILENCE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_POISON_PARALYZING;
import static l1j.server.server.model.skill.L1SkillId.STATUS_POISON_PARALYZED;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CURSE_PARALYZING;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CURSE_PARALYZED;
import static l1j.server.server.model.skill.L1SkillId.STATUS_FLOATING_EYE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HOLY_WATER;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HOLY_MITHRIL_POWDER;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HOLY_WATER_OF_EVA;
import static l1j.server.server.model.skill.L1SkillId.STATUS_ELFBRAVE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_RIBRAVE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CUBE_IGNITION_TO_ALLY;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CUBE_QUAKE_TO_ALLY;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CUBE_SHOCK_TO_ALLY;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CUBE_BALANCE;
//import static l1j.server.server.model.skill.L1SkillId.STATUS_THIRD_SPEED;
import static l1j.server.server.model.skill.L1SkillId.MIRROR_IMAGE;
import static l1j.server.server.model.skill.L1SkillId.UNCANNY_DODGE;
import static l1j.server.server.model.skill.L1SkillId.RESIST_FEAR;
//import static l1j.server.server.model.skill.L1SkillId.STATUS_FLORA_POTION_STR;
//import static l1j.server.server.model.skill.L1SkillId.STATUS_FLORA_POTION_DEX;
import static l1j.server.server.model.skill.L1SkillId.STATUS_FREEZE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CURSE_BARLOG;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CURSE_YAHEE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_WEAKNESS_EXPOSURE_LV1;
import static l1j.server.server.model.skill.L1SkillId.STATUS_WEAKNESS_EXPOSURE_LV2;
import static l1j.server.server.model.skill.L1SkillId.STATUS_WEAKNESS_EXPOSURE_LV3;
import static l1j.server.server.model.skill.L1SkillId.STATUS_DESTRUCTION_NOSTRUM;
/*import static l1j.server.server.model.skill.L1SkillId.STATUS_EXP_UP;
import static l1j.server.server.model.skill.L1SkillId.STATUS_EXP_UP_II;
import static l1j.server.server.model.skill.L1SkillId.POTION_OF_SWORDMAN;
import static l1j.server.server.model.skill.L1SkillId.POTION_OF_MAGICIAN;
import static l1j.server.server.model.skill.L1SkillId.POTION_OF_RECOVERY;
import static l1j.server.server.model.skill.L1SkillId.POTION_OF_MEDITATION;
import static l1j.server.server.model.skill.L1SkillId.POTION_OF_LIFE;
import static l1j.server.server.model.skill.L1SkillId.POTION_OF_MAGIC;
import static l1j.server.server.model.skill.L1SkillId.POTION_OF_MAGIC_RESIST;
import static l1j.server.server.model.skill.L1SkillId.POTION_OF_STR;
import static l1j.server.server.model.skill.L1SkillId.POTION_OF_DEX;
import static l1j.server.server.model.skill.L1SkillId.POTION_OF_CON;
import static l1j.server.server.model.skill.L1SkillId.POTION_OF_INT;
import static l1j.server.server.model.skill.L1SkillId.POTION_OF_WIS;
import static l1j.server.server.model.skill.L1SkillId.POTION_OF_RAGE;*/

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.ActionCodes;
import l1j.server.server.GMCommands;
import l1j.server.server.PCommands;
import l1j.server.server.command.L1Commands;
import l1j.server.server.controllers.CrackOfTimeController;
import l1j.server.server.controllers.JailController;
import l1j.server.server.controllers.JailController.JailInfo;
import l1j.server.server.controllers.WarTimeController;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.datatables.GetBackRestartTable;
import l1j.server.server.datatables.LogReporterTable;
import l1j.server.server.datatables.SkillTable;
import l1j.server.server.log.LogIP;
import l1j.server.server.model.BotCheckActivityManager;
import l1j.server.server.model.Getback;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1Cooking;
import l1j.server.server.model.L1Cube;
import l1j.server.server.model.L1CurseParalysis;
import l1j.server.server.model.L1FourthOfJulyEvent;
import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.L1War;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.model.poison.L1DamagePoison;
import l1j.server.server.model.poison.L1ParalysisPoison;
import l1j.server.server.model.poison.L1SilencePoison;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.network.Client;
import l1j.server.server.serverpackets.S_ActiveSpells;
import l1j.server.server.serverpackets.S_AddSkill;
import l1j.server.server.serverpackets.S_BookmarkLoad;
import l1j.server.server.serverpackets.S_CharTitle;
import l1j.server.server.serverpackets.S_CharacterConfig;
import l1j.server.server.serverpackets.S_ClanName;
import l1j.server.server.serverpackets.S_Emblem;
import l1j.server.server.serverpackets.S_InitialAbilityGrowth;
import l1j.server.server.serverpackets.S_InvList;
import l1j.server.server.serverpackets.S_Karma;
import l1j.server.server.serverpackets.S_Liquor;
import l1j.server.server.serverpackets.S_LoginGame;
import l1j.server.server.serverpackets.S_Mail;
import l1j.server.server.serverpackets.S_MapID;
import l1j.server.server.serverpackets.S_OwnCharAttrDef;
import l1j.server.server.serverpackets.S_OwnCharPack;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_Paralysis;
import l1j.server.server.serverpackets.S_PlayTime;
import l1j.server.server.serverpackets.S_RuneSlot;
import l1j.server.server.serverpackets.S_SPMR;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillBrave;
import l1j.server.server.serverpackets.S_SkillHaste;
import l1j.server.server.serverpackets.S_SkillIconAura;
import l1j.server.server.serverpackets.S_SkillIconBlessOfEva;
import l1j.server.server.serverpackets.S_SkillIconGFX;
import l1j.server.server.serverpackets.S_SkillIconWisdomPotion;
import l1j.server.server.serverpackets.S_SummonPack;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_War;
import l1j.server.server.serverpackets.S_Weather;
import l1j.server.server.serverpackets.S_bonusstats;
import l1j.server.server.templates.L1Command;
import l1j.server.server.templates.L1GetBackRestart;
import l1j.server.server.templates.L1Skill;
import l1j.server.server.utils.SQLUtil;
import l1j.server.server.utils.SystemUtil;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket
public class C_LoginToServer extends ClientBasePacket {
	private static final String C_LOGIN_TO_SERVER = "[C] C_LoginToServer";
	private static Logger _log = LoggerFactory.getLogger(C_LoginToServer.class.getName());
	// See note on updateIcons()
	private static List<String> accountsWithIcons = new ArrayList<String>();

	public C_LoginToServer(byte abyte0[], Client client) throws FileNotFoundException, Exception {
		super(abyte0);

		String login = client.getAccountName();
		String charName = readS();

		if (client.getActiveChar() != null) {
			_log.info("Invalid character logged in from " + client.getHostname() + ".");
			client.close();
			return;
		}
		L1PcInstance pc = L1PcInstance.load(charName);
		if (pc == null || !login.equals(pc.getAccountName())) {
			_log.info("Invalid login request=" + charName + " account=" + login + " host=" + client.getHostname());
			client.close();
			return;
		}
		BotCheckActivityManager.loadBotCheckProgress(pc);

		// auto-run any auto-run commands the user has access to
		for (L1Command command : L1Commands.availableCommandList(pc.getAccessLevel().getLevel())) {
			if (command.isRunOnLogin())
				GMCommands.getInstance().handleCommands(pc, command.getName());
		}

		if (Config.LEVEL_DOWN_RANGE != 0) {
			if (pc.getHighLevel() - pc.getLevel() >= Config.LEVEL_DOWN_RANGE) {
				_log.info("Login request of the character which exceeded: char=" + charName + " account=" + login
						+ " host=" + client.getHostname());
				client.kick();
				return;
			}
		}
		_log.info("Character login: char=" + charName + " account=" + login + " host=" + client.getHostname()
				+ " Current Memory: " + SystemUtil.getUsedMemoryMB() + "MB RAM" + " CharactersOnline="
				+ (L1World.getInstance().getAllPlayers().size() + 1));
		client.setLastActiveCharName(charName);
		LogIP li = new LogIP();
		li.storeLogIP(pc, client.getHostname());

		int currentHpAtLoad = pc.getCurrentHp();
		int currentMpAtLoad = pc.getCurrentMp();

		pc.clearSkillMastery();

		pc.setOnlineStatus(1);
		CharacterTable.updateOnlineStatus(pc);

		L1World.getInstance().storeObject(pc);

		pc.setNetConnection(client);

		pc.setPacketOutput(client);
		client.setActiveChar(pc);
		
		pc.sendPackets(new S_InitialAbilityGrowth(pc));
		pc.sendPackets(new S_LoginGame());
		pc.sendPackets(new S_BookmarkLoad(pc));
		
		//TODO -- set minigame playing if working

		GetBackRestartTable gbrTable = GetBackRestartTable.getInstance();
		L1GetBackRestart[] gbrList = gbrTable.getGetBackRestartTableList();
		for (L1GetBackRestart gbr : gbrList) {
			if (pc.getMapId() == gbr.getArea()) {
				pc.setX(gbr.getLocX());
				pc.setY(gbr.getLocY());
				pc.setMap(gbr.getMapId());
				break;
			}
		}
		if (Config.GET_BACK) {
			int[] loc = Getback.GetBack_Location(pc, true);
			pc.setX(loc[0]);
			pc.setY(loc[1]);
			pc.setMap((short) loc[2]);
		}

		int castle_id = L1CastleLocation.getCastleIdByArea(pc);
		if (0 < castle_id) {
			if (WarTimeController.getInstance().isNowWar(castle_id)) {
				L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
				if (clan != null) {
					if (clan.getCastleId() != castle_id) {
						int[] loc = new int[3];
						loc = L1CastleLocation.getGetBackLoc(castle_id);
						pc.setX(loc[0]);
						pc.setY(loc[1]);
						pc.setMap((short) loc[2]);
					}
				} else {
					int[] loc = new int[3];
					loc = L1CastleLocation.getGetBackLoc(castle_id);
					pc.setX(loc[0]);
					pc.setY(loc[1]);
					pc.setMap((short) loc[2]);
				}
			}
		}

		L1World.getInstance().addVisibleObject(pc);
		pc.sendPackets(new S_Mail(pc , 0));
		pc.sendPackets(new S_Mail(pc , 1));
		pc.sendPackets(new S_Mail(pc , 2));
		pc.sendPackets(new S_ActiveSpells(pc));
		
		pc.beginGameTimeCarrier();
		
		pc.sendPackets(new S_OwnCharStatus(pc));
		pc.sendPackets(new S_MapID(pc.getMapId(), pc.getMap().isUnderwater()));
		pc.sendPackets(new S_OwnCharPack(pc));
		pc.sendPackets(new S_SPMR(pc));
		
		S_CharTitle s_charTitle = new S_CharTitle(pc.getId(), pc.getTitle());
		pc.sendPackets(s_charTitle);
		pc.broadcastPacket(s_charTitle);

		//updateIcons(pc);
		pc.sendVisualEffectAtLogin();
		
		pc.sendPackets(new S_Weather(L1World.getInstance().getWeather()));
		
		items(pc);
		skills(pc);
		buff(client, pc);
		buffBlessOfAin(pc);
		
		pc.turnOnOffLight();
		
		pc.sendPackets(new S_Karma(pc)); 
		pc.sendPackets(new S_PacketBox(S_PacketBox.DODGE_RATE_PLUS, pc.getDodge()));
		pc.sendPackets(new S_PacketBox(S_PacketBox.DODGE_RATE_MINUS, pc.getNdodge()));
		
		pc.sendPackets(new S_RuneSlot(S_RuneSlot.RUNE_CLOSE_SLOT, 3));
		pc.sendPackets(new S_RuneSlot(S_RuneSlot.RUNE_OPEN_SLOT, 1));

		pc.setEquipped(pc, true);
		
		
		//TODO -- pc.setServivalScream();
		
		//TODO -- checkPledgeRecommendation
		

		if (pc.getCurrentHp() > 0) {
			pc.setDead(false);
			pc.setStatus(0);
		} else {
			pc.setDead(true);
			pc.setStatus(ActionCodes.ACTION_Die);
		}
		if (pc.getLevel() >= 51 && pc.getLevel() - 50 > pc.getBonusStats()) {
			if ((pc.getBaseStr() + pc.getBaseDex() + pc.getBaseCon() + pc.getBaseInt() + pc.getBaseWis()
					+ pc.getBaseCha()) < 210) {
				pc.sendPackets(new S_bonusstats(pc.getId(), 1));
			}
		}
		if (Config.CHARACTER_CONFIG_IN_SERVER_SIDE) {
			pc.sendPackets(new S_CharacterConfig(pc.getId()));
		}

		searchSummon(pc);

		WarTimeController.getInstance().checkCastleWar(pc);

		if (pc.getClanid() != 0) {
			L1Clan clan = L1World.getInstance().getClan(pc.getClanname());

			// *** REVISED: Send S_ClanName with BASE clanId ***
			int baseClanId = clan.getClanId(); // Get the BASE clan ID
			_log.debug("Player {} Clan {} Base ClanID: {}", pc.getName(), clan.getClanName(), baseClanId); // Optional debug log
			// Send the packet using the player's context (pc), the BASE clanId, and player's rank
			pc.sendPackets(new S_ClanName(pc, baseClanId, pc.getClanRank())); 
			// *** END REVISED ***
			if (clan != null) {
				if (pc.getClanid() == clan.getClanId()
						&& pc.getClanname().toLowerCase().equals(clan.getClanName().toLowerCase())) {
					L1PcInstance[] clanMembers = clan.getOnlineClanMember();
					for (L1PcInstance clanMember : clanMembers) {
						if (clanMember.getId() != pc.getId()) {
							clanMember.sendPackets(new S_ServerMessage(843, pc.getName()));
						}
					}
					for (L1War war : L1World.getInstance().getWarList()) {
						boolean ret = war.CheckClanInWar(pc.getClanname());
						if (ret) {
							String enemy_clan_name = war.GetEnemyClanName(pc.getClanname());
							if (enemy_clan_name != null) {
								pc.sendPackets(new S_War(8, pc.getClanname(), enemy_clan_name));
							}
							break;
						}
					}
				} else {
					pc.setClanid(0);
					pc.setClanname("");
					pc.setClanRank(0);
					pc.save();
				}
			}
		}
		if (pc.getPartnerId() != 0) {
			L1PcInstance partner = (L1PcInstance) L1World.getInstance().findObject(pc.getPartnerId());
			if (partner != null && partner.getPartnerId() != 0) {
				if (pc.getPartnerId() == partner.getId() && partner.getPartnerId() == pc.getId()) {
					pc.sendPackets(new S_ServerMessage(548));
					partner.sendPackets(new S_ServerMessage(549));
				}
			}
		}
		if (currentHpAtLoad > pc.getCurrentHp()) {
			pc.setCurrentHp(currentHpAtLoad);
		}
		if (currentMpAtLoad > pc.getCurrentMp()) {
			pc.setCurrentMp(currentMpAtLoad);
		}
		pc.startHpRegeneration();
		pc.startMpRegeneration();
		pc.startObjectAutoUpdate();
		client.CharReStart(false);
		pc.beginExpMonitor();
		pc.save();
		pc.sendPackets(new S_OwnCharStatus(pc));
		if (pc.getHellTime() > 0) {
			pc.beginHell(false);
		}
		checkUnreadMail(pc);
		
		//TODO -- pc.startExpirationTimer();
		pc.startMapLimiter();
		pc.sendPackets(new S_PlayTime());

		if (Config.ALT_POLYEVENT)
			pc.sendPackets(new S_SystemMessage("\\fRPolymorph Event Is Currently Running. Enjoy!"));

		if (pc.getMapId() == 99 && !pc.isGm()) {
			pc.sendPackets(new S_SystemMessage("\\fRYou've logged in while jailed!"));

			JailInfo jailInfo = JailController.getInstance().getJailInfo(pc.getName());

			if (jailInfo == null) {
				pc.sendPackets(new S_SystemMessage("\\fRFor more info, contact a GameMaster."));
			} else {
				pc.sendPackets(new S_SystemMessage("\\fRReason: " + jailInfo.getMessage()));
				pc.sendPackets(new S_SystemMessage("\\fRWill be unjailed in: " + jailInfo.getReleaseTimeFormatted()));
			}
		}
		
		if (Config.BKLM_EVENT) {
			pc.sendPackets(new S_SystemMessage(L1FourthOfJulyEvent.getCurrentBonusMessage()));
		}
		
		pc.checkPersistentMute(pc);
		pc.startBotTracking();

		if (CrackOfTimeController.getCrackOfTimeStatus()) {
			pc.sendPackets(new S_SystemMessage("The Crack of Time is open."));	
		}

		long lastReport = LogReporterTable.getLastSuspicion(pc.getId());
		long lastReportEndTime = lastReport + (Config.REPORT_TIME_MINUTES * 60000);

		if (lastReport > 0 && lastReportEndTime > System.currentTimeMillis()) {
			pc.enableLogPackets(lastReportEndTime);
		}
	}

	private void checkUnreadMail(final L1PcInstance character) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet result = null;

		try {
			connection = L1DatabaseFactory.getInstance().getConnection();
			statement = connection.prepareStatement("SELECT COUNT(*) FROM mail WHERE inbox_id=? AND receiver=? AND read_status=?");
			statement.setInt(1, character.getId());
			statement.setString(2, character.getName());
			statement.setInt(3, 0);

			result = statement.executeQuery();
			result.next();
			int count = result.getInt(1);
			if (count > 0) {
				String message = String.format("You've got %d unread %s!", count, count == 1 ? "message" : "messages");
				character.sendPackets(new S_SystemMessage(message));
			}
		} catch (SQLException e) {
			_log.error(e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(statement);
			SQLUtil.close(connection);
		}
	}

	// Updates the given client with all pledge icons.
	// Keeps a cache of seen account names - it's a slow leak, but seems better
	// than bombarding each client every time a character logs on.
	private void updateIcons(final L1PcInstance character) {
		for (L1Clan clan : L1World.getInstance().getAllClans()) {
			character.sendPackets(new S_Emblem(clan.getClanId()));
		}
	}


	private void items(L1PcInstance pc) {
		CharacterTable.getInstance().restoreInventory(pc);
		pc.sendPackets(new S_InvList(pc.getInventory().getItems()));
	}

	private void skills(L1PcInstance pc) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;

		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM character_skills WHERE char_obj_id=?");
			pstm.setInt(1, pc.getId());
			rs = pstm.executeQuery();
			int i = 0;
			int lv1 = 0;
			int lv2 = 0;
			int lv3 = 0;
			int lv4 = 0;
			int lv5 = 0;
			int lv6 = 0;
			int lv7 = 0;
			int lv8 = 0;
			int lv9 = 0;
			int lv10 = 0;
			int lv11 = 0;
			int lv12 = 0;
			int lv13 = 0;
			int lv14 = 0;
			int lv15 = 0;
			int lv16 = 0;
			int lv17 = 0;
			int lv18 = 0;
			int lv19 = 0;
			int lv20 = 0;
			int lv21 = 0;
			int lv22 = 0;
			int lv23 = 0;
			int lv24 = 0;
			int lv25 = 0;
			int lv26 = 0;
			int lv27 = 0;
			int lv28 = 0;
			while (rs.next()) {
				int skillId = rs.getInt("skill_id");
				L1Skill l1skills = SkillTable.getInstance().findBySkillId(skillId);
				if (l1skills.getSkillLevel() == 1) {
					lv1 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 2) {
					lv2 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 3) {
					lv3 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 4) {
					lv4 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 5) {
					lv5 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 6) {
					lv6 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 7) {
					lv7 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 8) {
					lv8 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 9) {
					lv9 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 10) {
					lv10 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 11) {
					lv11 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 12) {
					lv12 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 13) {
					lv13 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 14) {
					lv14 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 15) {
					lv15 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 16) {
					lv16 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 17) {
					lv17 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 18) {
					lv18 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 19) {
					lv19 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 20) {
					lv20 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 21) {
					lv21 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 22) {
					lv22 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 23) {
					lv23 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 24) {
					lv24 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 25) {
					lv25 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 26) {
					lv26 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 27) {
					lv27 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 28) {
					lv28 |= l1skills.getId();
				}
				i = lv1 + lv2 + lv3 + lv4 + lv5 + lv6 + lv7 + lv8 + lv9 + lv10 + lv11 + lv12 + lv13 + lv14 + lv15 + lv16
						+ lv17 + lv18 + lv19 + lv20 + lv21 + lv22 + lv23 + lv24 + lv25 + lv26 + lv27 + lv28;
				pc.setSkillMastery(skillId);
			}
			if (i > 0) {
				pc.sendPackets(new S_AddSkill(lv1, lv2, lv3, lv4, lv5, lv6, lv7, lv8, lv9, lv10, lv11, lv12, lv13, lv14,
						lv15, lv16, lv17, lv18, lv19, lv20, lv21, lv22, lv23, lv24, lv25, lv26, lv27, lv28));
			}
		} catch (SQLException e) {
			_log.error(e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	private void searchSummon(L1PcInstance pc) {
		for (L1SummonInstance summon : L1World.getInstance().getAllSummons()) {
			if (summon.getMaster().getId() == pc.getId()) {
				summon.setMaster(pc);
				pc.addPet(summon);
				for (L1PcInstance visiblePc : L1World.getInstance().getVisiblePlayer(summon)) {
					visiblePc.sendPackets(new S_SummonPack(summon, visiblePc));
				}
			}
		}
	}

	private void buff(Client client, L1PcInstance pc) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;

		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM character_buff WHERE char_obj_id=?");
			pstm.setInt(1, pc.getId());
			rs = pstm.executeQuery();
			while (rs.next()) {
				int skillid = rs.getInt("skill_id");
				int remaining_time = rs.getInt("remaining_time");
				if (skillid == SHAPE_CHANGE) {
					int poly_id = rs.getInt("poly_id");
					L1PolyMorph.doPoly(pc, poly_id, remaining_time, L1PolyMorph.MORPH_BY_LOGIN);
				} else if (skillid == STATUS_BRAVE) {
					pc.sendPackets(new S_SkillBrave(pc.getId(), 1, remaining_time));
					pc.broadcastPacket(new S_SkillBrave(pc.getId(), 1, 0));
					pc.setBraveSpeed(1);
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_ELFBRAVE) {
					pc.sendPackets(new S_SkillBrave(pc.getId(), 3, remaining_time));
					pc.broadcastPacket(new S_SkillBrave(pc.getId(), 3, 0));
					pc.setBraveSpeed(1);
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_HASTE) {
					pc.sendPackets(new S_SkillHaste(pc.getId(), 1, remaining_time));
					pc.broadcastPacket(new S_SkillHaste(pc.getId(), 1, 0));
					pc.setMoveSpeed(1);
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_BLUE_POTION) {
					pc.sendPackets(new S_SkillIconGFX(34, remaining_time));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_CHAT_PROHIBITED) {
					pc.sendPackets(new S_SkillIconGFX(36, remaining_time));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid >= COOKING_1_0_N && skillid <= COOKING_1_6_N
						|| skillid >= COOKING_1_0_S && skillid <= COOKING_1_6_S
						|| skillid >= COOKING_2_0_N && skillid <= COOKING_2_6_N
						|| skillid >= COOKING_2_0_S && skillid <= COOKING_2_6_S
						|| skillid >= COOKING_3_0_N && skillid <= COOKING_3_6_N
						|| skillid >= COOKING_3_0_S && skillid <= COOKING_3_6_S) {
					L1Cooking.eatCooking(pc, skillid, remaining_time);
				}  else if (skillid == STATUS_UNDERWATER_BREATH) { // エヴァの祝福
					pc.sendPackets(new S_SkillIconBlessOfEva(pc.getId(), remaining_time));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == 1004) { // ウィズダムポーション
					pc.sendPackets(new S_SkillIconWisdomPotion(remaining_time / 4));
					pc.addSp(2);
					pc.addMpr(2);
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_CHAT_PROHIBITED) { // チャット禁止
					pc.sendPackets(new S_SkillIconGFX(36, remaining_time));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_POISON) { // 毒
					L1DamagePoison.doInfection(pc, pc, 3000, 5); // 3秒周期で5ダメージ
				} else if (skillid == STATUS_POISON_SILENCE) { // 沈黙毒
					L1SilencePoison.doInfection(pc);
				} else if (skillid == STATUS_POISON_PARALYZING) { // 麻痺毒(麻痺前)
					L1ParalysisPoison.doInfection(pc, remaining_time * 1000, 16000);
				} else if (skillid == STATUS_POISON_PARALYZED) { // 麻痺毒(麻痺後)
					L1ParalysisPoison.doInfection(pc, 0, remaining_time * 1000);
				} else if (skillid == STATUS_CURSE_PARALYZING) { // 呪い麻痺(麻痺前)
					L1CurseParalysis.curse(pc, remaining_time * 1000, 16000);
				} else if (skillid == STATUS_CURSE_PARALYZED) { // 呪い麻痺(麻痺後)
					L1CurseParalysis.curse(pc, 0, remaining_time * 1000);
				} else if (skillid == STATUS_FLOATING_EYE) { // フローティングアイ肉
					pc.setSkillEffect(skillid, 0);
				} else if (skillid == STATUS_HOLY_WATER) { // 聖水
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_HOLY_MITHRIL_POWDER) { // 神聖なミスリルパウダー
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_HOLY_WATER_OF_EVA) { // 神聖なエヴァの水
					pc.setSkillEffect(skillid, remaining_time * 1000);
					pc.sendPackets(new S_SkillIconAura(221, remaining_time, 5));
				} else if (skillid == STATUS_ELFBRAVE) { // エルヴンワッフル
					pc.sendPackets(new S_SkillBrave(pc.getId(), 3, remaining_time));
					pc.broadcastPacket(new S_SkillBrave(pc.getId(), 3, 0));
					pc.setBraveSpeed(1);
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_RIBRAVE) { // ユグドラの実
					pc.setSkillEffect(skillid, remaining_time * 1000);
					pc.setBraveSpeed(1);
				} else if (skillid == STATUS_CUBE_IGNITION_TO_ALLY) { // キューブ[イグニション]
					pc.addFire(30);
					pc.sendPackets(new S_OwnCharAttrDef(pc));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_CUBE_QUAKE_TO_ALLY) { // キューブ[クエイク]
					pc.addEarth(30);
					pc.sendPackets(new S_OwnCharAttrDef(pc));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_CUBE_SHOCK_TO_ALLY) { // キューブ[ショック]
					pc.addWind(30);
					pc.sendPackets(new S_OwnCharAttrDef(pc));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_CUBE_BALANCE) { //	キューブ[バランス]
					pc.setSkillEffect(skillid, remaining_time * 1000);
					L1Cube cube = new L1Cube(pc, pc, skillid);
					cube.begin();
				}/* else if (skillid == STATUS_THIRD_SPEED) { // 三段加速
					pc.sendPackets(new S_Liquor(pc.getId(), 8));
					pc.broadcastPacket(new S_Liquor(pc.getId(), 8));
					pc.sendPackets(new S_SkillIconThirdSpeed(remaining_time / 4));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				}*/ else if (skillid == MIRROR_IMAGE || skillid == UNCANNY_DODGE) { // ミラーイメージ、アンキャニードッジ
					pc.addDodge((byte) 5); // 近距離回避率 + 50%
					pc.sendPackets(new S_PacketBox(S_PacketBox.DODGE_RATE_PLUS, pc.getDodge()));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == RESIST_FEAR) { // フィアー
					pc.addNdodge((byte) 5); // 近距離回避率 - 50%
					pc.sendPackets(new S_PacketBox(S_PacketBox.DODGE_RATE_MINUS, pc.getNdodge()));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				}/* else if (skillid == STATUS_FLORA_POTION_STR) { // 激励、フローラポーション
					L1FloraPotion potion = L1FloraPotion.get(40922);
					int str = potion.getEffect(pc).getStr();
					pc.addStr(str);
					pc.sendPackets(new S_Strup(pc, str, remaining_time));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_FLORA_POTION_DEX) { // 才能、フローラポーション
					L1FloraPotion potion = L1FloraPotion.get(40923);
					int dex = potion.getEffect(pc).getDex();
					pc.addDex(dex);
					pc.sendPackets(new S_Dexup(pc, dex, remaining_time));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} */else if (skillid == STATUS_FREEZE) { // 凍結
					pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_BIND, true));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_CURSE_BARLOG) {
					pc.sendPackets(new S_SkillIconAura(221, remaining_time, 2));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_CURSE_YAHEE) {
					pc.sendPackets(new S_SkillIconAura(221, remaining_time, 1));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_WEAKNESS_EXPOSURE_LV1) { // 弱点露出Lv1
					pc.sendPackets(new S_SkillIconGFX(75, 1));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_WEAKNESS_EXPOSURE_LV2) { // 弱点露出Lv2
					pc.sendPackets(new S_SkillIconGFX(75, 2));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_WEAKNESS_EXPOSURE_LV3) { // 弱点露出Lv3
					pc.sendPackets(new S_SkillIconGFX(75, 3));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_DESTRUCTION_NOSTRUM) { // 破壊の秘薬
					pc.sendPackets(new S_SkillIconAura(221, remaining_time, 6));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == PLAYERSTATUS_HPBAR) {
				    PCommands.hpbar(pc, "on");
				}/* else if (skillid == STATUS_EXP_UP) { // 祈りのポーション
					L1ExtraPotion potion = L1ExtraPotion.get(50616);
					pc.addExpBonusPct(potion.getEffect().getExp());
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_EXP_UP_II) { // 祈りのポーションII
					L1ExtraPotion potion = L1ExtraPotion.get(50617);
					pc.addExpBonusPct(potion.getEffect().getExp());
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == POTION_OF_SWORDMAN) { // 剣士のポーション
					L1ExtraPotion potion = L1ExtraPotion.get(50618);
					pc.addMaxHp(potion.getEffect().getHp());
					pc.addHpr(potion.getEffect().getHpr());
					pc.sendPackets(new S_HpUpdate(pc.getCurrentHp(), pc.getMaxHp()));
					pc.startHpRegeneration();
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == POTION_OF_MAGICIAN) { // 術師のポーション
					L1ExtraPotion potion = L1ExtraPotion.get(50619);
					pc.addMaxMp(potion.getEffect().getMp());
					pc.addMpr(potion.getEffect().getMpr());
					pc.sendPackets(new S_MpUpdate(pc.getCurrentMp(), pc.getMaxMp()));
					pc.startMpRegeneration();
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == POTION_OF_RECOVERY) { // 治癒のポーション
					L1ExtraPotion potion = L1ExtraPotion.get(50620);
					pc.addHpr(potion.getEffect().getHpr());
					pc.startHpRegeneration();
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == POTION_OF_MEDITATION) { // 瞑想のポーション
					L1ExtraPotion potion = L1ExtraPotion.get(50621);
					pc.addMpr(potion.getEffect().getMpr());
					pc.startMpRegeneration();
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == POTION_OF_LIFE) { // 生命のポーション
					L1ExtraPotion potion = L1ExtraPotion.get(50622);
					pc.addMaxHp(potion.getEffect().getHp());
					pc.sendPackets(new S_HpUpdate(pc.getCurrentHp(), pc.getMaxHp()));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == POTION_OF_MAGIC) { // 魔法のポーション
					L1ExtraPotion potion = L1ExtraPotion.get(50623);
					pc.addMaxMp(potion.getEffect().getMp());
					pc.sendPackets(new S_MpUpdate(pc.getCurrentMp(), pc.getMaxMp()));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == POTION_OF_MAGIC_RESIST) { // 魔法抵抗のポーション
					L1ExtraPotion potion = L1ExtraPotion.get(50624);
					pc.addMr(potion.getEffect().getMr());
					pc.sendPackets(new S_SpMr(pc));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == POTION_OF_STR) { // 腕力のポーション
					L1ExtraPotion potion = L1ExtraPotion.get(50625);
					pc.addStr(potion.getEffect().getStr());
					pc.sendPackets(new S_OwnCharStatus(pc));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == POTION_OF_DEX) { // 機敏のポーション
					L1ExtraPotion potion = L1ExtraPotion.get(50626);
					pc.addDex(potion.getEffect().getDex());
					pc.sendPackets(new S_OwnCharStatus(pc));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == POTION_OF_CON) { // 体力のポーション
					L1ExtraPotion potion = L1ExtraPotion.get(50627);
					pc.addCon(potion.getEffect().getCon());
					pc.sendPackets(new S_OwnCharStatus(pc));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == POTION_OF_INT) { // 知力のポーション
					L1ExtraPotion potion = L1ExtraPotion.get(50628);
					pc.addInt(potion.getEffect().getInt());
					pc.sendPackets(new S_OwnCharStatus(pc));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == POTION_OF_WIS) { // 精神のポーション
					L1ExtraPotion potion = L1ExtraPotion.get(50629);
					pc.addWis(potion.getEffect().getWis());
					pc.sendPackets(new S_OwnCharStatus(pc));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == POTION_OF_RAGE) { // 憤怒のポーション
					L1ExtraPotion potion = L1ExtraPotion.get(50630);
					pc.addHitup(potion.getEffect().getHit());
					pc.addDmgup(potion.getEffect().getDmg());
					pc.addBowHitup(potion.getEffect().getBowHit());
					pc.addBowDmgup(potion.getEffect().getBowDmg());
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == POTION_OF_CONCENTRATION) { // 集中のポーション
					L1ExtraPotion potion = L1ExtraPotion.get(50631);
					pc.addSp(potion.getEffect().getSp());
					pc.sendPackets(new S_SpMr(pc));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid >= COOKING_BEGIN && skillid <= COOKING_END) { // 料理
					L1Skill skill = SkillTable.getInstance().findByskillid(skillid);
					L1BuffSkillExecutor exe = skill.newBuffSkillExecutor();
					exe.addEffect(null, pc, remaining_time);
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == BLOODSTAIN_OF_ANTHARAS) { // アンタラスの血痕
					if (remaining_time - differenceTime > 0) {
						L1BuffUtil.bloodstain(pc, (byte) 0,
								(remaining_time - differenceTime) / 60, false);
					}
				} else if (skillid == BLOODSTAIN_OF_FAFURION) { // パプリオンの血痕
					if (remaining_time - differenceTime > 0) {
						L1BuffUtil.bloodstain(pc, (byte) 1,
								(remaining_time - differenceTime) / 60, false);
					}
				} else if (skillid == BLOODSTAIN_OF_LINDVIOR) { // リンドビオルの血痕
					if (remaining_time - differenceTime > 0) {
						L1BuffUtil.bloodstain(pc, (byte) 2,
								(remaining_time - differenceTime) / 60, false);
					}
					//} else if (skillid == BLOODSTAIN_OF_VALAKAS) { // ヴァラカスの血痕(未実装)
					//	if (remaining_time - differenceTime > 0) {
					//		L1BuffUtil.bloodstain(pc, (byte) 3,
					//				(remaining_time - differenceTime) / 60, false);
					//	}
				} else if (skillid == BLESS_OF_CRAY) { // クレイの祝福
					L1BuffUtil.effectBlessOfDragonSlayer(pc, skillid, 2400, 7681);
				} else if (skillid == BLESS_OF_SAEL) { // サエルの祝福
					L1BuffUtil.effectBlessOfDragonSlayer(pc, skillid, 2400, 7680);
				} else if (skillid == BLESS_OF_GUNTER) { // グンターの助言
					L1BuffUtil.effectBlessOfDragonSlayer(pc, skillid, 2400, 7683);
				} else if (skillid == MAGIC_EYE_OF_ANTHARAS) { // 地竜の魔眼
					pc.addResistHold(5);
					pc.addDodge((byte) 1); // 回避率 + 10%
					pc.sendPackets(new S_PacketBox(S_PacketBox.DODGE_RATE_PLUS, pc.getDodge()));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == MAGIC_EYE_OF_FAFURION) { // 水竜の魔眼
					pc.addResistFreeze(5);
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == MAGIC_EYE_OF_LINDVIOR) { // 風竜の魔眼
					pc.addResistSleep(5);
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == MAGIC_EYE_OF_VALAKAS) { // 火竜の魔眼
					pc.addResistStun(5);
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == MAGIC_EYE_OF_BIRTH) { // 誕生の魔眼
					pc.addResistHold(5);
					pc.addResistFreeze(5);
					pc.addDodge((byte) 1); // 回避率 + 10%
					pc.sendPackets(new S_PacketBox(S_PacketBox.DODGE_RATE_PLUS, pc.getDodge()));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == MAGIC_EYE_OF_SHAPE) { // 形象の魔眼
					pc.addResistHold(5);
					pc.addResistFreeze(5);
					pc.addResistSleep(5);
					pc.addDodge((byte) 1); // 回避率 + 10%
					pc.sendPackets(new S_PacketBox(S_PacketBox.DODGE_RATE_PLUS, pc.getDodge()));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == MAGIC_EYE_OF_LIFE) { // 生命の魔眼
					pc.addResistHold(5);
					pc.addResistFreeze(5);
					pc.addResistSleep(5);
					pc.addResistStun(5);
					pc.addDodge((byte) 1); // 回避率 + 10%
					pc.sendPackets(new S_PacketBox(S_PacketBox.DODGE_RATE_PLUS, pc.getDodge()));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STONE_OF_DRAGON) { // ドラゴンの石
					L1FloraPotion potion = L1FloraPotion.get(50555);
					pc.addHitup(potion.getEffect(pc).getHit());
					pc.addDmgup(potion.getEffect(pc).getDmg());
					pc.addBowHitup(potion.getEffect(pc).getBowHit());
					pc.addBowDmgup(potion.getEffect(pc).getBowDmg());
					pc.addSp(potion.getEffect(pc).getSp());
					pc.sendPackets(new S_SpMr(pc));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == BLESS_OF_COMA1) { // コマの祝福Ａ
					pc.setSkillEffect(skillid, remaining_time * 1000);
					pc.addStr(5);
					pc.addDex(5);
					pc.addCon(1);
					pc.addHitup(3);
					pc.addAc(-3);
				} else if (skillid == BLESS_OF_COMA2) { // コマの祝福Ｂ
					pc.addStr(5);
					pc.addDex(5);
					pc.addCon(3);
					pc.addHitup(5);
					pc.addAc(-8);
					pc.addSp(1);
					pc.addExpBonusPct(20);
					pc.sendPackets(new S_SpMr(pc));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == BLESS_OF_SAMURAI) { // 武士の心得
					pc.addExpBonusPct(10);
					pc.setSkillEffect(skillid, remaining_time * 1000);
				}*/else {
					L1SkillUse l1skilluse = new L1SkillUse();
					l1skilluse.handleCommands(client.getActiveChar(), skillid, pc.getId(), pc.getX(), pc.getY(), null,
							remaining_time, L1SkillUse.TYPE_LOGIN);
				}
			}
		} catch (SQLException e) {
			_log.error(e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
	
	private void buffBlessOfAin(L1PcInstance pc) {
		if(pc.getBlessOfAin() >= 2000000){
			pc.setBlessOfAin(2000000);
			pc.sendPackets(new S_PacketBox(S_PacketBox.BLESS_OF_AIN, pc.getBlessOfAin()));
			return;
		}
		
		Timestamp logoutTime = pc.getLogoutTime();
		if (logoutTime == null) {
			logoutTime = new Timestamp(System.currentTimeMillis());
		}
		
		int tmp = (int)((System.currentTimeMillis() - logoutTime.getTime()) / 900000);
		int sum = pc.getBlessOfAin() + (tmp * 10000);
		if(sum >= 2000000) {
			pc.setBlessOfAin(2000000);
		} else {
			pc.setBlessOfAin(sum);
		}
		
		pc.sendPackets(new S_PacketBox(S_PacketBox.BLESS_OF_AIN, pc.getBlessOfAin()));
	}

	@Override
	public String getType() {
		return C_LOGIN_TO_SERVER;
	}
}