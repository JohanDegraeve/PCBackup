package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        
        BufferedWriter writer = null;
        
        try {
        	writer = new BufferedWriter(new FileWriter("c:\\temp\\missing.txt")) ;
        	
            
        } catch (IOException e) {
            e.printStackTrace();
        }
		/**
		 * where to find the source files
		 */
        Path sourceFolderPath = Paths.get(commandLineArguments.source);
        
        List<Path> allBackupFolders =  new ArrayList<>();
        
        try {
			allBackupFolders = ListBackupsInFolder.getAllBackupFolders(sourceFolderPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        for (Path backpfolder: allBackupFolders) {
        	
        	/*if (backpfolder.getFileName().toString().contains("Full")) {
        		continue;
        	}*/
        	
        	System.out.println("processing " + backpfolder.toString());
        	try {
				writer.write("processing " + backpfolder.toString()+ "\n");
				writer.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	
        	String backupFolderName = backpfolder.getFileName().toString();
        	
        	AFileOrAFolder theActualFolders = createJsonStructure(commandLineArguments, backpfolder, backupFolderName);

        	AFileOrAFolder thejsonfile = FileAndFolderUtilities.fromFolderlistDotJsonToAFileOrAFolder(backpfolder.resolve("folderlist.json"));            

        	FileAndFolderUtilities.compareAndUpdate(thejsonfile, theActualFolders, sourceFolderPath, sourceFolderPath, new ArrayList<String>(), backupFolderName, writer);
        	
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
	
}
