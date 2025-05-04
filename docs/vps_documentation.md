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
- Use alias (if configured in `.bashrc`): `start`
- This runs `/usr/local/bin/start-lineage.sh` which executes `sudo systemctl start lineage.service && journalctl -f -u lineage.service --since "0 seconds ago"`
- *(Note: Requires permissions to run the script, typically root or via sudo)*

### Viewing Server Console (Live Tail)
- Use alias (if configured in `.bashrc`): `console`
- This runs `journalctl -f -u lineage.service`

### Checking Server Status
- Use alias: `status`
- Runs `systemctl status lineage.service`
- *(Note: May require `sudo` for non-root users, depending on polkit/sudo configuration)*

### Stopping the Server
- Use alias: `stop`
- Runs `systemctl stop lineage.service`
- *(Note: Requires permissions, typically root or via sudo)*

### Restarting the Server
- Use alias: `restart`
- Runs `systemctl restart lineage.service`
- *(Note: Requires permissions, typically root or via sudo)*

### Deploying Code Changes
1. SSH to the server (`soa`).
2. Navigate to project dir: `cd /opt/SanctuaryOfAden`
3. Pull latest code from the `main` branch:
   `# Ensure the correct SSH key for GitHub is specified (e.g., ~/.ssh/github_key)`
   `GIT_SSH_COMMAND='ssh -i [path_to_github_ssh_key] -o StrictHostKeyChecking=no' git pull origin main`
4. If code changes pulled (check `git log -1`): run `build` alias
5. Restart service: run `restart` alias
6. Check status: run `status` alias

### Building the Project
- Use alias (if configured in `.bashrc`): `build`
- This runs `cd /opt/SanctuaryOfAden && ./build.sh`
- *(Note: Requires write permissions in the project directory)*

### Accessing Telnet Console (Localhost only)
- From SSH session: `telnet localhost 23`
- Available commands: `echo`, `playerid`, `charstatus`, `globalchat`, `shutdown` (and others defined in `TelnetCommandList.java`)

### Accessing MySQL
- **Root Access:** `mysql -u root -p` (Requires root password)
- **App User Access:** `mysql -u [app_username] -p [database_name]` (e.g., `mysql -u moveit124 -p soadb`)
- Remote access (e.g., Workbench) requires SSH Tunneling to connect to `127.0.0.1:3306` on the server.

### Manual Backup
- As root: `mysqldump -u root -p soadb > /opt/SanctuaryOfAden/db/soadb_backup_$(date +%Y%m%d_%H%M%S).sql`
