package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.AFile;
import model.AFileOrAFolder;
import model.AFolder;
import model.CommandLineArguments;
import utilities.FileAndFolderUtilities;
import utilities.ListBackupsInFolder;
import utilities.Logger;
import utilities.WriteToFile;

public class Search {


	public static void search() {
	
		Date searchDate = new Date();
		
		CommandLineArguments commandLineArguments = CommandLineArguments.getInstance();
		
		/**
		 * where to find the backup files, this is the backup folder path without the specific folder (ie without '2023-12-06 18;24;41 (Full)' or anything like that)
		 */
        Path sourceFolderPath = Paths.get(commandLineArguments.source);
        
		// get list of all backup folders at and before searchdate
    	// first get the latest, then get all older ones and insert the latest
    	// this is trick to reuse existing functions
        List<String> allBackups = new ArrayList<>();
        try {
			String latestBackupFolderName = ListBackupsInFolder.getMostRecentBackup(sourceFolderPath, searchDate);
			allBackups = ListBackupsInFolder.getAllBackupFoldersAsStrings(sourceFolderPath, latestBackupFolderName);
			allBackups.add(0, latestBackupFolderName);
        } catch (IOException e) {
			e.printStackTrace();
			Logger.log("Exception in search, while reading backup folder ");
            Logger.log(e.toString());
        }
        
        // will have search results after calling iterateThroughFolderOrFile 
		Map<String, String> results = new HashMap<>();
		
		// iterate through all backups and search for the text
		for (String backupFolderName: allBackups) {
			
			Path pathWithJsonFile = sourceFolderPath.resolve(backupFolderName).resolve("folderlist.json");
			Logger.log("Parsing " + pathWithJsonFile.toString());
			AFileOrAFolder listOfFilesAndFoldersInBackup = FileAndFolderUtilities.fromFolderlistDotJsonToAFileOrAFolder(pathWithJsonFile);
			
			iterateThroughFolderOrFile(listOfFilesAndFoldersInBackup, commandLineArguments.searchTextPattern, sourceFolderPath, backupFolderName, Paths.get(""), results);
			
		}

		// seperator to use in csv files
		String seperator = ",";
		
		// string where search results will be written to and finally written to output file
		String textToWrite = "";

		// first add "sep=," this tells Excel that , is the seperator. ";" can't be the seperator because ";" is already used in the backup folder names
		textToWrite += "sep=,\n";

		// we have the results, create text to write to file
		textToWrite += "backupfolder" + seperator + "name of matching item " + seperator + "full path\n";
		for (Map.Entry<String, String> entry : results.entrySet()) {
            
			// create the full path where the entry can be found
			// source FolderPath is source where all backups are stored
			// entry.getValue() is the specific backup (ie something like '2024-03-10 20;31;55 (Incremental)'
			// entry.getKey() is the full subfolder, if it's a file , this is inclusive the filename
			Path fullPath = sourceFolderPath.resolve(entry.getValue()).resolve(entry.getKey());
			
			// add backupfoldername to textToWrite
			textToWrite += entry.getValue()  + seperator;
			
			// add just the name of the file or the folder
			textToWrite += fullPath.getFileName().toString()  + seperator;
			
			// now add the full path
			textToWrite += fullPath.toString();
			
			// add a newline
			textToWrite += "\n";
			
        }
		
		// get path filename to write to
		Path pathToWriteTo = Paths.get(commandLineArguments.destination).resolve(createSearchResultFilename(Paths.get(commandLineArguments.destination)));
		
		try {
			WriteToFile.writeToFile(textToWrite, pathToWriteTo.toString());
			Logger.log("Search results written to " + pathToWriteTo.toString());
		} catch (IOException e) {
			e.printStackTrace();
        	Logger.log("Failed to write search results to  " + pathToWriteTo.toString());
			System.exit(1);
		}

	}
	
	/**
	 * Go through all subfolders and files in aFileOrAFolder, search the item (subfolder or file) with matching name, if found, get the backup folder name<br>
	 * updates results with new results<br>
	 * Only adds matching items that are not yet in results, because some items may have been found in more recent backups. Goal is that the function is first called for the most recent backup
	 * example: (here searchTextPattern could be "test"<br>
	 *   - key = submap2/submap21/submap211/test.txt<br>
	 *   - value = 2024-03-10 17;53;19 (Incremental)<br>
	 * In this example a matching item is found in submap2/submap21/submap211 with filename test.txt, most backup where the item is found = 2024-03-10 17;53;19
	 * @param aFileOrAFolder instance of AFileOrAFolder to search in. This should always match the contents of sourceFolderPath/backupfoldername/subfolder, meaning the caller must make sure this is a correct match
	 * @param searchTextPattern to search
	 * @param subfolder is subfolder within a backup where the actual aFileOrAFolder is stored
	 */
	private static void iterateThroughFolderOrFile(AFileOrAFolder aFileOrAFolder, Pattern searchTextPattern, Path sourceFolderPath, String backupfoldername, Path subfolder, Map<String, String> results) {
		
		Matcher matcher = searchTextPattern.matcher(aFileOrAFolder.getName());
		
		if (matcher.find()) {
			
			// path without backup folder name
			Path pathWhereItemWasFound = subfolder;
			if (aFileOrAFolder instanceof AFile) {
				pathWhereItemWasFound = pathWhereItemWasFound.resolve(aFileOrAFolder.getName());
			}
			// if not yet in results, then add it, with the backup folername where the latest version is stored
			if (!results.containsKey(pathWhereItemWasFound.toString())) {
				results.put(pathWhereItemWasFound.toString(), aFileOrAFolder.getPathToBackup());
			}
			
			
		}
		
		if (aFileOrAFolder instanceof AFile) {
			// no further processing needed
		} else {
			for (AFileOrAFolder aFileOrAFolder1: ((AFolder)aFileOrAFolder).getFileOrFolderList()) {
				
				if (aFileOrAFolder1 instanceof AFile) {

					iterateThroughFolderOrFile(aFileOrAFolder1, searchTextPattern, sourceFolderPath, backupfoldername, subfolder, results);

				} else {

					iterateThroughFolderOrFile(aFileOrAFolder1, searchTextPattern, sourceFolderPath, backupfoldername, subfolder.resolve(aFileOrAFolder1.getName()), results);

				}
				
			}
		}
		
	}
	
	/**
	 * will check if 'searchresults.txt' exists in path, if it exists it tries with 'searchresults (1).txt' , 'searchresults (2).txt' ... until a not yet existing file is found<br>
	 * if not existing filename is found, it is returned. Max up to 1000 attempts, if not found within 1000, it returns "searchresults.txt" 
	 * @param path must be an existing folder 
	 * @return the filename to use
	 */
	private static String createSearchResultFilename(Path path) {
		
		Path pathToCheck = path.resolve("searchresults.csv");
		
		// check if searchresults already exists, and if not,then this will be the file name
		if (!Files.exists(pathToCheck)) {
			return "searchresults.csv";
		}
		
		for(int i = 1; i <= 1000; i++) {
		    String newFileName = "searchresults (" + i + ").csv"; 
		    pathToCheck = path.resolve(newFileName);
		    if (!Files.exists(pathToCheck)) {
				return newFileName;
			}
		}
		
		return "searchresults.csv";
		
	}
	
}
