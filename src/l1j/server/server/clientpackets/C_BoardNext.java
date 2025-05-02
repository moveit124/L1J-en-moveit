package l1j.server.server.clientpackets;

import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1BoardInstance;
import l1j.server.server.network.Client;
import l1j.server.server.serverpackets.S_ServerMessage;

// Handles the action for moving to the next board page
public class C_BoardNext extends ClientBasePacket {

    private static final String C_BOARD_NEXT = "[C] C_BoardNext";

    public C_BoardNext(byte abyte0[], Client client) {
        super(abyte0);
        int objId = readD();
        int lastSeenId = readD();

        L1Object obj = L1World.getInstance().findObject(objId);
        if (obj instanceof L1BoardInstance) {
            L1BoardInstance board = (L1BoardInstance) obj;

            if (lastSeenId == 0) {  
                client.getActiveChar().sendPackets(new S_ServerMessage(1234)); // "No more messages"
                return;
            }

            client.getActiveChar().setLastBugBoardPage(lastSeenId);

            board.onNextPage(client.getActiveChar(), lastSeenId);
        }
    }

    @Override
    public String getType() {
        return C_BOARD_NEXT;
    }
} 