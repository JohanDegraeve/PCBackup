# PCBackup
Backup, full and incremental.

# Backup
- Full backup copies all source files and source folders to another place/disk.
  - A full backup uses the same foldername as [Perfect Backup](https://www.perfect-backup.com/), so that it can also work on backups that were previously created by PerfectBackup
- Incremental Backup
  - create a new folder where the incremental backup will be stored
  - create a csv file in the root folder of the backup, which will keep a list of delete files and folders (deletedfilesandfolders-yyyy-mm-yy.csv)
  - copy all folders from the previous incremental backup (if that exists). If there is no previous incremental or full backup, then it copies the folders from the first full backup
  - Iterate through all folders and files in the source
    - folders that don't exist yet are created (ie folders that were added since the previous backup)
    - check the creation timestamp of each file in each folder in the source. If it's newer than the creation date of the previous backup, then the file is copied to the incremental backup
  - For incremental backup only, we need to know if files and folders have been deleted since the previous backup.
    - For each backup full and incremental, starting with the most recent full backup
      - Iterate through all folders and files in the new incremental backup
      - If a folder or file does not exist anymore in the source
        - delete the folder or file in the new incremental backup, if it's a folder, then inclusive subfolders (there shouldn't be any files in it, this is checked and if there are files, then skip and log an error)
        - add the deletion in the csv file (deletedfiles-yyyy-mm-yy.csv)
     
# Restore
- The user specifies the folder to restore (can be complete or a subfolder) and the date + time (yyyy-mm-dd HH:mm:ss) ==> restore_folder and restore_time
- Search the most recent full backup created before restore_time (if restore_time < earlist full backup then give an error and stop)
- Copy the restore_folder from that full backup
- for each incremental backup after that full backup, and earlier than restore_time, starting with the oldest
  - iterate through all folders in the incremental backup. A folder that doesn't exist yet in the restore_folder => copy it completely, inclusive subfolders and files
  - if the folder exists, interate again through the folders and do the same, recusively
  - if a file is found that is not yet in the restore_folder, copy it
  - iterate through the file (deletedfilesandfolders-yyyy-mm-yy.csv) (if it doens't exist then skip that step)
    - each folder and file found in that file, delete it from the restore_foler
   
    
