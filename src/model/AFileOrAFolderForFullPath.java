package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;

@JsonSubTypes({
    @JsonSubTypes.Type(value = AFileWithLastModified.class, name = "afile"),
    @JsonSubTypes.Type(value = AFolderWithFullPath.class, name = "afolder")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AFileOrAFolderForFullPath {

	/**
	 * the path<br>
	 * - for folders: it will have the full path only if it contains at least one file, otherwise empty string<br>
	 * - for files: just the filename
	 */
	private String path;
	
	/**
	 * in which backup folder can we find the latest version of the file<br>
	 * for Folders: empty string
	 * It's a path relative to the source folder of the backup.
	 */
	private String pathToBackup;

	public AFileOrAFolderForFullPath(String path, String pathToBackup) {
		if (path == null) {throw new IllegalArgumentException("in constructor AFileOrAFolderForFullPath, path cannot be null");}
		if (pathToBackup == null) {throw new IllegalArgumentException("in constructor AFileOrAFolderForFullPath, pathToBackup cannot be null");}
		this.path = path;
		this.pathToBackup = pathToBackup;
	}
	
	/**
	 * created to allow json deserialisation
	 */
	public AFileOrAFolderForFullPath() {}

	public String getPathToBackup() {
		return pathToBackup;
	}

	public void setPathToBackup(String pathToBackup) {
		this.pathToBackup = pathToBackup;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
