package model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AFileWithLastModified extends AFile {

	/**
	 * used only for creating json file with last modified timestamp in full readable text<br>
	 * In fact it will appear in two json files: folderlist.json with value 'null' and folderlist-withfullpaths.json with date  
	 */
	private String lastmodified = null;
	
	// to avoid that ts (inherited from superclass) gets in json string, when writing an object of type AFileWithLastModified to json
	@JsonIgnore
	private long ts;
	
	public AFileWithLastModified(String name, String pathToBackup) {
		
		super(name, 0L, pathToBackup);
		

		if (pathToBackup == null) {throw new IllegalArgumentException("pathToIncrementalBackup cannot be null");}
	}

	public String getlastmodified() {
		return lastmodified;
	}
	
	public void setlastmodified(String lastmodified) {
		this.lastmodified = lastmodified;
	}


	
}
