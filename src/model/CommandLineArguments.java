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
         * "YYYY-MM-DD-HH-mm-ss"
         */
        restoredate,
        
        /**
         * The specific folder within source that needs to be restored<br>
         * If the complete backup needs to be restored, then omit this argument<br>
         * If a specific subfolder needs to be restored, then specify that folder here
         */
        subfoldertorestore,
        
        /**
         * Sometimes SharePoint sync gives different foldernames to the main folder. Example sometimes the Documents folder is named "XDrive Documents", but 
         * on another PC it may be named "XDrive Documenten"<br>
         * The app allows to do mapping of foldernames. This is only applicable to the initial folder, not the subfolders. It's a list of mappings. Example line:<br>
         * &nbsp &nbsp &nbsp &nbsp    XDrive Documents=XDrive Documenten<br>
         * When doing a backup:<br>
         * &nbsp &nbsp &nbsp &nbsp    When a folder in the source is named "XDrive Documents", then the file folderlist.json will be named "XDrive Documenten" and also in the backup there will be a folder with name "XDrive Documenten"<br>
         * &nbsp &nbsp &nbsp &nbsp    So when we reuse a hard disk with backups taken on another PC, and the folder on that disk is named "XDrive Documenten", while on the new PC 
         * where we do the backup, it's named "XDrive Documents", then we need to add the line 'XDrive Documents=XDrive Documenten' in the file that is specified here.<br>
         * &nbsp &nbsp &nbsp &nbsp    Then if we do a backup on that new PC, the app sees the folder XDrive Documents, then the file folderlist.json will contain XDrive Documenten. 
         */
        foldernamemapping,
        
        /**
         * paths to exclude, full paths that need to be exclude, starting from the main source folder, ie not start for instance with c:\\..
         */
        excludedpathlist
        
        
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
     * filenames that should be ignored, ie not added to the folderlist.json and not copied in backups<br>
     */
    public List<String> excludedFiles = new ArrayList<>();
    
    /**
     * paths that should be ignored, ie not added to the folderlist.json and not copied in backups<br>
     * These are full paths, starting from the main folder
     */
    public List<String> excludedPaths = new ArrayList<>();
    
    /**
     * only for restore, Date for which restore needs to be done
     */
    public Date restoreDate = null;
    
    /**
     * used in case of restore, can be an empty string<br>
     * not null<br>
     * Specifies the subfolder within the source to restore 
     */
    public String subfolderToRestore = "";
    
    /**
     * used to store foldername mappings folderNameMapping
     */
    public HashMap<String, String> folderNameMapping = new HashMap<>();
    
	/**
	 * valid argument names, build based on the Enum ArgumentName
	 */
	private static final Set<String> validArgumentNames = buildValidArgumentNamesSet();

	private static final Map<String, String> argumentMap = new HashMap<>();
	
	private static boolean argumentsInitialized = false;
	
	// Private static instance of the class
	private static volatile CommandLineArguments instance;
	
	/**
	 * Private constructor to prevent instantiation outside the class<br>
	 * Here the arguments are checked for validity
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
    	
    	String excludedfilelist = getArgumentValue(ArgumentName.excludedfilelist);
    	if (excludedfilelist != null) {
    		Path folderPath = Paths.get(excludedfilelist);
        	if (!(Files.exists(folderPath))) {
        		System.out.println("You specified file " + excludedfilelist + " as excludedfilelist but it does not exist. Create it first or check the argument 'excludedfilelist'");
        		giveMinimumArgumentsInfo();System.exit(1);
        	}
    		if (Files.isDirectory(folderPath)) {
    			System.out.println("You specified file " + excludedfilelist + " as excludedfilelist but it is a directory, not a file");
    			giveMinimumArgumentsInfo();System.exit(1);
    		}
        	try (BufferedReader reader = new BufferedReader(new FileReader(excludedfilelist))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    excludedFiles.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("IOException while opening file " + excludedfilelist);
				giveMinimumArgumentsInfo();System.exit(1);
            }
    	}
    	
    	String excludedpathlist = getArgumentValue(ArgumentName.excludedpathlist);
    	if (excludedpathlist != null) {
    		Path folderPath = Paths.get(excludedpathlist);
        	if (!(Files.exists(folderPath))) {
        		System.out.println("You specified file " + excludedpathlist + " as excludedpathlist but it does not exist. Create it first or check the argument 'excludedpathlist'");
        		giveMinimumArgumentsInfo();System.exit(1);
        	}
    		if (Files.isDirectory(folderPath)) {
    			System.out.println("You specified file " + excludedpathlist + " as excludedpathlist but it is a directory, not a file");
    			giveMinimumArgumentsInfo();System.exit(1);
    		}
        	try (BufferedReader reader = new BufferedReader(new FileReader(excludedpathlist))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    excludedPaths.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("IOException while opening file " + excludedpathlist);
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

    	String restoreDateAsString = getArgumentValue(ArgumentName.restoredate); 
    	if (restoreDateAsString != null && backup) {
            System.out.println("You gave a restore date but that's not necessary when doing a backup.");
			giveMinimumArgumentsInfo();System.exit(1);
    	} else if (restoreDateAsString != null) {
        	// try to parse the restore data
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.restoreDateFormat);
            try {
    			restoreDate = dateFormat.parse(restoreDateAsString);
    		} catch (ParseException e) {
    			e.printStackTrace();
                System.out.println("restoredate seems to be a wrong format. Expected format = YYYY-MM-DD-HH-mm-ss");
    			giveMinimumArgumentsInfo();System.exit(1);
    		}
    	}

    	// if a restore is requested but the restoreDate is null, then stop
    	if (!backup && restoreDate == null) {
    		System.out.println("Type is restore but no restoredate is given. Add argument restoredate in format " + Constants.restoreDateFormat);
    		giveMinimumArgumentsInfo();System.exit(1);
    	}
    	
    	subfolderToRestore = getArgumentValue(ArgumentName.subfoldertorestore);
    	if (subfolderToRestore == null) {subfolderToRestore = "";}
    	
    	String folderNameMappingPath = getArgumentValue(ArgumentName.foldernamemapping);
    	if (folderNameMapping != null) {
    		folderNameMapping = readFolderNameMappings(folderNameMappingPath);
    	}
    	
    	printeArgumentSummary();
    	
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

    private void printeArgumentSummary() {
    	
    	System.out.println("You selected following arguments");
    	if (backup) {
    		// BACKUP
    		if (fullBackup) {
    			System.out.println("   Type :                              full backup");	
    		} else {
    			System.out.println("   Type :                              incremental backup");
    		}
    		
    		    System.out.println("   Folder to backup :                  " + source);
    		    System.out.println("   Destination to backup :             " + destination);
    		
    	} else {
    		// RESTORE
    		    System.out.println("   Folder where backup is stored :     " + source);
    		    System.out.println("   Destination to restore to :         " + destination);
    		    SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.backupFolderDateFormat);
    			String backupfoldernameJustTheDate = dateFormat.format(restoreDate);
    		    System.out.println("   Restore date :                      " + backupfoldernameJustTheDate);
    		    
    	}
    
    	if (logfilefolder != null) {
    		    System.out.println("   Log file :                          " + logfilefolder);
    	} else {
    		    System.out.println("   Log file :                          none");
    	}
    	
    	System.out.println("");
    	
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
            	if (!(argValue.startsWith("f") || argValue.startsWith("F") || argValue.startsWith("I") || argValue.startsWith("i") || argValue.startsWith("R") || argValue.startsWith("r"))) {
            		return false;
            	}
            	return true;
            case "excludedfilelist":
            	return true;

            case "restoredate":
            	return true;
            	
            case "subfoldertorestore":
            	return true;
            	
            case "foldernamemapping":
            	return true;
            	
            case "excludedpathlist":
            	return true;
            	
            default:
                // Unknown argument name
                System.out.println("Unknown argument name: " + argName);
                return false;
        }
    }
    
    private static HashMap<String, String> readFolderNameMappings(String folderNameMappingPath) {
    	
    	HashMap<String, String> replacementMap = new HashMap<>();

    	if (folderNameMappingPath == null) {return replacementMap;}
    	
    	// Read the file line by line and process each line
        try (BufferedReader br = new BufferedReader(new FileReader(folderNameMappingPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Split the line by "="
                String[] parts = line.split("=");

                // Ensure there are two parts (key and value)
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    // Store key-value pair in the HashMap
                    replacementMap.put(key, value);
                } else {
                    // Handle invalid lines if necessary
                    System.out.println("Invalid line: " + line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to read  folderNameMapping "  + folderNameMappingPath);
            giveMinimumArgumentsInfo();System.exit(1);
        }
        
        return replacementMap;
        
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
    	System.out.println("  --excludedpathlist: full paths that need to be exclude, starting from the main source folder, ie not start for instance with c:\\..");
    	System.out.println("  --restoredate: mandatory if type arguments = R. Date and time for which restore should occur. Format " + Constants.restoreDateFormat);
    	System.out.println("  --subfoldertorestore: The specific folder within source that needs to be restored, If the complete backup needs to be restored, then omit this argument, If a specific subfolder needs to be restored, then specify that folder here.");
    	System.out.println("  --foldernamemapping:");
    	System.out.print(  "          Sometimes SharePoint sync gives different foldernames to the main folder. Example sometimes the Documents folder is named \"XDrive Documents\", but");
    	System.out.println(" on another PC it may be named \"XDrive Documenten\"");
    	System.out.println("          The app allows to do mapping of foldernames. This is only applicable to the initial folder, not the subfolders. It's a list of mappings. Example line:");
    	System.out.println("              XDrive Documents=XDrive Documenten");
    	System.out.println("              When doing a backup:");
    	System.out.println("              When a folder in the source is named \"XDrive Documents\", then the file folderlist.json will be named \"XDrive Documenten\" and also in the backup there will be a folder with name \"XDrive Documenten\"");
    	System.out.print(  "              So when we reuse a hard disk with backups taken on another PC, and the folder on that disk is named \"XDrive Documenten\", while on the new PC, ");
    	System.out.println("where we do the backup, it's named \"XDrive Documents\", then we need to add the line 'XDrive Documents=XDrive Documenten' in the file that is specified here.");
    	System.out.println("              Then if we do a backup on that new PC, the app sees the folder XDrive Documents, then the file folderlist.json will contain XDrive Documenten.");
    }
    
}
