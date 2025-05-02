/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package l1j.server.server.model;

import l1j.server.server.model.L1Character;

/**
 * Interface for handling character death events.
 * 
 * NOTE: This interface is not currently used in the codebase.
 * Slime death tracking is handled directly in L1MonsterInstance and L1GroundInventory.
 * This interface is kept for potential future implementation.
 */
public interface L1DeathListener {
    /**
     * Called when a character dies
     * 
     * @param character The character that died
     */
    public void onDeath(L1Character character);
} 