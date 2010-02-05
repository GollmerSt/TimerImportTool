// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package TVInfo ;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import Misc.* ;

public final class TVInfo {
	private final String userName ;
	private final String md5Hex ;
	private final String url ;
	
	public TVInfo( String userName,
			       String md5Hex,
			       String url )
	{
		this.userName = userName ;
		this.md5Hex   = md5Hex ;
		this.url      = url ;
	}
	public static String translateToMD5( String password )
	{
		MessageDigest md = null ;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		md.update( password.getBytes() ) ;
		return Conversions.bytesToString(md.digest()) ;
	}
	public InputStream connect() throws DigestException, NoSuchAlgorithmException
	{
		String completeURL = this.url + "?username=" + this.userName + "&password=" + this.md5Hex ;
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
