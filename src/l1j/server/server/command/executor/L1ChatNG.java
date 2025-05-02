package l1j.server.server.command.executor;

import static l1j.server.server.model.skill.L1SkillId.STATUS_CHAT_PROHIBITED;
import static l1j.server.server.model.skill.L1SkillId.STATUS_GLOBAL_CHAT_PROHIBITED;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillIconGFX;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.utils.SQLUtil;

public class L1ChatNG implements L1CommandExecutor {

    private L1ChatNG() {}

    public static L1CommandExecutor getInstance() {
        return new L1ChatNG();
    }

    @Override
    public void execute(L1PcInstance pc, String cmdName, String arg) {
        try {
            String[] args = arg.split(" ");

            if (args.length < 2 || args.length > 3) {
                pc.sendPackets(new S_SystemMessage("." + cmdName + " <player> <duration> <type (full/global)>"));
                return;
            }

            String name = args[0];
            String durationStr = args[1];
            String type = args.length == 3 ? args[2].toLowerCase() : "full";

            if (!type.equals("full") && !type.equals("global")) {
                pc.sendPackets(new S_SystemMessage("Type must be 'full' or 'global'."));
                return;
            }

            long durationMs = parseDuration(durationStr);

            L1PcInstance target = L1World.getInstance().getPlayer(name);
            if (target == null) {
                pc.sendPackets(new S_SystemMessage("Player not found."));
                return;
            }

            if (durationMs <= 0) {
                removeMuteFromDatabase(target.getId());
                target.removeSkillEffect(STATUS_CHAT_PROHIBITED);
                target.removeSkillEffect(STATUS_GLOBAL_CHAT_PROHIBITED);
                target.sendPackets(new S_SkillIconGFX(36, 0));
                pc.sendPackets(new S_SystemMessage("You have unmuted " + name + "."));
            } else {
                Timestamp endTime = new Timestamp(System.currentTimeMillis() + durationMs);
                saveMuteToDatabase(target.getId(), endTime, type);

                int cappedDuration = (int) Math.min(durationMs, 40_000_000L);

                if (type.equals("global")) {
                    target.setSkillEffect(STATUS_GLOBAL_CHAT_PROHIBITED, cappedDuration);
                } else {
                    target.setSkillEffect(STATUS_CHAT_PROHIBITED, cappedDuration);
                }

                pc.sendPackets(new S_SystemMessage("You have muted " + name + " (" + type + ") for " + formatDuration(durationMs) + "."));

                propagateMute(target.getAccountName(), endTime, type);
            }

        } catch (Exception e) {
            pc.sendPackets(new S_SystemMessage("." + cmdName + " <player> <duration> <type (full/global)>"));
        }
    }

    private void propagateMute(String accountName, Timestamp endTime, String type) {
        Connection con = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        PreparedStatement ps4 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        ResultSet rs3 = null;

        try {
            con = L1DatabaseFactory.getInstance().getConnection();

            ps1 = con.prepareStatement("SELECT ip FROM accounts WHERE login = ?");
            ps1.setString(1, accountName);
            rs1 = ps1.executeQuery();

            if (rs1.next()) {
                String ip = rs1.getString("ip");

                ps2 = con.prepareStatement("SELECT login FROM accounts WHERE ip = ?");
                ps2.setString(1, ip);
                rs2 = ps2.executeQuery();

                while (rs2.next()) {
                    String otherAccount = rs2.getString("login");

                    ps3 = con.prepareStatement("SELECT objid FROM characters WHERE account_name = ?");
                    ps3.setString(1, otherAccount);
                    rs3 = ps3.executeQuery();

                    while (rs3.next()) {
                        int charId = rs3.getInt("objid");

                        ps4 = con.prepareStatement("SELECT 1 FROM character_mute WHERE char_id = ?");
                        ps4.setInt(1, charId);
                        ResultSet rs4 = ps4.executeQuery();

                        if (!rs4.next()) {
                            PreparedStatement insert = con.prepareStatement("REPLACE INTO character_mute (char_id, end_time, type) VALUES (?, ?, ?)");
                            insert.setInt(1, charId);
                            insert.setTimestamp(2, endTime);
                            insert.setString(3, type);
                            insert.execute();
                            insert.close();
                        }
                        rs4.close();
                        ps4.close();
                    }
                    rs3.close();
                    ps3.close();
                }
                rs2.close();
                ps2.close();
            }
            rs1.close();
            ps1.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            SQLUtil.close(rs3);
            SQLUtil.close(ps3);
            SQLUtil.close(rs2);
            SQLUtil.close(ps2);
            SQLUtil.close(rs1);
            SQLUtil.close(ps1);
            SQLUtil.close(con);
        }
    }

    private long parseDuration(String input) {
        try {
            input = input.trim().toLowerCase();
            long multiplier;

            if (input.endsWith("m")) {
                multiplier = 60_000L;
                input = input.replace("m", "");
            } else if (input.endsWith("h")) {
                multiplier = 60 * 60_000L;
                input = input.replace("h", "");
            } else if (input.endsWith("d")) {
                multiplier = 24 * 60 * 60_000L;
                input = input.replace("d", "");
            } else if (input.endsWith("w")) {
                multiplier = 7L * 24 * 60 * 60_000L;
                input = input.replace("w", "");
            } else if (input.endsWith("y")) {
                multiplier = 365L * 24 * 60 * 60_000L;
                input = input.replace("y", "");
            } else if (input.equals("0")) {
                return 0;
            } else {
                multiplier = 60_000L;
            }

            long amount = Long.parseLong(input);
            return amount * multiplier;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;

        if (seconds < 60)
            return seconds + " second(s)";
        else if (seconds < 3600)
            return (seconds / 60) + " minute(s)";
        else if (seconds < 86400)
            return (seconds / 3600) + " hour(s)";
        else if (seconds < 604800)
            return (seconds / 86400) + " day(s)";
        else if (seconds < 31_536_000)
            return (seconds / 604800) + " week(s)";
        else
            return (seconds / 31_536_000) + " year(s)";
    }

    private void saveMuteToDatabase(int charId, Timestamp endTime, String type) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = L1DatabaseFactory.getInstance().getConnection();
            ps = con.prepareStatement("REPLACE INTO character_mute (char_id, end_time, type) VALUES (?, ?, ?)");
            ps.setInt(1, charId);
            ps.setTimestamp(2, endTime);
            ps.setString(3, type);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLUtil.close(ps);
            SQLUtil.close(con);
        }
    }

    private void removeMuteFromDatabase(int charId) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = L1DatabaseFactory.getInstance().getConnection();
            ps = con.prepareStatement("DELETE FROM character_mute WHERE char_id = ?");
            ps.setInt(1, charId);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLUtil.close(ps);
            SQLUtil.close(con);
        }
    }
}
