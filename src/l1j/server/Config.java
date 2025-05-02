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
package l1j.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.server.utils.IntRange;

public final class Config {
	private static Logger _log = LoggerFactory.getLogger(Config.class);

	/** Debug/release mode */
	public static final boolean DEBUG = false;

	/** Thread pools size */
	public static int THREAD_P_EFFECTS;

	public static int THREAD_P_GENERAL;
	public static int AI_MAX_THREAD;

	public static int THREAD_P_TYPE_GENERAL;

	public static int THREAD_P_SIZE_GENERAL;

	/** Server control */
	public static String GAME_SERVER_HOST_NAME;

	public static int GAME_SERVER_PORT;

	public static String DB_DRIVER;

	public static String DB_URL;

	public static String DB_LOGIN;

	public static String DB_PASSWORD;

	public static String PASSWORD_SALT;

	public static String TIME_ZONE;

	public static int CLIENT_LANGUAGE;

	public static boolean HOSTNAME_LOOKUPS;

	public static int AUTOMATIC_KICK;

	public static boolean AUTO_CREATE_ACCOUNTS;

	public static short MAX_ONLINE_USERS;

	public static boolean CACHE_MAP_FILES;

	public static boolean CHECK_MOVE_INTERVAL;

	public static boolean CHECK_ATTACK_INTERVAL;

	public static boolean CHECK_SPELL_INTERVAL;

	public static short INJUSTICE_COUNT;

	public static int JUSTICE_COUNT;

	public static int CHECK_STRICTNESS;

	public static byte LOGGING_WEAPON_ENCHANT;
	
	public static byte LOGGING_ENCHANT_FAIL;

	public static byte LOGGING_ARMOR_ENCHANT;

	public static boolean LOGGING_CHAT_NORMAL;

	public static boolean LOGGING_CHAT_WHISPER;

	public static boolean LOGGING_CHAT_SHOUT;

	public static boolean LOGGING_CHAT_WORLD;

	public static boolean LOGGING_CHAT_CLAN;

	public static boolean LOGGING_CHAT_PARTY;

	public static boolean LOGGING_CHAT_COMBINED;

	public static boolean LOGGING_CHAT_CHAT_PARTY;

	public static boolean CLAN_CHAT = true;
	
	public static boolean PARTY_CHAT = true;

	public static boolean LOGGING_INCOMING_PACKETS;

	public static int AUTOSAVE_INTERVAL;

	public static int AUTOSAVE_INTERVAL_INVENTORY;

	public static int SKILLTIMER_IMPLTYPE;
	
	public static int CLIENT_HISTORICAL_PACKETS;

	public static int NPCAI_IMPLTYPE;

	public static boolean TELNET_SERVER;

	public static int TELNET_SERVER_PORT;
	
	public static boolean TELNET_LOCALHOST_ONLY;
	
	public static boolean SSH_SERVER;
	
	public static int SSH_PORT;
	
	public static String SSH_KEY_FILES_DIRECTORY;
	
	public static String SSH_HOST_KEY;
	
	public static String[] SSH_ALLOWED_USERNAMES;

	public static int PC_RECOGNIZE_RANGE;

	public static int NPC_PATHING_RANGE;

	public static int NPC_ACTIVE_RANGE;

	public static boolean CHARACTER_CONFIG_IN_SERVER_SIDE;

	public static boolean ALLOW_2PC;

	public static int LEVEL_DOWN_RANGE;

	public static boolean SEND_PACKET_BEFORE_TELEPORT;

	public static boolean DETECT_DB_RESOURCE_LEAKS;
	
	public static boolean DUAL_PINK;

	public static boolean CHAO_PINK;

	public static boolean AUTO_BAN;
	
	public static int ANIMATION_SPEED;
	
	public static int REPORT_HOURS_RESET;
	
	public static int REPORT_TIME_MINUTES;

	public static boolean RESET_DUNGEONS_DAILY;
	
	public static String DUNGEON_RESET_TIME;
	
	public static boolean RESTRICT_ACCOUNT_IPS;
	
	public static String RESTRICT_ACCOUNT_IPS_MESSAGE;

	/** Rate control */
	public static float RATE_HP_REGEN;

	public static int RATE_MP_REGEN;

	public static int RATE_HP_CASTLE;

	public static int RATE_HP_HOUSE;

	public static int RATE_HP_HOTEL;

	public static int RATE_HP_MOTHERTREE;

	public static int RATE_HP_DRAGONKNIGHTTOWN;

	public static int RATE_HP_ILLUSIONISTTOWN;

	public static int RATE_MP_CASTLE;

	public static int RATE_MP_HOUSE;

	public static int RATE_MP_HOTEL;

	public static int RATE_MP_MOTHERTREE;

	public static int RATE_MP_DRAGONKNIGHTTOWN;

	public static int RATE_MP_ILLUSIONISTTOWN;

	public static double PETEXP_RATE;
	
	public static double SUMMON_STEAL_RATE;
	
	public static double PET_STEAL_RATE;

	public static double PARTYEXP_RATE;

	public static double RATE_XP;

	public static double RATE_LA;
	
	public static double RATE_CHAO_LA;

	public static double RATE_KARMA;

	public static double RATE_DROP_ADENA;

	public static double RATE_DROP_ITEMS;

	public static int ENCHANT_CHANCE_WEAPON;

	public static int ENCHANT_CHANCE_ARMOR;

	public static int ATTR_ENCHANT_CHANCE;

	public static double RATE_WEIGHT_LIMIT;

	public static double RATE_WEIGHT_LIMIT_PET;

	public static double RATE_SHOP_SELLING_PRICE;

	public static double RATE_SHOP_PURCHASING_PRICE;

	/* Original settings to enable on-the-fly changes and reset of the rates */
	public static double RATE_LA_ORG;

	public static double RATE_KARMA_ORG;

	public static double RATE_XP_ORG;

	public static double RATE_DROP_ADENA_ORG;

	public static double RATE_DROP_ITEMS_ORG;

	public static double RATE_WEIGHT_LIMIT_ORG;
	/* Org Rates End */

	public static int CREATE_CHANCE_DIARY;

	public static int CREATE_CHANCE_RECOLLECTION;

	public static int CREATE_CHANCE_MYSTERIOUS;

	public static int CREATE_CHANCE_PROCESSING;

	public static int CREATE_CHANCE_PROCESSING_DIAMOND;

	public static int CREATE_CHANCE_DANTES;

	public static int CREATE_CHANCE_ANCIENT_AMULET;

	public static int CREATE_CHANCE_HISTORY_BOOK;

	/** AltSettings control */
	public static short MIN_GM_ACCESS_LEVEL;
	
	public static short GLOBAL_CHAT_LEVEL;

	public static short WHISPER_CHAT_LEVEL;

	public static byte AUTO_LOOT;

	public static int LOOTING_RANGE;

	public static boolean ALT_NONPVP;

	public static boolean ALT_ATKMSG;

	public static boolean CHANGE_TITLE_BY_ONESELF;

	public static int MAX_CLAN_MEMBER;

	public static boolean CLAN_ALLIANCE;

	public static int PET_RACE_MIN_PLAYER = 2;

	public static int PET_RACE_MAX_LAP = 3;

	public static int DEATH_MATCH_MIN_PLAYER = 6;

