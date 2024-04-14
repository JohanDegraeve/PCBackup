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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import utilities.OtherUtilities;

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
         * in case restore is used, foldername where date should be restored to
         */
        restoreto,
        
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
        excludedpathlist,
        
        /**
         * when restoring files, should copy be done in overwrite or not
         */
        overwrite,
        
        /**
         * search text, in case type = S
         */
        searchtext,
        
        /**
         * folder where search results should be written
         */
        writesearchto,
        
        /**
         * for search, backups older than this date will not be searched in
         */
        startsearchdate,
        
        /**
         * for search, backups younger than this date will not be searched in
         */
        endsearchdate,
        
        /**
         * for testing purposes only, add the path length while converting a full directory a AFileOrAFolder
         */
        addpathlengthforallfolders,
        
        /**
         * for testing purposes only, add the path length while converting a full directory a AFileOrAFolder, only if the folder contains new or modified files
         */
        addpathlengthforfolderswithnewormodifiedcontent
        
    }
    
    /**
     * for search, backups younger than this date will not be searched in
     */
    public Date startSearchDate = new Date(0);
    
    /**
     * for search, backups younger than this date will not be searched in
     */
    public Date endSearchDate = new Date();
    
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
     * in case restore is used, foldername where date should be restored to
     */
    public String restoreto;
    
    /**
     * If true, full backup, if false, incremental backup
     */
    public boolean fullBackup = false;
    
    /**
     * is it a backup  or a restore
     */
    public boolean backup = true;
    
    /**
     * user will search through the backups, if true then backup value is ignored
     */
    public boolean search = false;
    
    /**
     *  Folder where logfile should be written<br>
     *  can be null, in that case log to System.out
     */
    private String logfilefolder;
    
    /**
     * path for logging, this includes the filename
     */
    public static Path logFilePathAsPath = null;
    
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
     * when restoring, should copy be done in overwrite mode or not
     */
    public boolean overwrite = false;
    
    /**
     * folder to store search results
     */
    public String writesearchto = null;
   
    /**
     * regex pattern to use in search
     */
	public Pattern searchTextPattern = null;
	
	/**
	 * for testing only
	 */
	public boolean addpathlengthforallfolders = false;
	
	/**
	 * for testing only
	 */
	public boolean addpathlengthforfolderswithnewormodifiedcontent = false;

    /**
     * text to search for, uses regex
     */
    private String searchText = null;

	/**
	 * valid argument names, build based on the Enum ArgumentName
	 */
	private static final Set<String> validArgumentNames = buildValidArgumentNamesSet();

	private static final Map<String, String> argumentMap = new HashMap<>();
	
	private static boolean argumentsInitialized = false;
	
	// Private static instance of the class
	private static volatile CommandLineArguments instance;
	
	// just to display selected strings at the end
	private static String excludedfilelist = null;
	
	// just to display selected strings at the end
	private static String excludedpathlist = null;
	
	// just to display selected strings at the end
	private static String foldernamemapping = null;
	
	/**
	 * Private constructor to prevent instantiation outside the class<br>
	 * Here the arguments are checked for validity
	 */
    private CommandLineArguments() {
        
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
    		} else if (getArgumentValue(ArgumentName.type).equalsIgnoreCase("S")) {
    			search = true;
    		} else {
    			System.out.println("Invalid value for type " + getArgumentValue(ArgumentName.type));
        		giveMinimumArgumentsInfo();System.exit(1);
    		}
    	}

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
        	
        	// check also that folderPath is a directory
        	if (!Files.isDirectory(folderPath)){
        		System.out.println(destination + " seems to be a file, not a folder. Check the argument 'destination'");
        		giveMinimumArgumentsInfo();System.exit(1);
        	}
        	
    	}
    	
    	restoreto = getArgumentValue(ArgumentName.restoreto);
    	if (restoreto == null && backup == false && search == false) {// it's not backup, it's not search, so it's restore, but restoreto is not given
    		System.out.println("restoreto argument is missing, you must specify the restoreto folder name if you want to restore data");
    		giveMinimumArgumentsInfo();System.exit(1);
    	} else if (restoreto != null ){
        	// check if restoreto folder exists
        	Path folderPath = Paths.get(restoreto);
        	if (!(Files.exists(folderPath))) {
        		System.out.println("folder " + restoreto + " does not exist. Create it first or check the argument 'restoreto'");
        		giveMinimumArgumentsInfo();System.exit(1);
        	}
        	
        	// check also that folderPath is a directory
        	if (!Files.isDirectory(folderPath)){
        		System.out.println(restoreto + " seems to be a file, not a folder. Check the argument 'restoreto'");
        		giveMinimumArgumentsInfo();System.exit(1);
        	}
        	
    	}
    	
    	writesearchto = getArgumentValue(ArgumentName.writesearchto);
    	if (writesearchto == null && search == true) {// it's search, so but writesearchto is not given
    		System.out.println("writesearchto argument is missing, you must specify the writesearchto folder name if you want to search");
    		giveMinimumArgumentsInfo();System.exit(1);
    	} else if (writesearchto != null ){
        	// check if writesearchto folder exists
        	Path folderPath = Paths.get(writesearchto);
        	if (!(Files.exists(folderPath))) {
        		System.out.println("folder " + writesearchto + " does not exist. Create it first or check the argument 'writesearchto'");
        		giveMinimumArgumentsInfo();System.exit(1);
        	}
        	
        	// check also that folderPath is a directory
        	if (!Files.isDirectory(folderPath)){
        		System.out.println(writesearchto + " seems to be a file, not a folder. Check the argument 'writesearchto'");
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
    	
    	excludedfilelist = getArgumentValue(ArgumentName.excludedfilelist);
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
    	
    	excludedpathlist = getArgumentValue(ArgumentName.excludedpathlist);
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
    	
    	String overwriteAsString = getArgumentValue(ArgumentName.overwrite); 
    	if (overwriteAsString != null) {
    		if (overwriteAsString.equalsIgnoreCase("true")) {
    			overwrite = true;
    		}
    	}
    	    	
    	String restoreDateAsString = getArgumentValue(ArgumentName.restoredate); 
    	if (restoreDateAsString != null) {
        	// try to parse the restore data
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.ARGUMENTDATEFORMAT_STRING);
            try {
    			restoreDate = dateFormat.parse(restoreDateAsString);
    		} catch (ParseException e) {
    			System.out.println("restoredate seems to be a wrong format. Expected format = YYYY-MM-DD-HH-mm-ss");
    			giveMinimumArgumentsInfo();System.exit(1);
    		}
    	}
    	
    	searchText = getArgumentValue(ArgumentName.searchtext);
    	if (search) {
    		if (searchText != null) {
    			if (searchText.length() == 0) {
    				System.out.println("searchtext seems an empty string, check the argument searchtext");
        			giveMinimumArgumentsInfo();System.exit(1);
    			} else {
    				searchTextPattern = Pattern.compile(searchText);
    			}
    		} else {
    			System.out.println("searchtext argument missing");
    			giveMinimumArgumentsInfo();System.exit(1);
    		}
    	}
    	
    	String endSearchDateAsString = getArgumentValue(ArgumentName.endsearchdate);
    	if (endSearchDateAsString != null) {
    		// try to parse the restore data
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.ARGUMENTDATEFORMAT_STRING);
            try {
    			endSearchDate = dateFormat.parse(endSearchDateAsString);
    		} catch (ParseException e) {
    			System.out.println("endsearchdate seems to be a wrong format. Expected format = " + Constants.ARGUMENTDATEFORMAT_STRING);
    			giveMinimumArgumentsInfo();System.exit(1);
    		}
    	}

    	String startSearchDateAsString = getArgumentValue(ArgumentName.startsearchdate);
    	if (startSearchDateAsString != null) {
    		// try to parse the restore data
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.ARGUMENTDATEFORMAT_STRING);
            try {
    			startSearchDate = dateFormat.parse(startSearchDateAsString);
    		} catch (ParseException e) {
    			System.out.println("startsearchdate seems to be a wrong format. Expected format = " + Constants.ARGUMENTDATEFORMAT_STRING);
    			giveMinimumArgumentsInfo();System.exit(1);
    		}
    	}

    	// if a restore is requested but the restoreDate is null, then stop
    	if (!backup && restoreDate == null) {
    		System.out.println("Type is restore but no restoredate is given. Add argument restoredate in format " + Constants.ARGUMENTDATEFORMAT_STRING);
    		giveMinimumArgumentsInfo();System.exit(1);
    	}
    	
    	String addpathlengthforallfoldersAsString = getArgumentValue(ArgumentName.addpathlengthforallfolders);
    	if (addpathlengthforallfoldersAsString != null) {
    		if (addpathlengthforallfoldersAsString.equalsIgnoreCase("true")) {
    			addpathlengthforallfolders = true;
    		}
    	}
    	
    	String addpathlengthforfolderswithnewormodifiedcontentAsString = getArgumentValue(ArgumentName.addpathlengthforfolderswithnewormodifiedcontent);
    	if (addpathlengthforfolderswithnewormodifiedcontentAsString != null) {
    		if (addpathlengthforfolderswithnewormodifiedcontentAsString.equalsIgnoreCase("true")) {
    			addpathlengthforfolderswithnewormodifiedcontent = true;
    		}
    	}
    	
    	subfolderToRestore = getArgumentValue(ArgumentName.subfoldertorestore);
    	if (subfolderToRestore == null) {subfolderToRestore = "";}
    	
    	foldernamemapping = getArgumentValue(ArgumentName.foldernamemapping);
    	if (folderNameMapping != null) {
    		folderNameMapping = readFolderNameMappings(foldernamemapping);
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
    	if (search) {
    		System.out.println("   Type:                                   search text through backups");
    		System.out.println("   Folder where backups are stored:        " + destination);
    		System.out.println("   Folder where results will be stored:    " + writesearchto);
    		System.out.println("   Searchtext:                             " + searchText);
    		System.out.println("   startSearchDate:                        " + OtherUtilities.dateToString(startSearchDate, Constants.OUTPUTDATEFORMAT_STRING));
    		System.out.println("   endSearchDate:                          " + OtherUtilities.dateToString(endSearchDate, Constants.OUTPUTDATEFORMAT_STRING));
    	} else if (backup) {
    		// BACKUP
    		if (fullBackup) {
    			System.out.println("   Type:                               full backup");	
    		} else {
    			System.out.println("   Type:                               incremental backup");
    		}
    		
    		    System.out.println("   Folder to backup:                   " + source);
    		    System.out.println("   Destination to backup to:           " + destination);
    		
    	} else {
    		// RESTORE
    		    System.out.println("   Folder where backups are stored:    " + destination);
    		    System.out.println("   Destination to restore to:          " + restoreto);
    		    SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.BACKUPFOLDERDATEFORMAT_STRING);
    			String backupfoldernameJustTheDate = dateFormat.format(restoreDate);
    		    System.out.println("   Restore date:                       " + backupfoldernameJustTheDate);
    		    System.out.println("   overwrite:                          " + (overwrite ? "Y":"N"));
    		    
    	}
    
    	if (logFilePathAsPath != null) {
    		    System.out.println("   Log file:                               " + logFilePathAsPath.toString());
    	} else {
    		    System.out.println("   Log file:                               none");
    	}
    	
    	if (!search) {
        	if (excludedfilelist != null) {
        		System.out.println("   excludedfilelist:                   " + excludedfilelist);
        	} else {
        		System.out.println("   excludedfilelist:                           none");
        	}
        	
        	if (excludedpathlist != null) {
        		System.out.println("   excludedpathlist:                   " + excludedpathlist);
        	} else {
        		System.out.println("   excludedpathlist:                   none");
        	}
        	
        	if (subfolderToRestore.length() > 0) {
        		System.out.println("   subfoldertorestore:                 " + subfolderToRestore);
        	} else {
        		System.out.println("   subfoldertorestore:                 none, which means the full backup will be restored");
        	}
        	
        	if (foldernamemapping != null) {
        		System.out.println("   foldernamemapping:                           " + foldernamemapping);
        	} else {
        		System.out.println("   foldernamemapping:                  none");
        	}
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
    	
    	// create filename
    	String logfileNameString = "PCBackup-" + OtherUtilities.dateToString(new Date(), Constants.LOGFILEDATEFORMAT_STRING) + ".log";
    	
    	// create the Path
    	logFilePathAsPath = Paths.get(logFilePath, logfileNameString);
    	
        System.out.println("Logging to file " + logFilePathAsPath);
        
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
            	if (!(argValue.startsWith("f") || argValue.startsWith("F") || argValue.startsWith("I") || argValue.startsWith("i") || argValue.startsWith("R") || argValue.startsWith("r") || argValue.startsWith("s") || argValue.startsWith("S"))) {
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
            	
            case "overwrite":
            	return true;
            	
            case "searchtext":
            	return true;
            	
            case "addpathlengthforallfolders":
            	return true;
            	
            case "addpathlengthforfolderswithnewormodifiedcontent":
            	return true;
            	
            case "restoreto":
            	return true;
            	
            case "writesearchto":
            	return true;
            	
            case "startsearchdate":
            	return true;
            	
            case "endsearchdate":
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
    	System.out.println("  --type: F for Full backup, I for incremental backup, R for restore, S for searching in backups.");
    	System.out.println("  --source:  the folder that you want to backup, the contents will be backed up");
    	System.out.println("            Not used for RESTORE and SEARCH");
    	System.out.println("  --destination: folder where you want to backup to");
    	System.out.println("            for BACKUP : folder where you want to backup to");
    	System.out.println("            for RESTORE: the folder where your backup is stored and from where restore will happen, ie the folder where you previously backed up to");
    	System.out.println("            for SEARCH:  the folder where your backup is stored and where the search will happen, ie the folder where you previously backed up to");
    	System.out.println("");
    	System.out.println("Optional arguments:");
    	System.out.println("  --restoreto: only for restore. This is the foldername where you want to restore to. You can use the original source folder, in that case you should add the argument 'overwrite' with value 'true'");
    	System.out.println("  --writesearchto: foldername where searchresults will be written to");
    	System.out.println("            the file with the search results will be named searchresults.csv. If that file already exists, then it will be named for intance searchresults (1).txt");
    	System.out.println("  --overwrite: only for restore. If value = true then files that already exist in the destination will be overwritten. Default n (no)");
    	System.out.println("  --logfilefolder: location of the logfile, just the folder name, it must exist.");
    	System.out.println("  --excludedfilelist: list of file names to exclude, exact match is applied, case sensitive.");
    	System.out.println("  --excludedpathlist: list of folder names to exclude, these are subfolder names, exact match is applied, case sensitive.");
    	System.out.println("  --restoredate: mandatory if type arguments = R. Date and time for which restore should occur. Format " + Constants.ARGUMENTDATEFORMAT_STRING);
    	System.out.println("  --subfoldertorestore: The specific folder within source that needs to be restored, If the complete backup needs to be restored, then omit this argument or give an empty string (\"\"). If a specific subfolder needs to be restored, then specify that folder here.");
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
    	System.out.println("  --searchtext: the text to search for, mandatory in case type = S");
    	System.out.println("                   the searchtext is handled as a regular expression.");
    	System.out.println("                   Examples:");
    	System.out.println("                      - to search for files with 'Trident' or 'Jabra', the searchtext you specify here would be '\\b(?:Trident|Jabra)\\b'");
    	System.out.println("                      - to search for files with 'Trident' and 'Jabra', the searchtext you specify here would be '(?=.*\\bTrident\\b)(?=.*\\bJabra\\b)'");
    	System.out.println("  --startsearchdate: when searching, only search in backups created after this date. Format = " + Constants.ARGUMENTDATEFORMAT_STRING + ". Default = 1 1 1970 ");
    	System.out.println("  --endsearchdate: when searching, only search in backups created after before or at this date. Format = " + Constants.ARGUMENTDATEFORMAT_STRING + ". Default = now");
    }
    
}
