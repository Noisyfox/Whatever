package mynuaa.whatever;

import java.util.ArrayList;
import java.util.List;

public class CreateDataFactory {
	public static List<String> createUpdateData(int currentPage, int pageSize) {
		List<String> list = new ArrayList<String>();
		for (int i = (currentPage - 1) * pageSize; i < currentPage * pageSize; i++) {
			list.add("µÚ " + (i + 1) + " ÌõÄäÃû×´Ì¬¡£");
		}
		return list;
	}
}
