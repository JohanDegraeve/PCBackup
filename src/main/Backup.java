package main;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.AFileOrAFolder;
import model.AFolder;
import model.CommandLineArguments;
import model.Constants;
import utilities.CreateFullBackup;
import utilities.CreateSubFolder;
import utilities.FileAndFolderUtilities;
import utilities.ListBackupsInFolder;
import utilities.Logger;
import utilities.OtherUtilities;
import utilities.WriteToFile;

public class Backup {

	public static void backup() {
		
        CommandLineArguments commandLineArguments = CommandLineArguments.getInstance();
        
        String sourcesubfolder = "2023-12-06 18;24;41 (Full)";
        
        String destinationsubfolder = "2023-12-11 08;39;31 (Incrementeel)";
        
		/**
		 * where to find the source files
		 */
        Path sourceFolderPath = Paths.get(commandLineArguments.source).resolve(sourcesubfolder);

        /**
         * main path for backup, this is the backup folder path without the specific folder (ie without '2023-12-06 18;24;41 (Full)' or anything like that)
         */
        Path destinationFolderPath = Paths.get(commandLineArguments.source).resolve(destinationsubfolder);

        /**
         * path to previous most recent backup, either Full or Incremental. 
         */
    	Path mostRecentBackupPath = null;
    	
    	
    	// first read the json file from previous backup, ie source
        AFileOrAFolder listOfFilesAndFoldersInSource = FileAndFolderUtilities.fromFolderlistDotJsonToAFileOrAFolder(sourceFolderPath.resolve("folderlist.json"));
        
        // now create a json by analysing the destination folder
        AFolder listOfFilesAndFoldersInDest = new AFolder(sourceFolderPath.toString(), "");
        
        try {
        	
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(destinationFolderPath)) {
            	
                for (Path path : directoryStream) {
                	if (!(Files.isDirectory(path))) {
                		// check if the file is in the list of files to exclude, example .DS_Store
                		if (commandLineArguments.excludedFiles.contains(path.getFileName().toString())) {
                    		continue;
                		}
                		// check if the file is of format .849C9593-D756-4E56-8D6E-42412F2A707B seems a Microsoft hidden file
                		if (OtherUtilities.fileNeedsToBeIgnored(path.getFileName().toString())) {
                			continue;
                		}
                	}
                	listOfFilesAndFoldersInDest.getFileOrFolderList().add(FileAndFolderUtilities.createAFileOrAFolder(path, destinationsubfolder, commandLineArguments.excludedFiles));                	
                }
                
            }

        } catch (IOException e) {
            e.printStackTrace();
            Logger.log("Exception in main, while creating list of folders");
            Logger.log(e.toString());
            System.exit(1);
        }
        
        // now we should see what is in source but not in dest, add this and use backupfolder = previous one
        FileAndFolderUtilities.compareAndUpdate(listOfFilesAndFoldersInSource, listOfFilesAndFoldersInDest, null, sourceFolderPath, new ArrayList<String>(), destinationsubfolder);
        
        /**
    	 * needed for json encoding
    	 */
    	ObjectMapper objectMapper = new ObjectMapper();
    	
    	String destFolderToJson = "";
    	
        try {
        	
        	// json encoding of listOfFilesAndFoldersInPreviousBackupFolder which is now the new backup
        	destFolderToJson = objectMapper.writeValueAsString(listOfFilesAndFoldersInDest);
        	
        } catch (IOException e) {
        	e.printStackTrace();
            Logger.log("Exception in main, while creating json for listOfFilesAndFoldersInPreviousBackupFolder");
            Logger.log(e.toString());
            System.exit(1);
        }

		WriteToFile.writeToFile(destFolderToJson, "c:\\temp" + File.separator + "folderlist.json");

		
        System.out.println("hello");
        /*//if option is F, then create full backup
        if (commandLineArguments.fullBackup) {
            CreateFullBackup.createFullBackup(listOfFilesAndFoldersInSourceFolder, sourceFolderPath, destinationFolderPathSubFolder);
        } else {
        	        	
            // convert folderlist.json in mostrecent backup path to AFileOrAFolder
            AFileOrAFolder listOfFilesAndFoldersInPreviousBackupFolder = FileAndFolderUtilities.fromFolderlistDotJsonToAFileOrAFolder(mostRecentBackupPath.resolve("folderlist.json"));
            
            // we know for sure that both listOfFilesAndFoldersInSourceFolder and listOfFilesAndFoldersInPreviousBackupFolder are instance of AFolder
            // let's check anyway
            if (!(listOfFilesAndFoldersInSourceFolder instanceof AFolder)) {Logger.log("listOfFilesAndFoldersInSourceFolder is not an instance of AFolder");System.exit(1);} 
            if (!(listOfFilesAndFoldersInPreviousBackupFolder instanceof AFolder)) {Logger.log("listOfFilesAndFoldersInPreviousBackupFolder is not an instance of AFolder");System.exit(1);}
            // set the name of the first folder to "", because this may be the original main folder name which we don't need
            listOfFilesAndFoldersInSourceFolder.setName("");
            listOfFilesAndFoldersInPreviousBackupFolder.setName("");
            FileAndFolderUtilities.compareAndUpdate(listOfFilesAndFoldersInSourceFolder, listOfFilesAndFoldersInPreviousBackupFolder, sourceFolderPath, destinationFolderPathSubFolder, new ArrayList<String>(), backupfoldername);
            
            /**
        	 * needed for json encoding
        	 */
        	/*ObjectMapper objectMapper = new ObjectMapper();
        	
        	String destFolderToJson = "";
        	
            try {
            	
            	// json encoding of listOfFilesAndFoldersInPreviousBackupFolder which is now the new backup
            	destFolderToJson = objectMapper.writeValueAsString(listOfFilesAndFoldersInPreviousBackupFolder);
            	
            } catch (IOException e) {
            	e.printStackTrace();
                Logger.log("Exception in main, while creating json for listOfFilesAndFoldersInPreviousBackupFolder");
                Logger.log(e.toString());
                System.exit(1);
            }

            // now write the file folderlist.json to the backup folder
    		// first write the json file to destination folder
    		WriteToFile.writeToFile(destFolderToJson, destinationFolderPathSubFolder.toString() + File.separator + "folderlist.json");
            
            System.out.println("Backup finished, see " + destinationFolderPathSubFolder.toString());

        }*/
        
        

	}
	
}
