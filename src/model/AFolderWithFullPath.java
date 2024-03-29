package model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AFolderWithFullPath extends AFileOrAFolderForFullPath {

	/**
	 * the list of instance of AFileOrAFolder
	 */
	private List<AFileOrAFolderForFullPath> fileOrFolderList;

	public List<AFileOrAFolderForFullPath> getFileOrFolderList() {
		return fileOrFolderList;
	}

	public void setFileOrFolderList(List<AFileOrAFolderForFullPath> fileOrFolderList) {
		this.fileOrFolderList = fileOrFolderList;
	}

	public AFolderWithFullPath(String path, String pathToBackup) {
		
		super(path, pathToBackup);
		this.fileOrFolderList = new ArrayList<>();
	}

}
