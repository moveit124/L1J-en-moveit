# Sanctuary of Aden - Development Workflow

This document outlines the standard workflow for making changes to the Sanctuary of Aden codebase and deploying them to the live server. Following this process ensures consistency, allows for verification, and minimizes disruption to the live environment.

## Workflow Steps

1.  **Branch Creation (if needed):**
    *   For significant features or changes, create a new feature branch off the `main` branch locally:
        ```bash
        # Ensure main is up-to-date
        git checkout main
        git pull origin main

        # Create and switch to your new branch
        git checkout -b feature/your-feature-name
        ```
    *   For smaller changes or fixes specific to your development, use one of the established personal development branches (`devRez`, `devMove`). If you need to create a new one:
        ```bash
        git checkout main
        git pull origin main
        git checkout -b dev-yourname # Or another specific name
        ```

2.  **Local Development & Editing:**
    *   Make all code changes, configuration updates, or documentation edits on your local machine within your designated branch (`feature/your-feature-name`, `devRez`, or `devMove`).

3.  **Testing & Verification (Local):**
    *   Compile the code locally (`./build.sh`).
    *   Run tests if available.
    *   Ensure the changes function as expected in a local test environment if possible.

4.  **Commit Changes:**
    *   Stage the changes you want to include:
        ```bash
        git add <file1> <file2> ...
        ```
    *   Commit the staged changes with a clear, descriptive message:
        ```bash
        git commit -m "Brief description of changes (e.g., Fix login bug, Add systemd config)"
        ```

5.  **Push to Your Remote Branch:**
    *   Push your local branch commits to the corresponding remote branch on GitHub:
        ```bash
        # For the first push of a new branch:
        git push -u origin <your-branch-name>

        # For subsequent pushes:
        git push origin <your-branch-name>
        ```

6.  **Verification & Code Review (Optional but Recommended):**
    *   **Option A (Pull Request):** Create a Pull Request (PR) on GitHub from your branch (`feature/your-feature-name` or `dev-yourname`) targeting the `main` branch. This allows for discussion, automated checks (if configured), and code review by collaborators before merging.
    *   **Option B (Staging Server):** Deploy your branch to a separate staging/test server (if available) for more thorough testing before affecting `main`.
    *   **Option C (Direct Verification):** If the change is small and well-understood, proceed directly after local verification.

7.  **Merge to Main Branch:**
    *   **If using a PR:** Once the PR is approved and passes checks, merge it into the `main` branch via the GitHub interface.
    *   **If not using a PR:** Manually merge your verified branch into `main` locally and push `main`:
        ```bash
        # Switch to main and ensure it's up-to-date
        git checkout main
        git pull origin main

        # Merge your branch into main
        git merge <your-branch-name> --no-ff # Use --no-ff for a clearer history

        # Push the updated main branch
        git push origin main
        ```

8.  **Deploy to Live Server (`soa`):**
    *   SSH into the live server (`ssh soa`).
    *   Navigate to the application directory: `cd /opt/SanctuaryOfAden`
    *   Ensure you are on the `main` branch: `git checkout main`
    *   Pull the latest changes from the `main` branch on GitHub:
        ```bash
        GIT_SSH_COMMAND='ssh -i /root/.ssh/id_ed25519_github -o StrictHostKeyChecking=no' git pull origin main
        ```
    *   **If code changes were pulled:**
        *   Stop the running service: `systemctl stop lineage.service`
        *   Rebuild the application: `./build.sh`
        *   Restart the service: `systemctl start lineage.service`
        *   Check the status: `systemctl status lineage.service`
    *   **If only configuration or non-code files were pulled:**
        *   Restart the service to apply changes: `systemctl restart lineage.service`
        *   Check the status: `systemctl status lineage.service`

## Notes

*   **Always pull before pushing or merging** to avoid unnecessary conflicts.
*   Keep commits small and focused on a single logical change.
*   Write clear commit messages.
*   Communicate with your collaborators about ongoing work and merges. 