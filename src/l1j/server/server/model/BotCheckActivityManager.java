package l1j.server.server.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.command.executor.BotCheckManager;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_CloseList;
import l1j.server.server.serverpackets.S_RawStringDialog;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.utils.SQLUtil;

public class BotCheckActivityManager {

    public static void load(L1PcInstance pc) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = L1DatabaseFactory.getInstance().getConnection();
            ps = con.prepareStatement("SELECT account_name FROM bot_check_activity WHERE account_name = ?");
            ps.setString(1, pc.getAccountName());
            rs = ps.executeQuery();

            if (!rs.next()) { // No record exists
                SQLUtil.close(ps);
                ps = con.prepareStatement("INSERT INTO bot_check_activity (account_name) VALUES (?)");
                ps.setString(1, pc.getAccountName());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(ps);
            SQLUtil.close(con);
        }
    }

    private static void updateField(L1PcInstance pc, String field, int amount, boolean allowNegative) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = L1DatabaseFactory.getInstance().getConnection();

            // Step 1: Check if account exists
            ps = con.prepareStatement("SELECT 1 FROM bot_check_activity WHERE account_name = ?");
            ps.setString(1, pc.getAccountName());
            rs = ps.executeQuery();

            boolean exists = false;
            if (rs.next()) {
                exists = true;
            }
            rs.close();
            ps.close();

            if (!exists) {
                // Insert new row if missing
                ps = con.prepareStatement(
                    "INSERT INTO bot_check_activity (account_name, haunted_house_entries, poly_race_entries, dog_fight_entries, monster_kills, successful_checks, failed_checks_in_a_row) " +
                    "VALUES (?, 0, 0, 0, 0, 0, 0)"
                );
                ps.setString(1, pc.getAccountName());
                ps.executeUpdate();
                ps.close();
            }

            // Step 2: Now update
            String operation = allowNegative ?
                field + " = " + field + " + ?" :
                field + " = GREATEST(" + field + " + ?, 0)";

            ps = con.prepareStatement("UPDATE bot_check_activity SET " + operation + " WHERE account_name = ?");
            ps.setInt(1, amount);
            ps.setString(2, pc.getAccountName());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(ps);
            SQLUtil.close(con);
        }
    }


    private static void setField(L1PcInstance pc, String field, int value) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = L1DatabaseFactory.getInstance().getConnection();
            ps = con.prepareStatement("UPDATE bot_check_activity SET " + field + " = ? WHERE account_name = ?");
            ps.setInt(1, value);
            ps.setString(2, pc.getAccountName());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLUtil.close(ps);
            SQLUtil.close(con);
        }
    }

    public static void incrementHauntedHouse(L1PcInstance pc) {
        updateField(pc, "haunted_house_entries", 1, true);
    }

    public static void incrementPolyRace(L1PcInstance pc) {
        updateField(pc, "poly_race_entries", 1, true);
    }

    public static void incrementDogFight(L1PcInstance pc) {
        updateField(pc, "dog_fight_entries", 1, true);
    }
    public static void flushMonsterKills(L1PcInstance pc) {
        int current = pc.getRawMonsterKills();
        int last = pc.getLastFlushedMonsterKills();
        int delta = current - last;

        if (delta <= 0) return;

        addKills(pc, delta);
        pc.setLastFlushedMonsterKills(current);
    }




    private static void addKills(L1PcInstance pc, int amount) {
        updateField(pc, "monster_kills", amount, true); // true = allow negative (though should never happen here)
    }

    public static void handleBotCheckSuccess(L1PcInstance pc) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = L1DatabaseFactory.getInstance().getConnection();

            // Step 1: Read current successful_checks
            ps = con.prepareStatement("SELECT successful_checks FROM bot_check_activity WHERE account_name = ?");
            ps.setString(1, pc.getAccountName());
            rs = ps.executeQuery();

            int currentSuccess = 0;
            boolean accountExists = false;
            if (rs.next()) {
                accountExists = true;
                currentSuccess = rs.getInt("successful_checks");
            }

            rs.close();
            ps.close();

            if (!accountExists) {
                // Insert new row if it doesn't exist
                ps = con.prepareStatement("INSERT INTO bot_check_activity (account_name, successful_checks, failed_checks_in_a_row) VALUES (?, 0, 0)");
                ps.setString(1, pc.getAccountName());
                ps.executeUpdate();
                ps.close();
            }

            // Step 2: Update counters
            if (currentSuccess < 100) {
                ps = con.prepareStatement(
                    "UPDATE bot_check_activity " +
                    "SET successful_checks = successful_checks + 1, " +
                    "failed_checks_in_a_row = GREATEST(failed_checks_in_a_row - 1, 0) " +
                    "WHERE account_name = ?"
                );
                ps.setString(1, pc.getAccountName());
                ps.executeUpdate();
                ps.close();

                currentSuccess += 1; // Since we just added +1
            } else {
                ps = con.prepareStatement(
                    "UPDATE bot_check_activity " +
                    "SET failed_checks_in_a_row = GREATEST(failed_checks_in_a_row - 1, 0) " +
                    "WHERE account_name = ?"
                );
                ps.setString(1, pc.getAccountName());
                ps.executeUpdate();
                ps.close();
            }

            // Step 3: Always give reward based on final currentSuccess
            int rewardItemId = getBotCheckRewardItem(currentSuccess);

            if (rewardItemId > 0) {
                pc.sendPackets(new S_CloseList(pc.getId()));
                pc.getInventory().storeItem(rewardItemId, 1);
                pc.sendPackets(new S_SystemMessage("You received a reward for passing the botcheck!"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(ps);
            SQLUtil.close(con);
        }
    }


    private static int getBotCheckRewardItem(int totalSuccess) {
        if (totalSuccess > 60) {
            return 49251;
        } else if (totalSuccess >= 30) {
            return 49056;
        } else if (totalSuccess >= 0) {
            return 41284;
        }
        return 0; // Should never happen
    }

    public static void handleBotCheckFailure(L1PcInstance pc) {
        updateField(pc, "successful_checks", -2, false); // -5 success, minimum 0
        updateField(pc, "failed_checks_in_a_row", 2, true); // +2 fails
    }

    public static void resetHauntedHouseEntries(L1PcInstance pc) {
        setField(pc, "haunted_house_entries", 0);
    }

    public static void resetPolyRaceEntries(L1PcInstance pc) {
        setField(pc, "poly_race_entries", 0);
    }

    public static void resetDogFightEntries(L1PcInstance pc) {
        setField(pc, "dog_fight_entries", 0);
    }

    public static void resetMonsterKills(L1PcInstance pc) {
        setField(pc, "monster_kills", 0);
    }

    public static void resetFailedChecks(L1PcInstance pc) {
        setField(pc, "failed_checks_in_a_row", 0);
    }
	
    public static void triggerBotCheck(L1PcInstance pc) {
        if (pc == null || pc.isDead()) {
            return;
        }

        List<String> questionList = new ArrayList<>(BotCheckManager.QUESTIONS.keySet());
        String question = questionList.get(BotCheckManager.RANDOM.nextInt(questionList.size()));
        String expectedAnswer = BotCheckManager.QUESTIONS.get(question);
        
        pc.sendPackets(new S_RawStringDialog(pc.getId(), "Bot Check", question));
        pc.setAwaitingBotCheck(true);
        pc.setBotCheckQuestion(expectedAnswer);
        pc.setBotCheckQuestionText(question); // ✅ THIS is what's missing
        pc.setBotCheckStartTime(System.currentTimeMillis());

        // 🛡️ Add 20-second timeout handler
        GeneralThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(25000); // 20 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (pc != null && pc.isAwaitingBotCheck()) {
                    // Timeout occurred
                    pc.setAwaitingBotCheck(false);
                    pc.setBotCheckQuestion(null);
                    pc.sendPackets(new S_SystemMessage("Botcheck timed out - Failed Bot Check!"));
                    pc.sendPackets(new S_CloseList(pc.getId()));

                    BotCheckActivityManager.handleBotCheckFailure(pc);
                }
            }
        });
    }

    public static void loadBotCheckProgress(L1PcInstance pc) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = L1DatabaseFactory.getInstance().getConnection();
            ps = con.prepareStatement("SELECT monster_kills, haunted_house_entries, poly_race_entries FROM bot_check_activity WHERE account_name = ?");
            ps.setString(1, pc.getAccountName());
            rs = ps.executeQuery();

            if (rs.next()) {
            	pc.setRawMonsterKills(rs.getInt("monster_kills"));
            	pc.setLastFlushedMonsterKills(rs.getInt("monster_kills")); // ✅ new line
                pc.setHauntedHouseEntryBuffer(rs.getInt("haunted_house_entries"));
                pc.setPolyRaceEntryBuffer(rs.getInt("poly_race_entries"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(ps);
            SQLUtil.close(con);
        }
    }


}
