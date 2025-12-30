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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import l1j.server.server.encryptions.Opcodes;

// Referenced classes of package l1j.server.server.serverpackets:
// ServerBasePacket
public class S_Emblem extends ServerBasePacket {
	private static final String S_EMBLEM = "[S] S_Emblem";
	private byte[] _byte = null;

	public S_Emblem(int clanid) {
		BufferedInputStream bis = null;
		try {
			String emblem_file = String.valueOf(clanid);
			File file = new File("emblem/" + emblem_file);
			if (file.exists()) {
				int data = 0;
				bis = new BufferedInputStream(new FileInputStream(file));
				writeC(Opcodes.S_OPCODE_EMBLEM);
				writeD(clanid);
				while ((data = bis.read()) != -1) {
					writeP(data);
				}
				writeC(0x00);
				writeH(0x0000);
			} else {
				_byte = null; // Mark as invalid if emblem file doesn't exist
			}
		} catch (Exception e) {
			_byte = null; // Mark as invalid on read error
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException ignore) {
					// ignore
				}
			}
		}
	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = getBytes();
		}
		return _byte != null ? _byte : new byte[0];
	}


	@Override
	public String getType() {
		return S_EMBLEM;
	}
}
