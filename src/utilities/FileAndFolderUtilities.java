package utilities;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import model.AFileOrAFolder;
import model.AFolder;
import model.AFile;

/**
 * utilities related to creation and processing of instances of type AFile and AFileOrAFolder
 */
public class FileAndFolderUtilities {

	public static int amountoffolders= 0;
	public static int amountoffiles= 0;
	
    /**
     * creates an instance of AFileOrAFolder for folderPath.<br>
     * @param folderOrStringPath can be path to a folder or a file
     * @param incrementalBackupFolderName just a foldername of the full or incremental backup where to find the file, example '2024-01-12 16;46;55 (Full)' This is actually not used just stored in an instance of AFile (not if it's a folder) 
     * @return an instance of either a folder or a file
     * @throws IOException
     */
    public static AFileOrAFolder createAFileOrAFolder(Path folderOrStringPath, String incrementalBackupFolderName) throws IOException {
    	
    	// return value is a folder, with the folderPath as name
    	AFolder returnValue = new AFolder(folderOrStringPath);
    	
        // Create a DirectoryStream to iterate over the contents of the folder
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folderOrStringPath)) {
        	
            for (Path path : directoryStream) {
            	
            	String fileOrFolderNameWithoutFullPath = path.toString().substring(folderOrStringPath.toString().length() + 1);
            	
            	if (Files.isDirectory(path)) {
            		amountoffolders++;
            		// work recursively now. Call createAFileOrAFolder with path, so we get back a new aFolder
            		// in the end this will result in a instance of AFileOrAFolder 
            		AFileOrAFolder aFolder = createAFileOrAFolder(path, incrementalBackupFolderName);

            		// the name used in aFolder is now a full path
            		// this is not good because it will take a lot of space in the resulting json file, so we remove the original path
            		klopt dit?
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
        
        return returnValue;
        
    }


}
