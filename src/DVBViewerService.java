// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;




public class DVBViewerService {
	private final String url ;
	private final String userName ;
	private final String password ;
	private boolean enableWOL = false ;
	private String broadCastAddress ;
	private String macAddress ;
	private int waitTimeAfterWOL ;
	DVBViewerService( String url, String name, String password)
	{
		this.url = url ;
		this.userName = name ;
		this.password = password ;
		Authenticator.setDefault( new DVBViewerService.MyAuthenticator( this.userName, this.password ) ) ; 

	}
	private class MyAuthenticator extends Authenticator
	{
		private final String user ;
		private final String password ;
		
		public MyAuthenticator( String user, String password)
		{
			super() ;
			this.user     = user ;
			this.password = password ;
		}
		protected PasswordAuthentication getPasswordAuthentication() 
		{ 
			//System.out.println( "Hier erfolgt die Authentifizierung" ) ;
			//System.out.printf( "url=%s, host=%s, ip=%s, port=%s%n", 
	        //               getRequestingURL(), getRequestingHost(), 
	        //               getRequestingSite(), getRequestingPort() ); 
	 
			return new PasswordAuthentication( this.user, this.password.toCharArray() ); 
		}
	}
	public void setEnableWOL( boolean e ) { this.enableWOL = e ; } ;
	public void setBroadCastAddress( String b ) { this.broadCastAddress = b ; } ;
	public void setMacAddress( String m ) { this.macAddress = m ; } ;
	public void setWaitTimeAfterWOL( int w ) { this.waitTimeAfterWOL = w ; } ;
	public void setTimerEntry( String channel, String title, long start, long end ) throws IOException
	{
		String completeURL = "http://" + this.url ;
		completeURL += "/API/timeradd.html?ch=" + URLEncoder.encode( channel, "UTF-8" ) ;
		completeURL += "&dor=" + Conversions.longToSvcDateString( start ) ;
		completeURL += "&encoding=255" ;
		completeURL += "&enable=1" ;
		completeURL += "&start=" + Conversions.longToSvcMinutesString( start ) ;
		completeURL += "&stop=" + Conversions.longToSvcMinutesString( end ) ;
		completeURL += "&title=" + URLEncoder.encode( title, "UTF-8" ) ;
		Log.out( true, completeURL ) ;
		
		URL dvbViewerServiceURL = new URL( completeURL ) ;

		InputStream input = null ;
		int repeat = 0 ;
		while ( true )
		{
			try {
				input = dvbViewerServiceURL.openStream();
			} catch ( ProtocolException e1) {
				throw new ErrorClass("Authenticator error on access to the DVBViewerService. Username/password should be checked.");
			} catch (IOException e) {
				if ( this.enableWOL && repeat < 1 )
				{
					if ( repeat == 0 )
					{
						WakeOnLan.execute( this.broadCastAddress, this.macAddress ) ;
						try {
							Thread.sleep( this.waitTimeAfterWOL * 1000 ) ;
						} catch (InterruptedException e1) { }
					}
					repeat++ ;
					continue ;
				}
				throw new ErrorClass(e,"DVBViewerService not responding, DVBViewerService not alive or URL not correct?"); 
			}
			break ;
		}
		BufferedReader b = new BufferedReader(new InputStreamReader(input));
		String line = b.readLine() ;
		if ( line != null )
		{
			Log.out( "Unexpected response on access to URL \""
		            + completeURL + "\": " ) ;
			ErrorClass.setWarníng() ;
			{
				Log.out( line );
			} while ( ( line = b.readLine() ) != null ) ;
		}
	} ;
}
