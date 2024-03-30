package model;

import java.util.ArrayList;
import java.util.List;

public class AFolderWithFullPath extends AFileOrAFolderForFullPath {

	/**
	 * the path: it will have the full path only if it contains at least one file, otherwise just the subfolder name<br>
	 */
	private String path;

	/**
	 * the list of instances of AFileOrAFolder
	 */
	private List<AFileOrAFolderForFullPath> fileOrFolderList;

	public AFolderWithFullPath(String path, String pathToBackup) {
		
		this.path = path;
		this.fileOrFolderList = new ArrayList<>();
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<AFileOrAFolderForFullPath> getFileOrFolderList() {
		return fileOrFolderList;
	}

	public void setFileOrFolderList(List<AFileOrAFolderForFullPath> fileOrFolderList) {
		this.fileOrFolderList = fileOrFolderList;
	}

}
