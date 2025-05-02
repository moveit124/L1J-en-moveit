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

package l1j.server.server.network;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/*
 * (Tricid) This is the second class in the pipeline that handles packets (after ChannelInit).
 * All this is doing is continually reading bytes until a proper packet is formed.  Afterwards, it passes it
 * to the next in the pipeline (the decrypter). 
 */
public class PacketDecoder extends ByteToMessageDecoder {

	Logger _log = LoggerFactory.getLogger(PacketDecoder.class);

	// ➡️ Add tracker here
	private static final ConcurrentHashMap<String, BadPacketTracker> badPacketMap = new ConcurrentHashMap<>();

	private static class BadPacketTracker {
		int count;
		long lastBadTime;
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
		try {
			ctx.close();
		} catch (Exception e) {
		}

		if (!(cause instanceof java.io.IOException)) {
			_log.error("", cause);
		}
	}

	@Override
	public void channelInactive(final ChannelHandlerContext ctx) {
		Client client = NetworkServer.getInstance().getClients().get(ctx.channel().id());

		if (client != null) {
			try {
				client.handleDisconnect();
			} catch (Exception e) {
				_log.error("", e);
			}
		}

		NetworkServer.getInstance().getIps().remove(client.getIp());
		NetworkServer.getInstance().getClients().remove(ctx.channel().id());
	}

	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		int dataLength = 0;
		if (in.readableBytes() >= 2) {
			int hiByte = in.getByte(in.readerIndex()) & 0xff;
			int loByte = in.getByte(in.readerIndex() + 1) & 0xff;
			dataLength = (loByte * 256 + hiByte) - 2;
		}

		if (dataLength < 0 || dataLength > 4096) {
			_log.warn("⚠️ Invalid packet size (" + dataLength + ") from " + ctx.channel().remoteAddress()
				+ " | Channel ID: " + ctx.channel().id()
				+ " | Readable Bytes: " + in.readableBytes()
				+ " | First Bytes: " + hexPreview(in));

			// ➡️ Bad packet tracking
			String ip = ctx.channel().remoteAddress().toString();
			BadPacketTracker tracker = badPacketMap.getOrDefault(ip, new BadPacketTracker());

			long now = System.currentTimeMillis();
			if (now - tracker.lastBadTime > 10000) { // 10 seconds timeout
				tracker.count = 1;
			} else {
				tracker.count++;
			}

			tracker.lastBadTime = now;
			badPacketMap.put(ip, tracker);

			if (tracker.count >= 6) {
			    _log.warn("🚫 Auto-banning IP for repeated bad packets: " + ip);
			    banIpToFile(ip);
			}

			ctx.close();
			return null;
		}

		if (dataLength <= 0 || dataLength > 10240) {
			_log.warn("⚠️ Suspect packet length (" + dataLength + ") from " + ctx.channel().remoteAddress()
				+ " | Channel ID: " + ctx.channel().id()
				+ " | Readable Bytes: " + in.readableBytes()
				+ " | First Bytes: " + hexPreview(in));
			// Still allow it to flow through; no behavior change
		}

		if (in.readableBytes() >= dataLength + 2) {
			byte[] frame = new byte[dataLength];
			in.getBytes(in.readerIndex() + 2, frame, 0, dataLength);
			in.readerIndex(in.readerIndex() + dataLength + 2);
			return frame;
		}
		return null;
	}

	private void banIpToFile(String ip) {
	    try {
	        String cleanIp = ip.replace("/", "").split(":")[0]; // Remove leading / and port
	        java.nio.file.Path path = java.nio.file.Paths.get("banned_ips.txt");

	        // Load current banned IPs
	        java.util.List<String> lines = java.nio.file.Files.exists(path) 
	            ? java.nio.file.Files.readAllLines(path) 
	            : new java.util.ArrayList<>();

	        // Check if already banned
	        boolean alreadyBanned = lines.stream().anyMatch(line -> line.startsWith(cleanIp + " "));

	        if (!alreadyBanned) {
	            String entry = cleanIp + " " + System.currentTimeMillis(); // Save IP + ban time
	            java.nio.file.Files.write(
	                path,
	                (entry + System.lineSeparator()).getBytes(),
	                java.nio.file.StandardOpenOption.CREATE,
	                java.nio.file.StandardOpenOption.APPEND
	            );
	            _log.warn("📄 Banned IP written to banned_ips.txt: " + cleanIp);
	        } else {
	            _log.warn("⏩ IP already in banned list: " + cleanIp);
	        }
	    } catch (Exception e) {
	        _log.error("Failed to write banned IP to file: " + ip, e);
	    }
	}

	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
	    String ip = ctx.channel().remoteAddress().toString().replace("/", "").split(":")[0];
	    if (isBannedIp(ip)) {
	        _log.warn("🚫 Connection attempt from banned IP: " + ip);
	        ctx.close();
	        return;
	    }
	    super.channelActive(ctx); // Allow connection if not banned
	}
	
	private boolean isBannedIp(String ip) {
	    try {
	        java.nio.file.Path path = java.nio.file.Paths.get("banned_ips.txt");
	        if (!java.nio.file.Files.exists(path)) {
	            return false; // No banned IPs yet
	        }

	        java.util.List<String> bannedIps = java.nio.file.Files.readAllLines(path);
	        for (String line : bannedIps) {
	            String[] parts = line.split(" ");
	            if (parts.length > 0 && parts[0].equals(ip)) {
	                return true;
	            }
	        }
	    } catch (Exception e) {
	        _log.error("Failed to read banned IPs file.", e);
	    }
	    return false;
	}

	
	private String hexPreview(ByteBuf in) {
		int len = Math.min(in.readableBytes(), 10);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			sb.append(String.format("%02X ", in.getByte(in.readerIndex() + i)));
		}
		return sb.toString().trim();
	}

	@Override
	protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		Object decoded = decode(ctx, in);
		if (decoded != null) {
			out.add(decoded);
		}
	}
}
