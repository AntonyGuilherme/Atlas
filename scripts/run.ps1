# This PowerShell script connects to multiple Linux machines and
# executes a pre-uploaded "Fat" JAR in parallel, opening a new
# PowerShell window for each machine to show its output individually.
#
# IMPORTANT: This script relies on password-based SSH authentication.
# You will be prompted to enter the password for EACH machine in its
# respective new window during execution.
# For truly automated and secure connections, consider setting up SSH keys.
#
# This script assumes the JAR has already been uploaded to the remote machines.

# --- Configuration ---

# Define the single username to be used for all school computers
$commonUsername = "costa-24" # Replaced with your actual username.

# Define the list of hostnames/IPs for the school computers
# Replaced with your actual hostnames/IP addresses.
$schoolComputers = @(
    "tp-1a201-01.enst.fr",
    "tp-1a201-03.enst.fr"
)

# Remote directory on the Linux machines where the JAR is located
# This will resolve to ~/my_java_app
$remoteDirRelative = "my_java_app"

# Name of the JAR file on the remote machine
$remoteJarName = "atlas-all.jar"

# --- Script Logic ---

Write-Host "Starting parallel JAR execution process. New windows will open for each machine."
Write-Host "You will be prompted for the SSH password in each new window."

foreach ($remoteHost in $schoolComputers) {
    $username = $commonUsername # Use the common username for each connection

    # Construct the command that `java -jar` will execute on the remote Linux machine.
    # The `~` is a literal for SSH, but needs to be escaped with a backtick for PowerShell if it were part of a drive specifier.
    # Here, it's fine as `~/$remoteDirRelative/$remoteJarName` will be passed correctly to ssh.
    $remoteCommandForSsh = "java -jar ~/$remoteDirRelative/$remoteJarName"

    # Construct the full SSH command string that the *new* PowerShell window will run.
    # This string needs to be correctly quoted so that the `ssh` command and its arguments
    # are passed as a single unit to the new PowerShell instance.
    # Example: ssh "user@host" "java -jar /path/to/jar"
    # We use backticks to escape the inner double quotes for the new PowerShell's parsing.
    $sshCommandForNewWindow = "ssh `"$username@$remoteHost`" `"$remoteCommandForSsh`""

    # The complete command string that the new PowerShell window will execute.
    # This string is passed as the `-Command` argument to `powershell.exe`.
    # It includes debugging output and a pause at the end.
    # Changed ReadKey to Read-Host for better compatibility in new windows.
    $newWindowCommand = @"
Write-Host '--- Connecting to $username@$remoteHost ---'
Write-Host 'Attempting to execute SSH command:'
Write-Host '$sshCommandForNewWindow'
Write-Host '--- SSH Output Below ---'
& $sshCommandForNewWindow # The '&' (call operator) ensures the string is executed as a command
Write-Host '--- SSH Output End ---'
Write-Host '`n--- Press Enter to close this window ---'
Read-Host | Out-Null # Pauses the window until Enter is pressed
"@

    Write-Host "Launching new window for $username@$remoteHost..."

    try {
        # Start a new PowerShell process (window) for each SSH connection.
        # The `-ArgumentList` is used to pass the `-NoExit` and `-Command` parameters
        # along with the constructed `$newWindowCommand` string.
        Start-Process powershell.exe -ArgumentList "-NoExit", "-Command", $newWindowCommand -WindowStyle Normal -WorkingDirectory $PSScriptRoot -PassThru | Out-Null
        # -WindowStyle Normal: Ensures the window is visible.
        # -WorkingDirectory $PSScriptRoot: Sets the working directory of the new process to the script's directory.
        # -PassThru | Out-Null: Prevents Start-Process from returning process objects to the current console.

    } catch {
        Write-Error "An error occurred while launching window for $username@$remoteHost : $($_.Exception.Message)"
    }
}

Write-Host "`n--- All execution windows launched. Check individual windows for results. ---"
