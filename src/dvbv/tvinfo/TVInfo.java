// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.tvinfo ;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dvbv.misc.* ;
import dvbv.provider.Provider;

public final class TVInfo extends Provider {	
	public TVInfo()
	{
		super( true, true, "TVInfo" ) ;
	}
	private String getMD5()
	{
		if ( ! this.isValid )
			throw new ErrorClass( "Provider \"TVInfo\" data is missing in the control file" ) ;
		MessageDigest md = null ;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		md.update( this.password.getBytes() ) ;
		return Conversions.bytesToString(md.digest()) ;
	}
	public InputStream connect() throws DigestException, NoSuchAlgorithmException
	{
		String completeURL = this.url + "?username=" + this.username + "&password=" + this.getMD5() ;
		Log.out( true, "TVInfo URL: " + completeURL ) ;
		
		URL tvInfoURL;
		try {
			tvInfoURL = new URL( completeURL );
		} catch (MalformedURLException e2) {
			// TODO Auto-generated catch block
			throw new ErrorClass( e2, "TVInfo URL not correct, the TVInfo should be checked." ) ;
		}

		URLConnection tvInfo = null ;
		InputStream result = null ;
		try {
			tvInfo = tvInfoURL.openConnection();
			result = tvInfo.getInputStream() ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new ErrorClass( e, "TVInfo data not available." );
		}
		return result ;
	}
}
