package main;

import model.CommandLineArguments;

public class BackupMainClass {

// sample command to do backup
// --destination=c:\temp\backuptest  --source="C:\Users\johan.degraeve.BWR-DC\Kenniscentrum WWZ\XDrive - test" --excludedfilelist="C:\Users\johan.degraeve.BWR-DC\eclipse-workspace\PCBackup\excludedfiles.txt" --type=I
	
	
// sample command to do restore
// --destination=/Users/johandegraeve/Downloads/backup --restoreto=/Users/johandegraeve/Downloads/restore --excludedfilelist="/Users/johandegraeve/OneDrive/Eclipse projects/PCBackup/excludedfiles.txt" --type=R --restoredate=2024-02-20-00-00-00
	
    public static void main(String[] args) {
    	
    	System.out.println("version 1.2.4");
        
    	CommandLineArguments commandLineArguments = CommandLineArguments.getInstance(args);

    	if (commandLineArguments.search) {
    		Search.search();
    		return;
    	}
    	
    	if (commandLineArguments.backup) {
    		
    		Backup.backup();
    		
    	} else {
    		
    		Restore.restore();
    		
    	}
    		
    }

}

