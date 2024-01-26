package main;

import java.io.*;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import model.CommandLineArguments;
import model.Constants;
import utilities.ListBackupsInFolder;

public class Restore {

	public static void restore() {
		

        CommandLineArguments commandLineArguments = CommandLineArguments.getInstance();
        
		/**
		 * where to find the backup files, this is the backup folder path without the specific folder (ie without '2023-12-06 18;24;41 (Full)' or anything like that)
		 */
        Path sourceFolderPath = Paths.get(commandLineArguments.source);
        
        /**
         * where to restore the files
         */
        Path destinationFolderPath = Paths.get(commandLineArguments.destination);

        try {
        	
			String latestBackupFolderName = ListBackupsInFolder.getMostRecentBackup(sourceFolderPath, commandLineArguments.restoreDate);
			
			if (latestBackupFolderName == null) {
                System.out.println("No backups are found that were created before " + (new SimpleDateFormat(Constants.restoreDateFormat)).format(commandLineArguments.restoreDate));
                System.exit(1);
			}
			
			// full path of latest backup before the restoredate
			sourceFolderPath = sourceFolderPath.resolve(latestBackupFolderName);
			System.out.println(sourceFolderPath.toString());

		} catch (IOException e) {
			e.printStackTrace();
		}
        
	}

	
}
