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

### 2.1 Local Configuration Setup (One-Time)

*   The server uses template configuration files (`.template`) tracked by Git.
*   The actual configuration files (e.g., `config/server.properties`) are ignored by Git (`.gitignore`) to allow local overrides.
*   **First time setup:** Copy the template to create your local configuration file:
    ```bash
    cp config/server.properties.template config/server.properties
    ```
*   **Modify Local Config:** Edit the *local* `config/server.properties` (NOT the template) with your specific database credentials or other settings needed for your local development environment.

*   Make your code edits on your **local machine**.
*   If you need to change a configuration setting *for everyone* (including the live server), edit the `.template` file (e.g., `config/server.properties.template`). Ask Rez to review before merging.
*   If you only need to change a setting for your *local* testing, edit the actual config file (e.g., `config/server.properties`), which is ignored by Git.
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
    # Ensure the correct SSH key for GitHub is specified (e.g., ~/.ssh/github_key)
    GIT_SSH_COMMAND='ssh -i /home/moveit124/.ssh/id_ed25519 -o StrictHostKeyChecking=no' git pull origin main
    ```
*   **Check which files changed:** `git log -1 --name-status` or check pull output.
*   **If `.template` config files changed:** Copy the updated template(s) to the live configuration file(s):
    ```bash
    # Example for server.properties:
    cp config/server.properties.template config/server.properties
    # Add commands for other templates if needed
    ```
*   **If code changed (e.g., `.java` files):** Run `build` alias, then `restart` alias.
    ```bash
    build
    restart
    ```
*   **If *only* config templates or non-code files (docs, etc.) changed:** Just run `restart` alias after copying templates:
    ```bash
    restart
    ```
*   Verify the server status:
    ```bash
    status
    ```
    *(Look for "active (running)")*

## 6. Viewing Live Server Logs

*   Connect via SSH: `ssh moveit124@soa`
*   Use the alias (requires a new shell session after initial setup):
    ```bash
    console
    ```
*   (Alternatively, run the full command: `journalctl -f -u lineage.service`)
*   Press `Ctrl+C` to stop viewing logs.

## 7. Other Server Commands (Aliases)

*   `start`: Starts the server and follows logs (like `start_interactive.sh`).
*   `stop`: Stops the server.
*   `status`: Checks the server status.
*   `build`: Runs the build script (`./build.sh`).

## 8. Key Reminders

*   Always run `git pull origin devMove` on your `devMove` branch before starting new work.
*   Communicate frequently with Rez, especially before merging changes to `main`.
*   Write clear commit messages! 