package dvbviewertimerimport.misc;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Html {
	public static InputStream  getStream( String urlString )
	{
		return getStream( urlString, null ) ;
	}

	public static InputStream  getStream( String urlString, String urlType )
	{
		return getStream( urlString, urlType, null ) ;
	}

	public static InputStream  getStream( String urlString, String urlType, String cookies )
	{
		if ( urlType != null )
			Log.out( true, urlType + " URL: " + urlString ) ;
		URL url;
		try {
			url = new URL( urlString );
		} catch (MalformedURLException e) {
			throw new ErrorClass( e, "The URL \"" + urlString + "\" is malformed, the setup should be checked." ) ;
		}
		
		URLConnection conn = null ;
		InputStream result = null ;
		
		try {
			conn = url.openConnection();
			if ( cookies != null )
				conn.setRequestProperty("Cookie", cookies );
			conn.connect() ;
			result = conn.getInputStream() ;
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
/*
		try {
			result = url.openStream() ;
		} catch (IOException e) {
			throw new ErrorClass( e, "The URL \"" + urlString + "\" is not available." );
		}
*/
		return result ;
	}

}
