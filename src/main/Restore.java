package main;

/*import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;*/

/*
public class Restore {

	public static void restore() {
		

		        String sourceFolder = args[0];
		        String destinationFolder = args[1];
		        LocalDate restoreDate = LocalDate.parse(args[2]);

		        // Find the latest backup before the restore date
		        String backupFolderToRestore = findLatestBackup(sourceFolder, restoreDate);

		        if (backupFolderToRestore == null) {
		            System.out.println("No backup found before the restore date.");
		            System.exit(1);
		        }

		        // Restore files from the backup
		        restoreFiles(backupFolderToRestore, destinationFolder);
	}

		    private static String findLatestBackup(String sourceFolder, LocalDate restoreDate) {
		        try {
		            // Assuming backup folders have date prefixes and are sorted in descending order
		            List<Path> backupFolders = Files.list(Paths.get(sourceFolder))
		                    .filter(Files::isDirectory)
		                    .sorted((folder1, folder2) -> folder2.getFileName().toString().compareTo(folder1.getFileName().toString()))
		                    .collect(Collectors.toList());

		            for (Path backupFolder : backupFolders) {
		                LocalDate backupDate = LocalDate.parse(backupFolder.getFileName().toString());
		                if (backupDate.isBefore(restoreDate)) {
		                    return backupFolder.toString();
		                }
		            }
		        } catch (IOException e) {
		            e.printStackTrace();
		        }

		        return null;
		    }

		    private static void restoreFiles(String backupFolder, String destinationFolder) {
		        try {
		            // Read folderlist.json from the backup
		            String folderListJsonPath = Paths.get(backupFolder, "folderlist.json").toString();
		            String folderListJson = Files.readString(Paths.get(folderListJsonPath));

		            // Parse folderlist.json and restore files
		            List<AFileOrAFolder> fileList = parseFolderListJson(folderListJson);

		            for (AFileOrAFolder fileOrFolder : fileList) {
		                if (fileOrFolder instanceof AFile) {
		                    AFile file = (AFile) fileOrFolder;
		                    String sourceFilePath = Paths.get(backupFolder, file.getPathToBackup(), file.getName()).toString();
		                    String destinationFilePath = Paths.get(destinationFolder, file.getName()).toString();

		                    // Copy the file from backup to restore destination
		                    Files.copy(Paths.get(sourceFilePath), Paths.get(destinationFilePath), StandardCopyOption.REPLACE_EXISTING);
		                } else if (fileOrFolder instanceof AFolder) {
		                    // For folders, create the folder in the restore destination
		                    AFolder folder = (AFolder) fileOrFolder;
		                    String destinationFolderPath = Paths.get(destinationFolder, folder.getName()).toString();
		                    Files.createDirectories(Paths.get(destinationFolderPath));
		                }
		            }

		            System.out.println("Restore completed successfully.");
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		    }

		    private static List<AFileOrAFolder> parseFolderListJson(String folderListJson) {
		        // Implement the logic to parse folderlist.json and create a list of AFileOrAFolder objects
		        // ...
		        // You can use a JSON library like Jackson or Gson for parsing JSON.
		        // The exact implementation depends on the structure of your JSON.
		    }
		

		
	}
	
}*/
