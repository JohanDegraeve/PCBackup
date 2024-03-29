package model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public class AFolderWithFullPath extends AFolder {

	public String path;
	
	@JsonIgnore
	private String name;

	public AFolderWithFullPath(String name, String pathToBackup) {
		
		super(name, pathToBackup);
        
	}

	/**
	 * @return the path
	 */
	@JsonValue
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
	
}
