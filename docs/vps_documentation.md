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

## Common Operations

### Starting the Server & Following Logs
- From SSH session (root or moveit124), use alias (if configured in `.bashrc`): `start-lineage`
- This runs `/usr/local/bin/start-lineage.sh` which executes `sudo systemctl start lineage.service && journalctl -f -u lineage.service --since "0 seconds ago"`

### Viewing Server Console (Live Tail)
- From SSH session (root or moveit124), use alias (if configured in `.bashrc`): `console`
- This runs `journalctl -f -u lineage.service`

### Checking Server Status
- `systemctl status lineage.service` (or `sudo systemctl status lineage.service` for `moveit124`)

### Stopping the Server
- `systemctl stop lineage.service` (or `sudo systemctl stop lineage.service` for `moveit124`)

### Restarting the Server
- `systemctl restart lineage.service` (or `sudo systemctl restart lineage.service` for `moveit124`)

### Deploying Code Changes (as root)
1. SSH to `soa` as `root`.
2. Navigate to project dir: `cd /opt/SanctuaryOfAden`
3. Pull latest code:
   `# Ensure the correct key is used if not the default id_ed25519_github`
   `GIT_SSH_COMMAND='ssh -i /root/.ssh/[root_github_key_filename] -o StrictHostKeyChecking=no' git pull origin main`
4. If code changes pulled (check `git log -1`): run `build` alias
5. Restart service: `systemctl restart lineage.service`
6. Check status: `systemctl status lineage.service`

### Deploying Code Changes (as moveit124)
1. SSH to `soa` as `moveit124`.
2. Navigate to project dir: `cd /opt/SanctuaryOfAden`
3. Pull latest code:
   `# Make sure the path to your private key is correct (e.g., /home/moveit124/.ssh/your_key_name)`
   `GIT_SSH_COMMAND='ssh -i /home/moveit124/.ssh/[your_github_key] -o StrictHostKeyChecking=no' git pull origin main` 
4. If code changes pulled (check `git log -1`): run `build` alias
5. Restart service: `sudo systemctl restart lineage.service`
6. Check status: `sudo systemctl status lineage.service`

### Building the Project
- From SSH session (root or moveit124), use alias (if configured in `.bashrc`): `build`
- This runs `cd /opt/SanctuaryOfAden && ./build.sh`

### Accessing Telnet Console (Localhost only)
- From SSH session: `telnet localhost 23`
- Available commands: `echo`, `playerid`, `charstatus`, `globalchat`, `shutdown` (and others defined in `TelnetCommandList.java`)

### Accessing MySQL
- As root: `mysql -u root -p`
- As moveit124: `mysql -u moveit124 -p soadb`
- Remote access (e.g., Workbench) requires SSH Tunneling to connect to `127.0.0.1:3306` on the server.

### Manual Backup
- As root: `mysqldump -u root -p soadb > /opt/SanctuaryOfAden/db/soadb_backup_$(date +%Y%m%d_%H%M%S).sql`

## Server Details
# ... existing code ...

# (Removed sensitive credential block that was here)