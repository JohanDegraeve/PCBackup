package model;

public abstract class AFileOrAFolder {

	/**
	 * name of the file or folder
	 */
	private String name;
	
	/**
	 * in which backup folder can we find the latest version of the file<br>
	 * It's a path relative to the source folder of the backup. Meaning it doesn't include the source folder name, and it doesn't start with a slash<br>
	 * It's something like '2023-12-06 18;24;41 (Full)' or '2023-12-28 17;07;13 (Incremental)'
	 */
	private String pathToBackup;

	public AFileOrAFolder(String name, String pathToBackup) {
		if (name == null) {throw new IllegalArgumentException("in constructor AFileOrAFolder, Name cannot be null");}
		if (pathToBackup == null) {throw new IllegalArgumentException("in constructor AFileOrAFolder, pathToBackup cannot be null");}
		this.name = name;
		this.pathToBackup = pathToBackup;
	}
	
	public String getPathToBackup() {
		return pathToBackup;
	}

	/**
	 * getter for name of the file or folder
	 * 
	 * @return the name of the file or the folder
	 */
    public String getName() {
        return name;
    }

    /**
     * setter for the name of the file or folder
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * to be implemented by subclass, if it's not a file, then it's a folder
     * @return
     */
    public abstract boolean isFile();

}
