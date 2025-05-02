package l1j.server.server.command.executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.server.model.BotCheckActivityManager;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_RawStringDialog;
import l1j.server.server.serverpackets.S_SystemMessage;

public class BotCheckManager implements L1CommandExecutor {

	private static Logger _log = LoggerFactory.getLogger(BotCheckManager.class);

	public static final Map<String, String> QUESTIONS = Map.ofEntries(
		    Map.entry("Type the number inside parentheses: (3841)", "3841"),
		    Map.entry("Type the number inside parentheses: (7920)", "7920"),
		    Map.entry("Type the number inside parentheses: (1503)", "1503"),
		    Map.entry("Type the number inside parentheses: (9487)", "9487"),
		    Map.entry("Type the number inside parentheses: (5821)", "5821"),
		    Map.entry("Type the number inside parentheses: (2048)", "2048"),
		    Map.entry("Type the number inside parentheses: (6735)", "6735"),
		    Map.entry("Type the number inside parentheses: (9182)", "9182"),
		    Map.entry("Type the number inside parentheses: (1057)", "1057"),
		    Map.entry("Type the number inside parentheses: (4328)", "4328"),

		    Map.entry("Type only the second word: Magic Spell", "Spell"),
		    Map.entry("Type only the second word: Brave Warrior", "Warrior"),
		    Map.entry("Type only the second word: Blue Sky", "Sky"),
		    Map.entry("Type only the second word: Red Dragon", "Dragon"),
		    Map.entry("Type only the second word: Strong Sword", "Sword"),
		    Map.entry("Type only the second word: Golden Shield", "Shield"),
		    Map.entry("Type only the second word: White Horse", "Horse"),
		    Map.entry("Type only the second word: Dark Forest", "Forest"),
		    Map.entry("Type only the second word: Silver Bow", "Bow"),
		    Map.entry("Type only the second word: Mystic Wand", "Wand"),

		    Map.entry("Type the opposite of YES.", "NO"),
		    Map.entry("Type the opposite of NO.", "YES"),
		    Map.entry("Type the opposite of UP.", "DOWN"),
		    Map.entry("Type the opposite of LEFT.", "RIGHT"),
		    Map.entry("Type the opposite of OPEN.", "CLOSE"),
		    Map.entry("Type the opposite of DAY.", "NIGHT"),
		    Map.entry("Type the opposite of HOT.", "COLD"),
		    Map.entry("Type the opposite of HIGH.", "LOW"),
		    Map.entry("Type the opposite of WIN.", "LOSE"),
		    Map.entry("Type the opposite of FAST.", "SLOW"),

		    Map.entry("Type the word: RED", "RED"),
		    Map.entry("Type the word: BLUE", "BLUE"),
		    Map.entry("Type the word: GREEN", "GREEN"),
		    Map.entry("Type the word: YELLOW", "YELLOW"),
		    Map.entry("Type the word: WHITE", "WHITE"),
		    Map.entry("Type the word: BLACK", "BLACK"),
		    Map.entry("Type the word: ORANGE", "ORANGE"),
		    Map.entry("Type the word: PURPLE", "PURPLE"),
		    Map.entry("Type the word: BROWN", "BROWN"),
		    Map.entry("Type the word: GRAY", "GRAY"),

		    Map.entry("Type the number one.", "1"),
		    Map.entry("Type the number two.", "2"),
		    Map.entry("Type the number three.", "3"),
		    Map.entry("Type the number four.", "4"),
		    Map.entry("Type the number five.", "5"),
		    Map.entry("Type the number six.", "6"),
		    Map.entry("Type the number seven.", "7"),
		    Map.entry("Type the number eight.", "8"),
		    Map.entry("Type the number nine.", "9"),
		    Map.entry("Type the number zero.", "0"),

		    Map.entry("Type only the third word: Rise of Heroes", "Heroes"),
		    Map.entry("Type only the third word: Call of Battle", "Battle"),
		    Map.entry("Type only the third word: Light of Dawn", "Dawn"),
		    Map.entry("Type only the third word: Sword of Power", "Power"),
		    Map.entry("Type only the third word: Heart of Gold", "Gold"),
		    Map.entry("Type only the third word: Path of Warriors", "Warriors"),
		    Map.entry("Type only the third word: Wings of Freedom", "Freedom"),
		    Map.entry("Type only the third word: Sea of Storms", "Storms"),
		    Map.entry("Type only the third word: Land of Shadows", "Shadows"),
		    Map.entry("Type only the third word: Tower of Eternity", "Eternity"),

		    Map.entry("Type only the second word: Ancient Tree", "Tree"),
		    Map.entry("Type only the second word: Mystic Stone", "Stone"),
		    Map.entry("Type only the second word: Golden Bow", "Bow"),
		    Map.entry("Type only the second word: Silver Sword", "Sword"),
		    Map.entry("Type only the second word: Silent River", "River"),
		    Map.entry("Type only the second word: Burning Flame", "Flame"),
		    Map.entry("Type only the second word: Frozen Lake", "Lake"),
		    Map.entry("Type only the second word: Shining Star", "Star"),
		    Map.entry("Type only the second word: Dark Knight", "Knight"),
		    Map.entry("Type only the second word: Light Arrow", "Arrow"),
		    
		    Map.entry("Type the first letter of: Knight", "K"),
		    Map.entry("Type the first letter of: Spirit", "S"),
		    Map.entry("Type the first letter of: Energy", "E"),
		    Map.entry("Type the first letter of: Battle", "B"),
		    Map.entry("Type the first letter of: Dragon", "D"),
		    Map.entry("Type the first letter of: Magic", "M"),
		    Map.entry("Type the first letter of: Sword", "S"),
		    Map.entry("Type the first letter of: Shield", "S"),
		    Map.entry("Type the first letter of: Hero", "H"),
		    Map.entry("Type the first letter of: Quest", "Q"),

		    Map.entry("Type the last letter of: Castle", "E"),
		    Map.entry("Type the last letter of: Knight", "T"),
		    Map.entry("Type the last letter of: Legend", "D"),
		    Map.entry("Type the last letter of: Treasure", "E"),
		    Map.entry("Type the last letter of: Monster", "R"),
		    Map.entry("Type the last letter of: Dungeon", "N"),
		    Map.entry("Type the last letter of: Spirit", "T"),
		    Map.entry("Type the last letter of: Energy", "Y"),
		    Map.entry("Type the last letter of: Sword", "D"),
		    Map.entry("Type the last letter of: Shield", "D"),

		    Map.entry("Type the color of the sky", "BLUE"),
		    Map.entry("Type the color of grass", "GREEN"),
		    Map.entry("Type the color of blood", "RED"),
		    Map.entry("Type the color of the sun", "YELLOW"),
		    Map.entry("Type the color of snow", "WHITE"),
		    Map.entry("Type the color of a pumpkin", "ORANGE"),
		    Map.entry("Type the color of chocolate", "BROWN"),
		    Map.entry("Type the color of amethyst", "PURPLE"),

		    Map.entry("Type the sum of 1 + 2", "3"),
		    Map.entry("Type the sum of 4 + 5", "9"),
		    Map.entry("Type the sum of 3 + 6", "9"),
		    Map.entry("Type the sum of 7 + 1", "8"),
		    Map.entry("Type the sum of 5 + 2", "7"),
		    Map.entry("Type the sum of 6 + 3", "9"),
		    Map.entry("Type the sum of 8 + 2", "10"),
		    Map.entry("Type the sum of 9 + 1", "10"),
		    Map.entry("Type the sum of 2 + 4", "6"),
		    Map.entry("Type the sum of 5 + 5", "10")

		);

	public static final Random RANDOM = new Random();

	private BotCheckManager() {
	}

	public static L1CommandExecutor getInstance() {
		return new BotCheckManager();
	}

	@Override
	public void execute(L1PcInstance gm, String cmdName, String arg) {
		try {
			if (arg == null || arg.isEmpty()) {
				throw new Exception();
			}

			L1PcInstance target = L1World.getInstance().getPlayer(arg);
			if (target == null) {
				gm.sendPackets(new S_SystemMessage("Player not found."));
				return;
			}

			if (target.isDead() || target.isTeleport() || target.isInCombat()) {
				gm.sendPackets(new S_SystemMessage("Cannot bot check player in combat or teleporting."));
				return;
			}

			BotCheckActivityManager.triggerBotCheck(target);

			gm.sendPackets(new S_SystemMessage("Sent botcheck question to " + target.getName() + "."));

		} catch (Exception e) {
			_log.error("Error in BotCheckManager", e);
			gm.sendPackets(new S_SystemMessage("." + cmdName + " <playername>"));
		}
	}
}
