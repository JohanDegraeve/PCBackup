package main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.AFileOrAFolder;
import model.CommandLineArguments;
import model.CommandLineArguments.ArgumentName;

import utilities.CreateSubFolder;
import utilities.FileAndFolderUtilities;
import utilities.WriteToFile;

public class BackupMainClass {

    public static void main(String[] args) {
    	
        CommandLineArguments.getInstance(args);
        
		// Specify the path to the source folder
        Path sourceFolderPath = Paths.get(CommandLineArguments.getInstance().getArgumentValue(ArgumentName.source));

        // create subfoldername for the backup, this folder will be created within destination folder
        // example for full backup : '2023-12-06 18;24;41 (Full)'
        // example for incremental backup : '2023-12-28 17;07;13 (Incremental)'
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH;mm;ss");
		String subfolder = dateFormat.format(new Date());
        if (CommandLineArguments.getInstance().getArgumentValue(ArgumentName.type).equalsIgnoreCase("F")) {
        	subfolder = subfolder + " (Full)";
        } else {
        	subfolder = subfolder + " (Incremental)";
        }
        Path destinationFolderPath = CreateSubFolder.createSubFolder(CommandLineArguments.getInstance().getArgumentValue(ArgumentName.destination), subfolder);
     
        
        
        try {
        	
        	String json = "";
        	
            /**
             * source folder stored in AFileOrAFolder
             */
        	AFileOrAFolder aFileOrAFolderSourceFolder = FileAndFolderUtilities.createAFileOrAFolder(sourceFolderPath.toString());

        	ObjectMapper objectMapper = new ObjectMapper();
        	
            try {
            	
            	json = objectMapper.writeValueAsString(aFileOrAFolderSourceFolder);
            	WriteToFile.writeToFile(json, destinationFolderPath.toString() + File.separator + "folderlist.json");
            	
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            System.out.println("amount of files   = " + FileAndFolderUtilities.amountoffiles);
            System.out.println("amount of folders = " + FileAndFolderUtilities.amountoffolders);
            System.out.println("json              = " + json.length());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

