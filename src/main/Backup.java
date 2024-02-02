package main;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.AFileOrAFolder;
import model.AFolder;
import model.CommandLineArguments;
import utilities.FileAndFolderUtilities;
import utilities.Logger;
import utilities.OtherUtilities;
import utilities.WriteToFile;

public class Backup {

	public static void backup() {
		
		/**
		 * modified version to allow reconstruction of folderlist.json<br>
		 * Use type F if goal is to create a json file from a full backup<br>
		 * Use type I if goal is to create a json file from an incremental backup
		 */
		
		// not all commands are applicable
		// but I did use this:
		// --destination=E:\SharePointBackupDisk1 --source=E:\SharePointBackupDisk1 --excludedfilelist="C:\temp\excludedfiles.txt" --type=I
		
        CommandLineArguments commandLineArguments = CommandLineArguments.getInstance();
        
        /**
         * create here the subfolder name, with either the full or incremental backup<br>
         * In case a complete folderlist.json of a full backup is taken, then this folder does not contain a folderlist.json<br>
         * If the goal is to create a folderlist.json for an incremental backup, then this folder already has a folderlist.json<br> 
         */
        String sourcesubfolder = "2024-02-02 11;35;29 (Full)";
        
        /**
         * needed if goal is to create folderlist.json for an incremental backup<br>
         * Renamed
         */
        String destinationsubfolder = "2023-12-13 09;30;04 (Incremental)";
        
		/**
		 * source path + sourcesubfolder
		 */
        Path sourceFolderPath = Paths.get(commandLineArguments.source).resolve(sourcesubfolder);

        /**
         * source path + destinationsubfolder
         */
        Path destinationFolderPath = Paths.get(commandLineArguments.source).resolve(destinationsubfolder);

        if (commandLineArguments.fullBackup) {
        	
        	AFileOrAFolder thething = createJsonStructure(commandLineArguments, sourceFolderPath, sourcesubfolder);
        	
        	WriteToFile.writeToFile(fromAFileOrAFolderToJsonString(thething), "c:\\temp" + File.separator + "folderlist.json");
        	
        } else {

        	/**
        	 * TO SIMPLIFY
        	 */
        	
        	// first read the json file from previous backup, meaning the source
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

        }
        

	}
	
	private static AFileOrAFolder createJsonStructure(CommandLineArguments commandLineArguments, Path folderPath, String pathToBackupToUse) {
		
        // now create a json by analysing the folder
        AFolder listOfFilesAndFoldersInDest = new AFolder(folderPath.toString(), "");
        
        try {
        	
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folderPath)) {
            	
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
                	listOfFilesAndFoldersInDest.getFileOrFolderList().add(FileAndFolderUtilities.createAFileOrAFolder(path, pathToBackupToUse, commandLineArguments.excludedFiles));                	
                }
                
            }

        } catch (IOException e) {
            e.printStackTrace();
            Logger.log("Exception in main, while creating list of folders");
            Logger.log(e.toString());
            System.exit(1);
        }
        
        return listOfFilesAndFoldersInDest;

	}
	
	private static String fromAFileOrAFolderToJsonString (AFileOrAFolder aFileOrAFolder) {
		
        /**
    	 * needed for json encoding
    	 */
    	ObjectMapper objectMapper = new ObjectMapper();
    	
    	String destFolderToJson = "";
    	
        try {
        	
        	// json encoding of listOfFilesAndFoldersInPreviousBackupFolder which is now the new backup
        	destFolderToJson = objectMapper.writeValueAsString(aFileOrAFolder);
        	
        } catch (IOException e) {
        	e.printStackTrace();
            Logger.log("Exception in main, while creating json for listOfFilesAndFoldersInPreviousBackupFolder");
            Logger.log(e.toString());
            System.exit(1);
        }

        return destFolderToJson;
		
	}
	
}
