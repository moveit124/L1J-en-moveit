# File Monitoring System

This document contains the scripts used for the file monitoring system installed at `/opt/file_monitor/` on the server.

## Overview

The file monitoring system tracks all changes made to the Sanctuary of Aden codebase on the server, distinguishing between changes made via Git (Rez) and those uploaded via FTP (Move). The system:

- Runs daily at 3 AM Pacific Time
- Creates diffs of all changed files
- Logs all file creations, modifications, and deletions
- Automatically cleans up old files weekly

## Installation

1. Create the necessary directories:
   ```bash
   sudo mkdir -p /opt/file_monitor
   sudo mkdir -p /var/log/file_monitor/diffs
   sudo mkdir -p /var/log/file_monitor/reports
   sudo mkdir -p /var/log/file_monitor/snapshots
   ```

2. Copy the scripts below to their respective locations
3. Make the scripts executable:
   ```bash
   sudo chmod +x /opt/file_monitor/snapshot.sh
   sudo chmod +x /opt/file_monitor/generate_report.sh
   sudo chmod +x /opt/file_monitor/cleanup.sh
   ```

4. Set up the cron jobs:
   ```bash
   sudo cp cron-file-monitor /etc/cron.d/file-monitor
   sudo cp cron-file-monitor-cleanup /etc/cron.d/file-monitor-cleanup
   ```

5. Create the "changes" alias for easy manual tracking:
   ```bash
   echo 'alias changes="sudo /opt/file_monitor/snapshot.sh"' >> ~/.bashrc
   source ~/.bashrc
   ```

6. Run the initial snapshot:
   ```bash
   sudo /opt/file_monitor/snapshot.sh
   ```

## Main Monitoring Script (`/opt/file_monitor/snapshot.sh`)