	public static boolean USE_TOI_CHARM_ANYWHERE;

	public static boolean ELEMENTAL_ENCHANTING;

	public static int ELEMENTAL_ENCHANT_LIMIT;

	public static boolean ACCESSORY_ENCHANTING;

	public static int ACCESSORY_ENCHANT_LIMIT;

	public static int BOSS_RANDOM_START_MIN; // in minutes
	public static int BOSS_RANDOM_START_MAX; // in minutes

	
	public static boolean RANDOMIZE_BOSS_SPAWNS;
	public static double RANDOMIZED_BOSS_SPAWN_FACTOR;
	public static boolean ROYAL_LEVEL_DAMAGE;
	public static boolean USE_INT_PROCS;
	public static boolean AUTO_STONE;

	public static int MAX_PT;

	public static int MAX_CHAT_PT;

	public static boolean SIM_WAR_PENALTY;

	public static boolean GET_BACK;

	public static int NEWBIEMAPLEVELS;

	public static String ALT_ITEM_DELETION_TYPE;

	public static int ALT_ITEM_DELETION_TIME;

	public static int ALT_ITEM_DELETION_RANGE;

	public static boolean ALT_GMSHOP;

	public static int ALT_GMSHOP_MIN_ID;

	public static int ALT_GMSHOP_MAX_ID;

	public static boolean ALT_HALLOWEENEVENT;
	
	public static boolean ALT_HALLOWEENEVENTNPC;
	
	public static boolean ALT_POLYEVENT;
	
	public static boolean ALT_JPPRIVILEGED;

	public static boolean ALT_TALKINGSCROLLQUEST;

	public static boolean ALT_WHO_COMMAND;

	public static boolean ALT_REVIVAL_POTION;

	public static int ALT_WAR_TIME;

	public static int ALT_WAR_TIME_UNIT;

	public static int ALT_WAR_INTERVAL;

	public static int ALT_WAR_INTERVAL_UNIT;

	public static int ALT_RATE_OF_DUTY;
	
	public static boolean ALT_BOSS_EVENT;
	
	public static String ALT_BOSS_EVENT_BOSSES;
	
	public static boolean ALT_BOSS_EVENT_DAILY_RESET;
	
	public static String ALT_BOSS_EVENT_RESET_TIME;
	
	public static int ALT_BOSS_EVENT_MAX_SPAWNS_PER_IP;
	
	public static long ALT_DAYS_LIMIT_PLEDGE_JOIN;

	public static boolean INIT_BOSS_SPAWN;

	public static int ELEMENTAL_STONE_AMOUNT;

	public static int HAUNTEDHOUSETIME;

	public static int HOUSE_TAX_INTERVAL;

	public static int MAX_DOLL_COUNT;

	public static boolean RETURN_TO_NATURE;

	public static int MAX_NPC_ITEM;

	public static int MAX_PERSONAL_WAREHOUSE_ITEM;

	public static int MAX_CLAN_WAREHOUSE_ITEM;

	public static boolean DELETE_CHARACTER_AFTER_7DAYS;

	public static boolean MONITOR_COMMANDS;

	public static boolean WARP;

	public static boolean STACKING;

	public static boolean SKT_START;

	public static boolean SOFT_AC;

	public static boolean GHEY_MARRAIGE;

	public static int NPC_DELETION_TIME;

	public static int DEFAULT_CHARACTER_SLOT;

	public static int MONSTERPOTIONINTUSE;

	public static int COOKING_TIME;

	/** CharSettings control */
	public static int PRINCE_MAX_HP;

	public static int PRINCE_MAX_MP;

	public static int KNIGHT_MAX_HP;

	public static int KNIGHT_MAX_MP;

	public static int ELF_MAX_HP;

	public static int ELF_MAX_MP;

	public static int WIZARD_MAX_HP;

	public static int WIZARD_MAX_MP;

	public static int DARKELF_MAX_HP;

	public static int DARKELF_MAX_MP;

	public static int DRAGONKNIGHT_MAX_HP;

	public static int DRAGONKNIGHT_MAX_MP;

	public static int ILLUSIONIST_MAX_HP;

	public static int ILLUSIONIST_MAX_MP;

	public static float LV50_EXP;

	public static float LV51_EXP;

	public static float LV52_EXP;

	public static float LV53_EXP;

	public static float LV54_EXP;

	public static float LV55_EXP;

	public static float LV56_EXP;

	public static float LV57_EXP;

	public static float LV58_EXP;

	public static float LV59_EXP;

	public static float LV60_EXP;

	public static float LV61_EXP;

	public static float LV62_EXP;

	public static float LV63_EXP;

	public static float LV64_EXP;

	public static float LV65_EXP;

	public static float LV66_EXP;

	public static float LV67_EXP;

	public static float LV68_EXP;

	public static float LV69_EXP;

	public static float LV70_EXP;

	public static float LV71_EXP;

	public static float LV72_EXP;

	public static float LV73_EXP;

	public static float LV74_EXP;

	public static float LV75_EXP;

	public static float LV76_EXP;

	public static float LV77_EXP;

	public static float LV78_EXP;

	public static float LV79_EXP;

	public static float LV80_EXP;

	public static float LV81_EXP;

	public static float LV82_EXP;

	public static float LV83_EXP;

	public static float LV84_EXP;

	public static float LV85_EXP;

	public static float LV86_EXP;

	public static float LV87_EXP;

	public static float LV88_EXP;

	public static float LV89_EXP;

	public static float LV90_EXP;

	public static float LV91_EXP;

	public static float LV92_EXP;

	public static float LV93_EXP;

	public static float LV94_EXP;

	public static float LV95_EXP;

	public static float LV96_EXP;

	public static float LV97_EXP;

	public static float LV98_EXP;

	public static float LV99_EXP;
	
	public static boolean STOP_DROP;

	/** Player Command Settings */
	public static boolean PLAYER_COMMANDS;

	public static boolean PLAYER_BUFF;

	public static boolean POWER_BUFF;

	public static boolean DK_BUFF;

	/** Configuration files */
	public static final String SERVER_CONFIG_FILE = "./config/server.properties";

	public static final String RATES_CONFIG_FILE = "./config/rates.properties";

	public static final String ALT_SETTINGS_FILE = "./config/altsettings.properties";

	public static final String CHAR_SETTINGS_CONFIG_FILE = "./config/charsettings.properties";

	public static final String PCOMMANDS_SETTINGS_FILE = "./config/pcommands.properties";

	public static final int MANA_DRAIN_LIMIT_PER_NPC = 40;

	public static final int MANA_DRAIN_LIMIT_PER_SOM_ATTACK = 9;
	
	/*

	public static final boolean SPAWN_HOME_POINT = false;

	public static final int SPAWN_HOME_POINT_COUNT = 0;

	public static final int SPAWN_HOME_POINT_DELAY = 0;

	public static final int SPAWN_HOME_POINT_RANGE = 0;
	*/

	public static final boolean BOSS_SPAWN_LOG = true;

	public static final boolean HALLOWEEN_EVENT = false;

	public static final boolean LOGINS_TO_AUTHENTICATION = false;

	public static final byte[] RSA_KEY_E = null;

	public static final byte[] RSA_KEY_D = null;

	public static final boolean LOGINS_TO_AUTOENTICATION = false;

	public static final boolean AUTOSAVE = false;

