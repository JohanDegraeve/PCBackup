package utilities;

import java.nio.file.Path;
import java.util.List;

public class PathUtilities {

	public static Path concatenatePaths(Path basePath, List<String> subfolders) {
        // Start with the base path
        Path resultPath = basePath;

        // Iterate through the subfolders and resolve each one
        for (String subfolder : subfolders) {
            resultPath = resultPath.resolve(subfolder);
        }

        return resultPath;
    }	
	
	/**
	 * say Path is a relative path, example subfolder1/subfolder2/subfolder3<br>
	 * this function will split into an array of Paths, one per subfolder
	 * @param path
	 * @return
	 */
	public static Path[] splitPath(Path path) {
        // Get the number of elements in the path
        int count = path.getNameCount();

        // Create an array of Path objects
        Path[] subfolders = new Path[count];

        // Iterate through the path's elements and store them in the array
        for (int i = 0; i < count; i++) {
            subfolders[i] = path.getName(i);
        }

        return subfolders;
    }
	
}
