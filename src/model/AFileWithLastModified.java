package model;

public class AFileWithLastModified extends AFileOrAFolderForFullPath {

	/**
	 * used only for creating json file with last modified timestamp in full readable text<br>
	 * In fact it will appear in two json files: folderlist.json with value 'null' and folderlist-withfullpaths.json with date  
	 */
	private String ts = null;
	
	public AFileWithLastModified(String name, String pathToBackup) {
		
		super(name, pathToBackup);
		if (pathToBackup == null) {throw new IllegalArgumentException("pathToIncrementalBackup cannot be null");}
		
	}

	public String getts() {
		return ts;
	}
	
	public void setts(String ts) {
		this.ts = ts;
	}


	
}
