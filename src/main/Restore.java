package main;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;

import model.AFile;
import model.AFileOrAFolder;
import model.AFolder;
import model.CommandLineArguments;
import model.Constants;
import utilities.FileAndFolderUtilities;
import utilities.ListBackupsInFolder;
import utilities.Logger;
import utilities.PathUtilities;

public class Restore {

	public static void restore() {
		

        CommandLineArguments commandLineArguments = CommandLineArguments.getInstance();
        
		/**
		 * where to find the backup files, this is the backup folder path without the specific folder (ie without '2023-12-06 18;24;41 (Full)' or anything like that)
		 */
        Path sourceFolderPath = Paths.get(commandLineArguments.source);
        
        /**
         * where to restore the files
         */
        Path destinationFolderPath = Paths.get(commandLineArguments.destination);

        try {
        	
			String latestBackupFolderName = ListBackupsInFolder.getMostRecentBackup(sourceFolderPath, commandLineArguments.restoreDate);
			
			if (latestBackupFolderName == null) {
                System.out.println("No backups are found that were created before " + (new SimpleDateFormat(Constants.restoreDateFormat)).format(commandLineArguments.restoreDate));
                Logger.log("No backups are found that were created before " + (new SimpleDateFormat(Constants.restoreDateFormat)).format(commandLineArguments.restoreDate));
                System.exit(1);
			}
			
			Logger.log("Found backup " + latestBackupFolderName + " created before " + (new SimpleDateFormat(Constants.restoreDateFormat).format(commandLineArguments.restoreDate)));
			
			// get the file folderlist.json as AFileOrAFolder
			AFileOrAFolder listOfFilesAndFoldersInLastBackup = FileAndFolderUtilities.fromFolderlistDotJsonToAFileOrAFolder(sourceFolderPath.resolve(latestBackupFolderName).resolve("folderlist.json"));
			
			if (listOfFilesAndFoldersInLastBackup instanceof AFolder) {
				
		    	// if subfolder is specified, then we need to search within folderToBackup for an instance of AFileOrAFolder that matches that subfolder
		    	AFolder folderToStart =  getSubFolderAsAFolder((AFolder)listOfFilesAndFoldersInLastBackup, Paths.get(commandLineArguments.subfolderToRestore));

				restore(folderToStart, destinationFolderPath, sourceFolderPath, Paths.get(commandLineArguments.subfolderToRestore));
				
			} else {
				Logger.log("First element in folderlist.json is not a folder, looks like a coding error");
	            System.exit(1);
			}
			
			System.out.println("Restore finished, see " + destinationFolderPath.toString());
			
		} catch (IOException e) {
			Logger.log("Exception in restore");
            Logger.log(e.toString());
            System.exit(1);
		}
        
	}
	
	/**
	 * reads folderToBackup, goes through the folders and files one by one, and reads from the correct original backup and restores
	 * @param folderToBackup instance of AFolder to backup
	 * @param destinationFolder where to copy to, this is for example c:\restorefolder. destinationFolder is an absolute Path
	 * @param sourceBackupRootFolder this is for example c:\backupfolder without the name of the incremental or full folder. sourceBackupRootFolder is an absolute Path
	 * @param subfolder within the folderToBack that is being restored, will also be used as subfolder in the sourceBackupRootFolder where to find the original file, subfolder is a relative path
	 */
    private static void restore(AFolder folderToBackup, Path destinationFolder, Path sourceBackupRootFolder, Path subfolder) {
    	
    	// That folder doesn't exist yet in the destinationFolder
    	// let's create it but check anyway
    	if (!Files.exists(destinationFolder.resolve(subfolder))) {
    		try {
				Files.createDirectories(destinationFolder.resolve(subfolder));
			} catch (IOException e) {
				e.printStackTrace();
				Logger.log("Exception in restore, while creating the directory " + destinationFolder.resolve(subfolder).toString());
	            Logger.log(e.toString());
	            System.exit(1);
			}
    	}
    	
    	for (AFileOrAFolder sourceItem : folderToBackup.getFileOrFolderList()) {
    		
    		if (sourceItem instanceof AFolder) {
    			
    			Path folderToCreate = destinationFolder.resolve(subfolder).resolve(sourceItem.getName());
    			
    			// create the folder in the destination and call restore for the folder recursively
    			try {
    				
					Files.createDirectories(folderToCreate);
					
					Restore.restore((AFolder)sourceItem, destinationFolder, sourceBackupRootFolder, subfolder.resolve(sourceItem.getName()));
					
				} catch (IOException e) {
					e.printStackTrace();
					Logger.log("Exception in restore, while creating the directory " + folderToCreate.getFileName().toString());
		            Logger.log(e.toString());
		            System.exit(1);
				}
    			
    		} else {
    			
    			Path sourceToCopy = sourceBackupRootFolder.resolve(sourceItem.getPathToBackup()).resolve(subfolder).resolve(sourceItem.getName());
    			Path destination = destinationFolder.resolve(subfolder).resolve(sourceItem.getName());
    			
    			try {
    				
					Files.copy(sourceToCopy, destination, StandardCopyOption.COPY_ATTRIBUTES);
					
				} catch (IOException e) {
					Logger.log("Exception in restore, while copying the file " + sourceToCopy.toString() + " to " + destination.toString());
		            Logger.log(e.toString());
		            System.exit(1);
				}
    			
    		}
    		
    	}
    }
    
    /**
     * if user specified a foldertorestore, then we need to find the corresponding instance of AFileOrAFolder within folderToSearchIn
     * @param folderToSearchIn
     * @param subfolder
     * @return the folder
     */
    private static AFolder getSubFolderAsAFolder(AFolder folderToSearchIn, Path subfolder) {
    	
    	// split subfolder, because it can be a concatentation of paths, in other words split by "/" or "\" (depending on os)
    	Path[] subfolders = PathUtilities.splitPath(subfolder);
    	int subfoldersCounter = 0;
    	
    	AFolder deeperAFileOrAFolder = folderToSearchIn;
    	
    	while (subfoldersCounter < subfolders.length) {

    		// if subfolder is an empty string, then actually no subfolder is specified
    		// in this case, the loop exist immediately
    		if (subfolders[subfoldersCounter].toString().length() == 0) {
    			break;
    		}
    		
        	// find the instance of AFileOrAFolder in folderToSearchIn list (ie in the list of files and folders contained within folderToSearchIn)
        	AFileOrAFolder folderFound = FileAndFolderUtilities.findMatchingItem(new AFolder(subfolders[subfoldersCounter].toString(), ""), deeperAFileOrAFolder.getFileOrFolderList());
        	
        	if (folderFound == null) {
        		Logger.log("in getSubFolderAsAFolder. foldertorestore " + subfolder + " does not exist in backup.");
                System.exit(1);
        	}
        	
        	// folderFound must be a directory, if not, it's a file, and user made some mistake
        	if (folderFound instanceof AFile) {
        		Logger.log("in getSubFolderAsAFolder. foldertorestore " + subfolder + " was given as argument. But this seems to be a file, not a folder");
                System.exit(1);
        	}

        	deeperAFileOrAFolder = (AFolder)folderFound;

        	subfoldersCounter++;
        	
    	}
    	
    	return deeperAFileOrAFolder;
    	
    	
    }
	
}
