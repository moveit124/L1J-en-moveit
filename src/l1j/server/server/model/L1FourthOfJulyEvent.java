package l1j.server.server.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;

public class L1FourthOfJulyEvent {
	private static Logger _log = LoggerFactory.getLogger(L1FourthOfJulyEvent.class.getName());
	
	private static int EventKillCountMage = 0;
	
	private static int EventKillCountOther = 0;
	
	private static int EventKillCount = 0;
	
	public static void addEventKillCountMage() {
		EventKillCountMage++;
		EventKillCount++;

		checkMageLeadMessages();
		updateEventKillCountsInDB();

		switch (EventKillCount) {
		case 2500:
			Config.setParameterValue("RateXp", "" + (Config.RATE_XP_ORG * 1.05));
			broadcast("The community has slain 2,500 event mobs! +5% XP unlocked for Independence Day!");
			break;
		case 5000:
			Config.setParameterValue("RateDropAdena", "" + (Config.RATE_DROP_ADENA_ORG * 1.05));
			broadcast("5,000 kills! +5% Adena drops lit up like fireworks!");
			break;
		case 10000:
			Config.setParameterValue("RateDropItems", "" + (Config.RATE_DROP_ITEMS_ORG * 1.05));
			broadcast("10,000 mobs down! +5% item drops to celebrate the Fourth!");
			break;
		case 20000:
			Config.setParameterValue("RateDropAdena", "" + (Config.RATE_DROP_ADENA_ORG * 1.10));
			Config.setParameterValue("RateXp", "" + (Config.RATE_XP_ORG * 1.10));
			broadcast("20,000 kills! +10% XP and +10% Adena. You're heating up!");
			break;
		case 50000:
			Config.setParameterValue("RateDropItems", "" + (Config.RATE_DROP_ITEMS_ORG * 1.10));
			broadcast("50,000 mobs slain! +10% drop rate. You're lighting up the sky!");
			break;
		case 100000:
			Config.setParameterValue("RateXp", "" + (Config.RATE_XP_ORG * 1.20));
			Config.setParameterValue("RateDropItems", "" + (Config.RATE_DROP_ITEMS_ORG * 1.20));
			Config.setParameterValue("RateDropAdena", "" + (Config.RATE_DROP_ADENA_ORG * 1.20));
			broadcast("100,000 kills! +20% XP, Drops, and Adena! The server spirit is unbreakable this Fourth of July!");
			break;
		}
	}

	
	public static void addEventKillCountOther() {
		EventKillCountOther++;
		EventKillCount++;
		
		checkMageLeadMessages();
		updateEventKillCountsInDB();

		switch (EventKillCount) {
		case 2500:
			Config.setParameterValue("RateXp", "" + (Config.RATE_XP_ORG * 1.05));
			broadcast("The community has slain 2,500 event mobs! +5% XP unlocked for Independence Day!");
			break;
		case 5000:
			Config.setParameterValue("RateDropAdena", "" + (Config.RATE_DROP_ADENA_ORG * 1.05));
			broadcast("5,000 kills! +5% Adena drops lit up like fireworks!");
			break;
		case 10000:
			Config.setParameterValue("RateDropItems", "" + (Config.RATE_DROP_ITEMS_ORG * 1.05));
			broadcast("10,000 mobs down! +5% item drops to celebrate the Fourth!");
			break;
		case 20000:
			Config.setParameterValue("RateDropAdena", "" + (Config.RATE_DROP_ADENA_ORG * 1.10));
			Config.setParameterValue("RateXp", "" + (Config.RATE_XP_ORG * 1.10));
			broadcast("20,000 kills! +10% XP and +10% Adena. You're heating up!");
			break;
		case 50000:
			Config.setParameterValue("RateDropItems", "" + (Config.RATE_DROP_ITEMS_ORG * 1.10));
			broadcast("50,000 mobs slain! +10% drop rate. You're lighting up the sky!");
			break;
		case 100000:
			Config.setParameterValue("RateXp", "" + (Config.RATE_XP_ORG * 1.20));
			Config.setParameterValue("RateDropItems", "" + (Config.RATE_DROP_ITEMS_ORG * 1.20));
			Config.setParameterValue("RateDropAdena", "" + (Config.RATE_DROP_ADENA_ORG * 1.20));
			broadcast("100,000 kills! +20% XP, Drops, and Adena! The server spirit is unbreakable this Fourth of July!");
			break;
		}
	}
	
