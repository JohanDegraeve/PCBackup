package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;

@JsonSubTypes({
    @JsonSubTypes.Type(value = AFileWithLastModified.class, name = "afile"),
    @JsonSubTypes.Type(value = AFolderWithFullPath.class, name = "afolder")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AFileOrAFolderForFullPath {

	/**
	 * the path<br>
	 * - for folders: it will have the full path only if it contains at least one file, otherwise empty string<br>
	 * - for files: just the filename
	 */
	private String path;
	
	public AFileOrAFolderForFullPath(String path) {
		if (path == null) {throw new IllegalArgumentException("in constructor AFileOrAFolderForFullPath, path cannot be null");}
		this.path = path;
		
	}
	
	/**
	 * created to allow json deserialisation
	 */
	public AFileOrAFolderForFullPath() {}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
