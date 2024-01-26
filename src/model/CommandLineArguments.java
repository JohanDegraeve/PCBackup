package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import utilities.Logger;

/**
 * all the needed arguments, like Source folder, destination folder, ...
 * 
 */
public class CommandLineArguments {
	
	/**
	 * Enum to represent argument names
	 */
    private enum ArgumentName {
    	
    	/**
    	 * Folder where the actual source that we're backing up is stored<br>
    	 * This is the full path, example E:\sourcefolder\
    	 */
        source,
        
        /**
         * Folder where we store the backups, either full or incremental.<br>
         * Each time we create a new backup, a subfolder will be created in that folder 
         */
        destination,
        
        /**
         *  Folder where logfile should be written<br>
         */
        logfilefolder,
        
        /**
         * if backup : F or I (f or i is also good)<br>
         * F for full<br>
         * I for incremental<br>
         * 
         * if restore : R
         */
        type,
        
        /**
         * filenames, with full path, that contains list of filenames that should be ignored, ie not added to the folderlist.json and not copied in backups<br>
         */
        excludedfilelist,
        
        /**
         * only needed in case restore is done<br>
         * Specifies date and time for which restore needs to be done<br>
         * YYYY-MM-DD-HH-mm-ss
         */
        restoredate,
        
        /**
         * The specific folder within source that needs to be restored<br>
         * If the complete backup needs to be restored, then omit this argument<br>
         * If a specific subfolder needs to be restored, then specify that folder here
         */
        subfoldertorestore
        
        
        // Add more argument names as needed
        
    }
    
    /**
	 * Folder where the actual source that we're backing up is stored<br>
	 * This is the full path, example E:\sourcefolder\
	 */
    public String source;
    
    /**
     * Folder where we store the backups, either full or incremental.<br>
     * Each time we create a new backup, a subfolder will be created in that folder 
     */
    public String destination;
    
    /**
     * If true, full backup, if false, incremental backup
     */
    public boolean fullBackup = false;
    
    /**
     * is it a backup  or a restore
     */
    public boolean backup = true;
    
    /**
     *  Folder where logfile should be written<br>
     *  can be null, in that case log to System.out
     */
    public String logfilefolder;
    
    /**
     * filenames, with full path, that contains list of filenames that should be ignored, ie not added to the folderlist.json and not copied in backups<br>
     */
    public List<String> excludedFiles = new ArrayList<>();
    
    /**
     * only for restore, Date for which restore needs to be done
     */
    public Date restoreDate = null;
    
	/**
	 * valid argument names, build based on the Enum ArgumentName
	 */
	private static final Set<String> validArgumentNames = buildValidArgumentNamesSet();

	private static final Map<String, String> argumentMap = new HashMap<>();
	
	private static boolean argumentsInitialized = false;
	
	
	
	// Private static instance of the class
	private static volatile CommandLineArguments instance;
	