	public static boolean Use_Show_INGAMENEWS_Time;

	public static int Show_INGAMENEWS_Time;
	
	public static boolean USE_PINE_IN_SAFETY;
	
	public static boolean MOVE_MACROABLE_NPCS;
	
	public static int MIN_ATONEMENT;
	
	public static int ATONEMENT_COST;
	
	public static int NUM_PKS_HELL;
	
	public static int NUM_PKS_HELL_WARNING;

	public static int DOT_RELOAD_WAIT_TIME;
	public static int DOT_RELOAD_PINK_WAIT_TIME;
	
	public static int CASTLE_WAR_MIN_PRINCE_LEVEL;
	public static int CASTLE_WAR_MIN_MEMBERS_ONLINE;
	public static int CASTLE_WAR_MIN_MEMBERS_LEVEL;
	
	public static int ALT_RANKING_OVERALL_TOP;
	public static int ALT_RANKING_CLASS_TOP;
	public static int ALT_RANKING_MIN_LEVEL;
	public static String ALT_RANKING_PENALTY_TYPES;
	
	public static boolean LIMIT_WEAPON_SWITCHING;
	
	public static int MAX_SERVANT_SUMMONS;

	public static boolean ALT_PET_HUNGER_STATUS_CHANGE;

	/** Security Settings **/
	public static int DELAY_DISCONNECT;
	public static int NON_AGGRO_LOGOUT_TIMER;
	public static int CONNECTIONS_PER_IP;
	
	public static void load() {
		load(null, null);
	}
	
	public static void load(String serverConfigPath, String ratesConfigPath) {
		try {
			// Use custom server config path if provided, otherwise use default
			String serverConfig = serverConfigPath != null ? 
					serverConfigPath : SERVER_CONFIG_FILE;
			
			// Use custom rates config path if provided, otherwise use default
			String ratesConfig = ratesConfigPath != null ? ratesConfigPath : RATES_CONFIG_FILE;
					
			try {
				_log.info("Loading server configuration from: " + serverConfig);
				loadServerConfig(serverConfig);
				_log.info("Loading rates configuration from: " + ratesConfig);
				loadRatesConfig(ratesConfig);
				loadAltConfig();
				loadCharConfig();
				loadPcCommandConfig();
				validate();
				_log.info("All configuration files loaded successfully");
			} catch (Exception e) {
				_log.error("Config files failed to load", e);
				throw new Error("Failed to Load config files.");
			}
		} catch (Exception e) {
			_log.error("Failed to load config", e);
			throw new Error("Failed to initialize configuration system: " + e.getMessage());
		}
	}

	private static void loadServerConfig(String configFile) {
		try {
			Properties properties = new Properties();
			InputStream is = new FileInputStream(new File(configFile));
			properties.load(is);
			is.close();

			GAME_SERVER_HOST_NAME = properties.getProperty(
					"GameserverHostname", "*");
			GAME_SERVER_PORT = Integer.parseInt(properties.getProperty(
					"GameserverPort", "2000"));
			DB_DRIVER = properties.getProperty("Driver",
					"com.mysql.jdbc.Driver");
			DB_URL = properties
					.getProperty("URL",
							"jdbc:mysql://localhost/l1jdb?useUnicode=True&characterEncoding=UTF-8");
			DB_LOGIN = properties.getProperty("Login", "root");
			DB_PASSWORD = properties.getProperty("Password", "");
			PASSWORD_SALT = properties.getProperty("PasswordSalt", "");
			THREAD_P_TYPE_GENERAL = Integer.parseInt(
					properties.getProperty("GeneralThreadPoolType", "0"),
					10);
			THREAD_P_SIZE_GENERAL = Integer.parseInt(
					properties.getProperty("GeneralThreadPoolSize", "0"),
					10);
			TIME_ZONE = properties.getProperty("TimeZone", "EST");
			CLIENT_LANGUAGE = Integer.parseInt(properties.getProperty("ClientLanguage", "0"));
			HOSTNAME_LOOKUPS = Boolean.parseBoolean(properties.getProperty(
					"HostnameLookups", "False"));
			AUTOMATIC_KICK = Integer.parseInt(properties.getProperty(
					"AutomaticKick", "10"));
			AUTO_CREATE_ACCOUNTS = Boolean.parseBoolean(properties
					.getProperty("AutoCreateAccounts", "True"));
			MAX_ONLINE_USERS = Short.parseShort(properties.getProperty(
					"MaximumOnlineUsers", "30"));
			CACHE_MAP_FILES = Boolean.parseBoolean(properties.getProperty(
					"CacheMapFiles", "False"));
			CHECK_MOVE_INTERVAL = Boolean.parseBoolean(properties
					.getProperty("CheckMoveInterval", "False"));
			CHECK_ATTACK_INTERVAL = Boolean.parseBoolean(properties
					.getProperty("CheckAttackInterval", "False"));
			CHECK_SPELL_INTERVAL = Boolean.parseBoolean(properties
					.getProperty("CheckSpellInterval", "False"));
			Use_Show_INGAMENEWS_Time = Boolean.parseBoolean(properties
					.getProperty("UseINGAMENEWS_Time", "False"));
			Show_INGAMENEWS_Time = Integer.parseInt(properties.getProperty(
					"ShowINGAMENEWS_Time", "30"));
			INJUSTICE_COUNT = Short.parseShort(properties.getProperty(
					"InjusticeCount", "10"));
			JUSTICE_COUNT = Integer.parseInt(properties.getProperty(
					"JusticeCount", "4"));
			CHECK_STRICTNESS = Integer.parseInt(properties.getProperty(
					"CheckStrictness", "102"));
			LOGGING_WEAPON_ENCHANT = Byte.parseByte(properties.getProperty(
					"LoggingWeaponEnchant", "0"));
			LOGGING_ARMOR_ENCHANT = Byte.parseByte(properties.getProperty(
					"LoggingArmorEnchant", "0"));
			LOGGING_ENCHANT_FAIL = Byte.parseByte(properties.getProperty(
					"LoggingEnchantFails", "0"));
			LOGGING_CHAT_NORMAL = Boolean.parseBoolean(properties
					.getProperty("LoggingChatNormal", "False"));
			LOGGING_CHAT_WHISPER = Boolean.parseBoolean(properties
					.getProperty("LoggingChatWhisper", "False"));
			LOGGING_CHAT_SHOUT = Boolean.parseBoolean(properties
					.getProperty("LoggingChatShout", "False"));
			LOGGING_CHAT_WORLD = Boolean.parseBoolean(properties
					.getProperty("LoggingChatWorld", "False"));
			LOGGING_CHAT_CLAN = Boolean.parseBoolean(properties
					.getProperty("LoggingChatClan", "False"));
			LOGGING_CHAT_PARTY = Boolean.parseBoolean(properties
					.getProperty("LoggingChatParty", "False"));
			LOGGING_CHAT_COMBINED = Boolean.parseBoolean(properties
					.getProperty("LoggingChatCombined", "False"));
			LOGGING_CHAT_CHAT_PARTY = Boolean.parseBoolean(properties
					.getProperty("LoggingChatChatParty", "False"));
			LOGGING_INCOMING_PACKETS = Boolean.parseBoolean(properties
					.getProperty("LoggingIncomingPackets", "False"));
			AUTOSAVE_INTERVAL = Integer.parseInt(
					properties.getProperty("AutosaveInterval", "1200"), 10);
			AUTOSAVE_INTERVAL_INVENTORY = Integer.parseInt(properties
					.getProperty("AutosaveIntervalOfInventory", "300"), 10);
			SKILLTIMER_IMPLTYPE = Integer.parseInt(properties.getProperty(
					"SkillTimerImplType", "1"));
			
			CLIENT_HISTORICAL_PACKETS = Integer.parseInt(properties.getProperty(
					"ClientHistoricalPacketCount", "500"));
			
			NPCAI_IMPLTYPE = Integer.parseInt(properties.getProperty(
					"NpcAIImplType", "1"));
			TELNET_SERVER = Boolean.parseBoolean(properties.getProperty(
					"TelnetServer", "False"));
			TELNET_SERVER_PORT = Integer.parseInt(properties.getProperty(
					"TelnetServerPort", "23"));
			TELNET_LOCALHOST_ONLY = Boolean.parseBoolean(properties.getProperty(
					"TelnetLocalhostOnly", "True"));
			
			SSH_SERVER = Boolean.parseBoolean(properties.getProperty(
					"SSHServer", "True"));
			
			SSH_PORT = Integer.parseInt(properties.getProperty("SSHPort", "16483"));
			
			SSH_KEY_FILES_DIRECTORY = properties.getProperty("KeyFilesDirectory","/l1j/keys/");
			
			SSH_HOST_KEY = properties.getProperty("HostKey", "/l1j/keys/l1j_host_key");
			
			SSH_ALLOWED_USERNAMES = properties.getProperty("AllowedUsernames", "").replace(" ","").split(",");
			
			PC_RECOGNIZE_RANGE = Integer.parseInt(properties.getProperty(
					"PcRecognizeRange", "23"));
			NPC_PATHING_RANGE = Integer.parseInt(properties.getProperty(
					"NpcPathingRange", "18"));
			NPC_ACTIVE_RANGE = Integer.parseInt(properties.getProperty(
					"NpcActiveRange", "-1"));
			CHARACTER_CONFIG_IN_SERVER_SIDE = Boolean
					.parseBoolean(properties.getProperty(
							"CharacterConfigInServerSide", "True"));
			CONNECTIONS_PER_IP = Integer.parseInt(properties.getProperty(
					"ConnectionsPerIp", "2"));
			LEVEL_DOWN_RANGE = Integer.parseInt(properties.getProperty(
					"LevelDownRange", "0"));
			SEND_PACKET_BEFORE_TELEPORT = Boolean.parseBoolean(properties
					.getProperty("SendPacketBeforeTeleport", "False"));
			DETECT_DB_RESOURCE_LEAKS = Boolean.parseBoolean(properties
					.getProperty("EnableDatabaseResourceLeaksDetection",
							"False"));
			DELAY_DISCONNECT = Integer.parseInt(properties.getProperty(
					"DelayDisconnect", "0"));
			NON_AGGRO_LOGOUT_TIMER = Integer.parseInt(properties.getProperty(
					"NonAggroLogoutTimer", "10000"));
			DUAL_PINK = Boolean.parseBoolean(properties.getProperty("DualPink", "False"));
			CHAO_PINK = Boolean.parseBoolean(properties.getProperty("ChaoPink", "False"));
			AUTO_BAN = Boolean.parseBoolean(properties.getProperty("AutoBan", "False"));
			STOP_DROP = Boolean.parseBoolean(properties.getProperty("StopDrop", "False"));
			ANIMATION_SPEED = Integer.parseInt(properties.getProperty("DefaultAnimationSpeed", "720"));
       			MAX_SERVANT_SUMMONS = Integer.parseInt(properties.getProperty("MaxServantSummons", "100"));

			REPORT_HOURS_RESET = Integer.parseInt(properties.getProperty("ReportHoursReset", "24"));
			REPORT_TIME_MINUTES = Integer.parseInt(properties.getProperty("ReportTimeMinutes", "10"));

			RESET_DUNGEONS_DAILY = Boolean.parseBoolean(properties.getProperty("ResetDungeonsDaily", "True"));
			DUNGEON_RESET_TIME = properties.getProperty("DungeonResetTime", "00:00");
			RESTRICT_ACCOUNT_IPS = Boolean.parseBoolean(properties.getProperty("RestrictAccountIps", "False"));
			RESTRICT_ACCOUNT_IPS_MESSAGE = properties.getProperty("RestrictAccountIpsMessage");
		} catch (Exception e) {
			_log.error(e.getLocalizedMessage(), e);
			throw new Error("Failed to load " + configFile + " file.");
		}
	}

