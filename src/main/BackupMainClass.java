package main;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.AFileOrAFolder;
import model.AFolder;
import model.CommandLineArguments;
import model.CommandLineArguments.ArgumentName;
import utilities.CreateBackup;
import utilities.CreateSubFolder;
import utilities.FileAndFolderUtilities;

public class BackupMainClass {

    public static void main(String[] args) {
    	
        CommandLineArguments.getInstance(args);
        
		// Specify the path to the source folder
        Path sourceFolderPath = Paths.get(CommandLineArguments.getInstance().getArgumentValue(ArgumentName.source));

        // create backupfoldername for the backup, this folder will be created within destination folder
        // example for full backup : '2023-12-06 18;24;41 (Full)'
        // example for incremental backup : '2023-12-28 17;07;13 (Incremental)'
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH;mm;ss");
		String backupfoldername = dateFormat.format(new Date());
        if (CommandLineArguments.getInstance().getArgumentValue(ArgumentName.type).equalsIgnoreCase("F")) {
        	backupfoldername = backupfoldername + " (Full)";
        } else {
        	backupfoldername = backupfoldername + " (Incremental)";
        }
        
        Path destinationFolderPath = CreateSubFolder.createSubFolder(CommandLineArguments.getInstance().getArgumentValue(ArgumentName.destination), backupfoldername);

    	// first we make a list of files and folder in the sourceFolderPath,
        // for each file or folder we create an instance of AFileOrAFolder
        // Create a DirectoryStream to iterate over the contents of the sourceFolderPath
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
            
            CreateBackup.createFullBackup(listOfFilesAndFoldersInSourceFolder, sourceFolderPath, destinationFolderPath);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // as test , read back a json file
        String filePathString = "/Users/johandegraeve/Downloads/backup/" + backupfoldername + "/folderlist.json";
        Path filePath = Paths.get(filePathString);

        try {
            // Read the file contents as a string
            String fileContent = Files.readString(filePath, StandardCharsets.UTF_8);

            ObjectMapper objectMapper = new ObjectMapper();
            AFileOrAFolder aFileOrAFolder = objectMapper.readValue(fileContent, AFileOrAFolder.class);
            System.out.println("ready to veiw the prased json");
        } catch (IOException e) {
            // Handle IOException (e.g., file not found or permission issues)
            e.printStackTrace();
        }
        
        
        
    }

}

