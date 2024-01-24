package main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
        
		/**
		 * where to find the source files
		 */
        Path sourceFolderPath = Paths.get(commandLineArguments.source);
        
        /**
         * main path for backup, this is the backup folder path without the specific folder (ie without '2023-12-06 18;24;41 (Full)' or anything like that)
         */
        Path destinationFolderPath = Paths.get(commandLineArguments.destination);

        /**
         * path to previous most recent backup, either Full or Incremental. 
         */
    	Path mostRecentBackupPath = null;
    	// Get it now, if it exists before we create the new backup folder
    	try {
    		mostRecentBackupPath = ListBackupsInFolder.getMostRecentBackup(destinationFolderPath);
		} catch (IOException e) {
			e.printStackTrace();
            Logger.log("Exception in main, while getting list of backups");
            Logger.log(e.toString());
            System.exit(1);
		}
    	if (mostRecentBackupPath == null && !commandLineArguments.fullBackup) {
    		Logger.log("In Main, mostRecentBackup is null and you're asking an incremental backup. check the destination folder. Maybe it's the first time you take a backup? and there's no backup yet? " + destinationFolderPath.toString()); 
    		System.exit(1);
    	} else if (mostRecentBackupPath != null) {
        	System.out.println("mostRecentBackupPath = " + mostRecentBackupPath.toString());
    	}


        // create backupfoldername for the backup, this folder will be created within destination folder
        // example for full backup : '2023-12-06 18;24;41 (Full)'
        // example for incremental backup : '2023-12-28 17;07;13 (Incremental)'
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH;mm;ss");
        /**
         * name of the folder within destinationFolderPath, where backup will be placed<br>
         * So if we have destinationFolderPath, then destinationFolderPath/backupfoldername will contain the copied files and folders
         */
		String backupfoldername = dateFormat.format(new Date());
        if (commandLineArguments.fullBackup) {
        	backupfoldername = backupfoldername + " (Full)";
        } else {
        	backupfoldername = backupfoldername + " (Incremental)";
        }
        
        /**
         * where to write the backup, this includes the backupfoldername, like '2023-12-06 18;24;41 (Full)' or '2023-12-28 17;07;13 (Incremental)'
         */
        Path destinationFolderPathSubFolder = CreateSubFolder.createSubFolder(commandLineArguments.destination, backupfoldername);

    	// first we make a list of files and folder in the sourceFolderPath,
        // for each file or folder we create an instance of AFileOrAFolder
        // Create a DirectoryStream to iterate over the contents of the sourceFolderPath
        // we create an instance of AFolder to hold the list of files and folders in the source
        //    although we don't really need the source folder
        //    this is is done because otherwise the json deserialisation doesn't work
        AFolder listOfFilesAndFoldersInSourceFolder = new AFolder(sourceFolderPath.toString(), "");
        
        try {
        	
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(sourceFolderPath)) {
            	
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
                	listOfFilesAndFoldersInSourceFolder.getFileOrFolderList().add(FileAndFolderUtilities.createAFileOrAFolder(path, backupfoldername, commandLineArguments.excludedFiles));                	
                }
                
            }

            // this sorts the list by name of the folders and files in  the directory, not the files in the folders within those folders
            //   (that's done in the function createAFileOrAFolder itself)
            //Collections.sort(listOfFilesAndFoldersInSourceFolder.getFileOrFolderList(), (a, b) -> a.getName().compareTo(b.getName()));
            
            
        } catch (IOException e) {
            e.printStackTrace();
            Logger.log("Exception in main, while creating list of folders");
            Logger.log(e.toString());
            System.exit(1);
        }
        
        //if option is F, then create full backup
        if (commandLineArguments.fullBackup) {
            CreateFullBackup.createFullBackup(listOfFilesAndFoldersInSourceFolder, sourceFolderPath, destinationFolderPathSubFolder);
        } else {
        	        	
        	// read folderlist.json of in most recent backup folder
            String previousBackupJsonFile = mostRecentBackupPath.toString() + "/folderlist.json";

            // declare and init listOfFilesAndFoldersInPreviousBackupFolder
            // it's null, but we assume that it will be set to non nul value, or an exception will occur causing a crash
            AFileOrAFolder listOfFilesAndFoldersInPreviousBackupFolder = null;
            
            try {

                ObjectMapper objectMapper = new ObjectMapper();
                listOfFilesAndFoldersInPreviousBackupFolder = objectMapper.readValue(Files.readString(Paths.get(previousBackupJsonFile), StandardCharsets.UTF_8), AFileOrAFolder.class);
                
            } catch (IOException e) {
                // Handle IOException (e.g., file not found or permission issues)
            	e.printStackTrace();
                Logger.log("Exception while reading previous backup");
                Logger.log(e.toString());
                System.exit(1);
            }
            
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
        	ObjectMapper objectMapper = new ObjectMapper();
        	
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

            
            System.out.println("done");
            

        }
        
        

	}
	
}