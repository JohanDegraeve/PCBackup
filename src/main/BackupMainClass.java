package main;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.AFile;
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

    	// first we make a list of files and folder in the sourceFolderPath, as instance of AFileOrAFolder
        // Create a DirectoryStream to iterate over the contents of the folder
        List<AFileOrAFolder> listOfFilesAndFoldersInSourceFolder = new ArrayList<>();
        
        try {
        	
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(sourceFolderPath)) {
            	
                for (Path path : directoryStream) {
                	
                	String fileOrFolderNameWithoutFullPath = path.toString().substring(folderOrStringPath.toString().length() + 1);
                	
                	if (Files.isDirectory(path)) {
                		
                		listOfFilesAndFoldersInSourceFolder.add(new AFolder(fileOrFolderNameWithoutFullPath));

                		// the name used in aFolder is now a full path
                		// this is not good because it will take a lot of space in the resulting json file, so we remove the original path
                		
                		aFolder.setName(fileOrFolderNameWithoutFullPath);
                		
                		//System.out.println("adding a folder with name " + fileOrFolderNameWithoutFullPath);
                		
                		returnValue.addFileOrFolder(aFolder);
                		
                	} else {
                		amountoffiles++;
                		//System.out.println("adding a file with name " + fileOrFolderNameWithoutFullPath + " and lastmodifedtimestamp " + Files.getLastModifiedTime(path).toString());
                		
                		returnValue.addFileOrFolder(new AFile(fileOrFolderNameWithoutFullPath, Files.getLastModifiedTime(path).toMillis(), incrementalBackupFolderName));
                		
                	}
                }
                
            }

        	
            /**
             * source folder stored in AFileOrAFolder
             */
        	AFileOrAFolder aFileOrAFolderSourceFolder = FileAndFolderUtilities.createAFileOrAFolder(sourceFolderPath.toString(), backupfoldername);
        	
            CreateBackup.createFullBackup(aFileOrAFolderSourceFolder, sourceFolderPath, destinationFolderPath);
            
            System.out.println("amount of files   = " + FileAndFolderUtilities.amountoffiles);
            System.out.println("amount of folders = " + FileAndFolderUtilities.amountoffolders);
            //System.out.println("json              = " + sourceFolderToJson.length());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

