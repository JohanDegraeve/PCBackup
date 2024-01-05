package main;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import model.Arguments;
import utilities.FileAndFolderUtilities;

public class BackupMainClass {

    public static void main(String[] args) {
    	
        Arguments.getInstance();
		// Specify the path to the folder
        Path folderPath = Paths.get(Arguments.sourcePath);

        try {
            // List the contents of the folder
        	FileAndFolderUtilities.createAFileOrAFolder(folderPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

