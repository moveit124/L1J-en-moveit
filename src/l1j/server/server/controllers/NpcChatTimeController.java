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
package l1j.server.server.controllers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.Config;
import l1j.server.server.datatables.NpcChatTable;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.templates.L1NpcChat;

public class NpcChatTimeController implements Runnable {
	private static Logger _log = LoggerFactory.getLogger(NpcChatTimeController.class
			.getName());
	private static NpcChatTimeController _instance;

	public static NpcChatTimeController getInstance() {
		if (_instance == null) {
			_instance = new NpcChatTimeController();
		}
		return _instance;
	}

	@Override
	public void run() {
		Thread.currentThread().setName("NpcChatTimeController");
		try {
			while (true) {
				checkNpcChatTime();
				Thread.sleep(60000);
			}
		} catch (InterruptedException e) {
			_log.warn("NpcChatTimeController interrupted", e);
			Thread.currentThread().interrupt();
		} catch (Exception e1) {
			_log.error("Error in NpcChatTimeController loop", e1);
		}
	}

	private void checkNpcChatTime() {
		try {
			// Delay execution by 15 seconds to avoid colliding with other tasks at the top of the minute
			Thread.sleep(15000); 

			Calendar nowCal = getRealTime();
			SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
			int nowTime = Integer.parseInt(sdf.format(nowCal.getTime()));

			L1NpcChat[] allGameTimeChats = NpcChatTable.getInstance().getAllGameTime();
			if (allGameTimeChats == null || allGameTimeChats.length == 0) {
				return;
			}

			Map<Integer, List<L1NpcChat>> chatsForNowByNpcId = null;

			for (L1NpcChat chatConfig : allGameTimeChats) {
				if (chatConfig.getGameTime() == nowTime) {
					if (chatsForNowByNpcId == null) {
						chatsForNowByNpcId = new HashMap<>();
					}
					chatsForNowByNpcId.computeIfAbsent(chatConfig.getNpcId(), k -> new ArrayList<>()).add(chatConfig);
				}
			}

			if (chatsForNowByNpcId == null || chatsForNowByNpcId.isEmpty()) {
				return;
			}

			Collection<l1j.server.server.model.L1Object> allObjects = L1World.getInstance().getObject();
			if (allObjects == null) {
				_log.warn("L1World.getInstance().getObject() returned null.");
				return;
			}

			for (l1j.server.server.model.L1Object obj : allObjects) {
				if (!(obj instanceof L1NpcInstance)) {
					continue;
				}
				L1NpcInstance npc = (L1NpcInstance) obj;
				if (npc == null || npc.getNpcTemplate() == null) continue;

				int npcId = npc.getNpcTemplate().get_npcId();

				if (chatsForNowByNpcId.containsKey(npcId)) {
					try {
						npc.startChat(L1NpcInstance.CHAT_TIMING_GAME_TIME);
					} catch (Exception e) {
						_log.error("Error calling startChat for NPC ID: " + npcId, e);
					}
				}
			}
		} catch (Exception e) {
			_log.error("Error in checkNpcChatTime method", e);
		}
	}

	private static Calendar getRealTime() {
		TimeZone tz = TimeZone.getTimeZone(Config.TIME_ZONE);
		Calendar cal = Calendar.getInstance(tz);
		return cal;
	}
}