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
  - then the json structure is created for the current status of the source, ie the app will go through the current files and folders, read the names of the files and folders and the timestamps of the files
  - A comparison is made of the two json structures
      - when a file is removed in the source, it is removed also in the destination json structure
      - when a file is added in the source, it is added also in the destination json structure, and also copied to the destination folder. The json structure field pathToBackup will have the name of the new incremental backup
      - when a file is modified in the source (meaning has a more recent modified timestamp than the one in the latest backup), then the same as done as for new files
      - when a folder is added in the source, it is added also in the destination json structure, but not necessarily created in the destination, not if it has no files
      - when a folder is removed in the source, it is removed in the destination json structure

     
# Restore
- The user specifies the folder to restore and the restore date + time. "folder to restore" is path starting from the source folder.
- Search the most recent incremental or full backup created before requested restore date + time 
- parse folderlist.json that is stored in that backup
- go through the json structure
    - find the specified source folder. If not found, there's no restore. 
    - each folder and subfolder is created in the restore
    - each file is copied form the backup, the field pathToBackup indicates in which backup the latest version is stored
   
# Usage

The app is launched with a command line interface.

*Mandatory arguments*:
  * --type: F for Full backup, I for incremental backup, R for restore, S for searching in backups.
  * --source:  the folder that you want to backup, the contents will be backed up
            Not used for RESTORE and SEARCH
  * --destination: folder where you want to backup to
            for BACKUP : folder where you want to backup to
            for RESTORE: the folder where your backup is stored and from where restore will happen, ie the folder where you previously backed up to
            for SEARCH:  the folder where your backup is stored and where the search will happen, ie the folder where you previously backed up to

*Optional arguments*:

  * --restoreto: only for restore. This is the foldername where you want to restore to  
  * --writesearchto: foldername where searchresults will be written to
            the file with the search results will be named searchresults.csv. If that file already exists, then it will be named for intance searchresults (1).txt
  * --overwrite: only for restore. If value = true then files that already exist in the destination will be overwritten. Default n (no)
  * --logfilefolder: location of the logfile, just the folder name, it must exist.
  * --excludedfilelist: list of file names to exclude, exact match is applied, case sensitive.
  * --excludedpathlist: list of folder names to exclude, these are subfolder names, exact match is applied, case sensitive.
  * --restoredate: mandatory if type arguments = R. Date and time for which restore should occur. Format yyyy-MM-dd-HH-mm-ss
  * --subfoldertorestore: The specific folder within source that needs to be restored, If the complete backup needs to be restored, then omit this argument or give an empty string (""). If a specific subfolder needs to be restored, then specify that folder here.
  * --foldernamemapping:<br>
        Sometimes SharePoint sync gives different foldernames to the main folder. Example sometimes the Documents folder is named "XDrive Documents", but on another PC it may be named "XDrive Documenten"<br>
          The app allows to do mapping of foldernames. This is only applicable to the initial folder, not the subfolders. It's a list of mappings. Example line:<br>
              XDrive Documents=XDrive Documenten<br>
              When doing a backup:<br>
              When a folder in the source is named "XDrive Documents", then the file folderlist.json will be named "XDrive Documenten" and also in the backup there will be a folder with name "XDrive Documenten"<br>
              So when we reuse a hard disk with backups taken on another PC, and the folder on that disk is named "XDrive Documenten", while on the new PC, where we do the backup, it's named "XDrive Documents", then we need to add the line 'XDrive Documents=XDrive <br>Documenten' in the file that is specified here.
              Then if we do a backup on that new PC, the app sees the folder XDrive Documents, then the file folderlist.json will contain XDrive Documenten.
  * --searchtext: the text to search for, mandatory in case type = S<br>
                   the searchtext is handled as a regular expression.<br>
                   Examples:<br>
                      - to search for files with 'Trident' or 'Jabra', the searchtext you specify here would be '\b(?:Trident|Jabra)\b'<br>
                      - to search for files with 'Trident' and 'Jabra', the searchtext you specify here would be '(?=.*\bTrident\b)(?=.*\bJabra\b)'<br>
  * --startsearchdate: when searching, only search in backups created after this date. Format = yyyy-MM-dd-HH-mm-ss. Default = 1 1 1970 
  * --endsearchdate: when searching, only search in backups created after before or at this date. Format = yyyy-MM-dd-HH-mm-ss. Default = now