	private static void updateEventKillCountsInDB() {
		try (Connection con = L1DatabaseFactory.getInstance().getConnection();
		     PreparedStatement ps = con.prepareStatement("UPDATE event SET MageKills=?, OtherKills=?, TotalKills=?")) {
			ps.setInt(1, EventKillCountMage);
			ps.setInt(2, EventKillCountOther);
			ps.setInt(3, EventKillCount);
			ps.executeUpdate();
		} catch (SQLException e) {
			_log.error("Failed to update event kill counts: " + e.getMessage(), e);
		}
	}
	
	public static void resetEvent() {
		EventKillCountMage = 0;
		EventKillCountOther = 0;
		EventKillCount = 0;
		lastBroadcastBucket = -999;
		lastBroadcastTime = 0;

		// Reset server config values
		Config.setParameterValue("RateXp", "" + Config.RATE_XP_ORG);
		Config.setParameterValue("RateDropItems", "" + Config.RATE_DROP_ITEMS_ORG);
		Config.setParameterValue("RateDropAdena", "" + Config.RATE_DROP_ADENA_ORG);

		// Reset DB
		try (Connection con = L1DatabaseFactory.getInstance().getConnection();
		     PreparedStatement ps = con.prepareStatement("UPDATE event SET MageKills=0, OtherKills=0, TotalKills=0")) {
			ps.executeUpdate();
		} catch (SQLException e) {
			_log.error("Failed to reset event counts: " + e.getMessage(), e);
		}
	}

	public void setEventKillCount(int mage, int other, int total) {
		EventKillCountMage = mage;
		EventKillCountOther = other;
		EventKillCount = total;
	}
	
	public static void loadEventKillCountsFromDB() {
		try (Connection con = L1DatabaseFactory.getInstance().getConnection();
		     PreparedStatement ps = con.prepareStatement("SELECT MageKills, OtherKills, TotalKills FROM event");
		     ResultSet rs = ps.executeQuery()) {

			if (rs.next()) {
				EventKillCountMage = rs.getInt("MageKills");
				EventKillCountOther = rs.getInt("OtherKills");
				EventKillCount = rs.getInt("TotalKills");

				// Reapply milestone bonuses based on current total
				if (EventKillCount >= 100000) {
					Config.setParameterValue("RateXp", "" + (Config.RATE_XP_ORG * 1.20));
					Config.setParameterValue("RateDropItems", "" + (Config.RATE_DROP_ITEMS_ORG * 1.20));
					Config.setParameterValue("RateDropAdena", "" + (Config.RATE_DROP_ADENA_ORG * 1.20));
				} else if (EventKillCount >= 50000) {
					Config.setParameterValue("RateDropItems", "" + (Config.RATE_DROP_ITEMS_ORG * 1.10));
				} else if (EventKillCount >= 20000) {
					Config.setParameterValue("RateXp", "" + (Config.RATE_XP_ORG * 1.10));
					Config.setParameterValue("RateDropAdena", "" + (Config.RATE_DROP_ADENA_ORG * 1.10));
				} else if (EventKillCount >= 10000) {
					Config.setParameterValue("RateDropItems", "" + (Config.RATE_DROP_ITEMS_ORG * 1.05));
				} else if (EventKillCount >= 5000) {
					Config.setParameterValue("RateDropAdena", "" + (Config.RATE_DROP_ADENA_ORG * 1.05));
				} else if (EventKillCount >= 2500) {
					Config.setParameterValue("RateXp", "" + (Config.RATE_XP_ORG * 1.05));
				}
			}
		} catch (SQLException e) {
			_log.error("Failed to load event kill counts: " + e.getMessage(), e);
		}
	}

