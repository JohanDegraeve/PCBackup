package utilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class WriteToFile {

    /**
     * write textToWrite to file, defined by path
     * @throws IOException 
     */
    public static void writeToFile(String textToWrite, String path) throws IOException {
    	
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        	
	    writer.write(textToWrite);
	    writer.flush();
	    
	    writer.close();
	    
    }
}
