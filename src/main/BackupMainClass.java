/*
 * Copyright 2024 Johan Degraeve
 *
 * This file is part of PCBackup.
 *
 * PCBackup is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PCBackup is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PCBackup. If not, see <https://www.gnu.org/licenses/>.
 */
package main;

import model.CommandLineArguments;

public class BackupMainClass {

// sample command to do backup
// --destination=c:\temp\backuptest  --source="C:\Users\johan.degraeve.BWR-DC\Kenniscentrum WWZ\XDrive - test" --excludedfilelist="C:\Users\johan.degraeve.BWR-DC\eclipse-workspace\PCBackup\excludedfiles.txt" --type=I
	
	
// sample command to do restore
// --destination=/Users/johandegraeve/Downloads/backup --restoreto=/Users/johandegraeve/Downloads/restore --excludedfilelist="/Users/johandegraeve/OneDrive/Eclipse projects/PCBackup/excludedfiles.txt" --type=R --restoredate=2024-02-20-00-00-00
	
    public static void main(String[] args) {
    	
    	String version = "version 1.2.10";
    	
    	System.out.println(version);
        
    	CommandLineArguments commandLineArguments = CommandLineArguments.getInstance(args);

    	utilities.Logger.log(version);
    	
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

