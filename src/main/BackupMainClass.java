package main;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import model.AFileOrAFolder;
import model.CommandLineArguments;
import model.CommandLineArguments.ArgumentName;

import com.fasterxml.jackson.databind.ObjectMapper;

import utilities.FileAndFolderUtilities;

public class BackupMainClass {

    public static void main(String[] args) {
    	
        CommandLineArguments.getInstance(args);
        
		// Specify the path to the folder
        Path folderPath = Paths.get(CommandLineArguments.getInstance().getArgumentValue(ArgumentName.source));

        
        try {
        	
        	String json = "";
        	
            // List the contents of the folder
        	AFileOrAFolder aFileOrAFolder = FileAndFolderUtilities.createAFileOrAFolder(folderPath.toString());
        	
        	ObjectMapper objectMapper = new ObjectMapper();
            try {
            	
            	json = objectMapper.writeValueAsString(aFileOrAFolder);
            	//System.out.println(json);
            	
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

