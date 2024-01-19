package utilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class WriteToFile {

    /**
     * write textToWrite to file, defined by path
     */
    public static void writeToFile(String textToWrite, String path) {
    	
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
        	
            writer.write(textToWrite);
            writer.flush();
            Logger.log("Json written to file " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
