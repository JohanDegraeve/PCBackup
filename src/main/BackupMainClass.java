package main;

import model.CommandLineArguments;

public class BackupMainClass {

// sample command to do backup
// --destination=/Users/johandegraeve/Downloads/backup --source=/Users/johandegraeve/Downloads/temp/test --excludedfilelist="/Users/johandegraeve/OneDrive/Eclipse projects/PCBackup/excludedfiles.txt" --type=I
	
	
// sample command to do restore
// --source=/Users/johandegraeve/Downloads/backup --destination=/Users/johandegraeve/Downloads/restore --excludedfilelist="/Users/johandegraeve/OneDrive/Eclipse projects/PCBackup/excludedfiles.txt" --type=R --restoredate=2024-02-20-00-00-00
	
    public static void main(String[] args) {
        
    	CommandLineArguments commandLineArguments = CommandLineArguments.getInstance(args);

    	if (commandLineArguments.backup) {
    		
    		Backup.backup();
    		
    	} else {
    		
    		Restore.restore();
    		
    	}
    		
    }

}