	private static void loadRatesConfig(String ratesConfigPath) {
		try {
			Properties properties = new Properties();
			InputStream is = new FileInputStream(new File(ratesConfigPath));
			properties.load(is);
			is.close();
			_log.info("Loading rates configuration from: " + ratesConfigPath);

			RATE_HP_REGEN = Float.parseFloat(properties.getProperty(
					"RateHpRegen", "2"));
			RATE_MP_REGEN = Integer.parseInt(properties.getProperty(
					"RateMpRegen", "1"));
			RATE_HP_CASTLE = Integer.parseInt(properties.getProperty(
					"RateHpCastle", "5"));
			RATE_MP_CASTLE = Integer.parseInt(properties.getProperty(
					"RateMpCastle", "3"));
			RATE_HP_HOUSE = Integer.parseInt(properties.getProperty(
					"RateHpHouse", "5"));
			RATE_MP_HOUSE = Integer.parseInt(properties.getProperty(
					"RateMpHouse", "3"));
			RATE_HP_HOTEL = Integer.parseInt(properties.getProperty(
					"RateHpHotel", "5"));
			RATE_MP_HOTEL = Integer.parseInt(properties.getProperty(
					"RateMpHotel", "3"));
			RATE_HP_MOTHERTREE = Integer.parseInt(properties.getProperty(
					"RateHpMotherTree", "5"));
			RATE_MP_MOTHERTREE = Integer.parseInt(properties.getProperty(
					"RateMpMotherTree", "3"));

			RATE_HP_ILLUSIONISTTOWN = Integer.parseInt(properties
					.getProperty("RateHpIllusionisttown", "5"));
			RATE_MP_ILLUSIONISTTOWN = Integer.parseInt(properties
					.getProperty("RateMpIllusionisttown", "3"));
			RATE_HP_DRAGONKNIGHTTOWN = Integer.parseInt(properties
					.getProperty("RateHpDragonknighttown", "5"));
			RATE_MP_DRAGONKNIGHTTOWN = Integer.parseInt(properties
					.getProperty("RateMpDragonknighttown", "3"));

			PETEXP_RATE = Double.parseDouble(properties.getProperty("PetExp",
					"1.0"));
			SUMMON_STEAL_RATE = Double.parseDouble(properties.getProperty("SummonSteal",
					"1.0"));
			PET_STEAL_RATE = Double.parseDouble(properties.getProperty("PetSteal",
					"1.0"));
			PARTYEXP_RATE = Double.parseDouble(properties.getProperty(
					"PartyExp", "1.0"));
			RATE_XP = Double.parseDouble(properties.getProperty("RateXp",
					"1.0"));
			RATE_LA = Double.parseDouble(properties.getProperty("RateLawful",
					"1.0"));
			RATE_CHAO_LA = Double.parseDouble(properties.getProperty("RateChaoLawful",
					"1.0"));
			RATE_KARMA = Double.parseDouble(properties.getProperty(
					"RateKarma", "1.0"));
			RATE_DROP_ADENA = Double.parseDouble(properties.getProperty(
					"RateDropAdena", "1.0"));
			RATE_DROP_ITEMS = Double.parseDouble(properties.getProperty(
					"RateDropItems", "1.0"));
			ENCHANT_CHANCE_WEAPON = Integer.parseInt(properties.getProperty(
					"EnchantChanceWeapon", "68"));
			ENCHANT_CHANCE_ARMOR = Integer.parseInt(properties.getProperty(
					"EnchantChanceArmor", "52"));
			ATTR_ENCHANT_CHANCE = Integer.parseInt(properties.getProperty(
					"AttrEnchantChance", "10"));
			RATE_WEIGHT_LIMIT = Double.parseDouble(properties.getProperty(
					"RateWeightLimit", "1"));
			RATE_WEIGHT_LIMIT_PET = Double.parseDouble(properties
					.getProperty("RateWeightLimitforPet", "1"));
			RATE_SHOP_SELLING_PRICE = Double.parseDouble(properties
					.getProperty("RateShopSellingPrice", "1.0"));
			RATE_SHOP_PURCHASING_PRICE = Double.parseDouble(properties
					.getProperty("RateShopPurchasingPrice", "1.0"));
			CREATE_CHANCE_DIARY = Integer.parseInt(properties.getProperty(
					"CreateChanceDiary", "33"));
			CREATE_CHANCE_RECOLLECTION = Integer.parseInt(properties
					.getProperty("CreateChanceRecollection", "90"));
			CREATE_CHANCE_MYSTERIOUS = Integer.parseInt(properties
					.getProperty("CreateChanceMysterious", "90"));
			CREATE_CHANCE_PROCESSING = Integer.parseInt(properties
					.getProperty("CreateChanceProcessing", "90"));
			CREATE_CHANCE_PROCESSING_DIAMOND = Integer.parseInt(properties
					.getProperty("CreateChanceProcessingDiamond", "90"));
			CREATE_CHANCE_DANTES = Integer.parseInt(properties.getProperty(
					"CreateChanceDantes", "50"));
			CREATE_CHANCE_ANCIENT_AMULET = Integer.parseInt(properties
					.getProperty("CreateChanceAncientAmulet", "90"));
			CREATE_CHANCE_HISTORY_BOOK = Integer.parseInt(properties
					.getProperty("CreateChanceHistoryBook", "50"));
			RATE_LA_ORG = RATE_LA;
			RATE_KARMA_ORG = RATE_KARMA;
			RATE_XP_ORG = RATE_XP;
			RATE_DROP_ADENA_ORG = RATE_DROP_ADENA;
			RATE_DROP_ITEMS_ORG = RATE_DROP_ITEMS;
			RATE_WEIGHT_LIMIT_ORG = RATE_WEIGHT_LIMIT;
		} catch (Exception e) {
			_log.error(e.getLocalizedMessage(), e);
			throw new Error("Failed to load " + ratesConfigPath + " file.");
		}
	}

