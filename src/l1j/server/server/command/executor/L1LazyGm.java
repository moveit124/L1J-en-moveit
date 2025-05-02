package l1j.server.server.command.executor;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.server.GeneralThreadPool;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;

public class L1LazyGm implements L1CommandExecutor {

	private static Logger _log = LoggerFactory.getLogger(L1LazyGm.class);

	private static ConcurrentHashMap<Integer, LazyGmTimer> _lazyGmTimers = new ConcurrentHashMap<>();

	protected static class LazyGmTimer implements Runnable {
		private final L1PcInstance _gm;
		private final int _interval;
		private volatile boolean _stop = false;
		private final LinkedList<L1PcInstance> _playerQueue = new LinkedList<>();
		private final ConcurrentHashMap<String, int[]> _playerPositions = new ConcurrentHashMap<>();

		public LazyGmTimer(L1PcInstance gm, int interval) {
			_gm = gm;
			_interval = interval;
			initializeQueue();
		}

		private void initializeQueue() {
			for (L1PcInstance player : L1World.getInstance().getAllPlayers()) {
				if (isValidTarget(player)) {
					_playerQueue.add(player);
				}
			}
		}

		private boolean isValidTarget(L1PcInstance player) {
			return player != null &&
				   !player.isGhost() &&
				   !player.isDead() &&
				   player.getId() != _gm.getId() &&
				   !player.getMap().isTradeZone();
		}

		@Override
		public void run() {
			try {
				Thread.currentThread().setName("L1LazyGm");
				while (!_stop && _lazyGmTimers.containsKey(_gm.getId())) {
					try {
						if (_playerQueue.isEmpty()) {
							_gm.sendPackets(new S_SystemMessage("No players to observe. Stopping lazygm."));
							break;
						}

						// Update player list with any new valid players
						for (L1PcInstance player : L1World.getInstance().getAllPlayers()) {
							if (isValidTarget(player) && !_playerQueue.contains(player)) {
								_playerQueue.addLast(player);
							}
						}

						L1PcInstance personToWatch = null;
						int skippedCount = 0;
						int queueSize = _playerQueue.size();

						while (!_playerQueue.isEmpty()) {
							L1PcInstance next = _playerQueue.pollFirst();

							if (next == null || !L1World.getInstance().getAllPlayers().contains(next)) {
								if (next != null) _playerPositions.remove(next.getName());
								skippedCount++;
								continue;
							}

							if (!isValidTarget(next)) {
								_playerPositions.remove(next.getName());
								skippedCount++;
								continue;
							}

							int[] lastPos = _playerPositions.get(next.getName());
							boolean hasMoved = lastPos == null ||
									lastPos[0] != next.getX() ||
									lastPos[1] != next.getY() ||
									lastPos[2] != next.getMapId();

							if (!hasMoved) {
								// Don't spam: just silently skip and track
								_playerQueue.addLast(next);
								skippedCount++;
								continue;
							}

							// Found valid player
							_playerPositions.put(next.getName(), new int[] {
								next.getX(), next.getY(), next.getMapId()
							});
							personToWatch = next;
							_playerQueue.addLast(next);
							break;
						}

						// If everyone was skipped, sleep and retry later
						if (personToWatch == null && skippedCount >= queueSize) {
							_gm.sendPackets(new S_SystemMessage("No players have moved. Retrying in " + _interval + " seconds..."));
							Thread.sleep(_interval * 1000L);
							continue;
						}


						if (personToWatch == null) {
							_gm.sendPackets(new S_SystemMessage("Could not find valid players to watch. Retrying..."));
							Thread.sleep(3000);
							continue;
						}

						L1Teleport.teleport(_gm, personToWatch.getX(), personToWatch.getY(),
								personToWatch.getMapId(), 5, false);

						personToWatch.setFollowingGm(_gm);
						_gm.sendPackets(new S_SystemMessage("Moved on to " + personToWatch.getName()));

						for (int i = 0; i < _interval; i++) {
							if (_stop) break;
							Thread.sleep(1000);
						}
					} catch (Exception e) {
						break;
					}
				}

				stop();
			} catch (Exception e) {
				_log.error("", e);
			}
		}

		private void stopTeleport() {
			_lazyGmTimers.remove(_gm.getId());
		}

		private void stopFollow() {
			for (L1PcInstance player : L1World.getInstance().getAllPlayers()) {
				if (player.getFollowingGm() == _gm) {
					player.setFollowingGm(null);
				}
			}
		}

		private void stop() {
			_stop = true;
			stopFollow();
			stopTeleport();
		}
	}

	public L1LazyGm() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1LazyGm();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			if (arg.trim().equalsIgnoreCase("-stop")) {
				if (_lazyGmTimers.containsKey(pc.getId())) {
					_lazyGmTimers.get(pc.getId()).stop();
					pc.sendPackets(new S_SystemMessage("LazyGm has been stopped."));
				} else {
					pc.sendPackets(new S_SystemMessage("LazyGm is not running."));
				}
				return;
			}

			if (_lazyGmTimers.containsKey(pc.getId())) {
				pc.sendPackets(new S_SystemMessage("LazyGm is already running. Use '.lazygm -stop' first."));
				return;
			}

			int playersOnline = L1World.getInstance().getAllPlayers().size();
			if (playersOnline < 3) {
				pc.sendPackets(new S_SystemMessage("There has to be more than 1 person to watch!"));
				return;
			}

			if (!pc.isGmInvis()) {
				pc.sendPackets(new S_SystemMessage("You must be invisible to run this command!"));
				return;
			}

			int seconds = Integer.parseInt(arg);
			LazyGmTimer timer = new LazyGmTimer(pc, seconds);
			_lazyGmTimers.put(pc.getId(), timer);
			GeneralThreadPool.getInstance().execute(timer);

			pc.sendPackets(new S_SystemMessage("LazyGm has been started. You will move every " + seconds + " seconds."));
		} catch (Exception ex) {
			pc.sendPackets(new S_SystemMessage("." + cmdName + " <seconds> or -stop"));
		}
	}
}
