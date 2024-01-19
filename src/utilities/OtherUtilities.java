package utilities;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OtherUtilities {

	public static ArrayList<String> addString(ArrayList<String> source, String stringToAdd) {
		
		ArrayList<String> copyList = new ArrayList<>(source);
        copyList.add(stringToAdd);

		return copyList;
		
	}
	
	
	public static boolean fileNeedsToBeIgnored(String fileName) {
		
		if (fileIsWindowsHiddenFile(fileName)) {return true;}
		
		return false;
		
	}
	
	/**
	 * example files of format .849C9593-D756-4E56-8D6E-42412F2A707B need to be ignored
	 * @param fileName
	 * @return
	 */
	private static boolean fileIsWindowsHiddenFile(String fileName) {
		
		if (!(fileName.startsWith("."))) {return false;}
			
		if (!(fileName.length() == 37)) {return false;}	
		
		// Define the regex pattern
        String pattern = "^\\.[A-F0-9]{8}-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{12}$";

        // Create a Pattern object
        Pattern regex = Pattern.compile(pattern);

        // Create a Matcher object
        Matcher matcher = regex.matcher(fileName);

        // Check if the filename matches the pattern
        return matcher.matches();
		
	}
	
	/**
	 * recursively copying a folder from a source directory to a destination directory
	 * @param source
	 * @param destination
	 * @throws IOException
	 */
	public static void copyFolder(Path source, Path destination) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // Create corresponding directory in the destination
                Path targetDir = destination.resolve(source.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // Copy each file to the destination
                Files.copy(file, destination.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
    }
	
}
