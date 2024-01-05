package utilities;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import model.AFileOrAFolder;
import model.AFolder;
import model.AFile;

public class FileAndFolderUtilities {

    /**
     * creates an instance of AFileOrAFolder for folderPath. 
     * @param folderPath
     * @return an instance of 
     * @throws IOException
     */
    public static AFileOrAFolder createAFileOrAFolder(String folderPath) throws IOException {
    	
    	// return value is a folder, with the folderPath as name
    	AFolder returnValue = new AFolder(folderPath);
    	
        // Create a DirectoryStream to iterate over the contents of the folder
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(folderPath))) {
        	
            for (Path path : directoryStream) {
            	
            	if (Files.isDirectory(path)) {
            		System.out.println("adding a folder to with name " + path.toString());
            		returnValue.addFileOrFolder(new AFolder(path.toString()));
            	} else {
            		System.out.println("adding a file to with name " + path.toString());
            		returnValue.addFileOrFolder(new AFile(path.toString(), Files.getLastModifiedTime(path), folderPath));
            	}
            }
            
        }
        
        return returnValue;
        
    }


}
