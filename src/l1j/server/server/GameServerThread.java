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
package l1j.server.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.Config;
import l1j.server.server.controllers.AuctionTimeController;
import l1j.server.server.controllers.BossEventController;
import l1j.server.server.controllers.CrackOfTimeController;
import l1j.server.server.controllers.FishingTimeController;
import l1j.server.server.controllers.HomeTownTimeController;
import l1j.server.server.controllers.HouseTaxTimeController;
import l1j.server.server.controllers.JailController;
import l1j.server.server.controllers.LightTimeController;
import l1j.server.server.controllers.LoginController;
import l1j.server.server.controllers.MapTimeController;
import l1j.server.server.controllers.NpcChatTimeController;
import l1j.server.server.controllers.RankingsController;
import l1j.server.server.controllers.UbTimeController;
import l1j.server.server.controllers.WarTimeController;
import l1j.server.server.datatables.AccessLevelTable;
import l1j.server.server.datatables.ArmorSetTable;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.datatables.ChatLogTable;
import l1j.server.server.datatables.ClanTable;
import l1j.server.server.datatables.DoorTable;
import l1j.server.server.datatables.DropItemTable;
import l1j.server.server.datatables.DropTable;
import l1j.server.server.datatables.FurnitureSpawnTable;
import l1j.server.server.datatables.GetBackRestartTable;
import l1j.server.server.datatables.GetBackTable;
import l1j.server.server.datatables.HouseTable;
import l1j.server.server.datatables.InnTable;
import l1j.server.server.datatables.IpTable;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.datatables.MailTable;
import l1j.server.server.datatables.MapsTable;
import l1j.server.server.datatables.MobGroupTable;
import l1j.server.server.datatables.NPCTalkDataTable;
import l1j.server.server.datatables.NpcChatTable;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.datatables.PetTable;
import l1j.server.server.datatables.PetTypeTable;
import l1j.server.server.datatables.PolyTable;
import l1j.server.server.datatables.ResolventTable;
import l1j.server.server.datatables.SkillTable;
import l1j.server.server.datatables.SpawnTable;
import l1j.server.server.datatables.SprTable;
import l1j.server.server.datatables.TownTable;
import l1j.server.server.datatables.UBSpawnTable;
import l1j.server.server.datatables.UBTable;
import l1j.server.server.datatables.WeaponSkillTable;
import l1j.server.server.encryptions.IdFactory;
import l1j.server.server.model.Dungeon;
import l1j.server.server.model.ElementalStoneGenerator;
import l1j.server.server.model.Getback;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.gametime.L1GameTimeClock;
import l1j.server.server.model.map.L1MapLimiter;
import l1j.server.server.model.map.L1WorldMap;
import l1j.server.server.model.trap.L1WorldTraps;

public class GameServerThread {
	
	private static Logger _log = LoggerFactory.getLogger(GameServerThread.class.getName());

	private static GameServerThread _instance;
	private LoginController _loginController;

	public static GameServerThread getInstance() throws Exception {
		if (_instance == null) {
			_instance = new GameServerThread();
		}
		return _instance;
	}

	public GameServerThread() throws Exception {
		// Log important rates in a concise format
		_log.info("=== Server Rates ===");
		_log.info(String.format("Experience: %.1fx", Config.RATE_XP));
		_log.info(String.format("Adena/Items: %.1fx/%.1fx", Config.RATE_DROP_ADENA, Config.RATE_DROP_ITEMS));
		_log.info(String.format("Party/Pet XP: %.1fx/%.1fx", Config.PARTYEXP_RATE, Config.PETEXP_RATE));
		_log.info(String.format("Lawful/Karma: %.1fx/%.1fx", Config.RATE_LA, Config.RATE_KARMA));
		_log.info(String.format("Enchant Chances - Weapon: %d%% | Armor: %d%% | Attr: %d%%",
				Config.ENCHANT_CHANCE_WEAPON, Config.ENCHANT_CHANCE_ARMOR, Config.ATTR_ENCHANT_CHANCE));
		
		// Log critical server settings
		_log.info("=== Server Settings ===");
		_log.info(String.format("Max Players: %d | Global Chat Level: %d | PvP: %s",
				Config.MAX_ONLINE_USERS, Config.GLOBAL_CHAT_LEVEL,
				Config.ALT_NONPVP ? "On" : "Off"));
		
		if (Config.Use_Show_INGAMENEWS_Time) {
			_log.info("InGame News: Enabled (Interval: " + Config.Show_INGAMENEWS_Time + "min)");
		}

		System.gc();

		// Initialize server components
		IdFactory.getInstance();
		L1WorldMap.getInstance();
		_loginController = LoginController.getInstance();
		_loginController.setMaxAllowedOnlinePlayers(Config.MAX_ONLINE_USERS);
		
		// Initialize game data
		AccessLevelTable.getInstance();
		CharacterTable.getInstance().loadAllCharName();
		CharacterTable.clearOnlineStatus();
		L1GameTimeClock.init();

		// Start controllers
		startControllers();
		
		// Load maps and timers
		MapTimeController mapTimeController = MapTimeController.getInstance();
		mapTimeController.load();
		GeneralThreadPool.getInstance().execute(mapTimeController);
		
		// Load configurations
		GMCommandsConfig.load();
		
		// Initialize remaining components
		initializeComponents();
	}
	
