package utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.AFile;
import model.AFileOrAFolder;
import model.AFolder;

public class CreateBackup {

	/**
	 * create a full backup
	 * @param sourceFolderToJson : json formatted file and folder list
	 * @param destinationFolderPath : must exist
	 */

	public static void createFullBackup(AFileOrAFolder aFileOrAFolderSourceFolder, Path sourceFolderPath, Path destinationFolderPath) {
		
		// check if destination path already exists, otherwise stop, coding error
		if (!(Files.exists(destinationFolderPath))) {
			Logger.log("in createFullBackup, folder " + destinationFolderPath.toString() + " does not exist, looks like a coding error");
			System.exit(1);
		}
		
		String sourceFolderToJson = "";
		
    	/**
    	 * needed for json encoding
    	 */
    	ObjectMapper objectMapper = new ObjectMapper();
    	
        try {
        	
        	sourceFolderToJson = objectMapper.writeValueAsString(aFileOrAFolderSourceFolder);
        	
        } catch (IOException e) {
            e.printStackTrace();
        }

		// first write the json file to destination folder
		WriteToFile.writeToFile(sourceFolderToJson, destinationFolderPath.toString() + File.separator + "folderlist.json");
		
		// copy files that are in aFileOrAFolderSourceFolder
		copyFilesAndFoldersFromSourceToDest(aFileOrAFolderSourceFolder, sourceFolderPath, destinationFolderPath, false);
				
	}
	
	private static void copyFilesAndFoldersFromSourceToDest(AFileOrAFolder aFileOrAFolderSourceFolder, Path sourceFolderPath, Path destinationFolderPath, boolean createEmptyFolders) {

		// add filename to source and destination folders
		Path sourcePathToCopyFrom = sourceFolderPath.resolve(aFileOrAFolderSourceFolder.getName());
		Path destinationPathToCopyTo = destinationFolderPath.resolve(aFileOrAFolderSourceFolder.getName());
		

		if (aFileOrAFolderSourceFolder instanceof AFile) {

			// we need to copy the file from source to dest
			
			try {
				
				Files.copy(sourcePathToCopyFrom, destinationPathToCopyTo, StandardCopyOption.COPY_ATTRIBUTES);
				
			} catch (IOException e) {
				
				e.printStackTrace();
				
				Logger.log("Exception occurred while copying from " + sourcePathToCopyFrom.toString() + " to " + destinationPathToCopyTo.toString());
				
				System.exit(1);
				
			}
			
		} else if (aFileOrAFolderSourceFolder instanceof AFolder){
			
			// we need to iterate through the folder, each new file or folder in aFileOrAFolderSourceFolder is reprocessed
			for (AFileOrAFolder fileOrFolder : ((AFolder)aFileOrAFolderSourceFolder).getFileOrFolderList()) {
				
				copyFilesAndFoldersFromSourceToDest(fileOrFolder, sourcePathToCopyFrom, destinationPathToCopyTo, createEmptyFolders);
				
			}
			
		} else {
			
			// that's a coding error
			Logger.log("error in copyFilesAndFoldersFromSourceToDest, a AFileOrAFolder that is not AFile and not AFolder ...");
			System.exit(1);
		}
		
	}
	
}