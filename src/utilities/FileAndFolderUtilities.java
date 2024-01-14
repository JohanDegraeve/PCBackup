package utilities;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import model.AFileOrAFolder;
import model.AFolder;
import model.AFile;

/**
 * utilities related to creation and processing of instances of type AFile and AFileOrAFolder
 */
public class FileAndFolderUtilities {

    /**
     * creates an instance of AFileOrAFolder for folderPath.<br>
     * @param folderOrStringPath can be path to a folder or a file
     * @param backupFolderName just a foldername of the full or incremental backup where to find the file, example '2024-01-12 16;46;55 (Full)' This is actually not used just stored in an instance of AFile (not if it's a folder) 
     * @return an instance of either a folder or a file
     * @throws IOException
     */
    public static AFileOrAFolder createAFileOrAFolder(Path folderOrStringPath, String backupFolderName) throws IOException {

    	//String fileOrFolderNameWithoutFullPath = path.toString().substring(path.toString().length() + 1);
    	String fileOrFolderNameWithoutFullPath = folderOrStringPath.getFileName().toString();

    	if (Files.isDirectory(folderOrStringPath)) {
    		
    		AFolder returnValue = new AFolder(fileOrFolderNameWithoutFullPath, backupFolderName);

            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folderOrStringPath)) {
            	
                for (Path path : directoryStream) {
                	                	
                	returnValue.addFileOrFolder(createAFileOrAFolder(path, backupFolderName));
                		
                }
                
            }
            
            Collections.sort(returnValue.getFileOrFolderList(), (a, b) -> a.getName().compareTo(b.getName()));
            
            return returnValue;
            
    	} else {
    		
    		return new AFile(fileOrFolderNameWithoutFullPath, Files.getLastModifiedTime(folderOrStringPath).toMillis(), backupFolderName); 

    	}
    
    }

}