	private static void loadAltConfig() {
		_log.info("Loading AltSettings config.");
		try {
			Properties altSettings = new Properties();
			InputStream is = new FileInputStream(new File(ALT_SETTINGS_FILE));
			altSettings.load(is);
			is.close();
			MIN_GM_ACCESS_LEVEL = Short.parseShort(altSettings.getProperty(
					"GmAccessLevel", "100"));
			GLOBAL_CHAT_LEVEL = Short.parseShort(altSettings.getProperty(
					"GlobalChatLevel", "30"));
			WHISPER_CHAT_LEVEL = Short.parseShort(altSettings.getProperty(
					"WhisperChatLevel", "5"));
			AUTO_LOOT = Byte
					.parseByte(altSettings.getProperty("AutoLoot", "2"));
			LOOTING_RANGE = Integer.parseInt(altSettings.getProperty(
					"LootingRange", "3"));
			NEWBIEMAPLEVELS = Integer.parseInt(altSettings.getProperty(
					"Newbiemaplevel", "15"));
			ALT_NONPVP = Boolean.parseBoolean(altSettings.getProperty("NonPvP",
					"True"));
			ALT_ATKMSG = Boolean.parseBoolean(altSettings.getProperty(
					"AttackMessageOn", "False"));
			CHANGE_TITLE_BY_ONESELF = Boolean.parseBoolean(altSettings
					.getProperty("ChangeTitleByOneself", "False"));
			MAX_CLAN_MEMBER = Integer.parseInt(altSettings.getProperty(
					"MaxClanMember", "0"));
			CLAN_ALLIANCE = Boolean.parseBoolean(altSettings.getProperty(
					"ClanAlliance", "True"));
			MAX_PT = Integer.parseInt(altSettings.getProperty("MaxPT", "8"));
			MAX_CHAT_PT = Integer.parseInt(altSettings.getProperty("MaxChatPT",
					"8"));
			SIM_WAR_PENALTY = Boolean.parseBoolean(altSettings.getProperty(
					"SimWarPenalty", "True"));
			GET_BACK = Boolean.parseBoolean(altSettings.getProperty("GetBack",
					"False"));
			ALT_ITEM_DELETION_TYPE = altSettings.getProperty(
					"ItemDeletionType", "auto");
			ALT_ITEM_DELETION_TIME = Integer.parseInt(altSettings.getProperty(
					"ItemDeletionTime", "10"));
			ALT_ITEM_DELETION_RANGE = Integer.parseInt(altSettings.getProperty(
					"ItemDeletionRange", "5"));
			ALT_GMSHOP = Boolean.parseBoolean(altSettings.getProperty("GMshop",
					"False"));
			ALT_GMSHOP_MIN_ID = Integer.parseInt(altSettings.getProperty(
					"GMshopMinID", "0xffffffff"));
			ALT_GMSHOP_MAX_ID = Integer.parseInt(altSettings.getProperty(
					"GMshopMaxID", "0xffffffff"));
			ALT_HALLOWEENEVENT = Boolean.parseBoolean(altSettings.getProperty(
					"HalloweenEvent", "False"));
			ALT_HALLOWEENEVENTNPC = Boolean.parseBoolean(altSettings.getProperty(
					"HalloweenEventNpc", "False"));
			ALT_POLYEVENT = Boolean.parseBoolean(altSettings.getProperty(
					"PolyEvent", "False"));
			ALT_BOSS_EVENT = Boolean.parseBoolean(altSettings.getProperty(
					"BossEvent", "False"));
			ALT_BOSS_EVENT_BOSSES = altSettings.getProperty(
					"BossEventBosses", "");
			ALT_BOSS_EVENT_DAILY_RESET = Boolean.parseBoolean(altSettings.getProperty(
					"BossEventDailyReset", "False"));
			ALT_BOSS_EVENT_RESET_TIME = altSettings.getProperty("BossEventDailyResetTime", "00:00");
			
			ALT_DAYS_LIMIT_PLEDGE_JOIN = TimeUnit.DAYS.toMillis(Integer.parseInt(altSettings.getProperty("DaysLimitPledgeJoin", "0")));
			
			ALT_JPPRIVILEGED = Boolean.parseBoolean(altSettings.getProperty(
					"JpPrivileged", "False"));
			ALT_TALKINGSCROLLQUEST = Boolean.parseBoolean(altSettings
					.getProperty("TalkingScrollQuest", "False"));
			ALT_WHO_COMMAND = Boolean.parseBoolean(altSettings.getProperty(
					"WhoCommand", "False"));
			ALT_REVIVAL_POTION = Boolean.parseBoolean(altSettings.getProperty(
					"RevivalPotion", "False"));
			MONSTERPOTIONINTUSE = Integer.parseInt(altSettings.getProperty(
					"MonsterIntPotions", "13"));
			COOKING_TIME = Integer.parseInt(altSettings.getProperty(
					"CookingTime", "3"));
			HAUNTEDHOUSETIME = Integer.parseInt(altSettings.getProperty(
					"HauntedHouseTime", "90000"));
			RANDOMIZE_BOSS_SPAWNS = Boolean.parseBoolean(altSettings
					.getProperty("RandomizeBossSpawns", "False"));
			BOSS_RANDOM_START_MIN = Integer.parseInt(altSettings
					.getProperty("BossRandomStartMin", "1"));
			BOSS_RANDOM_START_MAX = Integer.parseInt(altSettings
					.getProperty("BossRandomStartMax", "180"));
			RANDOMIZED_BOSS_SPAWN_FACTOR = Double.parseDouble(altSettings
					.getProperty("RandomizedBossSpawnFactor", ".5"));
			ROYAL_LEVEL_DAMAGE = Boolean.parseBoolean(altSettings.getProperty(
					"RoyalLevelDamage", "False"));
			USE_INT_PROCS = Boolean.parseBoolean(altSettings.getProperty(
					"UseIntProcs", "False"));
			AUTO_STONE = Boolean.parseBoolean(altSettings.getProperty(
					"UseAutoStone", "False"));
			USE_PINE_IN_SAFETY = Boolean.parseBoolean(altSettings.getProperty("UsePineInSafety", "True"));
			MOVE_MACROABLE_NPCS = Boolean.parseBoolean(altSettings.getProperty("MoveMacroableNpcs", "False"));
			NUM_PKS_HELL = Integer.parseInt(altSettings.getProperty("PKsForHell", "10"));
			NUM_PKS_HELL_WARNING = Integer.parseInt(altSettings.getProperty("PKsForHellWarning", "5"));
			MIN_ATONEMENT = Integer.parseInt(altSettings.getProperty("MinPksForAtonement", "5"));
			ATONEMENT_COST = Integer.parseInt(altSettings.getProperty("CostOfAtonement", "700000"));
			DOT_RELOAD_WAIT_TIME = Integer.parseInt(altSettings.getProperty(
					"DotReloadWaitTime", "0"));
			DOT_RELOAD_PINK_WAIT_TIME = Integer.parseInt(altSettings.getProperty(
					"DotReloadPinkWaitTime", "0"));
			CASTLE_WAR_MIN_PRINCE_LEVEL = Integer.parseInt(altSettings.getProperty(
					"CastleWarMinPrinceLevel", "1"));
			CASTLE_WAR_MIN_MEMBERS_ONLINE = Integer.parseInt(altSettings.getProperty(
					"CastleWarMinMembersOnline", "0"));
			CASTLE_WAR_MIN_MEMBERS_LEVEL = Integer.parseInt(altSettings.getProperty(
					"CastleWarMinMembersLevel", "1"));
			ALT_RANKING_OVERALL_TOP = Integer.parseInt(altSettings.getProperty(
					"RankingOverallTop", "25"));
			ALT_RANKING_CLASS_TOP = Integer.parseInt(altSettings.getProperty(
					"RankingClassTop", "10"));
			
			ALT_RANKING_MIN_LEVEL = Integer.parseInt(altSettings.getProperty(
					"RankingMinLevel", "70"));
			ALT_RANKING_PENALTY_TYPES = altSettings.getProperty("RankingPenaltyTypes", "5,8,9");

			LIMIT_WEAPON_SWITCHING = Boolean.parseBoolean(altSettings.getProperty("LimitWeaponSwitching", "False"));
			String strWar;
			strWar = altSettings.getProperty("WarTime", "2h");
			if (strWar.indexOf("d") >= 0) {
				ALT_WAR_TIME_UNIT = Calendar.DATE;
				strWar = strWar.replace("d", "");
			} else if (strWar.indexOf("h") >= 0) {
				ALT_WAR_TIME_UNIT = Calendar.HOUR_OF_DAY;
				strWar = strWar.replace("h", "");
			} else if (strWar.indexOf("m") >= 0) {
				ALT_WAR_TIME_UNIT = Calendar.MINUTE;
				strWar = strWar.replace("m", "");
			}
			ALT_WAR_TIME = Integer.parseInt(strWar);
			strWar = altSettings.getProperty("WarInterval", "4d");
			if (strWar.indexOf("d") >= 0) {
				ALT_WAR_INTERVAL_UNIT = Calendar.DATE;
				strWar = strWar.replace("d", "");
			} else if (strWar.indexOf("h") >= 0) {
				ALT_WAR_INTERVAL_UNIT = Calendar.HOUR_OF_DAY;
				strWar = strWar.replace("h", "");
			} else if (strWar.indexOf("m") >= 0) {
				ALT_WAR_INTERVAL_UNIT = Calendar.MINUTE;
				strWar = strWar.replace("m", "");
			}
			ALT_WAR_INTERVAL = Integer.parseInt(strWar);
			INIT_BOSS_SPAWN = Boolean.parseBoolean(altSettings.getProperty(
					"InitBossSpawn", "True"));
			ELEMENTAL_STONE_AMOUNT = Integer.parseInt(altSettings.getProperty(
					"ElementalStoneAmount", "300"));
			HOUSE_TAX_INTERVAL = Integer.parseInt(altSettings.getProperty(
					"HouseTaxInterval", "10"));
			MAX_DOLL_COUNT = Integer.parseInt(altSettings.getProperty(
					"MaxDollCount", "1"));
			MONITOR_COMMANDS = Boolean.parseBoolean(altSettings.getProperty(
					"MonitorCommands", "True"));
			WARP = Boolean
					.parseBoolean(altSettings.getProperty("Warp", "True"));
			STACKING = Boolean.parseBoolean(altSettings.getProperty("Stacking",
					"True"));
			SKT_START = Boolean.parseBoolean(altSettings.getProperty(
					"SKTStart", "False"));
			SOFT_AC = Boolean.parseBoolean(altSettings.getProperty("SoftAC",
					"True"));
			GHEY_MARRAIGE = Boolean.parseBoolean(altSettings.getProperty(
					"GheyMarraige", "False"));
			MAX_CLAN_MEMBER = Integer.parseInt(altSettings.getProperty(
					"MaxClanMember", "0"));
			CLAN_ALLIANCE = Boolean.parseBoolean(altSettings.getProperty(
					"ClanAlliance", "True"));
			RETURN_TO_NATURE = Boolean.parseBoolean(altSettings.getProperty(
					"ReturnToNature", "False"));
			MAX_NPC_ITEM = Integer.parseInt(altSettings.getProperty(
					"MaxNpcItem", "20"));
			MAX_PERSONAL_WAREHOUSE_ITEM = Integer.parseInt(altSettings
					.getProperty("MaxPersonalWarehouseItem", "100"));
			MAX_CLAN_WAREHOUSE_ITEM = Integer.parseInt(altSettings.getProperty(
					"MaxClanWarehouseItem", "200"));
			DELETE_CHARACTER_AFTER_7DAYS = Boolean.parseBoolean(altSettings
					.getProperty("DeleteCharacterAfter7Days", "True"));
			NPC_DELETION_TIME = Integer.parseInt(altSettings.getProperty(
					"NpcDeletionTime", "10"));
			DEFAULT_CHARACTER_SLOT = Integer.parseInt(altSettings.getProperty(
					"DefaultCharacterSlot", "6"));
			PET_RACE_MIN_PLAYER = Integer.parseInt(altSettings.getProperty(
					"RaceMinPlayer", "2"));
			PET_RACE_MAX_LAP = Integer.parseInt(altSettings.getProperty(
					"RaceMaxLap", "3"));
			DEATH_MATCH_MIN_PLAYER = Integer.parseInt(altSettings.getProperty(
					"DeathMatchMinPlayer", "6"));
			USE_TOI_CHARM_ANYWHERE = Boolean.parseBoolean(altSettings
					.getProperty("UseToiCharmsAnywhere", "False"));
			ELEMENTAL_ENCHANTING = Boolean.parseBoolean(altSettings
					.getProperty("ElementalEnchanting", "False"));
			ELEMENTAL_ENCHANT_LIMIT = Integer.parseInt(altSettings.getProperty(
					"ElementalEnchantLimit", "3"));
			ACCESSORY_ENCHANTING = Boolean.parseBoolean(altSettings
					.getProperty("AccessoryEnchanting", "False"));
			ACCESSORY_ENCHANT_LIMIT = Integer.parseInt(altSettings.getProperty(
					"AccessoryEnchantLimit", "10"));
			ALT_PET_HUNGER_STATUS_CHANGE = Boolean.parseBoolean(altSettings.getProperty("PetHungerStatusChange", "True"));
			ALT_BOSS_EVENT_MAX_SPAWNS_PER_IP = Integer.parseInt(altSettings.getProperty("BossEventMaxSpawnsPerIP", "2"));
		} catch (Exception e) {
			_log.error(e.getLocalizedMessage(), e);
			throw new Error("Failed to load " + ALT_SETTINGS_FILE + " file.");
		}
	}

