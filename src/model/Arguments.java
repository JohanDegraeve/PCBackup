package model;

/**
 * all the needed arguments, like Source folder, destination folder, ...
 * 
 */
public class Arguments {

	public static String sourcePath = "C:\\Program Files\\Java\\jdk-21";

	// Private static instance of the class
	private static Arguments instance;
	
	// Private constructor to prevent instantiation outside the class
    private Arguments() {
        // Initialization code, if needed
    }
    
    public static Arguments getInstance() {
        if (instance == null) {
            synchronized (Arguments.class) {
                // Double-checking to ensure thread safety
                if (instance == null) {
                    instance = new Arguments();
                }
            }
        }
        return instance;
    }
}
