# Lineage Classic 363 - VPS Documentation

## Server Information

- **VPS Provider**: IONOS
- **Server IP**: 50.21.186.174
- **OS**: Ubuntu 24.04.2 LTS
- **CPU**: 4 Core x 3.5 GHz (Intel Xeon E3-1230 v6)
- **RAM**: 16 GB
- **Storage**: 2 x 480 GB Software RAID 1
- **Bandwidth**: 1 Gbps (Private and Public)
- **Server Type**: Dedicated Server L-16
- **Date Configured**: May 2024

## Initial Setup

### SSH Configuration
- SSH access configured with key-based authentication
- Local SSH config set up with alias `soa`
- SSH key: `~/.ssh/id_ed25519_soa`
- Connect to server via terminal using: `ssh soa`

### Environment Setup
- [x] Java installation (OpenJDK 21.0.6)
- [x] MySQL installation (MySQL 8.0.41)
- [x] Required dependencies (screen, rsync, unzip, zip, htop)
- [x] Firewall configuration (UFW)

## Database Migration
- [x] Database backup from old server
- [x] Database restoration to new server
- [x] Database configuration (Connection details set in server.properties; MySQL tuning TBD)

## Server Files
- [x] Game server files transfer (via GitHub repository)
- [x] Configuration updates (server.properties, startlive.sh)
- [x] Directory structure setup (via git clone, script execution)

## Service Configuration
- [x] Startup script updates (startlive.sh adapted for systemd)
- [x] SystemD service creation (lineage.service created and enabled)
- [x] Automatic restarts (via systemd Restart=on-failure)
- [ ] Log rotation (systemd journald handles service logs; review `startlive.sh` java0.log rotation if needed)

## Monitoring Setup
- [ ] System monitoring
- [ ] Performance metrics
- [ ] Alert configuration

## Backup Strategy
- [ ] Database backup schedule
- [ ] File backup schedule
- [ ] Backup retention policy

## Security Measures
- [x] Firewall rules (SSH & game server ports open; MySQL port 3306 open but recommend restricting/closing if direct external access not needed)
- [x] Regular updates
  - [x] Kernel upgraded from 6.8.0-55-generic to 6.8.0-58-generic (reboot completed)
  - **Service restarts completed**:
    - /etc/needrestart/restart.d/dbus.service
    - systemd-logind.service
    - unattended-upgrades.service
- [x] Intrusion prevention (Basic - UFW enabled, Telnet restricted to localhost)

## Telnet Access (Localhost Only)
- Enabled via `TelnetServer=True` in `config/server.properties`.
- Requires `telnet` client installed on VPS (`sudo apt install telnet`).
- Connect from VPS terminal: `telnet localhost 23`
- Known commands: `echo`, `playerid`, `charstatus`, `globalchat`, `shutdown`.
- Exit Telnet: Type `quit` or `exit` (if implemented) or use `Ctrl+]` then type `quit`.

## User Management

### User: `moveit124`
- **Purpose:** Collaborator development access.
- **Linux Account:** Created via `adduser moveit124`.
  - Initial Password: `staythere875` (User should change this if direct login needed).
- **SSH Access:** Key-based only. Public key added to `/home/moveit124/.ssh/authorized_keys`.
- **Group Membership:** Primary group `moveit124`, secondary group `gamedevs` (`usermod -aG gamedevs moveit124`).
- **Directory Permissions:** Group `gamedevs` has recursive group ownership (`chgrp -R`) and write/execute permissions (`chmod -R g+w`, `find ... -exec chmod g+x,g+s`) on `/opt/SanctuaryOfAden`.
- **MySQL Access:** User `moveit124@localhost` created with password `[See Secure Password Storage]`. Granted `SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, INDEX, ALTER, REFERENCES` on `soadb.*`. Access requires SSH tunnel.
- **Sudo Access:** Limited via `visudo` to allow `NOPASSWD:` execution of `systemctl start/stop/restart/status lineage.service` only.

