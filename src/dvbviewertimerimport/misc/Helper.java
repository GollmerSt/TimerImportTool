// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public final class Helper {

	static byte[] intToBytes(int n) {
		byte[] result = new byte[4];
		result[3] = (byte) (n & 0xff);
		result[2] = (byte) ((n >> 8) & 0xff);
		result[1] = (byte) ((n >> 16) & 0xff);
		result[0] = (byte) ((n >> 24) & 0xff);
		return result;
	}

	public static byte[] longToBytes(long n) {
		byte[] result = new byte[8];
		result[7] = (byte) (n & 0xff);
		result[6] = (byte) ((n >> 8) & 0xff);
		result[5] = (byte) ((n >> 16) & 0xff);
		result[4] = (byte) ((n >> 24) & 0xff);
		result[3] = (byte) ((n >> 32) & 0xff);
		result[2] = (byte) ((n >> 40) & 0xff);
		result[1] = (byte) ((n >> 48) & 0xff);
		result[0] = (byte) ((n >> 56) & 0xff);
		return result;
	}

	public static String bytesToString(byte[] b) {
		String hex = "";
		for (int i = 0; i < b.length; i++) {
			hex += String.format("%02x", (int) b[i] & 0xff);
		}
		return hex;
	}

	public static String replaceDiacritical(final String s) {
		if (!s.matches(".*[�������].*")) // \u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df]") )
			return s;

		String result = s;

		result = result.replaceAll("\u00e4", "ae");
		result = result.replaceAll("\u00f6", "oe");
		result = result.replaceAll("\u00fc", "ue");
		result = result.replaceAll("\u00c4", "Ae");
		result = result.replaceAll("\u00d6", "Oe");
		result = result.replaceAll("\u00dc", "Ue");
		result = result.replaceAll("\u00df", "ss");

		// System.out.println( result ) ;

		return result;
	}

	public static <T> T getTheBestChoice(String search, Collection<T> list, int weightOfFirstChar, int charCount,
			Function rework) {
		ArrayList<T> objects = getTheBestChoices(search, list, weightOfFirstChar, charCount, rework);

		return objects != null ? objects.get(0) : null;
	}

	public static <T> ArrayList<T> getTheBestChoices(String search, Collection<T> list, int weightOfFirstChar,
			int charCount, Function reworkFunc) {
		return getTheBestChoices(search, list, weightOfFirstChar, charCount, reworkFunc, null);
	}

	public static <T> ArrayList<T> getTheBestChoices(String search, Collection<T> list, int weightOfFirstChar,
			int charCount, Function reworkFunc, Function weightFunc) {
		if (reworkFunc == null)
			reworkFunc = new Function();
		if (weightFunc == null)
			weightFunc = new Function();
		String string = reworkFunc.stringToString(search);
		ArrayList<T> results = new ArrayList<T>();
		int weightMax = -1;
		int minDiff = 99999;
		for (T choiceObject : list) {
			String choiceOrg = choiceObject.toString();
			String choice = reworkFunc.stringToString(choiceOrg);
			int weigthFirstChar = 0;
			try {
				if (string.trim().length() >= charCount && choice.trim().length() >= charCount)
					if (string.trim().substring(0, charCount).equalsIgnoreCase(choice.trim().substring(0, charCount)))
						weigthFirstChar = weightOfFirstChar;

			} catch (Throwable e) {
				e.printStackTrace();
			}

			ArrayList<Integer> partLength = getSplitedLength(string, choice, charCount);

			int weight = weightFunc.arrayIntToInt(partLength, weigthFirstChar, string, choiceOrg);

			if (weight > weightMax) {
				minDiff = 99999;
				results.clear();
				weightMax = weight;
			}
			if (weight == weightMax) {
				results.add(choiceObject);
				int diff = Math.abs(choiceObject.toString().length() - string.length());
				if (diff < minDiff)
					minDiff = diff;
			}
		}
		for (Iterator<T> it = results.iterator(); it.hasNext();) {
			T o = it.next();
			int d = Math.abs(o.toString().length() - string.length());
			if (minDiff != d)
				it.remove();
		}
		return results.size() == 0 ? null : results;
	}

	private static ArrayList<Integer> getSplitedLength(final String left, final String right, int minChar) {
		ArrayList<Integer> result = new ArrayList<Integer>();

		int maxEqualLength = -1;
		int maxStart = -1;
		int maxEnd = -1;
		int maxPos = -1;

		int length = Math.min(left.length(), right.length());

		for (int ib = 0; ib < left.length(); ib++) {
			int ie = ib + length > left.length() ? left.length() : ib + length;
			for (; ie > ib; ie--) {
				if (ie - ib < minChar)
					break;
				int pos = right.indexOf(left.substring(ib, ie));
				if (pos >= 0)
					if (maxEqualLength < ie - ib) {
						if (minChar > ie - ib)
							continue;
						maxEqualLength = ie - ib;
						maxStart = ib;
						maxEnd = ie;
						maxPos = pos;
					}
			}
		}
		length = maxEnd - maxStart;

		if (maxStart > 0 && maxPos > 0) {
			result.addAll(Helper.getSplitedLength(left.substring(0, maxStart), right.substring(0, maxPos), minChar));
		}
		if (maxStart >= 0) {
			result.add(length);

			if (maxEnd < left.length() && maxPos + length < right.length())
				result.addAll(
						Helper.getSplitedLength(left.substring(maxEnd), right.substring(maxPos + length), minChar));
		}
		return result;
	}

	public static long dayTimeToLong(String time) throws ParseException {
		String[] parts = time.split(":");
		if (parts.length != 2) {
			throw new ParseException("Illegal time format: " + time, 0);
		}
		long t = (Long.valueOf(parts[0]) * 60L + Long.valueOf(parts[1])) * 60L * 1000L;
		return t;
	}

	public static void downsize(final File file, long maxSize, long downSize) {
		if (file == null)
			return;

		if (file.length() <= maxSize)
			return;

		InputStream is;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			return;
		}

		String destination = file.getAbsolutePath() + "__TEMP";

		File tempFile = new File(destination);

		BufferedReader bufR = new BufferedReader(new InputStreamReader(is));

		FileWriter fstream = null;
		try {
			fstream = new FileWriter(destination, false);
			BufferedWriter bufW = new BufferedWriter(fstream);

			String line = null;

			String lineSeparator = System.getProperty("line.separator");

			long newSize = file.length();

			while ((line = bufR.readLine()) != null) {
				line += lineSeparator;
				if (newSize <= downSize)
					bufW.write(line);
				newSize -= line.length();
			}
			bufR.close();
			bufW.close();
		} catch (IOException e) {
			throw new ErrorClass("Unexpected error on writing file \"" + tempFile.getAbsolutePath() + "\".");
		}
		file.delete();
		tempFile.renameTo(file);
	}

	public static class Reference<T> {
		private T value;
		
		public Reference( T value ) {
			this.value = value;
		}
		
		public Reference() {
			this.value = null;
		}
		
		public T get() {
			return this.value;
		}
		
		public void setValue( T value) {
			this.value = value;
		}
	}
	

	
	// TV-Info schickt an den Browser (Firefox, Edge ...) sch�n UTF-8 mit richtigem XML-Header.
	// Per Java jedoch immer ISO ..., was der XML Reader dann komplett verw�rfelt. Als Workaround
	// wird diese Verw�rfelung r�ckg�ngig gemacht, so dass die hier definierte Sonderzeichen
	// meist richtig dargestellt werden. Selten fehlen dadurch aber Zeichen komplett. Evtl. muss dazu
	// direkt im Stream eingegriffen werden

	public static String utf8Workaround(String in) {
		
		StringBuilder builder = new StringBuilder();
		
		Charset charset = Charset.forName("UTF-8");
				
		for ( int pos = 0 ; pos < in.length();  ) {
			int c = (int)in.charAt(pos++);
			int length ;
			byte [] convert = new byte[4];
			
			if ( c < 0x80 ) {
				length = 1 ;
			} else if ( (c & 0xe0) == 0xc0 ){
				length = 2;
			} else if ( (c & 0xf0) == 0xe0 ){
				length = 3;
			} else if ( (c & 0xf8) == 0xf0 ){
				length = 4;
			} else {
				length = 0;
			}
			
			convert[0] = (byte) c;
			boolean spaces = false ;
			
			for ( int cnt = 1 ; cnt < length ; ++cnt ) {
				int c1 = (int)in.charAt(pos++);
				if ( c1 == 0x20 ) {
					c1 = (int)in.charAt(pos++);
					spaces = true;
				}
				convert[cnt] = (byte) c1;
			}
			if ( spaces ) {
				++pos;
			}
			builder.append(new String(convert,0,length,charset)) ;
		}
		
		System.out.println(builder.toString());
		return builder.toString();
	}
	


}