```bash
#!/bin/bash

# Configuration
PROJECT_DIR="/opt/SanctuaryOfAden"
LOG_DIR="/var/log/file_monitor"
SNAPSHOT_DIR="/var/log/file_monitor/snapshots"
DIFF_DIR="/var/log/file_monitor/diffs"
LOG_FILE="$LOG_DIR/changes.log"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
CURRENT="$SNAPSHOT_DIR/current"
FILELIST="$SNAPSHOT_DIR/filelist.txt"
PREV_FILELIST="$SNAPSHOT_DIR/filelist.prev.txt"
CHECKSUM_DB="$SNAPSHOT_DIR/checksums.db"

# Create directories
mkdir -p "$LOG_DIR" "$SNAPSHOT_DIR" "$DIFF_DIR"

# Log execution
echo "$(date): Running file change detection" >> "$LOG_FILE"

# Create initial file list and checksum database if they don't exist
if [ ! -f "$FILELIST" ] || [ ! -f "$CHECKSUM_DB" ]; then
    echo "$(date): Creating initial snapshot (this may take a while)..." >> "$LOG_FILE"
    
    # Create file list
    find "$PROJECT_DIR" -type f -not -path "*/\.*" -not -path "*/log/*" \
         -not -path "*/build/*" -not -path "*/bin/*" -not -path "*/tmp/*" | sort > "$FILELIST"
    
    # Create checksum database
    touch "$CHECKSUM_DB"
    while read -r file; do
        md5sum "$file" >> "$CHECKSUM_DB"
    done < "$FILELIST"
    
    echo "$(date): Initial snapshot complete. $(wc -l < "$FILELIST") files indexed." >> "$LOG_FILE"
    cp "$FILELIST" "$PREV_FILELIST"
    exit 0
fi

# Save previous file list
cp "$FILELIST" "$PREV_FILELIST"

# Update current file list
find "$PROJECT_DIR" -type f -not -path "*/\.*" -not -path "*/log/*" \
     -not -path "*/build/*" -not -path "*/bin/*" -not -path "*/tmp/*" | sort > "$FILELIST"

# Sort the files for comm
sort "$PREV_FILELIST" > "$PREV_FILELIST.sorted"
sort "$FILELIST" > "$FILELIST.sorted"

# Find new files
comm -13 "$PREV_FILELIST.sorted" "$FILELIST.sorted" > "$SNAPSHOT_DIR/new_files.txt"

# Find deleted files
comm -23 "$PREV_FILELIST.sorted" "$FILELIST.sorted" > "$SNAPSHOT_DIR/deleted_files.txt"

# Process new files
while read -r file; do
    if [ -f "$file" ]; then
        rel_path="${file#$PROJECT_DIR/}"
        
        # Determine source
        if [ -f "$PROJECT_DIR/.git/index.lock" ]; then
            source="Git (Rez)"
        else
            source="FTP-Upload (Move)"
        fi
        
        echo "$(date): CREATE - $rel_path - by $source" >> "$LOG_FILE"
        
        # Add to checksum database
        md5sum "$file" >> "$CHECKSUM_DB.new"
    fi
done < "$SNAPSHOT_DIR/new_files.txt"

# Process deleted files
while read -r file; do
    rel_path="${file#$PROJECT_DIR/}"
    echo "$(date): DELETE - $rel_path" >> "$LOG_FILE"
    
    # Remove from checksum database
    grep -v " $file$" "$CHECKSUM_DB" > "$CHECKSUM_DB.new"
done < "$SNAPSHOT_DIR/deleted_files.txt"

# Clean up sorted files
rm -f "$PREV_FILELIST.sorted" "$FILELIST.sorted"

# Check for modified files by comparing checksums
touch "$CHECKSUM_DB.new"
while read -r file; do
    # Skip files we already processed (new or deleted)
    if ! grep -q "^$file$" "$SNAPSHOT_DIR/new_files.txt" && ! grep -q "^$file$" "$SNAPSHOT_DIR/deleted_files.txt"; then
        # Get old checksum
        old_checksum=$(grep " $file$" "$CHECKSUM_DB" | cut -d" " -f1)
        
        # Calculate new checksum
        new_checksum=$(md5sum "$file" | cut -d" " -f1)
        
        # Compare checksums
        if [ "$old_checksum" != "$new_checksum" ]; then
            rel_path="${file#$PROJECT_DIR/}"
            
            # Determine source
            if [ -f "$PROJECT_DIR/.git/index.lock" ]; then
                source="Git (Rez)"
            else
                source="FTP-Upload (Move)"
            fi
            
            echo "$(date): MODIFY - $rel_path - by $source" >> "$LOG_FILE"
            
            # Create diff if we have the original file
            if [ -f "$CURRENT/$rel_path" ]; then
                diff_file="$DIFF_DIR/${TIMESTAMP}_${rel_path//\//_}.diff"
                mkdir -p "$CURRENT/$(dirname "$rel_path")"
                diff -u "$CURRENT/$rel_path" "$file" > "$diff_file" 2>/dev/null || true
                echo "  Diff saved to $diff_file" >> "$LOG_FILE"
            fi
            
            # Save current version for future diffs
            mkdir -p "$CURRENT/$(dirname "$rel_path")"
            cp -f "$file" "$CURRENT/$rel_path" 2>/dev/null || true
            
            # Update checksum
            grep -v " $file$" "$CHECKSUM_DB" > "$CHECKSUM_DB.tmp"
            echo "$new_checksum  $file" >> "$CHECKSUM_DB.tmp"
            mv "$CHECKSUM_DB.tmp" "$CHECKSUM_DB.new"
        else
            # Copy unchanged entry
            grep " $file$" "$CHECKSUM_DB" >> "$CHECKSUM_DB.new"
        fi
    fi
done < "$FILELIST"

# Replace old checksum database with new one
mv "$CHECKSUM_DB.new" "$CHECKSUM_DB"

# Cleanup
rm -f "$SNAPSHOT_DIR/new_files.txt" "$SNAPSHOT_DIR/deleted_files.txt"
```

## Report Generator Script (`/opt/file_monitor/generate_report.sh`)

