package l1j.server.server.command.executor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.utils.SQLUtil;

public class BotCheck implements L1CommandExecutor {

    @Override
    public void execute(L1PcInstance gm, String commandName, String arg) {
        try {
            String[] args = arg.split(" ");
            String target = args[0];

            L1PcInstance player = l1j.server.server.model.L1World.getInstance().getPlayer(target);
            if (player == null) {
                gm.sendPackets(new S_SystemMessage("Player not found."));
                return;
            }

            int charId = player.getId();
            gm.sendPackets(new S_SystemMessage("[BotCheck] " + player.getName()));
            queryPotionStats(gm, charId);

        } catch (Exception e) {
            gm.sendPackets(new S_SystemMessage(".botcheck <name>"));
        }
    }
    public static L1CommandExecutor getInstance() {
        return new BotCheck();
    }

    private void queryPotionStats(L1PcInstance gm, int charId) {
        Map<Integer, Integer> data = new HashMap<>();
        int total = 0;

        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = L1DatabaseFactory.getInstance().getConnection();
            pstm = con.prepareStatement(
                "SELECT hp_bucket, SUM(usage_count) AS total " +
                "FROM botcheck_buckets " +
                "WHERE usage_type = 'POTION' AND session_id IN (" +
                "  SELECT session_id FROM botcheck_sessions WHERE char_id = ?" +
                ") GROUP BY hp_bucket"
            );
            pstm.setInt(1, charId);
            rs = pstm.executeQuery();

            while (rs.next()) {
                int bucket = rs.getInt("hp_bucket");
                int count = rs.getInt("total");
                data.put(bucket, count);
                total += count;
            }
        } catch (Exception e) {
            gm.sendPackets(new S_SystemMessage("[BotCheck] Error querying database."));
            return;
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(pstm);
            SQLUtil.close(con);
        }

        if (total < 50) {
            gm.sendPackets(new S_SystemMessage("Potions: " + total));
            gm.sendPackets(new S_SystemMessage("Insufficient Data (" + total + " samples)"));
            return;
        }


        double mean = data.entrySet().stream().mapToDouble(e -> e.getKey() * e.getValue()).sum() / total;
        double variance = data.entrySet().stream().mapToDouble(e -> Math.pow(e.getKey() - mean, 2) * e.getValue()).sum() / total;
        double stdDev = Math.sqrt(variance);

        int peakBucket = 0, peakValue = 0, min = 100, max = 0;
        for (Map.Entry<Integer, Integer> e : data.entrySet()) {
            int bucket = e.getKey();
            int count = e.getValue();
            if (count > peakValue) {
                peakValue = count;
                peakBucket = bucket;
            }
            if (bucket < min) min = bucket;
            if (bucket > max) max = bucket;
        }

        double peakRatio = (double) peakValue / total;
        String label;
        if (peakRatio > 0.50 && stdDev < 8) {
            label = "Likely Bot (" + total + " samples)";
        } else if (peakRatio > 0.40 && stdDev < 10) {
            label = "Suspicious Usage (" + total + " samples)";
        } else {
            label = "Normal Usage (" + total + " samples)";
        }

        gm.sendPackets(new S_SystemMessage("Potions: " + total));
        gm.sendPackets(new S_SystemMessage("Most Used HP: " + peakBucket + "%  (Peak " + (int)(peakRatio * 100) + "%)"));
        gm.sendPackets(new S_SystemMessage("Variance: ~" + String.format("%.1f", stdDev) + "%"));
        gm.sendPackets(new S_SystemMessage("Common HP Range: " + min + "% - " + max + "%"));
        gm.sendPackets(new S_SystemMessage(label));
    }
    
    
}
