package model;

//AFileOrAFolder class
public class AFile extends AFileOrAFolder {

	/**
	 * when was the file last modified 
	 */
	private long ts;
	
	/**
	 * creates a file with lastmodifedTimeStamp (ts)
	 * @param name 
	 * @param ts lastmodified timestamp, created shorter to save bytes in the json representation
	 * @param pathToBackup
	 */
	public AFile(String name, long ts, String pathToBackup) {

		super(name, pathToBackup);
		
		if (pathToBackup == null) {throw new IllegalArgumentException("pathToIncrementalBackup cannot be null");}
		
		this.ts = ts;
	}

	/**
	 * created to allow json deserialisation
	 */
	public AFile() {
		super();
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

}