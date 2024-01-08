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
    	
        CommandLineArguments.getInstance();
		// Specify the path to the folder
        Path folderPath = Paths.get(CommandLineArguments.getInstance(args).getArgumentValue(ArgumentName.source));

        try {
            // List the contents of the folder
        	AFileOrAFolder aFileOrAFolder = FileAndFolderUtilities.createAFileOrAFolder(folderPath.toString());
        	
        	ObjectMapper objectMapper = new ObjectMapper();
            try {
            	objectMapper.writeValueAsString(aFileOrAFolder);
            	
            	//System.out.println(objectMapper.writeValueAsString(aFileOrAFolder));
                //System.out.println("json length = " + objectMapper.writeValueAsString(aFileOrAFolder).length());
                //System.out.println("there are " + FileAndFolderUtilities.amountoffiles + " files");
                //System.out.println("there are " + FileAndFolderUtilities.amountoffolders + " folders");
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

