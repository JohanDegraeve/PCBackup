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
     * creates an instance of AFileOrAFolder for folderPath. 
     * @param folderPath
     * @return an instance of 
     * @throws IOException
     */
    public static AFileOrAFolder createAFileOrAFolder(String folderPath, String incrementalBackupFolderName) throws IOException {
    	
    	// return value is a folder, with the folderPath as name
    	AFolder returnValue = new AFolder(folderPath);
    	
        // Create a DirectoryStream to iterate over the contents of the folder
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(folderPath))) {
        	
            for (Path path : directoryStream) {
            	
            	String fileOrFolderNameWithoutFullPath = path.toString().substring(folderPath.length() + 1);
            	
            	if (Files.isDirectory(path)) {
            		amountoffolders++;
            		// work recursively now. Call createAFileOrAFolder with path, so we get back a new aFolder
            		// in the end this will result in a instance of AFileOrAFolder 
            		AFileOrAFolder aFolder = createAFileOrAFolder(path.toString());

            		// the name used in aFolder is now a full path
            		// this is not good becuase it will take a lot of space in the resulting json file, so we remove the original path
            		
            		aFolder.setName(fileOrFolderNameWithoutFullPath);
            		
            		//System.out.println("adding a folder with name " + fileOrFolderNameWithoutFullPath);
            		
            		returnValue.addFileOrFolder(aFolder);
            		
            	} else {
            		amountoffiles++;
            		//System.out.println("adding a file with name " + fileOrFolderNameWithoutFullPath + " and lastmodifedtimestamp " + Files.getLastModifiedTime(path).toString());
            		
            		returnValue.addFileOrFolder(new AFile(fileOrFolderNameWithoutFullPath, Files.getLastModifiedTime(path).toMillis(), folderPath));
            		
            	}
            }
            
        }
        
        return returnValue;
        
    }


}
