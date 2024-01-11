package model;

public class AFile extends AFileOrAFolder {

	/**
	 * when was the file last modified 
	 */
	private long ts;
	
	/**
	 * in which incremental backup can we find the latest version of the file<br>
	 * It's a path relative to the source folder of the backup. Meaning it doesn't include the source folder name, and it doesn't start with a slash
	 */
	private String pathToIncrementalBackup;

	/**
	 * creates a file with lastmodifedTimeStamp (ts)
	 * @param name 
	 * @param ts lastmodified timestamp, created shorter to save bytes in the json representation
	 * @param pathToIncrementalBackup
	 */
	public AFile(String name, long ts, String pathToIncrementalBackup) {

		super(name);
		
		if (ts == 0L) {throw new IllegalArgumentException("ts cannot be null");}
		if (pathToIncrementalBackup == null) {throw new IllegalArgumentException("pathToIncrementalBackup cannot be null");}
		
		this.ts = ts;
		this.pathToIncrementalBackup = "pathToIncrementalBackup";
	}

	/**
	 * @return the lastmodifedTimeStamp ts
	 * 
	 */
	public long getts() {
		return ts;
	}

	/**
	 * @param ts the lastmodifedTimeStamp to set
	 */
	public void setts(long ts) {
		if (ts == 0L) {throw new IllegalArgumentException("ts cannot be null");}
		this.ts = ts;
	}

	/**
	 * @return the pathToIncrementalBackup
	 */
	public String getbackup() {
		return pathToIncrementalBackup;
	}

	/**
	 * @param pathToIncrementalBackup the pathToIncrementalBackup to set
	 */
	public void setbackup(String pathToIncrementalBackup) {
		this.pathToIncrementalBackup = pathToIncrementalBackup;
	}

	@Override
	public boolean isFile() {
		return true;
	}

}