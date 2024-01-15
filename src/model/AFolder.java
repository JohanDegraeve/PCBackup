package model;

import java.util.List;

import java.util.ArrayList;

//AFileOrAFolder class
public class AFolder extends AFileOrAFolder {

	/**
	 * the list of instance of AFileOrAFolder
	 */
	private List<AFileOrAFolder> fileOrFolderList;
	
	/**
	 * created to allow json deserialisation
	 */
	public AFolder() {
		super();
	}
	
	public AFolder(String name, String pathToBackup) {
		
		super(name, pathToBackup);
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
	 * Method to add a file or folder to the list 
	 * @param fileOrFolder
	 */
    public void addFileOrFolder(AFileOrAFolder fileOrFolder) {
        fileOrFolderList.add(fileOrFolder);
    }
    
}
