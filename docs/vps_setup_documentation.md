# Lineage Classic 363 - VPS Setup Documentation

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
- [ ] Database configuration

## Server Files
- [x] Game server files transfer (via GitHub repository)
- [ ] Configuration updates
- [ ] Directory structure setup

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
- [x] Firewall rules (SSH, MySQL, and game server ports open)
- [x] Regular updates
  - [x] Kernel upgraded from 6.8.0-55-generic to 6.8.0-58-generic (reboot completed)
  - **Service restarts completed**:
    - /etc/needrestart/restart.d/dbus.service
    - systemd-logind.service
    - unattended-upgrades.service
- [ ] Intrusion prevention

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

## Post-Migration Verification
- [ ] Game server functionality
- [ ] Client connection
- [ ] Database operation
- [ ] Performance benchmarks

---

*This document should be updated regularly as changes are made to the server.* 