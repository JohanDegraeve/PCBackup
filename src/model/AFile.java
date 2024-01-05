package model;

import java.nio.file.attribute.FileTime;

public class AFile extends AFileOrAFolder {

	/**
	 * when was the file last modified 
	 */
	private FileTime lastmodifedTimeStamp;
	
	/**
	 * in which incremental backup can we find the latest version of the file<br>
	 * It's a path relative to the source folder of the backup. Meaning it doesn't include the source folder name, and it doesn't start with a slash
	 */
	private String pathToIncrementalBackup;

	/**
	 * creates a file with lastmodifedTimeStamp
	 * @param name 
	 * @param lastmodifedTimeStamp
	 * @param pathToIncrementalBackup
	 */
	public AFile(String name, FileTime lastmodifedTimeStamp, String pathToIncrementalBackup) {

		super(name);
		
		if (lastmodifedTimeStamp == null) {throw new IllegalArgumentException("lastmodifedTimeStamp cannot be null");}
		if (pathToIncrementalBackup == null) {throw new IllegalArgumentException("pathToIncrementalBackup cannot be null");}
		
		this.lastmodifedTimeStamp = lastmodifedTimeStamp;
		this.pathToIncrementalBackup = pathToIncrementalBackup;
	}

	/**
	 * @return the lastmodifedTimeStamp
	 */
	public FileTime getLastmodifedTimeStamp() {
		return lastmodifedTimeStamp;
	}

	/**
	 * @param lastmodifedTimeStamp the lastmodifedTimeStamp to set
	 */
	public void setLastmodifedTimeStamp(FileTime lastmodifedTimeStamp) {
		this.lastmodifedTimeStamp = lastmodifedTimeStamp;
	}

	/**
	 * @return the pathToIncrementalBackup
	 */
	public String getPathToIncrementalBackup() {
		return pathToIncrementalBackup;
	}

	/**
	 * @param pathToIncrementalBackup the pathToIncrementalBackup to set
	 */
	public void setPathToIncrementalBackup(String pathToIncrementalBackup) {
		this.pathToIncrementalBackup = pathToIncrementalBackup;
	}

}