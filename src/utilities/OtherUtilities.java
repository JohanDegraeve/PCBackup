package utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.AFileOrAFolder;
import model.AFolder;
import model.CommandLineArguments;
import model.Constants;

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
	
	/**
	 * concatenates subfolders with a / or \ between the names, depending on the platform
	 * @param source array of subfolders, eg submap1, submap2
	 * @return in example submap1/submap2, seperator is platform dependent
	 */
	public static String concatenateStrings(ArrayList<String> source) {
		String returnValue = "";
		for (String sourceItem : source) {
			String seperatorToAdd = "";
			if (returnValue.length() != 0) {
				seperatorToAdd = File.separator;
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
	 * @param commandLineArguments
	 * @throws IOException
	 */
	public static void copyFolder(Path source, Path destination, CommandLineArguments commandLineArguments) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // Create corresponding directory in the destination
                Path targetDir = destination.resolve(source.relativize(dir));
                Files.createDirectories(targetDir);
                if (commandLineArguments.addpathlengthforfolderswithnewormodifiedcontent) {
                    System.out.println("path length = " + String.format("%5s", targetDir.toString().length()) + "; path = " + targetDir.toString());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            	
            	Path destinationPath = destination.resolve(source.relativize(file));
            	
        		// check if the file is in the list of files to exclude, example .DS_Store
        		if (commandLineArguments.excludedFiles.contains(destinationPath.getFileName().toString())) {
        			return FileVisitResult.CONTINUE;
        		}
        		
        		// check if the file is of format .849C9593-D756-4E56-8D6E-42412F2A707B seems a Microsoft hidden file
        		if (OtherUtilities.fileNeedsToBeIgnored(destinationPath.getFileName().toString())) {
        			return FileVisitResult.CONTINUE;
        		}
            	
                // Copy each file to the destination
                Files.copy(file, destinationPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                if (commandLineArguments.addpathlengthforfolderswithnewormodifiedcontent) {
                    System.out.println("path length = " + String.format("%5s", destinationPath.toString().length()) + "; path = " + destinationPath.toString());
                }
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
	
	/**
	 * converts date to locale String
	 * @param date
	 * @return
	 */
	public static String dateToString(Date date, String dateFormat) {
		
        // Convert Date to Instant
        Instant instant = date.toInstant();

        // Create a ZonedDateTime using the default time zone
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());

        // Define a format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);

        // Format the ZonedDateTime
        return zonedDateTime.format(formatter);

	}

	/**
	 * 
	 * @param backupName eg backupName = 2024-03-19 23;06;08 (Incremental)
	 * @return date, in this example 2024-03-19 23;06;08 local time in Date object
	 */
	public static Date getBackupDate(String backupName) {
		
		if (backupName == null) {return new Date(0);}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.BACKUPFOLDERDATEFORMAT_STRING);
		
		String dateAsString = backupName.substring(0,18);
		
		try {
			return dateFormat.parse(dateAsString);
		} catch (ParseException e) {
			e.printStackTrace();
			Logger.log("");
			System.exit(1);
		}
		
		return new Date(0);
		
	}

}
