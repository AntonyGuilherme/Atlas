# This PowerShell script connects to multiple Linux machines and
# copies your "Fat" JAR to a specified remote directory.
#
# IMPORTANT: This script relies on password-based SSH authentication.
# You will be prompted to enter the password for EACH machine during execution.
# For truly automated and secure connections, consider setting up SSH keys.

# --- Configuration ---

# Define the single username to be used for all school computers
$commonUsername = "costa-24" # Replace with your actual username.

# Define the list of hostnames/IPs for the school computers
# Replace these with your actual hostnames/IP addresses.
$schoolComputers = @(
    "tp-1a201-01.enst.fr"
    #"tp-1a201-03.enst.fr"
)

# Local path to your "Fat" JAR file (atlas-all.jar)
# Ensure this path is correct on your Windows machine.
$localJarPath = "atlas-all.jar" # <<< IMPORTANT: UPDATE THIS PATH

# Remote directory on the Linux machines where the JAR will be copied
# This will be created in your user's home directory on the remote machine.
$remoteDirRelative = "my_java_app" # This will resolve to ~/my_java_app

# Name of the JAR file on the remote machine
$remoteJarName = "atlas-all.jar"

# --- Script Logic ---

Write-Host "Starting JAR upload process..."
Write-Host "You will be prompted for the SSH password for each machine."

foreach ($remoteHost in $schoolComputers) {
    $username = $commonUsername # Use the common username for each connection

    Write-Host "`n--- Processing $username@$remoteHost ---"

    try {
        # 1. Create the remote directory in the user's home folder
        Write-Host "Creating remote directory (~/$remoteDirRelative) on $remoteHost..."
        # Using 'mkdir -p' ensures the directory is created only if it doesn't exist
        $mkdirOutput = ssh "$username@$remoteHost" "mkdir -p ~/$remoteDirRelative"
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Failed to create remote directory on $remoteHost. Output: $mkdirOutput. Skipping."
            continue
        }

        # 2. Copy the "Fat" JAR to the remote machine
        Write-Host "Copying '$localJarPath' to $remoteHost`:`~/$remoteDirRelative/$remoteJarName..."
        # Use backtick (`) to escape the colon (:) to ensure PowerShell doesn't misinterpret it.
        scp "$localJarPath" "$username@$remoteHost`:`~/$remoteDirRelative/$remoteJarName"
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Failed to copy JAR to $remoteHost. Skipping."
            continue
        } else {
            Write-Host "JAR copied successfully to $remoteHost."
        }

    } catch {
        Write-Error "An error occurred while processing $username@$remoteHost`:` $($_.Exception.Message)"
    }
}

Write-Host "`n--- All upload operations completed. ---"
Write-Host "Review the output above for results on each machine."
