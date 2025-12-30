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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.server.datatables.CastleTable;
import l1j.server.server.datatables.NpcActionTable;
import l1j.server.server.datatables.NpcSpawnTable;
import l1j.server.server.datatables.PetItemTable;
import l1j.server.server.datatables.PetTable;
import l1j.server.server.datatables.PetTypeTable;
import l1j.server.server.datatables.ShopTable;
import l1j.server.server.datatables.SkillTable;
import l1j.server.server.datatables.SpawnTable;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1TowerInstance;
import l1j.server.server.network.Client;
import l1j.server.server.network.NetworkServer;
import l1j.server.server.templates.L1PetType;

public class GameServer extends Thread {
	//private ServerSocket _serverSocket;
	private static Logger _log = LoggerFactory.getLogger(GameServer.class.getName());
	//private int _port;

	/** Dedicated executor for handling potentially blocking DB operations during player logout. */
	public static final ExecutorService DB_LOGOUT_EXECUTOR = Executors.newCachedThreadPool();

	// Naive denial of service defense.
	//private static final int CONNECTION_LIMIT = 20;
	private static final int CACHE_REFRESH = 1000 * 60 * 4;
	// Might be overkill, but hard to test. =\
	private static final ConcurrentMap<String, Integer> connectionCache = new ConcurrentHashMap<String, Integer>();
	private static int _yesNoCount = 0;

	static {
		GeneralThreadPool.getInstance().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					connectionCache.clear();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					_log.error("",e);
				}
			}
		}, CACHE_REFRESH, CACHE_REFRESH);
	}

