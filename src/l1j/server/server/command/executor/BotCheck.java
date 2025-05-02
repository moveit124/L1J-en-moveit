package l1j.server.server.command.executor;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.server.GeneralThreadPool;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_SystemMessage;

public class BotCheck implements L1CommandExecutor {

	private static Logger _log = LoggerFactory.getLogger(BotCheck.class);

	public static L1CommandExecutor getInstance() {
		return new BotCheck();
	}

	@Override
	public void execute(L1PcInstance gm, String commandName, String arg) {
		try {
			String[] args = arg.split(" ");
			String target = args[0];

			L1PcInstance player = L1World.getInstance().getPlayer(target);
			if (player == null) {
				gm.sendPackets(new S_SystemMessage("Player not found."));
				return;
			}

			gm.sendPackets(new S_SystemMessage("Running botcheck on " + player.getName() + "..."));
			GeneralThreadPool.getInstance().execute(new BotCheckTask(gm, player));
		} catch (Exception e) {
			_log.error("", e);
			gm.sendPackets(new S_SystemMessage(".botcheck <name>"));
		}
	}

	private static class BotCheckTask implements Runnable {
		private final L1PcInstance gm;
		private final L1PcInstance player;
		private final Random random = new Random();

		private int totalChecks = 0;
		private int reactions = 0;
		private long totalReactionTime = 0;
		private long fastestReaction = Long.MAX_VALUE;

		public BotCheckTask(L1PcInstance gm, L1PcInstance player) {
			this.gm = gm;
			this.player = player;
		}

		@Override
		public void run() {
			long endTime = System.currentTimeMillis() + 60000; // 60 seconds

			while (System.currentTimeMillis() < endTime) {
				try {
					if (player.isDead()) break;

					if (player.isInCombat()) {
						Thread.sleep(5000); // defer if actively fighting
						continue;
					}

					int fakeHp = Math.max(1, (int) (player.getMaxHp() * (0.2 + random.nextDouble() * 0.3)));
					player.sendPackets(new S_OwnCharStatus(player, fakeHp, player.getCurrentMp()));
					long start = System.currentTimeMillis();

					Thread.sleep(100 + random.nextInt(200));

					player.sendPackets(new S_OwnCharStatus(player)); // restore correct status

					boolean reacted = player.isNowMoving() || player.isRecentlyUsedItem();
					long reactionTime = System.currentTimeMillis() - start;

					totalChecks++;
					if (reacted) {
						reactions++;
						totalReactionTime += reactionTime;
						if (reactionTime < fastestReaction) {
							fastestReaction = reactionTime;
						}
					}

					Thread.sleep(4000 + random.nextInt(2000)); // wait 4–6s before next fake
				} catch (Exception e) {
					_log.error("Error during botcheck", e);
					break;
				}
			}

			long avg = reactions > 0 ? totalReactionTime / reactions : 0;
			StringBuilder sb = new StringBuilder();
			sb.append("BotCheck on ").append(player.getName()).append("\n");
			sb.append("Total Checks: ").append(totalChecks).append("\n");
			sb.append("Reactions: ").append(reactions).append("\n");
			sb.append("Fastest: ").append(fastestReaction == Long.MAX_VALUE ? "N/A" : fastestReaction + "ms").append("\n");
			sb.append("Average Reaction: ").append(avg).append("ms\n");

			if (reactions > 0 && reactions >= 4 && avg < 250) {
				sb.append("⚠️ Suspicious behavior detected!");
			}

			for (String line : sb.toString().split("\n")) {
				gm.sendPackets(new S_SystemMessage(line));
			}
		}
	}
}
