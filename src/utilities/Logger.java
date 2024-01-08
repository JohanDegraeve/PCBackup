package utilities;

import model.CommandLineArguments;
import model.CommandLineArguments.ArgumentName;

public class Logger {

	public static String logFile = null;
	
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