//	@Override
//	public void run() {
//		_log.info("Server started. Memory used: "
//				+ SystemUtil.getUsedMemoryMB() + "MB");
//		_log.info("Waiting for connections!");
//		while (true) {
//			try {
//				Socket socket = _serverSocket.accept();
//				String host = socket.getInetAddress().getHostAddress();
//
//				connectionCache.putIfAbsent(host, 1);
//				
//				try {
//					if (connectionCache.get(host) == CONNECTION_LIMIT) {
//						// Log DOS detection once, but not more than once
//						_log.warn(
//						"GameServer::run: " + host + " hit connection limit.");
//					} else if (connectionCache.get(host) > CONNECTION_LIMIT) {
//						socket.close();
//					} else if (IpTable.getInstance().isBannedIp(host)) {
//						_log.info("Banned IP(" + host + ")");
//					} else {
//						_log.trace("Accepted connection from IP: "
//								+ socket.getInetAddress());
//						Client client = new Client(socket);
//						GeneralThreadPool.getInstance().execute(client);
//					}
//				
//					connectionCache.replace(host, connectionCache.get(host) + 1);
//				} catch(NullPointerException ex) { 
//					// Typically happens if the cache was cleared between the putIfAbsent and the get call
//					// So just close the socket and let them connect again
//					socket.close();
//				}
//			} catch (IOException ioexception) {
//				_log.error("Error creating a client thread connection! IO Exception!");
//				_log.error(ioexception.getLocalizedMessage(), ioexception);
//			} catch(Exception ex) {
//				_log.error("Error creating a client thread connection! General Exception!");
//				_log.error(ex.getLocalizedMessage(), ex);
//				throw ex;
//			}
//		}
//	}

	private static GameServer _instance;

	private GameServer() {
		super("GameServer");
	}

	public static GameServer getInstance() {
		if (_instance == null) {
			_instance = new GameServer();
		}
		return _instance;
	}

	public void initialize() throws Exception {
		_log.info("=================================================");
		_log.info("               L1J-En Server Starting");
		_log.info("=================================================");
		
		_log.info("Thread Lock Detection running");
		SkillTable.initialize();
		GameServerThread.getInstance();
		
		// Load NPC actions before spawning NPCs
		NpcActionTable.load();
		
		// Initialize castle data and tax rates
		_log.info("Loading castle data...");
		CastleTable.getInstance();
		L1CastleLocation.setCastleTaxRate();
		
		// Initialize shop data
		_log.info("Loading shop data...");
		ShopTable.getInstance();
		
		// Initialize boss cycles and spawns
		_log.info("Loading boss cycles...");
		l1j.server.server.model.L1BossCycle.load();
		
		// Initialize NPC spawns
		_log.info("Loading NPC spawns...");
		NpcSpawnTable.getInstance();
		SpawnTable.getInstance();
		PetTypeTable.load();
		PetItemTable.getInstance();
		
		//Load Royal Buff NPC's for war
        L1TowerInstance.initializeRegenNpcs();
		
		// Load treasure box data
		_log.info("Loading treasure box data...");
		l1j.server.server.model.item.L1TreasureBox.load();
		
		//Announcements
		//Announcements.getInstance();
		Announcecycle.getInstance();
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		Thread thread = new Thread(NetworkServer.getInstance());
		thread.start();
	}
	/**
	 * All players online to kick, character and preservation of information.
	 */
	public void disconnectAllCharacters() {
		Collection<L1PcInstance> players = L1World.getInstance()
				.getAllPlayers();
		for (L1PcInstance pc : players) {
			pc.getNetConnection().setActiveChar(null);
			pc.getNetConnection().kick();
		}
		// Kick save after all, make
		for (L1PcInstance pc : players) {
			Client.quitGame(pc, "--SENT FROM DISCONNECTALL--");
			L1World.getInstance().removeObject(pc);
		}
	}

	private class ServerShutdownThread extends Thread {
		private final int _secondsCount;

		public ServerShutdownThread(int secondsCount) {
		    _secondsCount = secondsCount;
		}

		@Override
		public void run() {
		    L1World world = L1World.getInstance();
		    try {
		        int secondsCount = _secondsCount;

		        Set<Integer> timeNotices = new HashSet<>(Arrays.asList(
		            259200, 172800, 86400,             // Days
		            64800, 43200, 21600, 18000, 14400, 10800, 7200, 3600, // Hours
		            2700, 1800, 900, 600, 300, 240, 180, 120, 60,         // Minutes
		            45, 30, 15, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1             // Seconds
		        ));

		        Set<Integer> courtesyCheckpoints = new HashSet<>(Arrays.asList(
		        	    604800, // 7 days
		        	    518400, // 6 days
		        	    432000, // 5 days
		        	    345600, // 4 days
		        	    259200, // 3 days
		        	    172800, // 2 days
		        	    86400,  // 1 day
		        	    64800,  // 18 hours
		        	    43200,  // 12 hours
		        	    21600,  // 6 hours
		        	    3600,   // 1 hour
		        	    600,    // 10 minutes
		        	    60,     // 1 minute
		        	    10      // 10 seconds
		        	));


		        while (secondsCount > 0) {
		            if (timeNotices.contains(secondsCount)) {
		                // ✅ 1. Only show courtesy message if this is a major checkpoint
		                if (courtesyCheckpoints.contains(secondsCount)) {
		                    String msg;
		                    if (secondsCount >= 86400) {
		                        msg = "Scheduled server maintenance is approaching. Please plan accordingly.";
		                    } else if (secondsCount >= 3600) {
		                        msg = "Server maintenance is approaching. Please wrap up your activities.";
		                    } else if (secondsCount >= 60) {
		                        msg = "Reminder: Server restart soon. Get to a safe place and prepare to log out.";
		                    } else {
		                        msg = "Shutdown is imminent. Log out now.";
		                    }
		                    world.broadcastServerMessage(msg);
		                }

		                // ✅ 2. Always show the countdown message
		                if (secondsCount >= 86400) {
		                    int days = secondsCount / 86400;
		                    world.broadcastServerMessage(days + " day" + (days > 1 ? "s" : "") + " till shutdown");
		                } else if (secondsCount >= 3600) {
		                    int hours = secondsCount / 3600;
		                    world.broadcastServerMessage(hours + " hour" + (hours > 1 ? "s" : "") + " till shutdown");
		                } else if (secondsCount >= 60) {
		                    int minutes = secondsCount / 60;
		                    world.broadcastServerMessage(minutes + " minute" + (minutes > 1 ? "s" : "") + " till shutdown");
		                } else {
		                    world.broadcastServerMessage(secondsCount + " seconds till shutdown");
		                }
		            }

		            Thread.sleep(1000);
		            secondsCount--;
		        }

		        world.broadcastServerMessage("Server is shutting down now!");
		        shutdown();

		    } catch (InterruptedException e) {
		        world.broadcastServerMessage("Server shutdown aborted! You may continue playing!");
		    }
		}
	}

	private ServerShutdownThread _shutdownThread = null;
	
	public synchronized boolean isShuttingDown() {
		return _shutdownThread != null;
	}

	public synchronized void shutdownWithCountdown(int secondsCount) {
		if (_shutdownThread != null) {
			// Shutting down the request is already being done
			// TODO error may need to be notified
			return;
		}
		_shutdownThread = new ServerShutdownThread(secondsCount);
		GeneralThreadPool.getInstance().execute(_shutdownThread);
	}

	public void shutdown() {
		disconnectAllCharacters();
		System.exit(0);
	}

	public synchronized void abortShutdown() {
		if (_shutdownThread == null) {
			// Shutting down the request has not been
			// TODO error may need to be notified
			return;
		}
		_shutdownThread.interrupt();
		_shutdownThread = null;
	}
	
	public static int getYesNoCount() {
		_yesNoCount += 1;
		return _yesNoCount;
	}
}