	private static void loadCharConfig() {
		_log.info("Loading CharSettings config.");
		try {
			Properties charSettings = new Properties();
			InputStream is = new FileInputStream(new File(CHAR_SETTINGS_CONFIG_FILE));
			charSettings.load(is);
			is.close();
			
			PRINCE_MAX_HP = Integer.parseInt(charSettings.getProperty(
					"PrinceMaxHP", "1000"));
			PRINCE_MAX_MP = Integer.parseInt(charSettings.getProperty(
					"PrinceMaxMP", "800"));
			KNIGHT_MAX_HP = Integer.parseInt(charSettings.getProperty(
					"KnightMaxHP", "1400"));
			KNIGHT_MAX_MP = Integer.parseInt(charSettings.getProperty(
					"KnightMaxMP", "600"));
			ELF_MAX_HP = Integer.parseInt(charSettings.getProperty("ElfMaxHP",
					"1000"));
			ELF_MAX_MP = Integer.parseInt(charSettings.getProperty("ElfMaxMP",
					"900"));
			WIZARD_MAX_HP = Integer.parseInt(charSettings.getProperty(
					"WizardMaxHP", "1000"));
			WIZARD_MAX_MP = Integer.parseInt(charSettings.getProperty(
					"WizardMaxMP", "1200"));
			DARKELF_MAX_HP = Integer.parseInt(charSettings.getProperty(
					"DarkelfMaxHP", "1000"));
			DARKELF_MAX_MP = Integer.parseInt(charSettings.getProperty(
					"DarkelfMaxMP", "900"));
			DRAGONKNIGHT_MAX_HP = Integer.parseInt(charSettings.getProperty(
					"DragonKnightMaxHP", "1400"));
			DRAGONKNIGHT_MAX_MP = Integer.parseInt(charSettings.getProperty(
					"DragonKnightMaxMP", "600"));
			ILLUSIONIST_MAX_HP = Integer.parseInt(charSettings.getProperty(
					"IllusionistMaxHP", "900"));
			ILLUSIONIST_MAX_MP = Integer.parseInt(charSettings.getProperty(
					"IllusionistMaxMP", "1100"));
			LV50_EXP = Float.parseFloat(charSettings.getProperty("Lv50Exp",
					"1.0"));
			LV51_EXP = Float.parseFloat(charSettings.getProperty("Lv51Exp",
					"1.0"));
			LV52_EXP = Float.parseFloat(charSettings.getProperty("Lv52Exp",
					"1.0"));
			LV53_EXP = Float.parseFloat(charSettings.getProperty("Lv53Exp",
					"1.0"));
			LV54_EXP = Float.parseFloat(charSettings.getProperty("Lv54Exp",
					"1.0"));
			LV55_EXP = Float.parseFloat(charSettings.getProperty("Lv55Exp",
					"1.0"));
			LV56_EXP = Float.parseFloat(charSettings.getProperty("Lv56Exp",
					"1.0"));
			LV57_EXP = Float.parseFloat(charSettings.getProperty("Lv57Exp",
					"1.0"));
			LV58_EXP = Float.parseFloat(charSettings.getProperty("Lv58Exp",
					"1.0"));
			LV59_EXP = Float.parseFloat(charSettings.getProperty("Lv59Exp",
					"1.0"));
			LV60_EXP = Float.parseFloat(charSettings.getProperty("Lv60Exp",
					"1.0"));
			LV61_EXP = Float.parseFloat(charSettings.getProperty("Lv61Exp",
					"1.0"));
			LV62_EXP = Float.parseFloat(charSettings.getProperty("Lv62Exp",
					"1.0"));
			LV63_EXP = Float.parseFloat(charSettings.getProperty("Lv63Exp",
					"1.0"));
			LV64_EXP = Float.parseFloat(charSettings.getProperty("Lv64Exp",
					"1.0"));
			LV65_EXP = Float.parseFloat(charSettings.getProperty("Lv65Exp",
					"2.0"));
			LV66_EXP = Float.parseFloat(charSettings.getProperty("Lv66Exp",
					"2.0"));
			LV67_EXP = Float.parseFloat(charSettings.getProperty("Lv67Exp",
					"2.0"));
			LV68_EXP = Float.parseFloat(charSettings.getProperty("Lv68Exp",
					"2.0"));
			LV69_EXP = Float.parseFloat(charSettings.getProperty("Lv69Exp",
					"2.0"));
			LV70_EXP = Float.parseFloat(charSettings.getProperty("Lv70Exp",
					"4.0"));
			LV71_EXP = Float.parseFloat(charSettings.getProperty("Lv71Exp",
					"4.0"));
			LV72_EXP = Float.parseFloat(charSettings.getProperty("Lv72Exp",
					"4.0"));
			LV73_EXP = Float.parseFloat(charSettings.getProperty("Lv73Exp",
					"4.0"));
			LV74_EXP = Float.parseFloat(charSettings.getProperty("Lv74Exp",
					"4.0"));
			LV75_EXP = Float.parseFloat(charSettings.getProperty("Lv75Exp",
					"8.0"));
			LV76_EXP = Float.parseFloat(charSettings.getProperty("Lv76Exp",
					"8.0"));
			LV77_EXP = Float.parseFloat(charSettings.getProperty("Lv77Exp",
					"8.0"));
			LV78_EXP = Float.parseFloat(charSettings.getProperty("Lv78Exp",
					"8.0"));
			LV79_EXP = Float.parseFloat(charSettings.getProperty("Lv79Exp",
					"16.0"));
			LV80_EXP = Float.parseFloat(charSettings.getProperty("Lv80Exp",
					"32.0"));
			LV81_EXP = Float.parseFloat(charSettings.getProperty("Lv81Exp",
					"64.0"));
			LV82_EXP = Float.parseFloat(charSettings.getProperty("Lv82Exp",
					"128.0"));
			LV83_EXP = Float.parseFloat(charSettings.getProperty("Lv83Exp",
					"256.0"));
			LV84_EXP = Float.parseFloat(charSettings.getProperty("Lv84Exp",
					"512.0"));
			LV85_EXP = Float.parseFloat(charSettings.getProperty("Lv85Exp",
					"1024.0"));
			LV86_EXP = Float.parseFloat(charSettings.getProperty("Lv86Exp",
					"2048.0"));
			LV87_EXP = Float.parseFloat(charSettings.getProperty("Lv87Exp",
					"4096.0"));
			LV88_EXP = Float.parseFloat(charSettings.getProperty("Lv88Exp",
					"8192.0"));
			LV89_EXP = Float.parseFloat(charSettings.getProperty("Lv89Exp",
					"16384.0"));
			LV90_EXP = Float.parseFloat(charSettings.getProperty("Lv90Exp",
					"32768.0"));
			LV91_EXP = Float.parseFloat(charSettings.getProperty("Lv91Exp",
					"65536.0"));
			LV92_EXP = Float.parseFloat(charSettings.getProperty("Lv92Exp",
					"131072.0"));
			LV93_EXP = Float.parseFloat(charSettings.getProperty("Lv93Exp",
					"262144.0"));
			LV94_EXP = Float.parseFloat(charSettings.getProperty("Lv94Exp",
					"524288.0"));
			LV95_EXP = Float.parseFloat(charSettings.getProperty("Lv95Exp",
					"1048576.0"));
			LV96_EXP = Float.parseFloat(charSettings.getProperty("Lv96Exp",
					"2097152.0"));
			LV97_EXP = Float.parseFloat(charSettings.getProperty("Lv97Exp",
					"4194304.0"));
			LV98_EXP = Float.parseFloat(charSettings.getProperty("Lv98Exp",
					"8388608.0"));
			LV99_EXP = Float.parseFloat(charSettings.getProperty("Lv99Exp",
					"16777216.0"));
		} catch (Exception e) {
			_log.error(e.getLocalizedMessage(), e);
			throw new Error("Failed to load " + CHAR_SETTINGS_CONFIG_FILE
					+ " file.");
		}
	}