	private void startControllers() {
		// UB Time Controller
		GeneralThreadPool.getInstance().execute(UbTimeController.getInstance());
		
		// War Time Controller
		GeneralThreadPool.getInstance().execute(WarTimeController.getInstance());
		
		// Elemental Stone Generator
		if (Config.ELEMENTAL_STONE_AMOUNT > 0) {
			GeneralThreadPool.getInstance().execute(ElementalStoneGenerator.getInstance());
		}
		
		// Initialize and start other controllers
		HomeTownTimeController.getInstance();
		GeneralThreadPool.getInstance().execute(AuctionTimeController.getInstance());
		GeneralThreadPool.getInstance().execute(HouseTaxTimeController.getInstance());
		GeneralThreadPool.getInstance().execute(FishingTimeController.getInstance());
		GeneralThreadPool.getInstance().execute(NpcChatTimeController.getInstance());
		GeneralThreadPool.getInstance().execute(LightTimeController.getInstance());
		GeneralThreadPool.getInstance().execute(RankingsController.getInstance());
		
		// Jail Controller (runs every minute)
		GeneralThreadPool.getInstance().scheduleAtFixedRate(JailController.getInstance(), 0, 60000);
		
		// Start Crack of Time
		CrackOfTimeController.getStart();
	}
	
	private void initializeComponents() {
		// Initialize remaining game components
		// Add initialization code here if needed		
		NpcTable.getInstance();		
		HouseTable.getInstance();
		TownTable.getInstance();
		DoorTable.getInstance();
		SpawnTable.getInstance();
		MobGroupTable.getInstance();
		SkillTable.getInstance();
		PolyTable.getInstance();
		ItemTable.getInstance();
		ArmorSetTable.getInstance();
		DropTable.getInstance();
		//.getInstance();
		NpcChatTable.getInstance();
		L1World.getInstance();
		L1WorldTraps.getInstance();
		Dungeon.getInstance();
		//RandomDungeonTable.getInstance();
		SpawnTable.getInstance();
		IpTable.getInstance();
		MapsTable.getInstance();
		//SpawnUbMobTable.getInstance();
		PetTable.getInstance();
		ClanTable.getInstance();
		//RestartLocationTable.getInstance();
		GeneralThreadPool.getInstance();
		//L1NpcRegenerationTimer.getInstance();
		ChatLogTable.getInstance();
		WeaponSkillTable.getInstance();
	    Getback.loadGetBack();
		GMCommands.getInstance();
		PetTypeTable.getInstance();
		SprTable.getInstance();
		ResolventTable.getInstance();
		FurnitureSpawnTable.getInstance();
		NpcChatTable.getInstance();
		MailTable.getInstance();
		SprTable.getInstance();
		///L1RaceTicket.getInstance();
		//L1BugBearRace.getInstance();
		InnTable.getInstance();
		//MagicDollTable.getInstance();
		//CookingRecipeTable.getInstance();
		//ShutdownRequestTable.removeAll();
		BossEventController.getInstance();
		DropItemTable.getInstance();
		GetBackRestartTable.getInstance();
		GetBackTable.getInstance();
		NPCTalkDataTable.getInstance();
		UBSpawnTable.getInstance();
		UBTable.getInstance();
		//L1DeleteItemOnGround.getInstance();
		L1MapLimiter.load();
	}
}