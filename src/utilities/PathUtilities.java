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
	
}
