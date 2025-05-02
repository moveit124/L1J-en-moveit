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
package l1j.server.server.serverpackets;

import l1j.server.server.model.Instance.L1PcInstance;

// Referenced classes of package l1j.server.server.serverpackets:
// ServerBasePacket
public class S_ChatPacket extends ServerBasePacket {

	private static final String _S__1F_NORMALCHATPACK = "[S] S_ChatPacket";
	private byte[] _byte = null;

	public S_ChatPacket(L1PcInstance pc, String chat, int opcode, int type) {
		String name = pc.getName();
		boolean isSpoofing = pc.getSpoofName() != null;
		
		if(pc.isGm() && isSpoofing) {
			name = pc.getSpoofName();
		}
		
		if (type == 0) {// normal chat
			writeC(opcode);
			writeC(type);
			if (pc.isInvisble()) {
				writeD(0);
			} else {
				writeD(pc.getId());
			}
			writeS(name + ": " + chat);
		} else if (type == 2) {// Shouting
			writeC(opcode);
			writeC(type);
			if (pc.isInvisble()) {
				writeD(0);
			} else {
				writeD(pc.getId());
			}
			writeS("<" + name + "> " + chat);
			writeH(pc.getX());
			writeH(pc.getY());
		} else if (type == 3) { // global chat
		    writeC(opcode);
		    writeC(type);
		    String colorPrefix = "\\fR"; // Purple

		    String prefix;
		    if (!isSpoofing && pc.getAccessLevel().getChatPrefix() != null && 
		        !pc.getAccessLevel().getChatPrefix().equals("")) {
		        prefix = "[" + pc.getAccessLevel().getChatPrefix() + ":" + name + "] ";
		    } else if ("moveit124".equals(pc.getAccountName()) || "zigmund".equals(pc.getAccountName())) {
		        prefix = "[" + name + "] ";
		    } else {
		        prefix = "[" + name + "] ";
		        colorPrefix = ""; // ❌ no color for non-GMs
		    }

		    int maxLineLength = 56;
		    int lineLength = prefix.length();

		    StringBuilder processedChat = new StringBuilder();
		    for (String word : chat.split(" ")) {
		        if (lineLength + word.length() + 1 > maxLineLength) {
		            if (!colorPrefix.isEmpty()) {
		                processedChat.append(colorPrefix);
		            }
		            lineLength = prefix.length();
		        }
		        processedChat.append(word).append(" ");
		        lineLength += word.length() + 1;
		    }

		    writeS((colorPrefix.isEmpty() ? "" : colorPrefix) + prefix + processedChat.toString().trim());
		} else if (type == 4) {// Clan chat
			writeC(opcode);
			writeC(type);
			writeS("{" + name + "} " + chat);
		} else if (type == 9) {//
			writeC(opcode);
			writeC(type);
			writeS("-> (" + name + ") " + chat);
		} else if (type == 11) {// Party chat
			writeC(opcode);
			writeC(type);
			writeS("(" + name + ") " + chat);
		} else if (type == 12) { // Union chat
			writeC(opcode);
			writeC(type);
			writeS("[" + name + "] " + chat);
		} else if (type == 13) { // alliance chat
			writeC(opcode);
			writeC(0x04);
			writeS("{{" + name + "}} " + chat);
		} else if (type == 14) { //
			writeC(opcode);
			writeC(type);
			if (pc.isInvisble()) {
				writeD(0);
			} else {
				writeD(pc.getId());
			}
			writeS("(" + name + ") " + chat);
		} else if (type == 16) { //
			writeC(opcode);
			writeS(pc.getName());
			writeS(chat);
		}
	}

	@Override
	public byte[] getContent() {
		if (null == _byte) {
			_byte = _bao.toByteArray();
		}
		return _byte;
	}

	@Override
	public String getType() {
		return _S__1F_NORMALCHATPACK;
	}

}
