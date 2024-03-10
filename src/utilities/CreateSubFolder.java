package utilities;

import java.io.File;
import java.nio.file.Path;

public class CreateSubFolder {

	public static Path createSubFolder(String parentFolderPath, String subfolderName) {

	    File parentFolder = new File(parentFolderPath);
	    

	    // Check if the parent folder exists
	    if (!parentFolder.exists()) {
	        throw new IllegalStateException("Parent folder does not exist: " + parentFolderPath);
	    }

	    // Create the subfolder
	    File subfolder = new File(parentFolder, subfolderName);

	    if (!subfolder.mkdir()) {
	    	Logger.log("Subfolder already exists or creation failed: " + subfolder.getAbsolutePath());
	    }
	    
	    return subfolder.toPath();
	    		
	 }
    
}
