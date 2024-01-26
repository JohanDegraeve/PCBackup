package utilities;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

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
     * @param excludefilelist array of strings that should be ignored as filename
     * @throws IOException
     */
    public static AFileOrAFolder createAFileOrAFolder(Path folderOrStringPath, String backupFolderName, List<String> excludefilelist) throws IOException {

    	String fileOrFolderNameWithoutFullPath = folderOrStringPath.getFileName().toString();

    	if (Files.isDirectory(folderOrStringPath)) {
    		
    		AFolder returnValue = new AFolder(fileOrFolderNameWithoutFullPath, backupFolderName);

            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folderOrStringPath)) {
            	
                for (Path path : directoryStream) {
                	   
                	//  if it's a file, check if it's in the excludefilelist
                	if (!(Files.isDirectory(path))) {
                		
                		// check if the file is in the list of files to exclude, example .DS_Store
                		if (excludefilelist.contains(path.getFileName().toString())) {
                    		continue;
                		}
                		
                		// check if the file is of format .849C9593-D756-4E56-8D6E-42412F2A707B seems a Microsoft hidden file
                		if (OtherUtilities.fileNeedsToBeIgnored(path.getFileName().toString())) {
                			continue;
                		}
                		
                	}
                	
                	returnValue.addFileOrFolder(createAFileOrAFolder(path, backupFolderName, excludefilelist));
                		
                }
                
            }
            
            return returnValue;
            
    	} else {
    		
    		return new AFile(fileOrFolderNameWithoutFullPath, Files.getLastModifiedTime(folderOrStringPath).toMillis(), backupFolderName); 

    	}
    
    }
    
    	/**
    	 * compares source and dest which are both intance of AFileOrAFolder<br>
    	 * Updates dest:<br>
    	 * - If there's a folder in source, that is not in dest, then the folder must be added in dest<br>
    	 * - If there's a folder in dest, that is not in source, then the folder must be deleted in dest<br>
    	 * - If a file in source has a more recent last modified timestamp than the same file in dest, then the entry in dest must be updated with the new timestamp<br>
    	 *    - in that case the actual file is also copied to the destination folder
    	 * - If there's a file in source, that is not in dest, then the file must be added in dest<br>
    	 *    - in that case the actual file is also copied to the destination folder
    	 * - If there's a file in dest that is not found in source, then the entry in dest must be deleted<br>
    	 * @param sourceFolderPath the source root path where the actual source files are stored
    	 * @param destBackupFolderPath the source backup folder path where files need to be copied to
    	 * @param sourceFileOrFolder instance of AFileOrAFolder that represents the contents in sourceFolderPath
    	 * @param subfolders is an arraylist of strings, representing the subfolders. We need to pass them through as we go recursively through the function. It's needed in case a file copy needs to be made to make sure we put it in the right folder.
    	 */
        public static void compareAndUpdate(AFileOrAFolder sourceFileOrFolder, AFileOrAFolder destFileOrFolder, Path sourceFolderPath, Path destBackupFolderPath, ArrayList<String> subfolders, String backupFolderName) {
        	
            // Compare and update files and folders
            if (sourceFileOrFolder instanceof AFile  && destFileOrFolder instanceof AFile) {
                // Compare and update files
                compareAndUpdateFiles((AFile) sourceFileOrFolder, (AFile) destFileOrFolder, sourceFolderPath, destBackupFolderPath, subfolders, backupFolderName);
            } else if (!(sourceFileOrFolder instanceof AFile) && !(destFileOrFolder instanceof AFile)) {
                // Compare and update folders
                compareAndUpdateFolders((AFolder) sourceFileOrFolder, (AFolder) destFileOrFolder, sourceFolderPath, destBackupFolderPath, OtherUtilities.addString(subfolders, sourceFileOrFolder.getName()), backupFolderName);
            } else {
            	Logger.log("in compareAndUpdate(AFileOrAFolder source, AFileOrAFolder dest), not both File and not both Folder");
            }
        }

        /**
         * 
         * @param folderlistPath Path for the folderlist.json
         * @return
         */
        public static AFileOrAFolder fromFolderlistDotJsonToAFileOrAFolder(Path folderlistPath) {
        	
            // declare and init listOfFilesAndFoldersInPreviousBackupFolder
            // it's null, but we assume that it will be set to non nul value, or an exception will occur causing a crash
            AFileOrAFolder listOfFilesAndFoldersInPreviousBackupFolder = null;
            
            try {

                ObjectMapper objectMapper = new ObjectMapper();
                listOfFilesAndFoldersInPreviousBackupFolder = objectMapper.readValue(Files.readString(folderlistPath, StandardCharsets.UTF_8), AFileOrAFolder.class);
                
            } catch (IOException e) {
                // Handle IOException (e.g., file not found or permission issues)
            	e.printStackTrace();
                Logger.log("Exception while converting file " + folderlistPath.toString() + " to json");
                Logger.log(e.toString());
                System.exit(1);
            }
            
            return listOfFilesAndFoldersInPreviousBackupFolder;
            

        }
        
        private static void compareAndUpdateFiles(AFile sourceFile, AFile destFile, Path sourceFolderPath, Path destBackupFolderPath, ArrayList<String> subfolders, String backupFolderName) {
            // Compare and update files based on last modified timestamp
            if (sourceFile.getts() > destFile.getts()) {
            	
                // Update destFile with the new timestamp
                destFile.setts(sourceFile.getts());
                Logger.log("setting ts for " + destFile.getName() + " to " + destFile.getts() + " and copying to backup");
                
                // set also the backup foldername
                destFile.setPathToBackup(backupFolderName);
                
                // create the folder in the destination if it doesn't exist yet
                try {
					Files.createDirectories(PathUtilities.concatenatePaths(destBackupFolderPath, subfolders));
				} catch (IOException e) {
					e.printStackTrace();
		            Logger.log("Exception in compareAndUpdateFiles(AFile,AFile) while creating the directory " + PathUtilities.concatenatePaths(destBackupFolderPath, subfolders).toString());
		            Logger.log(e.toString());
		            System.exit(1);
				}
                
                try {
                	// add sourcefile name to dest and source file, it's the same name
					Files.copy(PathUtilities.concatenatePaths(sourceFolderPath, OtherUtilities.addString(subfolders, sourceFile.getName())), PathUtilities.concatenatePaths(destBackupFolderPath, OtherUtilities.addString(subfolders, sourceFile.getName())), StandardCopyOption.COPY_ATTRIBUTES);
				} catch (IOException e) {
					e.printStackTrace();
		            Logger.log("Exception in compareAndUpdateFiles(AFile,AFile) while copying a file from " + PathUtilities.concatenatePaths(sourceFolderPath, subfolders).toString() + " to " + PathUtilities.concatenatePaths(destBackupFolderPath, subfolders));
		            Logger.log(e.toString());
		            System.exit(1);
				}
                
            } 
        }

        private static void compareAndUpdateFolders(AFolder sourceFolder, AFolder destFolder, Path sourceFolderPath, Path destBackupFolderPath, ArrayList<String> subfolders, String backupFolderName) {
            // Compare and update folders based on content
            List<AFileOrAFolder> sourceContents = sourceFolder.getFileOrFolderList();
            List<AFileOrAFolder> destContents = destFolder.getFileOrFolderList();

            // Process files and folders in source
            for (AFileOrAFolder sourceItem : sourceContents) {
                // Find the corresponding item in dest
                AFileOrAFolder matchingDestItem = findMatchingItem(sourceItem, destContents);

                if (matchingDestItem == null) {
                	
                    // Item in source doesn't exist in dest, add it
                    destContents.add(sourceItem);
                	Logger.log("in compareAndUpdateFolders(AFileOrAFolder, AFileOrAFolder.., adding " + sourceItem.getName() + " to " + destFolder.getName());
                	
                	if (sourceItem instanceof AFile) {

                        // create the folder in the destination if it doesn't exist yet
                        try {
        					Files.createDirectories(PathUtilities.concatenatePaths(destBackupFolderPath, subfolders));
        				} catch (IOException e) {
        					e.printStackTrace();
        		            Logger.log("Exception in compareAndUpdateFiles(AFileOrAFolder, AFileOrAFolder.. while creating the directory " + PathUtilities.concatenatePaths(destBackupFolderPath, subfolders).toString());
        		            Logger.log(e.toString());
        		            System.exit(1);
        				}
                        
                        try {
                        	// add sourcefile name to dest and source file, it's the same name
        					Files.copy(PathUtilities.concatenatePaths(sourceFolderPath, OtherUtilities.addString(subfolders, sourceItem.getName())), PathUtilities.concatenatePaths(destBackupFolderPath, OtherUtilities.addString(subfolders, sourceItem.getName())), StandardCopyOption.COPY_ATTRIBUTES);
        				} catch (IOException e) {
        					e.printStackTrace();
        		            Logger.log("Exception in compareAndUpdateFiles(AFileOrAFolder, AFileOrAFolder.. while copying a file from " + PathUtilities.concatenatePaths(sourceFolderPath, subfolders).toString() + " to " + PathUtilities.concatenatePaths(destBackupFolderPath, subfolders));
        		            Logger.log(e.toString());
        		            System.exit(1);
        				}


                	} else if (sourceItem instanceof AFolder) {// it has to be an instance of AFolder but let's check anyway
                		
                		// we need to copy the complete contents of the folder from source to dest
                		try {
							OtherUtilities.copyFolder(PathUtilities.concatenatePaths(sourceFolderPath, OtherUtilities.addString(subfolders, sourceItem.getName())), PathUtilities.concatenatePaths(destBackupFolderPath, OtherUtilities.addString(subfolders, sourceItem.getName())));
						} catch (IOException e) {
							e.printStackTrace();
        		            Logger.log("Exception in compareAndUpdateFiles(AFileOrAFolder, AFileOrAFolder.. while copying a folder from " + PathUtilities.concatenatePaths(sourceFolderPath, subfolders).toString() + " to " + PathUtilities.concatenatePaths(destBackupFolderPath, subfolders));
        		            Logger.log(e.toString());
        		            System.exit(1);
						}
                		
                	}
                	
                	
                } else {
                    // Recursively compare and update the matching items
                    compareAndUpdate(sourceItem, matchingDestItem, sourceFolderPath, destBackupFolderPath, subfolders, backupFolderName);
                }
            }

            // Process items in dest that don't exist in source
            if (destContents.removeIf(destItem -> !containsItem(destItem, sourceContents))) {
            	Logger.log("in compareAndUpdateFolders(AFileOrAFolder source, AFileOrAFolder dest), did remove one or more items from  " + destFolder.getName());
            }
        }

        private static AFileOrAFolder findMatchingItem(AFileOrAFolder sourceItem, List<AFileOrAFolder> destContents) {
            // Find an item in dest with the same name as the sourceItem
            return destContents.stream()
                    .filter(destItem -> destItem.getName().equals(sourceItem.getName()))
                    .findFirst()
                    .orElse(null);
        }

        private static boolean containsItem(AFileOrAFolder sourceFileOrFolder, List<AFileOrAFolder> fileOrFolderList) {
            // Check if itemList contains an item with the same name as the specified item
            return fileOrFolderList.stream().anyMatch(existingItem -> existingItem.getName().equals(sourceFileOrFolder.getName()));
        }


}
