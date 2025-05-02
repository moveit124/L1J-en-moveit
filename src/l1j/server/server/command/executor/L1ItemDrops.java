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
package l1j.server.server.command.executor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList; // Import ArrayList
import java.util.List;      // Import List

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.datatables.DropTable;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Item;
import l1j.server.server.utils.SQLUtil;

public class L1ItemDrops implements L1CommandExecutor {

	private L1ItemDrops() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1ItemDrops();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		int dropID = 0;
		try {
			dropID = Integer.parseInt(arg);
		} catch (NumberFormatException e) {
		    System.out.println("L1ItemDrops: NumberFormatException for arg='" + arg + "'"); // Console output
		    e.printStackTrace(); // Print error details to console
		    pc.sendPackets(new S_SystemMessage("DEBUG: NumberFormatException caught")); // In-game message
		    pc.sendPackets(new S_SystemMessage(".item itemID"));
            return; // Exit if parsing fails
		}
		if (dropID == 40308) {
			pc.sendPackets(new S_SystemMessage(
					"Adena(40308) drops from almost everything"));
		} else {
			Connection con = null;
			PreparedStatement pstm = null;
			ResultSet rs = null;

            // --- Use Lists instead of arrays initially ---
            List<Integer> mobIdList = new ArrayList<>();
            List<Integer> minList = new ArrayList<>();
            List<Integer> maxList = new ArrayList<>();
            List<Double> chanceList = new ArrayList<>();
            // No need for name list here, fetch name inside the final loop

			try {
				L1Item item = ItemTable.getInstance().getTemplate(dropID);
                // --- Check if item exists ---
                if (item == null) {
                    pc.sendPackets(new S_SystemMessage("Item not found: " + dropID));
                    return; 
                }

				String blessed;
				if (item.getBless() == 1) {
					blessed = "";
				} else if (item.getBless() == 0) {
					blessed = "\\fR";
				} else {
					blessed = "\\fY";
				}
				
				con = L1DatabaseFactory.getInstance().getConnection();
				
                // --- Block 1: Fetch droplist rows and populate Lists ---
				pstm = con.prepareStatement("SELECT mobId, min, max, chance FROM droplist WHERE itemId=?");
				pstm.setInt(1, dropID);
				rs = pstm.executeQuery();

                // --- Iterate through results and add to lists ---
				while (rs.next()) {
                    mobIdList.add(rs.getInt("mobId"));
                    minList.add(rs.getInt("min"));
                    maxList.add(rs.getInt("max"));

					int baseChance = rs.getInt("chance");
					double adjusted = DropTable.adjustChance(baseChance, Config.RATE_DROP_ITEMS);
                    // Calculate final percentage chance
					double finalChancePercent = (adjusted * Config.RATE_DROP_ITEMS) / 10000.0; 
                    chanceList.add(finalChancePercent);
				}
                // --- Close first ResultSet and PreparedStatement ---
                SQLUtil.close(rs); 
                SQLUtil.close(pstm);

                // --- Check if any drops were found ---
                if (mobIdList.isEmpty()) {
                     pc.sendPackets(new S_SystemMessage(blessed + item.getName() + "(" + dropID + ") does not drop from any known mob."));
                     return; // Exit if no drops found
                }

                // --- Block 2: Send Item Name Message ---
				pc.sendPackets(new S_SystemMessage(blessed + item.getName()
						+ "(" + dropID + ") drops from:"));

                // --- Block 3: Loop through collected list data, fetch names, and send details ---
				for (int j = 0; j < mobIdList.size(); j++) {
                    int mobId = mobIdList.get(j);
                    String npcName = "Unknown NPC [" + mobId + "]"; // Default name includes ID

                    // Prepare and execute query to get NPC name
					pstm = con.prepareStatement("SELECT name FROM npc WHERE npcid=?");
					pstm.setInt(1, mobId);
					rs = pstm.executeQuery();
                    // Check if a result was found
					if (rs.next()) { 
						npcName = rs.getString("name");
					}
                    // --- Close inner ResultSet and PreparedStatement ---
					SQLUtil.close(rs);
					SQLUtil.close(pstm);
                    
                    // Send the details message for this mob
					double chance = chanceList.get(j);
					String formattedPercent = String.format("%.1f%%", chance);
					String oneInX = (chance > 0) ? String.format(" (1/%.0f)", 100.0 / chance) : "";
					pc.sendPackets(new S_SystemMessage(
					    minList.get(j) + "-" + maxList.get(j) + " " +
					    mobId + " " + npcName + " " +
					    formattedPercent + oneInX
					));

				}
			} catch (Exception e) {
			    System.out.println("L1ItemDrops: Exception during processing for dropID=" + dropID); // Console output
			    e.printStackTrace(); // Print error details to console
			    pc.sendPackets(new S_SystemMessage("DEBUG: General Exception caught: " + e.getMessage())); // In-game message
			    pc.sendPackets(new S_SystemMessage(".item itemID")); 
			} finally {
                // --- Ensure all resources are closed ---
				SQLUtil.close(rs);   // Safe to close again, handles null
				SQLUtil.close(pstm); // Safe to close again, handles null
				SQLUtil.close(con);  // Close connection
			}
		}
	}
}