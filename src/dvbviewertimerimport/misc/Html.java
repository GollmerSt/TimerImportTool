package dvbviewertimerimport.misc;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class Html {
	public static InputStream  getStream( String urlString )
	{
		return getStream( urlString, null ) ;
	}

	public static InputStream  getStream( String urlString, String urlType )
	{
		if ( urlType != null )
			Log.out( true, urlType + " URL: " + urlString ) ;
		URL url;
		try {
			url = new URL( urlString );
		} catch (MalformedURLException e) {
			throw new ErrorClass( e, "The URL \"" + urlString + "\" is malformed, the setup should be checked." ) ;
		}

		InputStream result = null ;
		try {
			result = url.openStream() ;
		} catch (IOException e) {
			throw new ErrorClass( e, "The URL \"" + urlString + "\" is not available." );
		}
		return result ;
	}

}
