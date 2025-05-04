#!/bin/bash
echo "Starting lineage service..."
sudo systemctl start lineage.service

# Check if start command was successful before proceeding
if [ $? -ne 0 ]; then
    echo "Failed to start lineage.service. Check status with 'systemctl status lineage.service'"
    exit 1
fi

echo "Waiting 3 seconds..."
sleep 3

echo "Following logs (Ctrl+C to stop):"
# Jump to end and follow live output
journalctl -f -u lineage.service -e 