### Group: `gamedevs`
- **Purpose:** Grant shared write/execute access to the game server directory (`/opt/SanctuaryOfAden`).
- **Members:** `moveit124`.

## Common Operations

### Viewing Live Server Logs
- From SSH session (root or moveit124), use alias (if configured in `.bashrc`): `log-lineage`
- Direct command: `journalctl -f -u lineage.service -e`
- Alias definition: `alias log-lineage='journalctl -f -u lineage.service -e'`

### Starting Server & Viewing Logs
- From SSH session, run script (as root or moveit124 with sudo rule):
  ```bash
  start-lineage-log.sh
  ```

### Restarting Server
- From SSH session (root or moveit124 with sudo rule):
  ```bash
  sudo systemctl restart lineage.service
  ```

### Stopping Server
- From SSH session (root or moveit124 with sudo rule):
  ```bash
  sudo systemctl stop lineage.service
  ```

## Maintenance Log

| Date | Action | Details | Performed By |
|------|--------|---------|-------------|
| May 2024 | Initial setup | SSH key configuration | [Your Name] |
| May 2024 | Java installation | Installed OpenJDK 21.0.6 | [Your Name] |
| May 2024 | MySQL installation | Installed MySQL 8.0.41 | [Your Name] |
| May 2024 | Firewall setup | Configured UFW to allow SSH and MySQL | [Your Name] |
| May 2024 | Dependencies | Installed screen, rsync, unzip, zip, htop | [Your Name] |
| May 2024 | Git installation | Installed Git 2.43.0 | [Your Name] |
| May 2024 | Database creation | Created soadb MySQL database | [Your Name] |
| May 2024 | GitHub SSH key | Generated and added SSH key to GitHub | [Your Name] |
| May 2024 | Code deployment | Cloned repository to /opt/SanctuaryOfAden | [Your Name] |
| May 2024 | Firewall update | Opened game server ports (2000/tcp, 7777/tcp) | [Your Name] |
| May 2024 | System update | Rebooted to apply kernel update to 6.8.0-58-generic | [Your Name] |
| May 2024 | Database Restore | Restored l1jdb_backup.sql to soadb | [Your Name] |
| May 2024 | Server Config | Updated server.properties for VPS | [Your Name] |
| May 2024 | Build Tools | Installed ant | [Your Name] |
| May 2024 | Startup Script | Updated startlive.sh for systemd | [Your Name] |
| May 2024 | Systemd Service | Created, enabled, and started lineage.service | [Your Name] |
| May 2024 | Documentation | Created docs/workflowRez.md and docs/workflowMove.md | [Your Name] |
| May 2024 | Performance Tuning | Increased Java heap size to 6GB (-Xms6g -Xmx6g) | [Your Name] |
| May 2024 | Security | Corrected PasswordSalt in server.properties | [Your Name] |
| May 2024 | Telnet Setup | Installed telnet client | [Your Name] |
| May 2024 | Telnet Setup | Enabled TelnetServer=True (localhost only) | [Your Name] |
| May 2024 | User Setup | Created Linux user 'moveit124' | [Your Name] |
| May 2024 | User Setup | Added SSH key for 'moveit124' | [Your Name] |
| May 2024 | Permissions | Created 'gamedevs' group, added 'moveit124' | [Your Name] |
| May 2024 | Permissions | Set group ownership & permissions on /opt/SanctuaryOfAden | [Your Name] |
| May 2024 | User Setup | Created MySQL user 'moveit124@localhost' | [Your Name] |
| May 2024 | Permissions | Granted limited sudo for 'moveit124' via visudo | [Your Name] |
| May 2024 | Operations | Created start-lineage-log.sh script | [Your Name] |

## Post-Migration Verification
- [x] Game server functionality (Service running, accepting connections)
- [x] Client connection (Login successful)
- [x] Database operation (Account data read successfully)
- [ ] Performance benchmarks

---

*This document should be updated regularly as changes are made to the server.* 


move's

ssh and mysql username: moveit124
ssh and mysql initial pw: staythere875