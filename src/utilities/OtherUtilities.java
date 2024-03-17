package utilities;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.AFileOrAFolder;
import model.AFolder;
import model.CommandLineArguments;

public class OtherUtilities {

	public static ArrayList<String> addString(ArrayList<String> source, String stringToAdd) {
		
		ArrayList<String> copyList = new ArrayList<>(source);
        copyList.add(stringToAdd);

		return copyList;
		
	}
	
	
	public static boolean fileNeedsToBeIgnored(String fileName) {
		
		if (fileIsWindowsHiddenFile(fileName)) {return true;}
		
		return false;
		
	}
	
	public static String concatenateStrings(ArrayList<String> source) {
		String returnValue = "";
		for (String sourceItem : source) {
			String seperatorToAdd = "";
			if (returnValue.length() != 0) {
				seperatorToAdd = "\\";// TODO werkt dat op mac?
			}
			returnValue = returnValue +seperatorToAdd + sourceItem;
		}
		return returnValue;
	}
	
	/**
	 * example files of format .849C9593-D756-4E56-8D6E-42412F2A707B need to be ignored
	 * @param fileName
	 * @return
	 */
	private static boolean fileIsWindowsHiddenFile(String fileName) {
		
		if (!(fileName.startsWith("."))) {return false;}
			
		if (!(fileName.length() == 37)) {return false;}	
		
		// Define the regex pattern
        String pattern = "^\\.[A-F0-9]{8}-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{12}$";

        // Create a Pattern object
        Pattern regex = Pattern.compile(pattern);

        // Create a Matcher object
        Matcher matcher = regex.matcher(fileName);

        // Check if the filename matches the pattern
        return matcher.matches();
		
	}
	
	/**
	 * recursively copying a folder from a source directory to a destination directory
	 * @param source
	 * @param destination
	 * @throws IOException
	 */
	public static void copyFolder(Path source, Path destination) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // Create corresponding directory in the destination
                Path targetDir = destination.resolve(source.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // Copy each file to the destination
                Files.copy(file, destination.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
    }
	
	/**
	 * does the foldername mapping for the first folder, explanation see in commandline arguments, with the explanation of argument folderNameMapping
	 * @param listOfFilesAndFoldersInSourceFolder
	 * @param commandLineArguments
	 * @param sourceFolderPath
	 * @param destinationFolderPath must include the backup foldername
	 */
	public static void doFolderNameMapping(AFolder listOfFilesAndFoldersInSourceFolder, CommandLineArguments commandLineArguments, Path destinationFolderPath) {
		// now we will do foldername mapping
		// if one of the main folders in the source is fond in the foldername mapping list, then we update it in folderlist.json and we also rename the actual foldername in the backup
		for (AFileOrAFolder aFileOrAFolder: listOfFilesAndFoldersInSourceFolder.getFileOrFolderList()) {
			
			// we only do the mapping for folders
			if (!(aFileOrAFolder instanceof AFolder)) {continue;}
			
			if (commandLineArguments.folderNameMapping.containsKey((aFileOrAFolder).getName())) {
				
				String newName = commandLineArguments.folderNameMapping.get((aFileOrAFolder).getName());
				
				String oldName = aFileOrAFolder.getName();
				
				if (newName != null) {
					
					aFileOrAFolder.setName(newName);
					
					//now rename the actual backup folder
					Path sourcePath = destinationFolderPath.resolve(oldName);
					Path targetPath = destinationFolderPath.resolve(newName);
					
					// possibly the sourcepath doesn't exist, eg if it's an incremental backup, and there was no modified file in the folder and it's subfolders
					// then the path is not created, so Files.move would file
					// so first check if it exists.
					
					try {
						if (Files.exists(sourcePath)) {
							Files.move(sourcePath, targetPath);
						}
					} catch (IOException e) {
						e.printStackTrace();
						Logger.log("Exception occurred while renaming from " + sourcePath.toString() + " to " + targetPath.toString());
						System.exit(1);

					}
				}
				
			}
			
		}

	}
	
	
}
