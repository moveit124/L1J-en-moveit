# Sanctuary of Aden - Development Workflow (for Move)

This document outlines the standard workflow for 'moveit124' to make changes to the Sanctuary of Aden codebase and deploy them to the live server (`soa`).

## 1. Connecting to the Server (`soa`)

*   **SSH:** Use your configured SSH key to connect:
    ```bash
    ssh moveit124@soa
    ```
*   **MySQL:** Once connected via SSH, access the database using:
    ```bash
    # Use the password provided to you securely
    mysql -u moveit124 -p soadb
    ```
    *(Note: For GUI tools like MySQL Workbench, you must configure an SSH tunnel first to connect to 127.0.0.1:3306 on the server)*

## 2. Getting Code & Making Changes

*   **Always start by getting the latest code:**
    ```bash
    # Navigate to the project directory (if not already there)
    cd /opt/SanctuaryOfAden

    # Switch to your development branch
    git checkout devMove

    # Pull the latest changes for your branch from GitHub
    git pull origin devMove
    ```
    *(If this is your first time, you might need to checkout `main`, pull it, then `git checkout -b devMove`)*
*   Make your code, configuration, or documentation edits on your **local machine**.
*   **Test Locally:** If you changed code (`.java` files), run `./build.sh` locally to check for compilation errors before committing.

## 3. Saving Your Changes (Locally)

*   Stage the files you changed: `git add .` (stages all changes) or `git add <specific file>`
*   Commit your changes with a clear message:
    ```bash
    git commit -m "Brief description of your changes"
    ```
*   Push your commits to your branch on GitHub:
    ```bash
    git push origin devMove
    ```

## 4. Merging to `main` (Requires Coordination!)

*   Before your changes can go live, they must be merged into the `main` branch.
*   **Please coordinate with Rez for this step.**
*   Using **Pull Requests** on the GitHub repository is the preferred method for merging. Rez can help you set this up if needed.

## 5. Deploying `main` Branch Updates to the Live Server (`soa`)

*   **(Perform these steps ONLY after your changes have been merged into the `main` branch)**
*   Connect to the server via SSH:
    ```bash
    ssh moveit124@soa
    ```
*   Navigate to the application directory:
    ```bash
    cd /opt/SanctuaryOfAden
    ```
*   Ensure you are on the `main` branch (it should switch automatically if needed):
    ```bash
    git checkout main
    ```
*   Pull the latest `main` branch changes from GitHub (using your specific deploy key):
    ```bash
    # Make sure the path to your private key is correct (e.g., /home/moveit124/.ssh/your_key_name)
    GIT_SSH_COMMAND='ssh -i [Path to Your GitHub SSH Key on Server] -o StrictHostKeyChecking=no' git pull origin main
    ```
*   **Check if code was updated:** Look at the `git pull` output or run `git log -1` to see if `.java` files were changed.
*   **If CODE changed:** Stop, rebuild, and start the service:
    ```bash
    sudo systemctl stop lineage.service
    ./build.sh
    sudo systemctl start lineage.service
    ```
*   **If ONLY Config/Docs/Other non-code files changed:** Just restart the service:
    ```bash
    sudo systemctl restart lineage.service
    ```
*   Verify the server status:
    ```bash
    sudo systemctl status lineage.service
    ```
    *(Look for "active (running)")*

## 6. Viewing Live Server Logs

*   Connect via SSH: `ssh moveit124@soa`
*   Use the alias (requires a new shell session after initial setup):
    ```bash
    log-lineage
    ```
*   (Alternatively, run the full command: `journalctl -f -u lineage.service`)
*   Press `Ctrl+C` to stop viewing logs.

## 7. Key Reminders

*   Always run `git pull origin devMove` on your `devMove` branch before starting new work.
*   Communicate frequently with Rez, especially before merging changes to `main`.
*   Write clear commit messages! 