package dvbviewertimerimport.misc;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Html {
	public static InputStream getStream(String urlString, boolean utf8) {
		return getStream(urlString, null, utf8);
	}

	public static InputStream getStream(String urlString, String urlType, boolean utf8) {
		return getStream(urlString, urlType, null, utf8);
	}

	public static InputStream getStream(String urlString, String urlType, String cookies, boolean utf8) {
		if (urlType != null)
			Log.out(true, urlType + " URL: " + urlString);
		URL url;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			throw new ErrorClass(e, "The URL \"" + urlString + "\" is malformed, the setup should be checked.");
		}

		URLConnection conn = null;
		InputStream result = null;

		try {
			conn = url.openConnection();
			if (cookies != null)
				conn.setRequestProperty("Cookie", cookies);
			if (utf8) {
				conn.setRequestProperty("Accept-Charset", "UTF-8");
				conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
			}
			//conn.connect();

			result = conn.getInputStream();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		/*
		 * try { result = url.openStream() ; } catch (IOException e) { throw new
		 * ErrorClass( e, "The URL \"" + urlString + "\" is not available." ); }
		 */
		return result;
	}

}
