package main;

import model.CommandLineArguments;

public class BackupMainClass {

    public static void main(String[] args) {
        
    	CommandLineArguments commandLineArguments = CommandLineArguments.getInstance(args);

    	if (commandLineArguments.backup) {
    		
    		Backup.backup();
    		
    	} else {
    		
    		
    		
    	}
    		
    }

}