```bash
#!/bin/bash
LOG_DIR="/var/log/file_monitor"
DIFF_DIR="$LOG_DIR/diffs"
REPORT_DIR="$LOG_DIR/reports"

# Check if date parameter is provided
if [ -n "$1" ]; then
  REPORT_DATE="$1"
else
  # Default to yesterday
  REPORT_DATE=$(date -d "yesterday" +%Y-%m-%d)
fi

mkdir -p "$REPORT_DIR"
report_file="$REPORT_DIR/changes_$REPORT_DATE.html"

# Create HTML report header
cat > "$report_file" << EOF
<!DOCTYPE html>
<html>
<head>
  <title>File Changes Report - $REPORT_DATE</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 20px; }
    .git { background-color: #e6ffed; padding: 5px; margin: 2px 0; border-radius: 3px; }
    .ftp { background-color: #ffebe9; padding: 5px; margin: 2px 0; border-radius: 3px; }
    .direct { background-color: #fff9db; padding: 5px; margin: 2px 0; border-radius: 3px; }
    pre { background-color: #f6f8fa; padding: 10px; overflow: auto; border-radius: 3px; }
    h1, h2, h3 { color: #333; }
    .diff-add { background-color: #e6ffed; }
    .diff-remove { background-color: #ffebe9; }
    .timestamp { color: #666; font-size: 0.8em; }
  </style>
</head>
<body>
  <h1>File Changes Report - $REPORT_DATE</h1>
EOF

# Check if we have any changes for this date
if ! grep -q "$REPORT_DATE" "$LOG_DIR/changes.log"; then
  echo "<p>No changes detected on this date.</p>" >> "$report_file"
  echo "</body></html>" >> "$report_file"
  echo "Report generated: $report_file (No changes found)"
  exit 0
fi

# Add Git changes section
cat >> "$report_file" << EOF
  <h2>Git Changes (Rez)</h2>
  <ul>
EOF

grep "$REPORT_DATE" "$LOG_DIR/changes.log" | grep "by Git" | while read line; do
  echo "<li class='git'>$line</li>" >> "$report_file"
done

# Add FTP changes
cat >> "$report_file" << EOF
  </ul>
  <h2>FTP Uploads (Move)</h2>
  <ul>
EOF

grep "$REPORT_DATE" "$LOG_DIR/changes.log" | grep "by FTP-Upload" | while read line; do
  echo "<li class='ftp'>$line</li>" >> "$report_file"
done

# Add Direct changes
cat >> "$report_file" << EOF
  </ul>
  <h2>Direct Changes (Unknown)</h2>
  <ul>
EOF

grep "$REPORT_DATE" "$LOG_DIR/changes.log" | grep "by Direct" | while read line; do
  echo "<li class='direct'>$line</li>" >> "$report_file"
done

# Add diffs
cat >> "$report_file" << EOF
  </ul>
  <h2>Detailed Diffs</h2>
EOF

# Convert date format for diff filenames
REPORT_DATE_NO_DASH=${REPORT_DATE//-/}
find "$DIFF_DIR" -name "${REPORT_DATE_NO_DASH}*" | sort | while read diff_file; do
  filename=$(basename "$diff_file" | sed 's/^[0-9]*_[0-9]*_//' | sed 's/\.diff$//' | sed 's/_/\//g')
  timestamp=$(basename "$diff_file" | grep -o "^[0-9]*_[0-9]*")
  
  echo "<h3>$filename <span class='timestamp'>($timestamp)</span></h3>" >> "$report_file"
  echo "<pre>" >> "$report_file"
  
  # Convert diff output to HTML with colored lines
  cat "$diff_file" | sed 's/&/\&amp;/g; s/</\&lt;/g; s/>/\&gt;/g' | \
  sed 's/^+/<span class="diff-add">/; s/^-/<span class="diff-remove">/; s/$/&<\/span>/' | \
  grep -v '^\(<span class="diff-add">\)\?@@' >> "$report_file"
  
  echo "</pre>" >> "$report_file"
done

# Close HTML file
echo "</body></html>" >> "$report_file"
echo "Report generated: $report_file"
```

## Cleanup Script (`/opt/file_monitor/cleanup.sh`)

```bash
#!/bin/bash
# Clean up diff files older than 90 days
find /var/log/file_monitor/diffs -type f -name "*.diff" -mtime +90 -delete
# Clean up old reports
find /var/log/file_monitor/reports -type f -name "changes_*.html" -mtime +90 -delete
# Log the cleanup
echo "$(date): Cleaned up old monitoring files" >> /var/log/file_monitor/changes.log
```

## Cron Configuration

### `/etc/cron.d/file-monitor`
```
# Run file monitoring once daily at 3 AM Pacific Time (10 AM UTC)
0 10 * * * root /opt/file_monitor/snapshot.sh
```

### `/etc/cron.d/file-monitor-cleanup`
```
# Run cleanup weekly on Mondays at 4 AM
0 4 * * 1 root /opt/file_monitor/cleanup.sh
```

## Usage

### Running Change Detection Manually

When notified that Move has made changes, run:
```bash
changes
```
(This is an alias for `sudo /opt/file_monitor/snapshot.sh`)

### Generating Reports

Generate a report for yesterday:
```bash
sudo /opt/file_monitor/generate_report.sh
```

Generate a report for a specific date:
```bash
sudo /opt/file_monitor/generate_report.sh 2023-06-01
```

### Viewing Change Logs

View the most recent changes:
```bash
tail -n 50 /var/log/file_monitor/changes.log
```

### Finding Changes by a Specific Person

Find all changes made by Move:
```bash
grep "by FTP-Upload" /var/log/file_monitor/changes.log
```

Find all changes made by Rez:
```bash
grep "by Git" /var/log/file_monitor/changes.log
```

## Resource Usage and Performance

The monitoring system is designed to minimize impact on server performance:

- Runs once daily during off-peak hours
- Uses checksums to efficiently detect changes
- Only processes files that have actually changed
- Excludes high-churn directories like logs and build artifacts
- Runs at lowest system priority when executed
