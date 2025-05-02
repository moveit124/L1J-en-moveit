package l1j.server.server.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.server.datatables.NpcActionTable;
import l1j.server.server.datatables.NPCTalkDataTable;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1QuestInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.model.Instance.L1TeleporterInstance;
import l1j.server.server.model.Instance.L1FollowerInstance;
import l1j.server.server.model.Instance.L1GuardInstance;
import l1j.server.server.model.Instance.L1GuardianInstance;
import l1j.server.server.model.Instance.L1HousekeeperInstance;
import l1j.server.server.model.Instance.L1MerchantInstance;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.npc.L1NpcHtml;
import l1j.server.server.model.npc.action.L1NpcAction;
import l1j.server.server.model.L1NpcTalkData;
import l1j.server.server.network.Client;
import l1j.server.server.serverpackets.S_NPCTalkReturn;

public class C_NPCTalk extends ClientBasePacket {

    private static final String C_NPC_TALK = "[C] C_NPCTalk";
    private static Logger _log = LoggerFactory.getLogger(C_NPCTalk.class.getName());

    public C_NPCTalk(byte abyte0[], Client client) throws Exception {
        super(abyte0);
        int objid = readD();

        L1Object obj = L1World.getInstance().findObject(objid);
        L1PcInstance pc = client.getActiveChar();

        if (obj != null && pc != null) {
            if (obj instanceof L1NpcInstance) {
                L1NpcInstance npc = (L1NpcInstance) obj;
                L1NpcAction action = NpcActionTable.getInstance().get(pc, obj);
                if (action != null) {
                    String actionName = "";
                    L1NpcTalkData talkData = NPCTalkDataTable.getInstance().getTemplate(npc.getNpcTemplate().get_npcId());
                    // Use normalAction if it exists, otherwise proceed with custom handling
                    if (talkData != null && talkData.getNormalAction() != null && !talkData.getNormalAction().isEmpty()) {
                        actionName = talkData.getNormalAction();
                    }

                    L1NpcHtml html = action.execute(actionName, pc, obj, new byte[0]);
                    if (html != null) {
                        pc.sendPackets(new S_NPCTalkReturn(obj.getId(), html));
                    }
                    return;
                }
                // **NEW: Apply normal action logic ONLY for non-merchant NPCs**
        		if (!(npc instanceof L1MerchantInstance) &&
        			    !(npc instanceof L1TeleporterInstance) &&
        			    !(npc instanceof L1GuardInstance) &&
        			    !(npc instanceof L1GuardianInstance) &&
        			    !(npc instanceof L1MonsterInstance) &&
        			    !(npc instanceof L1PetInstance) &&
        			    !(npc instanceof L1SummonInstance) &&
        			    !(npc instanceof L1FollowerInstance) &&
        			    !(npc instanceof L1QuestInstance) &&
        			    !(npc instanceof L1HousekeeperInstance)) {
                    L1NpcTalkData talkData = NPCTalkDataTable.getInstance().getTemplate(npc.getNpcTemplate().get_npcId());
                    if (talkData != null && talkData.getNormalAction() != null && !talkData.getNormalAction().isEmpty()) {
                        L1NpcHtml html = new L1NpcHtml(talkData.getNormalAction());
                        pc.sendPackets(new S_NPCTalkReturn(obj.getId(), html));
                        return;
                    }
                }
            }
            // Proceed to custom handling if NPC action is not handled by the table
            obj.onTalkAction(pc);
        }
    }

    @Override
    public String getType() {
        return C_NPC_TALK;
    }
}