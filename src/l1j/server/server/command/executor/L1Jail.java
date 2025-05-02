package l1j.server.server.command.executor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.utils.SQLUtil;

public class L1Jail implements L1CommandExecutor {
	private static Logger _log = LoggerFactory.getLogger(L1Jail.class.getName());

	private static final int JAIL_X = 32737;
	private static final int JAIL_Y = 32796;
	private static final short JAIL_MAP = 99;

	private L1Jail() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1Jail();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			String[] args = arg.split(" ");

			if (args.length < 4)
				throw new Exception();

			String player = args[0];
			String durationType = args[1].toLowerCase();
			int duration = Integer.parseInt(args[2]);

			int messageIndex = 3;
			boolean broadcast = false;

			if (args[3].trim().equals("-n")) {
				broadcast = true;
				messageIndex = 4;
			}

			StringBuilder messageBuilder = new StringBuilder();
			for (int i = messageIndex; i < args.length; i++) {
				messageBuilder.append(args[i]).append(" ");
			}
			String message = messageBuilder.toString().trim();

			L1PcInstance convict = L1World.getInstance().getPlayer(player);
			Calendar cal = Calendar.getInstance();

			switch (durationType) {
				case "hours":
					cal.add(Calendar.HOUR_OF_DAY, duration);
					break;
				case "days":
					cal.add(Calendar.DAY_OF_YEAR, duration);
					break;
				default:
					pc.sendPackets(new S_SystemMessage("Invalid duration type. Use 'hours' or 'days'."));
					return;
			}

			if (convict != null) {
				L1Teleport.teleport(convict, JAIL_X, JAIL_Y, JAIL_MAP, 5, true);
				convict.sendPackets(new S_SystemMessage(String.format("%s jailed you for %d %s.", pc.getName(), duration, durationType)));
				convict.sendPackets(new S_SystemMessage("Reason: " + message));
			} else {
				convict = CharacterTable.getInstance().restoreCharacter(player);
				if (convict == null) {
					pc.sendPackets(new S_SystemMessage("A character with the name '" + player + "' does not exist."));
					return;
				}
				CharacterTable.getInstance().moveCharacter(convict, JAIL_X, JAIL_Y, JAIL_MAP);
				pc.sendPackets(new S_SystemMessage("Offline player " + convict.getName() + " has been jailed."));
			}

			String jailMessage = String.format("%s has been jailed for %d %s.", convict.getName(), duration, durationType);
			pc.sendPackets(new S_SystemMessage(jailMessage));
			if (broadcast) {
				L1World.getInstance().broadcastServerMessage(jailMessage + " for bad behaviour!");
			}

			int existingJailId = getExistingJailId(convict.getName());
			if (existingJailId != -1) {
				pc.sendPackets(new S_SystemMessage("Player is already jailed. Overwriting jail time."));
			}

			logJail(convict.getName(), pc.getName(), message, cal.getTimeInMillis());

		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("." + cmdName + " <player_name> [hours|days] <number> [-n] <reason>"));
		}
	}

	private static int getExistingJailId(String user) {
		int existingId = -1;
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;

		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT id FROM `log_jail` WHERE `status` = 'jailed' AND `jailed_char_name` = ?");
			pstm.setString(1, user);
			rs = pstm.executeQuery();

			if (rs.next()) {
				existingId = rs.getInt("id");
			}
		} catch (Exception ex) {
			_log.warn("Failed to look up existing user jail. Could lead to duplicate entries in the log_jail table!", ex);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}

		return existingId;
	}

	private static void logJail(String user, String jailer, String message, Long releaseTime) {
		Connection con = null;
		PreparedStatement pstm = null;
		int existingJailId = getExistingJailId(user);

		try {
			con = L1DatabaseFactory.getInstance().getConnection();

			if (existingJailId == -1) {
				pstm = con.prepareStatement("INSERT INTO `log_jail` " +
						"(`jailed_char_name`, `jailer`, `message`, `unjail`, `status`, `modified`) VALUES (?,?,?,?,?,NOW())");
				pstm.setString(1, user);
				pstm.setString(2, jailer);
				pstm.setString(3, message);
				pstm.setTimestamp(4, new Timestamp(releaseTime));
				pstm.setString(5, "jailed");
			} else {
				pstm = con.prepareStatement("UPDATE `log_jail` SET `jailer` = ?, `message` = ?, `unjail` = ?, `modified` = NOW() WHERE `id` = ?");
				pstm.setString(1, jailer);
				pstm.setString(2, message);
				pstm.setTimestamp(3, new Timestamp(releaseTime));
				pstm.setInt(4, existingJailId);
			}

			pstm.execute();
		} catch (Exception ex) {
			_log.warn("Failed to log jail for user: " + user, ex);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
}
