// $LastChangedDate: 2010-01-28 09:11:48 +0100 (Do, 28 Jan 2010) $
// $LastChangedRevision: 15 $
// $LastChangedBy: Stefan Gollmer $


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class TVInfo {
	private String userName ;
	private String password ;
	private String url ;
	
	public TVInfo( String userName,
			       String password,
			       String url )
	{
		this.userName      = userName ;
		this.password      = password ;
		this.url           = url ;
	}
	public InputStream connect() throws DigestException, NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("MD5") ;
		md.update(this.password.getBytes()) ;
		
		String hex = Conversions.bytesToString(md.digest()) ;
		String completeURL = this.url + "?username=" + this.userName + "&password=" + hex ;
		System.out.print( completeURL + "\n") ;
		
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
