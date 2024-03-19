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
import model.CommandLineArguments;
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
     * @param excludedpathlist array of strings that should be ignored as foldername
     * @param addpathlength for testing only
     * @throws IOException
     */
    public static AFileOrAFolder createAFileOrAFolder(Path folderOrStringPath, String backupFolderName, List<String> excludefilelist, List<String> excludedpathlist, boolean addpathlength) throws IOException {

    	String fileOrFolderNameWithoutFullPath = folderOrStringPath.getFileName().toString();

    	if (Files.isDirectory(folderOrStringPath)) {
    		
    		AFolder returnValue = new AFolder(fileOrFolderNameWithoutFullPath, backupFolderName);

    		/**
    		 * FOLLOWING CODE REPEATS IN BACKUP.JAVA, If you're changing anything here, check if the same change is necessary in BACKUP.JAVA
    		 */
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folderOrStringPath)) {
            	
            	directoryLoop: for (Path path : directoryStream) {
                	   
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
                		
                	} else {
                		// check if the directory contains any of the paths in excludedPaths
                		// it's a 'contains' check, example if directory = c:\temp\backuptest\submap1\submap2 
                		//    and if in the list excludedPaths there is a string submap1\submap2 then this is considered as a match
                		//    in that case, the directory will not be backed up
                		for (String excludedPath : excludedpathlist) {
                			
                			if (path.toString().toUpperCase().trim().indexOf(excludedPath.toUpperCase().trim()) >= 0) {
                				Logger.log("      Excluding folder '" + excludedPath + "' because " + excludedPath + " is in the file excludedpathlist");
                				continue directoryLoop;
                			}
                		}
                		
                	}
                	
                	returnValue.addFileOrFolder(createAFileOrAFolder(path, backupFolderName, excludefilelist, excludedpathlist, addpathlength));
                		
                }
                
            }
            
            if (addpathlength) {
                System.out.println("path length = " + String.format("%5s", folderOrStringPath.toString().length()) + "; path = " + folderOrStringPath.toString());
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
        public static void compareAndUpdate(AFileOrAFolder sourceFileOrFolder, AFileOrAFolder destFileOrFolder, Path sourceFolderPath, Path destBackupFolderPath, ArrayList<String> subfolders, String backupFolderName, Integer level, CommandLineArguments commandLineArguments) {
        	
            // Compare and update files and folders
            if (sourceFileOrFolder instanceof AFile  && destFileOrFolder instanceof AFile) {
                // Compare and update files
                compareAndUpdateFiles((AFile) sourceFileOrFolder, (AFile) destFileOrFolder, sourceFolderPath, destBackupFolderPath, subfolders, backupFolderName, commandLineArguments);
            } else if (!(sourceFileOrFolder instanceof AFile) && !(destFileOrFolder instanceof AFile)) {
                // Compare and update folders
                compareAndUpdateFolders((AFolder) sourceFileOrFolder, (AFolder) destFileOrFolder, sourceFolderPath, destBackupFolderPath, OtherUtilities.addString(subfolders, sourceFileOrFolder.getName()), backupFolderName, level, commandLineArguments);
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
        
        /**
         * search in destContents for sourceItem
         * @param sourceItem
         * @param destContents
         * @return
         */
        public static AFileOrAFolder findMatchingItem(AFileOrAFolder sourceItem, List<AFileOrAFolder> destContents) {
            // Find an item in dest with the same name as the sourceItem
            return destContents.stream()
                    .filter(destItem -> destItem.getName().equals(sourceItem.getName()))
                    .findFirst()
                    .orElse(null);
        }


        private static void compareAndUpdateFiles(AFile sourceFile, AFile destFile, Path sourceFolderPath, Path destBackupFolderPath, ArrayList<String> subfolders, String backupFolderName, CommandLineArguments commandLineArguments) {
            // Compare and update files based on last modified timestamp
            if (sourceFile.getts() > destFile.getts()) {
            	
                // Update destFile with the new timestamp
                destFile.setts(sourceFile.getts());
                Logger.log("   Copying updated file " + OtherUtilities.concatenateStrings(OtherUtilities.addString(subfolders, sourceFile.getName())));
                
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
                	Path destPath = PathUtilities.concatenatePaths(destBackupFolderPath, OtherUtilities.addString(subfolders, sourceFile.getName()));
					Files.copy(PathUtilities.concatenatePaths(sourceFolderPath, OtherUtilities.addString(subfolders, sourceFile.getName())), destPath, StandardCopyOption.COPY_ATTRIBUTES);
	                if (commandLineArguments.addpathlengthforfolderswithnewormodifiedcontent) {
	                    System.out.println("path length = " + String.format("%5s", destPath.toString().length()) + "; path = " + destPath.toString());
	                }

				} catch (IOException e) {
					e.printStackTrace();
		            Logger.log("Exception in compareAndUpdateFiles(AFile,AFile) while copying a file from " + PathUtilities.concatenatePaths(sourceFolderPath, subfolders).toString() + " to " + PathUtilities.concatenatePaths(destBackupFolderPath, subfolders));
		            Logger.log(e.toString());
		            System.exit(1);
				}
                
            } 
        }

        private static void compareAndUpdateFolders(AFolder sourceFolder, AFolder destFolder, Path sourceFolderPath, Path destBackupFolderPath, ArrayList<String> subfolders, String backupFolderName, Integer level, CommandLineArguments commandLineArguments) {
            // Compare and update folders based on content
            List<AFileOrAFolder> sourceContents = sourceFolder.getFileOrFolderList();
            List<AFileOrAFolder> destContents = destFolder.getFileOrFolderList();

            // Process files and folders in source
            for (AFileOrAFolder sourceItem : sourceContents) {
            	
            	// for the foldername mapping, we need to compare to the mapped name, so if a mapping is found, then we store the original name
            	String originalSourceItemName = sourceItem.getName();
            	String newSourceItemName = originalSourceItemName;
            	
            	// If it's a folder, and if it's the first level, then we'll do foldernamemapping
            	if (
            			   (level == 1) 
            			&& (sourceItem instanceof AFolder) 
            			&& (commandLineArguments.folderNameMapping.containsKey((originalSourceItemName)))
            			&& (commandLineArguments.folderNameMapping.get(originalSourceItemName) != null)
            		) {
            		newSourceItemName = commandLineArguments.folderNameMapping.get(originalSourceItemName);
            		sourceItem.setName(newSourceItemName);
            	}
            	
                // Find the corresponding item in dest
                AFileOrAFolder matchingDestItem = findMatchingItem(sourceItem, destContents);

                // set back the original source item name, we will replace again later on by calling the funcion doFolderNameMapping, somewhere else
            	sourceItem.setName(originalSourceItemName);

                if (matchingDestItem == null) {
                	
                    destContents.add(sourceItem);
                	if (sourceItem instanceof AFile) {

                    	Logger.log("   Adding new file : " + OtherUtilities.concatenateStrings(OtherUtilities.addString(subfolders, originalSourceItemName)));
                    	
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
                        	Path destPath = PathUtilities.concatenatePaths(destBackupFolderPath, OtherUtilities.addString(subfolders, originalSourceItemName));
        					Files.copy(PathUtilities.concatenatePaths(sourceFolderPath, OtherUtilities.addString(subfolders, originalSourceItemName)), destPath, StandardCopyOption.COPY_ATTRIBUTES);
        	                if (commandLineArguments.addpathlengthforfolderswithnewormodifiedcontent) {
        	                    System.out.println("path length = " + String.format("%5s", destPath.toString().length()) + "; path = " + destPath.toString());
        	                }


        				} catch (IOException e) {
        					e.printStackTrace();
        		            Logger.log("Exception in compareAndUpdateFiles(AFileOrAFolder, AFileOrAFolder.. while copying a file from " + PathUtilities.concatenatePaths(sourceFolderPath, subfolders).toString() + " to " + PathUtilities.concatenatePaths(destBackupFolderPath, subfolders));
        		            Logger.log(e.toString());
        		            System.exit(1);
        				}


                	} else if (sourceItem instanceof AFolder) {// it has to be an instance of AFolder but let's check anyway
                		
                    	Logger.log("   Adding new folder and it's contents : " + OtherUtilities.concatenateStrings(OtherUtilities.addString(subfolders, originalSourceItemName)));

                		// we need to copy the complete contents of the folder from source to dest
                		try {
							OtherUtilities.copyFolder(PathUtilities.concatenatePaths(sourceFolderPath, OtherUtilities.addString(subfolders, originalSourceItemName)), PathUtilities.concatenatePaths(destBackupFolderPath, OtherUtilities.addString(subfolders, originalSourceItemName)), commandLineArguments);
						} catch (IOException e) {
							e.printStackTrace();
        		            Logger.log("Exception in compareAndUpdateFiles(AFileOrAFolder, AFileOrAFolder.. while copying a folder from " + PathUtilities.concatenatePaths(sourceFolderPath, subfolders).toString() + " to " + PathUtilities.concatenatePaths(destBackupFolderPath, subfolders));
        		            Logger.log(e.toString());
        		            System.exit(1);
						}
                		
                	}
                	
                	
                } else {
                	
                    // Recursively compare and update the matching items
                    compareAndUpdate(sourceItem, matchingDestItem, sourceFolderPath, destBackupFolderPath, subfolders, backupFolderName, level + 1, commandLineArguments);
                    
                    // before leaving the function set matchingDestItem name to the originalSourceItemName
                    // later on we will call doFolderNameMapping, which iterates through the destination folder list. It will see that there's a mapping to be applied and then also rename the backuped folder
                    matchingDestItem.setName(originalSourceItemName);
                    
                }
            }

            // Process items in dest that don't exist in source
            // but only for not level 1 folders, meaning once a backup is taken of a sharepoint library, it will not be removed anymore
            if (level > 1) {
                if (destContents.removeIf(destItem -> !containsItem(destItem, sourceContents))) {
                	Logger.log("   Some files and/or folders in " + OtherUtilities.concatenateStrings(subfolders) + " that were still in previous backup are not found anymore in the source");
                	Logger.log("      Those files and/or folders will be removed from the json structure");
                }
            }
        }

        private static boolean containsItem(AFileOrAFolder sourceFileOrFolder, List<AFileOrAFolder> fileOrFolderList) {
            // Check if itemList contains an item with the same name as the specified item
            return fileOrFolderList.stream().anyMatch(existingItem -> existingItem.getName().equals(sourceFileOrFolder.getName()));
        }


}
