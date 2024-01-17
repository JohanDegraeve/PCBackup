package utilities;

import java.util.ArrayList;

public class OtherUtilities {

	public static ArrayList<String> addString(ArrayList<String> source, String stringToAdd) {
		
		ArrayList<String> copyList = new ArrayList<>(source);
        copyList.add(stringToAdd);

		return copyList;
		
	}
}
