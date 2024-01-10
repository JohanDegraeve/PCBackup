package utilities;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import model.CommandLineArguments;
import model.CommandLineArguments.ArgumentName;

public class Logger {

	public static String logFile = null;
	
	/**
	 * assigns logFile to path. path must include a full filename without extension. <br>
	 * Date YYYY-MM-DD-HH-MM-SS will be added and ".log"
	 * @param path
	 */
	public static void configureLogFile(String path) {
		
		// file will be in format path-yyyy-MM-dd-HH-mm-ss.log
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String fullPath = path + "-" + dateFormat.format(new Date()) + ".log";
		File file = new File(fullPath);

        try {
            if (file.createNewFile()) {
                System.out.println("File created: " + file.getAbsolutePath());
                logFile = fullPath;
            } else {
                System.out.println("File already exists: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 
	}
	
	public static void log(String texttoLog) {
		
		if (logFile == null) {
			System.out.println(texttoLog);
			return;
		}
		
		if (CommandLineArguments.getInstance().getArgumentValue(ArgumentName.logFile) != null) {
			// TODO : write to logfile
		}
		
		
	}
}