	/**
	 * Private constructor to prevent instantiation outside the class
	 */
    private CommandLineArguments() {
        
    	source = getArgumentValue(ArgumentName.source);
    	if (source == null) {
    		System.out.println("source argument is missing");
    		giveMinimumArgumentsInfo();System.exit(1);
    	} else {
        	// check if source folder exists
        	Path folderPath = Paths.get(source);
        	if (!(Files.exists(folderPath))) {
        		System.out.println("folder " + source + " does not exist. Create it first or check the argument 'source'");
        		giveMinimumArgumentsInfo();System.exit(1);
        	}
    	}
    	
    	destination = getArgumentValue(ArgumentName.destination);
    	if (destination == null) {
    		System.out.println("destination argument is missing");
    		giveMinimumArgumentsInfo();System.exit(1);
    	} else {
        	// check if destination folder exists
        	Path folderPath = Paths.get(destination);
        	if (!(Files.exists(folderPath))) {
        		System.out.println("folder " + destination + " does not exist. Create it first or check the argument 'destination'");
        		giveMinimumArgumentsInfo();System.exit(1);
        	}
    	}
    	
    	if (getArgumentValue(ArgumentName.type) == null) {
    		System.out.println("type argument is missing");
    		giveMinimumArgumentsInfo();System.exit(1);
    	} else {
    		if (getArgumentValue(ArgumentName.type).equalsIgnoreCase("F")) {
    			fullBackup = true;
    			backup = true;
    		} else if (getArgumentValue(ArgumentName.type).equalsIgnoreCase("I")) {
    			fullBackup = false;
    			backup = true;
    		} else if (getArgumentValue(ArgumentName.type).equalsIgnoreCase("R")) {
    			backup = false;
    		} else {
    			System.out.println("Invalid value for type " + getArgumentValue(ArgumentName.type));
        		giveMinimumArgumentsInfo();System.exit(1);
    		}
    	}
    	
    	logfilefolder = getArgumentValue(ArgumentName.logfilefolder);
    	// if logfilefolderfile is present, then check if it's a directory
    	if (logfilefolder != null) {
    		if (!(Files.isDirectory(Paths.get(logfilefolder)))) {
    			System.out.println("logfilefolder should be a directory");
    			giveMinimumArgumentsInfo();System.exit(1);
    		} else {
    			if (!(Files.exists(Paths.get(logfilefolder)))) {
    				System.out.println("logfilefolder does not exist");
    				giveMinimumArgumentsInfo();System.exit(1);
    			}
    		}
    		
    	}
    	
    	String restoreDateAsString = getArgumentValue(ArgumentName.restoredate); 
    	// try to parse the restore data
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD-HH-mm-ss");
        try {
			restoreDate = dateFormat.parse(restoreDateAsString);
		} catch (ParseException e) {
			e.printStackTrace();
            System.out.println("restoredate seems to be a wrong format. Expected format = YYYY-MM-DD-HH-mm-ss");
			giveMinimumArgumentsInfo();System.exit(1);
		}

    	
    	String excludedfilelist = getArgumentValue(ArgumentName.excludedfilelist);
    	if (excludedfilelist != null) {
    		
    		Path folderPath = Paths.get(excludedfilelist);
    		
    		if (Files.isDirectory(folderPath)) {
    			System.out.println("You specified file " + excludedfilelist + " as excludedfilelist but it does not exist. Create it is a directory, not a file'excludedfilelist'");
    			giveMinimumArgumentsInfo();System.exit(1);
    		}
    		
        	if (!(Files.exists(folderPath))) {
        		System.out.println("You specified file " + excludedfilelist + " as excludedfilelist but it does not exist. Create it first or check the argument 'excludedfilelist'");
        		giveMinimumArgumentsInfo();System.exit(1);
        	}
        	
        	try (BufferedReader reader = new BufferedReader(new FileReader(excludedfilelist))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    excludedFiles.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("logfilefolder does not exist");
				giveMinimumArgumentsInfo();System.exit(1);
            }
        	
    	}
    	
    }
    
    /**
     * get instance of CommandLineArguments, set the arguments with contents of args. This needs to be called only once, at the beginning<br>
     * subsequent calls can be done with the constructor CommandLineArguments()
     * @param args
     * @return
     */
    public static CommandLineArguments getInstance(String[] args) {
    	
    	if (argumentsInitialized) getInstance();
    	
    	initialize(args);
    	
    	argumentsInitialized = true;
    	
        return getInstance();
        
    }
    
    /**
     * get instance of CommandLineArguments, set the arguments with contents of args. This needs to be called only once, at the beginning<br>
     * subsequent calls can be done with 
     * @param args
     * @return
     */
    public static CommandLineArguments getInstance() {
    	
        if (instance == null) {
        	instance = new CommandLineArguments();
        }
        return instance;
    }

    /**
     * get argument value for a specific argument
     * @param argName
     * @return
     */
    public String getArgumentValue(ArgumentName argName) {
        return argumentMap.get(argName.toString());
    }

