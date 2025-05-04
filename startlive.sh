#!/usr/bin/env bash

# Exit if any command fails
set -e

# exit codes of GameServer:
#  0 normal shutdown
#  2 reboot attempt

# Setup logging directories
if [[ ! -e "log/" ]]; then
    mkdir -p log
fi

if [[ ! -e "emblem/" ]]; then
    mkdir -p emblem
fi

# Server command
# Commented out JMX agent: -javaagent:/opt/SanctuaryOfAden/jmx_prometheus_javaagent-0.16.1.jar=9404:/opt/SanctuaryOfAden/jmx_exporter_config.yaml
SERVER_CMD="java --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED -Xlog:gc*:file=/opt/SanctuaryOfAden/log/gc.log:time,uptime,level,tags:filecount=10,filesize=100m -Xms6g -Xmx6g -cp l1jen.jar:lib/* l1j.server.Server"
# SCREEN_NAME="live"

# Check if server is already running
# if screen -list | grep -q "\.$SCREEN_NAME"; then
#     echo "Error: Server screen session .$SCREEN_NAME already exists!"
#     exit 1
# fi

# Rotate logs (REMOVED - journald handles rotation)
# [ -f "log/java0.log" ] && mv "log/java0.log" "log/$(date +%Y-%m-%d_%H-%M-%S)_java.log"
# [ -f "log/stderr.log" ] && mv "log/stderr.log" "log/$(date +%Y-%m-%d_%H-%M-%S)_stderr.log"

# Start server directly, piping output to log file (REMOVED tee)
# screen -dmS "$SCREEN_NAME" bash -c "$SERVER_CMD 2>&1 | tee log/java0.log"
exec $SERVER_CMD

# Original messages (commented out as screen is not used)
# echo "Server started in screen session: $SCREEN_NAME"
# echo "Use 'screen -r $SCREEN_NAME' to attach to the server console" 