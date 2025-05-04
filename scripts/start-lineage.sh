#!/bin/bash
echo "Starting lineage service..."
sudo systemctl start lineage.service

# Check if start command was successful before proceeding
if [ $? -ne 0 ]; then
    echo "Failed to start lineage.service. Check status with 'systemctl status lineage.service'"
    exit 1
fi

# No explicit wait needed

echo "Following logs from start (Ctrl+C to stop):"
# Show logs since the start command was issued and follow
journalctl -f -u lineage.service --since "0 seconds ago" 