	public static String getCurrentBonusMessage() {
		String colorPrefix = "\\fY"; // Purple
		String prefix = ""; // No prefix
		int maxLineLength = 55;

		List<String> lines = new ArrayList<>();

		if (EventKillCount < 100) {
			lines.add("The Fourth of July event is just getting started — kill event mobs to help unlock server-wide bonuses!");
		} else {
			double magePercent = ((2.0 * EventKillCountMage - EventKillCount) / EventKillCount) * 100;
			int magePercentRounded = (int) magePercent;
			int EventKillCountOther = EventKillCount - EventKillCountMage;

			lines.add("The Fourth of July event is active!");
			if (magePercentRounded > 0) {
				lines.add("Currently, the Mages are winning by " + magePercentRounded + "% with " + EventKillCountMage + " kills vs " + EventKillCountOther + " kills.");
			} else if (magePercentRounded < 0) {
				lines.add("Currently, the Mages are losing by " + Math.abs(magePercentRounded) + "% with " + EventKillCountMage + " kills vs " + EventKillCountOther + " kills.");
			} else {
				lines.add("It's currently a tie! Both sides have " + EventKillCountMage + " kills.");
			}
			
			String bonus;
			if (EventKillCount >= 100000) {
				bonus = "+20% XP, +20% Drop, +20% Adena";
			} else if (EventKillCount >= 50000) {
				bonus = "+10% XP, +10% Drop, +10% Adena";
			} else if (EventKillCount >= 20000) {
				bonus = "+10% XP, +5% Drop, +10% Adena";
			} else if (EventKillCount >= 10000) {
				bonus = "+5% XP, +5% Drop, +5% Adena";
			} else if (EventKillCount >= 5000) {
				bonus = "+5% XP, +0% Drop, +5% Adena";
			} else if (EventKillCount >= 2500) {
				bonus = "+5% XP";
			} else {
				bonus = "No bonus yet, kill mobs to unlock rewards!";
			}
			lines.add("Current Bonus: " + bonus);
		}

		StringBuilder processed = new StringBuilder(colorPrefix);

		for (String rawLine : lines) {
			int lineLength = prefix.length();
			for (String word : rawLine.split(" ")) {
				if (lineLength + word.length() + 1 > maxLineLength) {
					processed.append("\n").append(colorPrefix);
					lineLength = 0;
				}
				processed.append(word).append(" ");
				lineLength += word.length() + 1;
			}
			if (!rawLine.equals(lines.get(lines.size() - 1))) {
			    processed.append("\n").append(colorPrefix);
			}
		}

		return processed.toString().trim();
	}
	
	public static void broadcast(String message) {
	    String colorPrefix = "\\fY";
	    int maxLineLength = 55;
	    int lineLength = 0;

	    List<String> lines = new ArrayList<>();
	    StringBuilder currentLine = new StringBuilder();

	    for (String word : message.split(" ")) {
	        if (lineLength + word.length() + 1 > maxLineLength) {
	            lines.add(currentLine.toString().trim());
	            currentLine = new StringBuilder();
	            lineLength = 0;
	        }
	        currentLine.append(word).append(" ");
	        lineLength += word.length() + 1;
	    }

	    if (currentLine.length() > 0) {
	        lines.add(currentLine.toString().trim());
	    }

	    StringBuilder processed = new StringBuilder(colorPrefix);
	    for (String line : lines) {
	        processed.append(line);
	        if (!line.equals(lines.get(lines.size() - 1))) {
	            processed.append("\n").append(colorPrefix);
	        }
	    }

	    String finalMessage = processed.toString();

	    for (L1PcInstance pc : L1World.getInstance().getAllPlayers()) {
	        pc.sendPackets(new S_SystemMessage(finalMessage));
	    }
	}

	private static int lastBroadcastBucket = -999;
	private static long lastBroadcastTime = 0;
	private static final long BROADCAST_COOLDOWN_MS = 15 * 60 * 1000; // 15 minutes
	