	private static void loadPcCommandConfig() {
		_log.info("Loading PcCommandSettings config.");
		try {
			Properties pccommands = new Properties();
			InputStream is = new FileInputStream(new File(PCOMMANDS_SETTINGS_FILE));
			pccommands.load(is);
			is.close();
			
			PLAYER_COMMANDS = Boolean.parseBoolean(pccommands
					.getProperty("PlayerCommands", "True"));
			PLAYER_BUFF = Boolean.parseBoolean(pccommands.getProperty(
					"PlayerBuff", "True"));
			POWER_BUFF = Boolean.parseBoolean(pccommands.getProperty(
					"PowerBuff", "False"));
			DK_BUFF = Boolean.parseBoolean(pccommands.getProperty(
					"DkBuff", "False"));

		} catch (Exception e) {
			_log.error(e.getLocalizedMessage(), e);
			throw new Error("Failed to load " + PCOMMANDS_SETTINGS_FILE
					+ " file.");
		}
	}

	private Config() {
	}

	public static boolean setParameterValue(String pName, String pValue) {
		try {
			// Handle special cases first
			if (pName.equalsIgnoreCase("DropItems")) {
				STOP_DROP = Boolean.parseBoolean(pValue);
				return true;
			}
			if (pName.equalsIgnoreCase("PolyEvent")) {
				ALT_POLYEVENT = Boolean.parseBoolean(pValue);
				return true;
			}
			if (pName.equalsIgnoreCase("Weightrate")) {
				RATE_WEIGHT_LIMIT = Double.parseDouble(pValue);
				return true;
			}
			if (pName.equalsIgnoreCase("DeathMatchMinPlayer")) {
				int value = Integer.parseInt(pValue);
				if (value < 2) value = 2; // Ensure minimum of 2 players
				DEATH_MATCH_MIN_PLAYER = value;
				return true;
			}

			// Handle rate changes
			switch (pName.toLowerCase()) {
				case "ratexp":
					RATE_XP = Double.parseDouble(pValue);
					return true;
				case "ratedropadena":
					RATE_DROP_ADENA = Double.parseDouble(pValue);
					return true;
				case "ratedropitems":
					RATE_DROP_ITEMS = Double.parseDouble(pValue);
					return true;
				case "ratelawful":
					RATE_LA = Double.parseDouble(pValue);
					return true;
				case "ratekarma":
					RATE_KARMA = Double.parseDouble(pValue);
					return true;
				default:
					return false;
			}
		} catch (Exception e) {
			_log.error("Error setting parameter " + pName + " to value " + pValue, e);
			return false;
		}
	}

