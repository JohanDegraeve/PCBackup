package model;

public class AFileWithLastModified extends AFileOrAFolderForFullPath {

	/**
	 * the name of the file
	 */
	private String name;
	
	/**
	 * the timestamp the file was created or modified, in human readable, local format<br>
	 */
	private String ts = null;

	/**
	 * in which backup folder can we find the latest version of the file<br>
	 * It's a path relative to the source folder of the backup.
	 */
	private String pathToBackup;

	public AFileWithLastModified(String name, String pathToBackup) {
		
		this.name = name; 
		this.pathToBackup = pathToBackup;
		
	}

	public String getts() {
		return ts;
	}
	
	public void setts(String ts) {
		this.ts = ts;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getPathToBackup() {
		return pathToBackup;
	}

	public void setPathToBackup(String pathToBackup) {
		this.pathToBackup = pathToBackup;
	}

}