	private static void checkMageLeadMessages() {
		if (EventKillCount < 100) return;

		// Check if cooldown has expired
		long now = System.currentTimeMillis();
		if (now - lastBroadcastTime < BROADCAST_COOLDOWN_MS) return;
		
		double percent = ((2.0 * EventKillCountMage - EventKillCount) / EventKillCount) * 100;

		int currentBucket = ((int) percent / 10) * 10;

		// Ignore if we're still in the same bucket
		if (currentBucket == lastBroadcastBucket) return;

		// Update only after we've decided to announce
		boolean increasing = currentBucket > lastBroadcastBucket;
		lastBroadcastBucket = currentBucket;
		lastBroadcastTime = now; // <-- Add this here

		if (currentBucket >= 100) {
			broadcast("TOTAL DOMINANCE! The mages hold 100% of the battlefield. #BKLM winning by an arcane landslide!");
		} else if (currentBucket >= 90) {
			broadcast(increasing
				? "The crowd chants 'Justice for BKs!' as mage support hits 90%! Democracy has never been so flammable!"
				: "Mages slip from total control,  support drops below 100%! Is the fire dying out?");
		} else if (currentBucket >= 80) {
			broadcast(increasing
				? "Mage propaganda floods the streets! 80% support and rising! The Black Knights will rise again!"
				: "Support shrinks to 80%,  rogue warriors campaign against #BKLM with sharpened memes!");
		} else if (currentBucket >= 70) {
			broadcast(increasing
				? "70% in favor of #BKLM! Mages now openly campaign in robes and riot gear!"
				: "Momentum cooling,  support dips to 70%! Mages blame melee misinformation.");
		} else if (currentBucket >= 60) {
			broadcast(increasing
				? "60% support for #BKLM! Mages distribute protest fliers... and fireballs."
				: "Opposition ads air in Giran: 'Stop the Spellcasters!' #BKLM drops to 60%.");
		} else if (currentBucket >= 50) {
			broadcast(increasing
				? "Mages hold a slim majority at 50%! The vote teeters like a summoned golem!"
				: "Majority lost! Mages drop below 50%,  swords are out for the #BKLM cause.");
		} else if (currentBucket >= 40) {
			broadcast(increasing
				? "A surprising upswing: #BKLM regains 40%! Are the townsfolk reconsidering?"
				: "Only 40% back #BKLM. Mages caught inflating petition signatures.");
		} else if (currentBucket >= 30) {
			broadcast(increasing
				? "Mage rallies spark hope,  30% support restored!"
				: "Mage support collapses to 30%! Protestors boo spellcasters off the stage.");
		} else if (currentBucket >= 20) {
			broadcast(increasing
				? "Is it a comeback? Mages scratch their way back to 20%!"
				: "Just 20% support for #BKLM. Mages lose control of the narrative.");
		} else if (currentBucket >= 10) {
			broadcast(increasing
				? "A glimmer of hope: 10% still stand with #BKLM!"
				: "Even the mages are questioning the cause,  10% support and falling.");
		} else if (currentBucket >= 0) {
			broadcast(increasing
				? "Support rises from the ashes,  the campaign reawakens at 0%!"
				: "All support lost. Mages consider rebranding the movement as #BlackRobeLivesMatter.");
		} else if (currentBucket >= -10) {
			broadcast("Public sentiment turns,  the anti-BKLM campaign holds a slim 10% lead!");
		} else if (currentBucket >= -20) {
			broadcast("Villagers vote melee! #BKLM drops further behind,  now trailing by 20%.");
		} else if (currentBucket >= -30) {
			broadcast("'Arcane agenda exposed!' chants fill the streets. 30% lead for the opposition!");
		} else if (currentBucket >= -40) {
			broadcast("Torches out, pitchforks up! The #BKLM movement teeters on collapse.");
		} else {
			broadcast("MASSIVE BACKLASH! -50% mage influence. New laws proposed to ban robes in public!");
		}
	}

	
	public int getEventKillCount(int type) {
		if (type == 0) {
			return EventKillCountMage;
		} else if (type == 1) {
			return EventKillCountOther;
		} else {
			return EventKillCount;
		}
	}
}
