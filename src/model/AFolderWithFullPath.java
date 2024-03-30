package model;

import java.util.ArrayList;
import java.util.List;

public class AFolderWithFullPath extends AFileOrAFolderForFullPath {

	/**
	 * the path<br>
	 * - for folders: it will have the full path only if it contains at least one file, otherwise empty string<br>
	 * - for files: just the filename
	 */
	private String path;

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
		
		this.path = path;
		this.fileOrFolderList = new ArrayList<>();
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
