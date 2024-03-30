package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;

@JsonSubTypes({
    @JsonSubTypes.Type(value = AFileWithLastModified.class, name = "afile"),
    @JsonSubTypes.Type(value = AFolderWithFullPath.class, name = "afolder")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AFileOrAFolderForFullPath {

	public AFileOrAFolderForFullPath() {}
	
}
