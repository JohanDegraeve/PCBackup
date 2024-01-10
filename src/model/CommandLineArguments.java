package model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import model.CommandLineArguments.ArgumentName;
import utilities.Logger;

/**
 * all the needed arguments, like Source folder, destination folder, ...
 * 
 */
public class CommandLineArguments {
	
	/**
	 * Enum to represent argument names
	 */
    public enum ArgumentName {
    	
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
         *  Folder and file name<br>
         *  WITHOUT EXTENSION --- .log WILL BE ADDED AUTOMATICALLY<br>
         *  
         *  Also date and time will be added
         */
        logFile,
        
        /**
         * F or I (f or i is also good)<br>
         * F for full<br>
         * I for incremental<br>
         */
        type
        
        // Add more argument names as needed
        
    }
    
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
        
    	if (
    			getArgumentValue(ArgumentName.source) == null ||
    			getArgumentValue(ArgumentName.destination) == null ||
    			getArgumentValue(ArgumentName.type) == null
    		) {
    		giveMinimumArgumentsInfo();
    		System.exit(0);
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
            synchronized (CommandLineArguments.class) {
                // Double-checking to ensure thread safety
                if (instance == null) {
                    instance = new CommandLineArguments();
                }
            }
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
                    giveMinimumArgumentsInfo();
                 }
             } else {
                    System.out.println("Invalid argument name: " + argName);
                    giveMinimumArgumentsInfo();
             }
                
            } else {
                System.out.println("Invalid argument format. Argument name must start with '--': " + arg);
                giveMinimumArgumentsInfo();
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

            case "logFile":
                // Process and validate logFile argument value
                configureLogFile(argValue);
                return true;
            case "type":
            	if (!(argValue.startsWith("f") || argValue.startsWith("F") || argValue.startsWith("I") || argValue.startsWith("i"))) {
            		return false;
            	}
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
    	System.out.println("  --source: location of the source data that you want to backup, it must be a folder name, the contents will be backedup");
    	System.out.println("  --destination: location of the destination to where you want to backup");
    	System.out.println("  --type: F for Full, I for incremental.");
    	System.out.println("");
    	System.out.println("Optional arguments:");
    	System.out.println("  --logfile: location of the logfile, just the folder name, it must exist.");
    }
    
}
