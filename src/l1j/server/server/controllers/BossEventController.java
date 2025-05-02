package l1j.server.server.controllers;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.Config;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.encryptions.IdFactory;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.utils.L1SpawnUtil;

public class BossEventController implements Runnable {
	private static Logger _log = LoggerFactory.getLogger(BossEventController.class.getName());
	SecureRandom rand;
	ArrayList<Integer> bossIds = new ArrayList<Integer>();
	HashMap<String, Integer> ipsHit = new HashMap<String, Integer>();
	private static final int randomRange = 10;
	
	private static BossEventController _instance;
	
	private BossEventController() {
		rand = new SecureRandom();
		
		String[] tmpBossIds = Config.ALT_BOSS_EVENT_BOSSES.split(",");
		
		for(String bossId : tmpBossIds) {
			bossIds.add(Integer.parseInt(bossId));
		}
	}
	
	public static BossEventController getInstance() {
		if(_instance == null)
			_instance = new BossEventController();
		
		return _instance;
	}
	
	private int lastDayRun = -1;
	private int lastHourRun = -1;
	private boolean spawnBoss = false;
	
	public ArrayList<Integer> getBossIds() {
		return bossIds;
	}
		
	@Override
	public void run() {
		Thread.currentThread().setName("BossEventController");
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		
		try {		
			while(true) {
				// by default sleep 5 minutes - 300000
				int sleepTime = 300000;
				
				Calendar rightNow = Calendar.getInstance();
				int hour = rightNow.get(Calendar.HOUR_OF_DAY);
				int day = rightNow.get(Calendar.DAY_OF_MONTH);
				
				if(day != lastDayRun && Config.ALT_BOSS_EVENT_DAILY_RESET 
						&& dateFormat.parse(dateFormat.format(rightNow.getTime())).after(dateFormat.parse(Config.ALT_BOSS_EVENT_RESET_TIME + ":00")))
				{
					lastDayRun = day;
					ipsHit.clear();
				}
				
				// if we haven't spawned a boss this hour, set the sleep time to some random interval 
				// that will cause them to spawn within this hour
				if(lastHourRun != hour && !spawnBoss) {
					int minute = 57 - rightNow.get(Calendar.MINUTE); // don't spawn within the last 3 minutes of the hour
					
					if(minute <= 0) {
						minute = 1; // set to 1 because rand.nextInt will crap out with a 0 value
					}

					sleepTime = rand.nextInt(minute * 60000);
					
					_log.info(String.format("The next boss will spawn in %d minutes!", sleepTime / 60000));
					spawnBoss = true;
				} else if(spawnBoss) {
					ArrayList<L1PcInstance> players = new ArrayList<L1PcInstance>(L1World.getInstance().getAllPlayers());
					if(players.size() > 0) { // really only needed for testing, but good to have
						lastHourRun = hour;
						spawnBoss = false;
						
						L1World world = L1World.getInstance();
						
						// 60% chance 1 boss, 30% chance 2 bosses, 10% chance 3 bosses
						int spawnRate = rand.nextInt(10000);
						
						int numBossesToSpawn = 1;  // default to 1 boss (60% chance)
						
						if(spawnRate < 1000) {  // 10% chance for 3 bosses
							numBossesToSpawn = 3;
						} else if(spawnRate < 4000) {  // 30% chance for 2 bosses
							numBossesToSpawn = 2;
						}
						
						int spawnedBosses = 0;
						
						while(spawnedBosses < numBossesToSpawn) {
							Thread.sleep(1000); // just so if it doesn't find someone, it isn't spamming
							L1PcInstance player = null;
							
							// Get a random player that hasn't had too many bosses spawn on them
							ArrayList<L1PcInstance> validPlayers = new ArrayList<L1PcInstance>();
							for(L1PcInstance p : players) {
								if(!ipsHit.containsKey(p.getNetConnection().getIp()) 
										|| ipsHit.get(p.getNetConnection().getIp()) < Config.ALT_BOSS_EVENT_MAX_SPAWNS_PER_IP) {
									validPlayers.add(p);
								}
							}
							
							if(validPlayers.size() == 0) {
								// If we can't find any valid players, just pick from everyone
								player = players.get(rand.nextInt(players.size()));
							} else {
								player = validPlayers.get(rand.nextInt(validPlayers.size()));
							}
							
							if(player == null || player.getMapId() > 10000) {
								continue;
							}
							
							L1Npc boss = NpcTable.getInstance().getTemplate(
									bossIds.get(rand.nextInt(bossIds.size())));
							
							L1NpcInstance crack = NpcTable.getInstance()
									.newNpcInstance(81277);
							crack.setId(IdFactory.getInstance().nextId());
							crack.setMap(player.getMapId());
							
							if(player.getMap().isInMap(player.getLocation())) {
								crack.getLocation().set(player.getLocation());
								crack.getLocation().forward(player.getHeading());
							}
							
							L1World.getInstance().storeObject(crack);
							L1World.getInstance().addVisibleObject(crack);
							
							Thread.sleep(3000);
							
							crack.deleteMe();
							
							// Spawn the boss and announce it immediately
							L1SpawnUtil.spawn(player, boss.get_npcId(), crack.getX(), crack.getY(), 1500000);
							world.broadcastServerMessage(String.format("\\fR[******] %s has appeared!", boss.get_name()));
							_log.info(String.format("The Boss Event has spawned %s on player %s!", boss.get_name(), player.getName()));
							
							int currentIpConnections = 0;
							if(ipsHit.containsKey(player.getNetConnection().getIp())) {
								currentIpConnections = ipsHit.get(player.getNetConnection().getIp());
							}
							
							ipsHit.put(player.getNetConnection().getIp(), currentIpConnections + 1);
							spawnedBosses++;
						}
					}
				}

				_log.trace("Boss Event Controller Finished run, sleeping.");
				Thread.sleep(sleepTime);
			}
		} catch(Exception ex) {
			_log.error(ex.getLocalizedMessage(), ex);
			_log.warn("Boss Event Controller Crashed! No bosses for event will spawn!");
		}
	}
}
