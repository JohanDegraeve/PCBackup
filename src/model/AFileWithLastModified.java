package model;

public class AFileWithLastModified extends AFileOrAFolderForFullPath {

	/**
	 * used only for creating json file with last modified timestamp in full readable text<br>
	 * In fact it will appear in two json files: folderlist.json with value 'null' and folderlist-withfullpaths.json with date  
	 */
	private String ts = null;
	
	/**
	 * in which backup folder can we find the latest version of the file<br>
	 * for Folders: empty string
	 * It's a path relative to the source folder of the backup.
	 */
	private String pathToBackup;


	public AFileWithLastModified(String name, String pathToBackup) {
		
		super(name);
		if (pathToBackup == null) {throw new IllegalArgumentException("pathToIncrementalBackup cannot be null");}
		this.pathToBackup = pathToBackup;
	}

	public String getts() {
		return ts;
	}
	
	public void setts(String ts) {
		this.ts = ts;
	}
	public String getPathToBackup() {
		return pathToBackup;
	}

	public void setPathToBackup(String pathToBackup) {
		this.pathToBackup = pathToBackup;
	}



	
}
