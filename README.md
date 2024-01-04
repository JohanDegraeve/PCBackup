# PCBackup
Backup, full and incremental.

# Backup
- Full backup copies all source files and source folders to another place/disk.
  - A full backup uses the same foldername as [Perfect Backup](https://www.perfect-backup.com/), so that it can also work on backups that were previously created by PerfectBackup
- Incremental Backup
  - create a new folder where the incremental backup will be stored
  - create a csv file in the root folder of the backup, which will keep a list of delete files and folders (deletedfilesandfolders-yyyy-mm-yy.csv)
  - copy all folders (not the files) from the previous incremental backup (if that exists). If there is no previous incremental, then it copies the folders from the previous full backup
  - Iterate through all folders and files in the source
    - folders that don't exist yet are created (ie folders that were added since the previous backup)
    - check the creation timestamp of each file in each folder in the source, and compare it to the timestamp of the latest version of that file. Means iterate through the folder in all incremental backups, if it's newer than the last creation date found , then the file is copied to the incremental backup
  - For incremental backup only, we need to know if files and folders have been deleted since the previous backup.
    - For each last full and each incremental backup after that last full backup, starting with the most recent full backup
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
   
# The implementation
- create a list that will hold all information for a backup (full or incremental) :
  - folder structure,
  - names of folders,
  - names of files in the folders,
  - timestamp of the file
- Define a generic, abstract class, named FileOrFolder. It has one attribute named "name". Two derived classes :
    - AFile : to hold a file , the name is in the parent class,
      - it also has a timestamp (AFile to distinguish from File)
      - and it has the name of the incremental backup, will be assigned to the incremental backup where the most recent version is stored
    - AFolder : to hold a folder, the name is in the parent class. it has a List of FileOrFolder, ie List<FileOrFolder>
- 
- If full backup is selected:
  - Copy all files and folders to the destination
  - Create an instance of FileOrFolder and initialize it to AFolder with the name of the destination folder, in which the List will contain the files and folders
  - Convert that to json, and store it in the destination folder
- If an incremental is selected:
  - start by importing the json from prevous full or incremental backup, deserialize it.
  - Then from that json, recreate the folder structure, just the folders, not yet the files in it, name it here previousstruct
  - Then iterate through the source, the first layer of files and folders and compare to previousstruct
  - Files or folders that are missing in the new destination => add them to previousstruct, also create them in the new incremental backup, files are copied, note the timestamp
  - Files that have a more recent timestamp in the source => copy the file to the new incremental backup, and write the new timestamp
  - Files or folders that are not anymore in the source => remove it from the previoussstruct, delete it from incremental backup (that would only be applicable to folders because files are no there), and write the deleted file or folder inthe file deletedfilesandfolders-yyyy-mm-yy.csv
  - export to json and add it in the incremental backup
 
When restoring : 
- Deserialze the json from the most recent incremental backup that is older than the requested restored date
- create the folder structure
- iterate through the files, find in which backup the file needs to be fetched and copy it

PROBABLY DON'T NEED the DELETESFILESANDFOLDERS ??
