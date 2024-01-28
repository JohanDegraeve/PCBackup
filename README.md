# PCBackup
Java app to Backup and Restore, full and incremental.

# Backup
- Full-backup copies all source files and source folders to another place/disk and creates the file folderlist.json
  - The app creates a subfolder in the destination, example "2024-01-19 00;03;00 (Full)" which indicates the date and time the backup was taken
- for each backup (full or incremental) creates a json file in the root folder of the backup named folderlist.json, (example : https://github.com/JohanDegraeve/PCBackup/blob/main/folderlist.json)
  - The provided JSON file represents a hierarchical structure of folders and files, serving as a backup configuration. At the top level, the "type" field indicates that it is a folder, denoted by "afolder." Each folder has a "name" and "pathToBackup" field, representing the name of the folder and the backup path, respectively. The "fileOrFolderList" field contains an array of objects, representing either files and/or nested folders.
  - Files are denoted by the "type" field as "afile," and each file object includes "name," "pathToBackup," and "ts" (timestamp) fields. The "name" field signifies the file's name, "pathToBackup" indicates the backup path, and "ts" represents the timestamp of the file.
  - "pathToBackup" is just a subfoldername, example "2024-01-19 00;26;57 (Incremental)" and tells us where, in which previous full or incremental bakcup, the latest version of the file is stored.
- Incremental Backup
  - creates a new folder where the incremental backup is stored, example "2024-01-19 00;26;57 (Incremental)"
  - Each time an incremental backup is taken, then first the file folderlist.json from the previous backup (either full or incremental) is copied and parsed.
  - then the json structure is created for the current status off the source, ie the app will go through the current files and folders, read the names of the files and folders and the timestamps of the files
  - A comparison is made of the two json structures
      - when a file is removed in the source, it is removed also in the destination json structure
      - when a file is added in the source, it is added also in the destination json structure, and also copied to the destination folder. The json structure field pathToBackup will have the name of the new incremental backup
      - when a file is modified in the source (meaning has a more recent modified timestamp than the one in the latest backup), then the same as done as for new files
      - when a folder is added in the source, it is added also in the destination json structure, but not necessarily created in the destination, not if it has no files
      - when a folder is removed in the source, it is removed in the destination json structure

     
# Restore
- The user specifies the folder to restore (can be complete or a subfolder) and the date + time
- Search the most recent incremental or full backup created before requested restore time 
- parse folderlist.json that is stored in that backup
- go through the json structure
    - each folder and subfolder is created in the restore
    - each file is copied form the backup, the field pathToBackup indicates in which backup the latest version is stored
   
# Usage

The app is launched with a command line interface.

*Mandatory arguments:*
  - --type: F for Full backup, I for incremental backup, R for restore.
  - --source:
    - for BACKUP : the folder that you want to backup, the contents will be backedup
    - for RESTORE: the folder where your backup is stored
  - --destination:
    - for BACKUP : folder where you want to backup to
    - for RESTORE: folder to where you want to restore (better not to take the same as the original source, maybe)

*Optional arguments:*
  - --logfilefolder: location of the logfile, just the folder name, it must exist.
  - --excludedfilelist: filenames, with full path, that contains list of filenames that should be ignored, ie not added to the folderlist.json and not copied in backups.
  - --restoredate: mandatory if type arguments = R. Date and time for which restore should occur. Format yyyy-MM-dd-HH-mm-ss
  - --subfoldertorestore: The specific folder within source that needs to be restored, If the complete backup needs to be restored, then omit this argument, If a specific subfolder needs to be restored, then specify that folder here.


Example Incremental backup

java -jar --destination=/Users/johandegraeve/Downloads/backup --source=/Users/johandegraeve/Downloads/temp/test --excludedfilelist="/Users/johandegraeve/OneDrive/Eclipse projects/PCBackup/excludedfiles.txt" --type=I

Example Restore

--source=/Users/johandegraeve/Downloads/backup --destination=/Users/johandegraeve/Downloads/temp/restore --excludedfilelist="/Users/johandegraeve/OneDrive/Eclipse projects/PCBackup/excludedfiles.txt" --type=R --restoredate=2024-02-20-00-00-00
