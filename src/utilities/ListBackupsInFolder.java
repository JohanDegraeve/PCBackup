package utilities;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class ListBackupsInFolder {

    public static Path getMostRecentBackup(Path backupFolder) throws IOException {
        List<Path> backupFolders = getAllBackupFolders(backupFolder);
        if (backupFolders.size() == 0) {
        	return null;
        }
        return Collections.max(backupFolders);
    }

    private static List<Path> getAllBackupFolders(Path backupFolder) throws IOException {
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
