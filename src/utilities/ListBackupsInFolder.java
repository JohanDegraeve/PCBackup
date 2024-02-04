package utilities;

import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

import model.Constants;

public class ListBackupsInFolder {

    public static Path getMostRecentBackup(Path backupFolder) throws IOException {
        List<Path> backupFolders = getAllBackupFolders(backupFolder);
        if (backupFolders.size() == 0) {
        	return null;
        }
        return Collections.max(backupFolders);
    }

    /**
     * list the backup folder names, ie of format "yyyy-MM-dd HH;mm;ss (Incremental|Full)"
     * @param backupFolder root source folder where backups are stored
     * @param beforeDate we're searching for a backup made before this date (either full or incremental)
     * @return null if none found, this is just the subfolder name, not the full Path
     * @throws IOException
     */
    public static String getMostRecentBackup(Path backupFolder, Date beforeDate) throws IOException {
    	
    	List<Path> backupFolders = getAllBackupFolders(backupFolder);
    	
    	String returnValue = null;
    	
    	// sort the backupFolder paths as per name of the last folder
    	// after sorting, the first element is the most recent backup
    	Collections.sort(backupFolders, (a,b) -> b.getFileName().toString().compareTo(a.getFileName().toString()));
    	
    	// foldername for the date, without the "full" or "incremental" because we don't need this
    	SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.backupFolderDateFormat);
		String backupfoldernameJustTheDate = dateFormat.format(beforeDate);
    	
    	for (int i = 0; i < backupFolders.size();i++) {
    		String backupFolderName = backupFolders.get(i).getFileName().toString();
    		if (backupFolderName.compareTo(backupfoldernameJustTheDate) <= 0) {
    			return backupFolderName;
    		}
    	}
    	
    	return returnValue;
    }
    
    public static List<Path> getAllBackupFolders(Path backupFolder) throws IOException {
        List<Path> backupFolders = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(backupFolder, entry ->
                Files.isDirectory(entry) && isValidBackupFolder(entry))) {
            for (Path entry : directoryStream) {
                backupFolders.add(entry);
            }
        }
        return backupFolders;
    }

    private static boolean isValidBackupFolder(Path folder) {
        String folderName = folder.getFileName().toString();
        return (folderName.contains("Full") || folderName.contains("Incremental"));
    }
}
