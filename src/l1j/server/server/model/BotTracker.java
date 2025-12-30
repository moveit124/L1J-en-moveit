package l1j.server.server.model;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class BotTracker {
    private final int charId;
    private final long sessionStart;
    private final Map<Integer, Integer> potionBuckets = new HashMap<>();
    private final Map<Integer, Integer> escapeBuckets = new HashMap<>();

    public BotTracker(int charId) {
        this.charId = charId;
        this.sessionStart = System.currentTimeMillis();
    }

    public void recordPotion(int hpPercent) {
        int bucket = Math.min(95, (hpPercent / 5) * 5);
        potionBuckets.merge(bucket, 1, Integer::sum);
    }

    public void recordEscape(int hpPercent) {
        int bucket = Math.min(95, (hpPercent / 5) * 5);
        escapeBuckets.merge(bucket, 1, Integer::sum);
    }

    public void flushToDatabase(Connection conn) {
        long sessionEnd = System.currentTimeMillis();
        int potScore = calculateScore(potionBuckets);
        int escScore = calculateScore(escapeBuckets);

        try {
            // Insert into botcheck_sessions and get session_id
            String insertSession = "INSERT INTO botcheck_sessions (char_id, session_start, session_end, pot_score, esc_score) " +
                                   "VALUES (?, FROM_UNIXTIME(? / 1000), FROM_UNIXTIME(? / 1000), ?, ?)";
            int sessionId;
            try (PreparedStatement ps = conn.prepareStatement(insertSession, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, charId);
                ps.setLong(2, sessionStart);
                ps.setLong(3, sessionEnd);
                ps.setInt(4, potScore);
                ps.setInt(5, escScore);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) return;
                    sessionId = rs.getInt(1);
                }
            }

            // Insert each bucket row
            String insertBucket = "INSERT INTO botcheck_buckets (session_id, hp_bucket, usage_type, usage_count) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertBucket)) {
                for (Map.Entry<Integer, Integer> e : potionBuckets.entrySet()) {
                    ps.setInt(1, sessionId);
                    ps.setInt(2, e.getKey());
                    ps.setString(3, "POTION");
                    ps.setInt(4, e.getValue());
                    ps.addBatch();
                }
                for (Map.Entry<Integer, Integer> e : escapeBuckets.entrySet()) {
                    ps.setInt(1, sessionId);
                    ps.setInt(2, e.getKey());
                    ps.setString(3, "ESCAPE");
                    ps.setInt(4, e.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            System.out.println("Flushing to DB. Pot score: " + potScore + ", Esc score: " + escScore);

        } catch (SQLException e) {
            e.printStackTrace(); // Or log to your game logger
        } finally {
            potionBuckets.clear();
            escapeBuckets.clear();
        }
    }

    private int calculateScore(Map<Integer, Integer> data) {
        int total = data.values().stream().mapToInt(i -> i).sum();
        if (total == 0) return 0;

        double mean = data.entrySet().stream().mapToDouble(e -> e.getKey() * e.getValue()).sum() / total;
        double variance = data.entrySet().stream()
            .mapToDouble(e -> Math.pow(e.getKey() - mean, 2) * e.getValue())
            .sum() / total;
        double stdDev = Math.sqrt(variance);

        int peakValue = data.values().stream().max(Integer::compare).orElse(0);
        double peakRatio = peakValue / (double) total;

        int score = (int) ((peakRatio * 100) - (stdDev / 2));
        return Math.max(score, 0);
    }
}
