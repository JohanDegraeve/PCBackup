package main;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;

import model.AFileOrAFolder;
import model.AFolder;
import model.CommandLineArguments;
import model.Constants;
import utilities.FileAndFolderUtilities;
import utilities.ListBackupsInFolder;
import utilities.Logger;

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
			
			// get the file folderlist.json as AFileOrAFolder
			AFileOrAFolder listOfFilesAndFoldersInLastBackup = FileAndFolderUtilities.fromFolderlistDotJsonToAFileOrAFolder(sourceFolderPath.resolve(latestBackupFolderName).resolve("folderlist.json"));
			
			if (listOfFilesAndFoldersInLastBackup instanceof AFolder) {
				
				restore((AFolder)listOfFilesAndFoldersInLastBackup, destinationFolderPath, sourceFolderPath, Paths.get(""));
				
			} else {
				Logger.log("First element in folderlist.json is not a folder, looks like a coding error");
	            System.exit(1);
			}
			
			System.out.println(sourceFolderPath.toString());

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
	 * @param subfolder within the folderToBack that is being restored, will also be used as subfolder in the sourceBackupRootFolder where  to find the original file, subfolder is a relative path
	 */
    private static void restore(AFolder folderToBackup, Path destinationFolder, Path sourceBackupRootFolder, Path subfolder) {
    	
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
    			
    			Path sourceToCopy = sourceBackupRootFolder.resolve(sourceItem.getPathToBackup()).resolve(sourceItem.getName());
    			Path destination = destinationFolder.resolve(subfolder).resolve(sourceItem.getName());
    			
    			try {
    				
					Files.copy(sourceToCopy, destination, StandardCopyOption.COPY_ATTRIBUTES);
					
				} catch (IOException e) {
					Logger.log("Exception in restore, while copy the file " + sourceToCopy.toString() + " to " + destination.toString());
		            Logger.log(e.toString());
		            System.exit(1);
				}
    			
    		}
    		
    	}
    }
    
	

	
}
