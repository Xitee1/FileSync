# FileSync
Sync your files with all of your Minecraft servers!

Download on SpigotMC is coming soon (when the plugin is finished)!

## Developer-API
You only have to enable the API option in the config.yml if you want to use "setAllowUpload()", "setGroups()" and "startSyncScheduler()".
The only thing this option does is to prevent that "setAllowUpload()", "setGroups()" and "startSyncScheduler()" gets executed when the plugin gets enabled.

If you use the Sync-System:
You have to set the groups if you want to use the startSyncScheduler() method.
Also, you don't have to use the startSyncScheduler method. You can also do this scheduler by yourself.
The syncScheduler just executes syncFiles(group); for each group.

```java
// Manage the Sync-System
FileSyncManager.setAllowUpload(false); // Allow/Disallow this server to upload files
FileSyncManager.setGroups(ArrayList<String>); // Set all groups
FileSyncManager.startSyncScheduler(seconds); // Start the SyncScheduler (not needed)
FileSyncManager.syncFiles(group); // Sync all files of a group
FileSyncManager.syncFiles(group, group); // Sync a specific file

// Manage a specific file
FileSyncManager fsm = new FileSyncManager(group, path);
fsm.writeFile(); // Add a new file to sync
fsm.setCommands(ArrayList<String>); // Set the commands
fsm.getCommands(); // Returns the commands
fsm.fileExists(); // Returns if the file exists (in the MySQL database)
fsm.getLastModified(); // Returns the last modified from the file in the MySQL database
fsm.readFile(); // Forces to read the file from the MySQL database and write it to the server (Use FileSyncManager.syncFiles() if you don't want to force override)
fsm.deleteFile(); // Removes the file from sync list (only deletes in database, no files on disk will be deleted)
```
