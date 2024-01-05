package model;

public abstract class AFileOrAFolder {

	/**
	 * name of the file or folder
	 */
	private String name;
	
	public AFileOrAFolder(String name) {
		if (name == null) {throw new IllegalArgumentException("Name cannot be null");}
		this.name = name;
	}
	
	/**
	 * getter for name of the file or folder
	 * 
	 * @return the name of the file or the folder
	 */
    public String getName() {
        return name;
    }

    /**
     * setter for the name of the file or folder
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
   
}
