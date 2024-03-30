package utilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import model.CommandLineArguments;

public class Logger {

	static BufferedWriter staticwriter = null;
	
	//@SuppressWarnings("static-access")
	public static void log(String texttoLog) {
		
		// always log to System.out
		System.out.println(texttoLog);
		
		if (CommandLineArguments.logFilePathAsPath != null) {
			
			try {
				
				if (staticwriter == null) {
					staticwriter = new BufferedWriter(new FileWriter(CommandLineArguments.logFilePathAsPath.toString()));
				}
				
				staticwriter.write(texttoLog + "\n");
				staticwriter.flush();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
		
		
	}
}
