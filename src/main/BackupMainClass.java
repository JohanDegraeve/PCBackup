package main;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.AFileOrAFolder;
import model.AFolder;
import model.CommandLineArguments;
import model.CommandLineArguments.ArgumentName;
import utilities.CreateBackup;
import utilities.CreateSubFolder;
import utilities.FileAndFolderUtilities;
import utilities.Logger;

public class BackupMainClass {

    public static void main(String[] args) {
    	
        CommandLineArguments.getInstance(args);
        
		/**
		 * where to find the source files
		 */
        Path sourceFolderPath = Paths.get(CommandLineArguments.getInstance().getArgumentValue(ArgumentName.source));

        // create backupfoldername for the backup, this folder will be created within destination folder
        // example for full backup : '2023-12-06 18;24;41 (Full)'
        // example for incremental backup : '2023-12-28 17;07;13 (Incremental)'
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH;mm;ss");
        /**
         * name of the folder within destinationFolderPath, where backup will be placed<br>
         * So if we have destinationFolderPath, then destinationFolderPath/backupfoldername will contain the copied files and folders
         */
		String backupfoldername = dateFormat.format(new Date());
        if (CommandLineArguments.getInstance().getArgumentValue(ArgumentName.type).equalsIgnoreCase("F")) {
        	backupfoldername = backupfoldername + " (Full)";
        } else {
        	backupfoldername = backupfoldername + " (Incremental)";
        }
        
        /**
         * where to write the backup, this includes the backupfoldername, like '2023-12-06 18;24;41 (Full)' or '2023-12-28 17;07;13 (Incremental)'
         */
        Path destinationFolderPath = CreateSubFolder.createSubFolder(CommandLineArguments.getInstance().getArgumentValue(ArgumentName.destination), backupfoldername);

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
                	listOfFilesAndFoldersInSourceFolder.getFileOrFolderList().add(FileAndFolderUtilities.createAFileOrAFolder(path, backupfoldername));                	
                }
                
            }

            // this sorts the list by name of the folders and files in  the directory, not the files in the folders within those folders
            //   (that's done in the function createAFileOrAFolder itself)
            Collections.sort(listOfFilesAndFoldersInSourceFolder.getFileOrFolderList(), (a, b) -> a.getName().compareTo(b.getName()));
            
            
        } catch (IOException e) {
            e.printStackTrace();
            Logger.log("Exception in main, while creating list of folders");
            Logger.log(e.toString());
            System.exit(1);
        }
        
        //if option is F, then create full backup
        if (CommandLineArguments.getInstance().getArgumentValue(ArgumentName.type).equalsIgnoreCase("F")) {
            CreateBackup.createFullBackup(listOfFilesAndFoldersInSourceFolder, sourceFolderPath, destinationFolderPath);
        } else {
        	
        	// incremental backup
        	
        	// TODO : find the previous incremental backup, or the last full backup
        	// now we do it with known backup
            String previousBackupJsonFile = "/Users/johandegraeve/Downloads/backup/2024-01-14 01;21;44 (Full)/folderlist.json";
            Path filePath = Paths.get(previousBackupJsonFile);

            // declare and init listOfFilesAndFoldersInPreviousBackupFolder
            // it's null, but we assume that it will be set to non nul value, or an exception will occur causing a crash
            AFileOrAFolder listOfFilesAndFoldersInPreviousBackupFolder = null;
            
            try {

                ObjectMapper objectMapper = new ObjectMapper();
                listOfFilesAndFoldersInPreviousBackupFolder = objectMapper.readValue(Files.readString(filePath, StandardCharsets.UTF_8), AFileOrAFolder.class);
                
            } catch (IOException e) {
                // Handle IOException (e.g., file not found or permission issues)
            	e.printStackTrace();
                Logger.log("Exception while reading previous backup");
                Logger.log(e.toString());
                System.exit(1);
            }
            
            FileAndFolderUtilities.compareAndUpdate(listOfFilesAndFoldersInSourceFolder, listOfFilesAndFoldersInPreviousBackupFolder);
            
            System.out.println("done");
            

        }
        
        
        
        
    }

}

