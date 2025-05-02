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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.encryptions.Opcodes;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.templates.L1BookMark;
import l1j.server.server.utils.SQLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class S_BookmarkLoad extends ServerBasePacket {
    private static final String S_BookMarkLoad = "[S] S_BookmarkLoad";
    private byte[] _byte = null;

    private static Logger _log = LoggerFactory.getLogger(S_BookmarkLoad.class.getName());
    private final int maxBookmarks = 100; //TODO -- pull from config.. but verify a certain # won't crash the client first
    	public S_BookmarkLoad(L1PcInstance pc) {
    	    Connection con = null;
    	    PreparedStatement pstm = null;
    	    ResultSet rs = null;

    	    try {
    	        // Clear existing bookmarks to avoid duplicates
    	        pc.getBookMarks().clear();

    	        // Temporary list to hold bookmarks before sorting
    	        List<L1BookMark> bookmarks = new ArrayList<>();

    	        // Map to store bookmark index positions for quicklist
    	        Map<Integer, Integer> bookmarkIdToIndex = new HashMap<>();
    	        List<Integer> quicklistIndexes = new ArrayList<>();

    	        con = L1DatabaseFactory.getInstance().getConnection();
    	        pstm = con.prepareStatement("SELECT * FROM character_teleport WHERE char_id=?");
    	        pstm.setInt(1, pc.getId());
    	        rs = pstm.executeQuery();

    	        while (rs.next()) {
    	            L1BookMark bookmark = new L1BookMark();
    	            bookmark.setId(rs.getInt("id"));
    	            bookmark.setCharId(rs.getInt("char_id"));
    	            bookmark.setName(rs.getString("name"));
    	            bookmark.setLocX(rs.getInt("locx"));
    	            bookmark.setLocY(rs.getInt("locy"));
    	            bookmark.setMapId(rs.getShort("mapid"));
    	            bookmark.setQuick(rs.getBoolean("quicklist"));

    	            // Add bookmark to list before sorting
    	            bookmarks.add(bookmark);
    	        }

    	        // Sort bookmarks manually by "cleaned name"
    	        bookmarks.sort(Comparator.comparing(bookmark -> cleanName(bookmark.getName())));

    	        // Store sorted bookmarks back to the player
    	        int index = 0;
    	        for (L1BookMark bookmark : bookmarks) {
    	            pc.addBookMark(bookmark);
    	            
    	            // Store the index position for this bookmark ID
    	            bookmarkIdToIndex.put(bookmark.getId(), index);
    	            
    	            // If it's a quicklist bookmark, store its index
    	            if (bookmark.isQuick()) {
    	                quicklistIndexes.add(index);
    	            }

    	            index++;
    	        }

    	        int count = pc.getBookMarkSize();
    	        writeC(Opcodes.S_OPCODE_CHARRESET);
    	        writeC(42); // type
    	        writeC(maxBookmarks);
    	        writeC(0x00); 
    	        writeC(0x02);

    	        // Write all bookmark indexes
    	        for (int i = 0; i < count; i++) {
    	            writeC(i);
    	        }

    	        // Write quicklist indexes
    	        for (int quickIndex : quicklistIndexes) {
    	            writeC(quickIndex);
    	        }

    	        // Fill remaining slots with 0xFF
    	        int blanks = maxBookmarks - 1 - count - quicklistIndexes.size();
    	        for (int i = 0; i < blanks; i++) {
    	            writeC(0xff);
    	        }

    	        writeC(maxBookmarks);
    	        writeC(0);
    	        writeH(count);

    	        // Write actual bookmark data
    	        for (L1BookMark bm : pc.getBookMarks()) {
    	            writeD(bm.getId());
    	            writeS(bm.getName());
    	            writeH(bm.getMapId());
    	            writeH(bm.getLocX());
    	            writeH(bm.getLocY());
    	        }

    	        // Debug log
    	        _log.debug("Loaded " + count + " bookmarks for player " + pc.getName() + 
    	                " with " + quicklistIndexes.size() + " quicklist items");

    	    } catch (SQLException e) {
    	        _log.error("Error loading bookmarks: " + e.getLocalizedMessage(), e);
    	    } finally {
    	        SQLUtil.close(rs, pstm, con);
    	    }
    	}

    	// ** Helper method to clean names like REGEXP_REPLACE() **
    	private String cleanName(String name) {
    	    return name.replaceAll("\\\\[a-zA-Z0-9]{2}", ""); // Removes \f0, \d0, etc.
    	}

    @Override
    public byte[] getContent() {
        if (_byte == null) {
            _byte = getBytes();
        }
        return _byte;
    }

    public String getType() {
        return S_BookMarkLoad;
    }
}
