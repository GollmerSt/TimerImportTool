// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.main;

import java.util.ArrayList;
import java.util.Collection;

public class Versions {
	private static String VERSION = "01.04.01.rc1";
	private static final String DVBVIEWER_COM_DLL_VERSION = "1.00.05";

	public static String getVersion() {

		String[] splited = VERSION.split(".");

		if (splited.length > 3) {
			int pos = VERSION.lastIndexOf('.');
			return VERSION.substring(0, pos) + ' ' + VERSION.substring(pos) + 1;
		} else {
			return VERSION;
		}
	}

	public static String getDVBViewerCOMVersion() {
		return DVBVIEWER_COM_DLL_VERSION;
	}

	/**
	 * @return triple containing the version numbers
	 */
	public static int[] getIntVersion() {
		Collection<Integer> col = new ArrayList<>();
		String[] splited = VERSION.split("\\.");
		boolean beta = false;
		for (String part : splited) {
			int number = -1;
			try {
				number = Integer.parseUnsignedInt(part);
			} catch (NumberFormatException e) {
			}
			if (number >= 0) {
				col.add(number);
			} else {
				beta = true;
			}
		}
		col.add(beta ? 1 : 0);
		int [] out = new int[col.size()];
		int i = 0;
		for ( Integer o:col) {
			out[i++] = o.intValue();
		}
		return out;
	}
}