    /**
     * initialize the CommandLineArguments based on list of arguments
     * @param args
     */
    private static void initialize(String[] args) {
    	
        for (String arg : args) {
        	
            // Split the argument into name and value
            String[] parts = arg.split("=", 2);

            // Check if the argument has a valid format
            if (parts.length == 2 && parts[0].startsWith("--")) {
                // Remove the leading "--" from the name
                String argName = parts[0].substring(2);
                String argValue = parts[1];

            // Handle quoted values
            if (argValue.startsWith("\"") && argValue.endsWith("\"")) {
                argValue = argValue.substring(1, argValue.length() - 1);
            }
                
            // Trim leading and trailing spaces from argValue
            argValue = argValue.trim();
            
             // Check if the argument name is valid
             if (isValidArgumentName(argName)) {
            	 
            	// Process and validate the argument value based on the argument name
                if (processArgumentValue(argName, argValue)) {
                    // Store the argument in the map
                    argumentMap.put(argName, argValue);
                 } else {
                    System.out.println("Invalid argument value for " + argName + ": " + argValue);
                    giveMinimumArgumentsInfo();System.exit(1);
                 }
             } else {
                    System.out.println("Invalid argument name: " + argName);
                    giveMinimumArgumentsInfo();System.exit(1);
             }
                
            } else {
                System.out.println("Invalid argument format. Argument name must start with '--': " + arg);
                giveMinimumArgumentsInfo();System.exit(1);
            }
        }

    }
    
    private static boolean isValidArgumentName(String argName) {
        return validArgumentNames.contains(argName.toLowerCase());
    }

    private static Set<String> buildValidArgumentNamesSet() {
        Set<String> argumentNames = EnumSet.allOf(ArgumentName.class).stream()
                .map(Enum::toString)
                .map(name -> name.toLowerCase()) // Optional: convert to lowercase for case-insensitivity
                .collect(Collectors.toSet());

        return argumentNames;
    }
    
    private static void configureLogFile(String logFilePath) {
        // Implement the logic to configure logging to the specified file
        // This method can be adapted based on your logging library and configuration
        // ...
    	// TODO check if logfilePath is valid
    	Logger.logFileFolder = logFilePath;
        System.out.println("Logging to file " + logFilePath);
    }
    
    /**
     * checks and validates the argValue for a specific argname
     * @param argName
     * @param argValue
     * @return if argValue is good, then return true
     */
    private static boolean processArgumentValue(String argName, String argValue) {
        switch (argName) {
            case "source":
                // Process and validate source argument value
                return true; // Placeholder, implement your logic here

            case "destination":
                // Process and validate destination argument value
                return true; // Placeholder, implement your logic here

            case "logfilefolder":
                // Process and validate logfilefolder argument value
                configureLogFile(argValue);
                return true;
            case "type":
            	if (!(argValue.startsWith("f") || argValue.startsWith("F") || argValue.startsWith("I") || argValue.startsWith("i"))) {
            		return false;
            	}
            	return true;
            case "excludedfilelist":
            	return true;
            // Add more cases for other argument names as needed

            default:
                // Unknown argument name
                System.out.println("Unknown argument name: " + argName);
                return false;
        }
    }
    
    /**
     * gives minimum arguments needed for the app, and prints this info to System.out.println
    */
    private static void giveMinimumArgumentsInfo() {
    	System.out.println("Mandatory arguments:");
    	System.out.println("  --type: F for Full backup, I for incremental backup, R for restore.");
    	System.out.println("  --source:");
    	System.out.println("            for BACKUP : the folder that you want to backup, the contents will be backedup");
    	System.out.println("            for RESTORE: the folder where your backup is stored");
    	System.out.println("  --destination:");
    	System.out.println("            for BACKUP : folder where you want to backup to");
    	System.out.println("            for RESTORE: folder to where you want to restore (better not to take the same as the original source, maybe)");
    	System.out.println("");
    	System.out.println("Optional arguments:");
    	System.out.println("  --logfilefolder: location of the logfile, just the folder name, it must exist.");
    	System.out.println("  --excludedfilelist: filenames, with full path, that contains list of filenames that should be ignored, ie not added to the folderlist.json and not copied in backups.");
    	System.out.println("  --restoredate: mandatory if type arguments = R. Date and time for which restore should occur. Format YYYY-MM-DD-HH-mm-ss Restore will copy the files and folders from the last backup before that date and time.");
    }
    
}