	public static void reset() {
		_log.info("Reloading rates config.");
		try {
			RATE_XP = RATE_XP_ORG;
			RATE_LA = RATE_LA_ORG;
			RATE_KARMA = RATE_KARMA_ORG;
			RATE_DROP_ADENA = RATE_DROP_ADENA_ORG;
			RATE_DROP_ITEMS = RATE_DROP_ITEMS_ORG;
			RATE_WEIGHT_LIMIT = RATE_WEIGHT_LIMIT_ORG;
		} catch (Exception e) {
			_log.error("Error resetting rates", e);
		}
	}

	private static void validate() {
		if (!IntRange.includes(Config.ALT_ITEM_DELETION_RANGE, 0, 5)) {
			_log.warn("ItemDeletionRange out of allowed range (0-5), using default: 5");
			Config.ALT_ITEM_DELETION_RANGE = 5;
		}
		
		if (!IntRange.includes(Config.ALT_ITEM_DELETION_TIME, 1, 35791)) {
			_log.warn("ItemDeletionTime out of allowed range (1-35791), using default: 10");
			Config.ALT_ITEM_DELETION_TIME = 10;
		}
		
		if (DEATH_MATCH_MIN_PLAYER < 2) {
			_log.warn("DeathMatchMinPlayer was less than 2, setting to default of 6");
			DEATH_MATCH_MIN_PLAYER = 6;
		}
		
		if (MAX_CLAN_MEMBER < 0) {
			_log.warn("MaxClanMember cannot be negative, setting to default of 0 (unlimited)");
			MAX_CLAN_MEMBER = 0;
		}
	}
}
