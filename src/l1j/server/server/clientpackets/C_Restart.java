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
package l1j.server.server.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.Config;
import l1j.server.server.ActionCodes;
import l1j.server.server.datatables.CharBuffTable;
import l1j.server.server.model.Getback;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.network.Client;
import l1j.server.server.serverpackets.S_CharVisualUpdate;
import l1j.server.server.serverpackets.S_HPUpdate;
import l1j.server.server.serverpackets.S_MPUpdate;
import l1j.server.server.serverpackets.S_MapID;
import l1j.server.server.serverpackets.S_OtherCharPacks;
import l1j.server.server.serverpackets.S_OwnCharPack;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_Weather;
import l1j.server.server.serverpackets.S_CharacterConfig;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_Restart extends ClientBasePacket {

	private static final String C_RESTART = "[C] C_Restart";
	private static Logger _log = LoggerFactory.getLogger(C_Restart.class);

	public C_Restart(byte abyte0[], Client client) throws Exception {
		super(abyte0);
		
		try {
			// Log the raw packet data for analysis
			StringBuilder packetData = new StringBuilder("Restart packet raw data: ");
			for (byte b : abyte0) {
				packetData.append(String.format("%02X ", b));
			}
			_log.info(packetData.toString());
			
			L1PcInstance pc = client.getActiveChar();
			if (pc == null) {
				_log.warn("C_Restart received but PC is null");
				return;
			}
			
			// Enhanced logging of player state
			_log.info("==== RESTART REQUEST RECEIVED ====");
			_log.info("Player: " + pc.getName() + " (ID:" + pc.getId() + ")");
			_log.info("Current state - isDead: " + pc.isDead() + ", HP: " + pc.getCurrentHp() + ", MP: " + pc.getCurrentMp());
			_log.info("Location: Map " + pc.getMapId() + " (" + pc.getX() + "," + pc.getY() + ")");
			_log.info("Client IP: " + client.getIp() + ", Account: " + pc.getAccountName());
			_log.info("Current Time: " + System.currentTimeMillis() + " ms since epoch");
			
			// Check if player can restart
			if (!pc.isDead()) {
				_log.warn("Player " + pc.getName() + " attempted to restart but was not dead!");
				return;
			}
			
			// Check for aggressive actions
			long lastAggressiveAct = pc.getLastAggressiveAct();
			long delayAmount = Config.NON_AGGRO_LOGOUT_TIMER - (System.currentTimeMillis() - lastAggressiveAct);
			if (delayAmount > 0 && !pc.isDead()) {
				_log.info("Player " + pc.getName() + " has aggressive timer active: " + delayAmount + "ms remaining");
				pc.sendPackets(new S_PacketBox(S_PacketBox.MSG_CANT_LOGOUT));
				return;
			}
			
			// Force save character data before restart to prevent loss of hotkeys and inventory
			_log.info("Step 0: Saving character data for " + pc.getName() + " before restart");
			try {
				pc.saveInventory();
			} catch (Exception e) {
				_log.error("Error saving character data before restart", e);
			}
			
			// Log respawn location calculation
			_log.info("Calculating respawn location for " + pc.getName());
			
			// Define respawn location
			int[] loc;
			if (pc.getHellTime() > 0) {
				_log.info("Player " + pc.getName() + " has hell time: " + pc.getHellTime());
				loc = new int[3];
				loc[0] = 32701;
				loc[1] = 32777;
				loc[2] = 666;
			} else {
				loc = Getback.GetBack_Location(pc, true);
				if (loc == null || loc.length != 3) {
					_log.error("Getback location error for player " + pc.getName() + ", using default location");
					loc = new int[3];
					loc[0] = 33089;
					loc[1] = 33397;
					loc[2] = 4;
				}
			}
			
			_log.info("Respawning player " + pc.getName() + " at location: " + loc[0] + "," + loc[1] + "," + loc[2]);
			
			// Log the restart process sequence
			_log.info("Step 1: Cleaning up player objects for " + pc.getName());
			
			// Basic cleanup before respawn
			pc.removeAllKnownObjects();
			pc.broadcastPacket(new S_RemoveObject(pc));
			
			_log.info("Step 2: Resetting character state for " + pc.getName());
			
			// Reset character state
			pc.setCurrentHp(pc.getLevel());
			pc.setCurrentMp(pc.getMaxMp() / 3);
			pc.set_food(40);
			pc.setDead(false);
			pc.stopPcDeleteTimer();
			pc.setStatus(0);
			
			_log.info("Step 3: Moving player " + pc.getName() + " to respawn location");
			
			// Move to respawn location
			L1World.getInstance().moveVisibleObject(pc, loc[2]);
			pc.setLocation(loc[0], loc[1], (short)loc[2]);
			
			_log.info("Step 4: Sending update packets to client for " + pc.getName());
			
			// Send packets in the right order for client update
			pc.sendPackets(new S_MapID(pc.getMapId(), pc.getMap().isUnderwater()));
			_log.info("Sent: S_MapID - Map " + pc.getMapId());
			
			pc.sendPackets(new S_OwnCharPack(pc));
			_log.info("Sent: S_OwnCharPack");
			
			pc.sendPackets(new S_HPUpdate(pc.getCurrentHp(), pc.getMaxHp()));
			_log.info("Sent: S_HPUpdate - HP " + pc.getCurrentHp() + "/" + pc.getMaxHp());
			
			pc.sendPackets(new S_MPUpdate(pc.getCurrentMp(), pc.getMaxMp()));
			_log.info("Sent: S_MPUpdate - MP " + pc.getCurrentMp() + "/" + pc.getMaxMp());
			
			pc.sendPackets(new S_CharVisualUpdate(pc));
			_log.info("Sent: S_CharVisualUpdate");
			
			// Make player visible to others
			pc.broadcastPacket(new S_OtherCharPacks(pc));
			_log.info("Broadcast: S_OtherCharPacks");
			
			_log.info("Step 5: Starting regeneration timers for " + pc.getName());

			CharBuffTable.DeleteBuff(pc);
			CharBuffTable.SaveBuff(pc);
			// Start regeneration timers
			pc.startHpRegeneration();
			pc.startMpRegeneration();
			
			// Other environmental packets
			pc.sendPackets(new S_Weather(L1World.getInstance().getWeather()));
			_log.info("Sent: S_Weather - Weather " + L1World.getInstance().getWeather());
			
			// Apply hell time if needed
			if (pc.getHellTime() > 0) {
				_log.info("Applying hell time for " + pc.getName() + ": " + pc.getHellTime());
				pc.beginHell(false);
			}
			
			// Handle n00b area HP/MP boost
			if ((pc.getMapId() == 68 || pc.getMapId() == 69) && (pc.getCurrentHp() <= (pc.getMaxHp() / 2))) {
				_log.info("Applying n00b area HP/MP boost for " + pc.getName());
				pc.setCurrentHp(pc.getMaxHp());
				pc.setCurrentMp(pc.getMaxMp());
				pc.sendPackets(new S_ServerMessage(77));
				pc.sendPackets(new S_SkillSound(pc.getId(), 830));
				pc.sendPackets(new S_HPUpdate(pc.getCurrentHp(), pc.getMaxHp()));
				pc.sendPackets(new S_MPUpdate(pc.getCurrentMp(), pc.getMaxMp()));
			}
			
			_log.info("==== RESTART COMPLETED SUCCESSFULLY ====");
			_log.info("Player " + pc.getName() + " respawned at " + pc.getX() + "," + pc.getY() + "," + pc.getMapId());
			
			// Force save character data after restart to ensure new state is properly saved
			_log.info("Final Step: Saving character data after restart");
			try {
				pc.save();
			} catch (Exception e) {
				_log.error("Error saving character data after restart", e);
			}
			
		} catch (Exception e) {
			_log.error("Error in C_Restart", e);
		}
	}

	@Override
	public String getType() {
		return C_RESTART;
	}
}