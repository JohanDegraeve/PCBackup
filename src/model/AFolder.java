package model;

import java.util.List;
import java.util.ArrayList;

public class AFolder extends AFileOrAFolder {

	/**
	 * the list of instance of AFileOrAFolder
	 */
	private List<AFileOrAFolder> fileOrFolderList;
	
	public AFolder(String name) {
		
		super(name);
		// Initialize the attribute with an empty list
        this.fileOrFolderList = new ArrayList<>();
        
	}

	/**
	 * @return the fileOrFolderList
	 */
	public List<AFileOrAFolder> getFileOrFolderList() {
		return fileOrFolderList;
	}

	/**
	 * @param fileOrFolderList the fileOrFolderList to set
	 */
	public void setFileOrFolderList(List<AFileOrAFolder> fileOrFolderList) {
		this.fileOrFolderList = fileOrFolderList;
	}
	
	/**
	 * Method to add a file or folder to the list 
	 * @param fileOrFolder
	 */
    public void addFileOrFolder(AFileOrAFolder fileOrFolder) {
        fileOrFolderList.add(fileOrFolder);
    }
}
