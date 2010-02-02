// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package DVBViewer ;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import Misc.* ;

public class DVBViewerService {
	private final String url ;
	private final String userName ;
	private final String password ;
	private boolean enableWOL = false ;
	private String broadCastAddress ;
	private String macAddress ;
	private int waitTimeAfterWOL ;
	private String lastURL ;
	private final Stack<String> pathTimer ;
	private final Stack<String> pathChannel ;
	private final Stack<String> pathID ;
	private final Stack<String> pathDescr ;
	private final long version ;
	
	public DVBViewerService( String url, String name, String password)
	{
		this.url = url ;
		this.userName = name ;
		this.password = password ;
		Authenticator.setDefault( new DVBViewerService.MyAuthenticator( this.userName, this.password ) ) ; 

		Stack<String> p = new Stack<String>() ;
		Collections.addAll( p, "Timers", "Timer" ) ;
		this.pathTimer = p ;

		p = new Stack<String>() ;
		Collections.addAll( p, "Timers", "Timer", "ID" ) ;
		this.pathID = p ;

		p = new Stack<String>() ;
		Collections.addAll( p, "Timers", "Timer", "Channel" ) ;
		this.pathChannel = p ;

		p = new Stack<String>() ;
		Collections.addAll( p, "Timers", "Timer", "Descr" ) ;
		this.pathDescr = p ;
		
		this.version = this.readVersion() ;
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
	private InputStream connect( String command, String query)
	{
		String completeURL = "http://" + this.url ;
		completeURL       += "/API/" + command + ".html" ;
		if ( query.length() != 0 )
			completeURL   += "?" + query ;
		
		this.lastURL = completeURL ;
		Log.out( true, completeURL ) ;
		
		URL dvbViewerServiceURL = null ;
		try {
			dvbViewerServiceURL = new URL( completeURL );
		} catch (MalformedURLException e2) {
			throw new ErrorClass( e2, "Error on building the URL of the service, check the format of the service-ip address." );
		}
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
		return input ;
	}
	private InputStream connect( String command) { return this.connect(command, "" ) ; } ;
	private long readVersion()
	{
		InputStream input = connect( "version" ) ;

		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		
		String version = null ;
		try {
			XMLEventReader  reader = inputFactory.createXMLEventReader( input );
			String actKey = "" ;
			while( reader.hasNext() ) {
				XMLEvent ev = reader.nextEvent();
				if ( ev.isStartElement() )
						actKey = ev.asStartElement().getName().getLocalPart() ;
				if ( ev.isCharacters() )
					if ( actKey == "version" )
						version = ev.asCharacters().getData();
			}
			reader.close();
		} catch (XMLStreamException e) {
			throw new ErrorClass( e,   "Error on reading the Service data." 
	                 + " Position: Line = " + Integer.toString( e.getLocation().getLineNumber() )
	                 +        ", column = " + Integer.toString(e.getLocation().getColumnNumber()) ) ;
		}
		Pattern pattern = Pattern.compile( "(\\d+\\.\\d+\\.\\d+\\.\\d+)" ) ;
		Matcher matcher = pattern.matcher(version) ;
		
		if ( matcher.find() )
			version = matcher.group(0) ;
		String splitted[] = version.split("\\." )  ;
		long result = 0 ;
		for( int i = 0 ; i < splitted.length - 1 ; i++ )
			result = result*100 + Long.valueOf( splitted[i] ) ;
		result = result * 1000 + Long.valueOf( splitted[ splitted.length - 1 ] ) ;
		
		return result ;
	}
	public void setTimerEntry( DVBViewerEntry e ) throws IOException
	{
		if ( e.mustIgnored() )
			return ;
		String query = "ch=" + URLEncoder.encode( e.getChannel(), "UTF-8" ) ;
		query += "&dor=" + Conversions.longToSvcDateString( e.getStart() ) ;
		query += "&encoding=255" ;
		query += "&start=" + Conversions.longToSvcMinutesString( e.getStart() ) ;
		query += "&stop=" + Conversions.longToSvcMinutesString( e.getEnd() ) ;
		String title = e.getTitle() ;
		if ( this.version <= 10500077 )
			title = Conversions.replaceDiacritical( title ) ;
		
		query += "&title=" + URLEncoder.encode( title , "UTF-8" ) ;
		
		String command = null ;
		
		if ( e.isEnabled() )
			query += "&enable=1" ;
		else
			query += "&enable=0" ;
		
		if ( e.mustUpdated() )
		{
			command = "timeredit" ;
			query += "&id=" + Long.toString( e.getID() ) ;
		}
		else
			command = "timeradd" ;

		
		InputStream input = connect( command, query) ;

		BufferedReader b = new BufferedReader(new InputStreamReader(input));
		String line = b.readLine() ;
		if ( line != null )
		{
			Log.out( "Unexpected response on access to URL \""
		            + this.lastURL + "\": " ) ;
			ErrorClass.setWarníng() ;
			{
				Log.out( line );
			} while ( ( line = b.readLine() ) != null ) ;
		}
	} ;
	public ArrayList<DVBViewerEntry> readTimers()
	{
		ArrayList<DVBViewerEntry> result = new ArrayList<DVBViewerEntry>() ;
		InputStream iS = connect( "timerlist" ) ; 
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader  reader = inputFactory.createXMLEventReader( iS );
			Stack<String>   stack = new Stack<String>() ;
			
			boolean enable     = true ;
			String channel     = null;
			String dateString  = null ;
			String startString = null ;
			String endString   = null ;
			String title       = null ;
			long id            = -1 ;
			
			while( reader.hasNext() ) {
				XMLEvent ev = reader.nextEvent();
				if( ev.isStartElement() )
				{
					int type = -1 ;
					stack.push( ev.asStartElement().getName().getLocalPart() );
					if ( stack.equals( this.pathTimer ) )
					{
						enable      = true ;
						channel     = null;
						dateString  = null ;
						startString = null ;
						endString   = null ;
						title       = "" ;
						id          = -1 ;
						type        = 1 ;
					}
					else if ( stack.equals( this.pathChannel ) )
						type = 2 ;

					Iterator<Attribute> iter = ev.asStartElement().getAttributes();
					while( iter.hasNext() )
					{
						Attribute a = iter.next();
						String attributeName = a.getName().getLocalPart() ;
						String value = a.getValue() ;
						
						switch ( type )
						{
							case 1 :
								if ( attributeName.equals( "Enabled" ) )
								{
									if ( value.equals( "0" ) )
										enable = false ;
									else if ( ! value.equals( "-1" ) )
										throw new ErrorClass( ev, "Format error: Unexpected enable bit format from service" ) ;
								}
								else if ( attributeName == "Date")
									dateString  = value ;
								else if ( attributeName == "Start")
									startString = value ;
								else if ( attributeName == "End")
									endString   = value ;
								else if ( attributeName == "Day")
									if ( value != "-------") 
										enable = false ;    // ignore periodic timer entry 
								break ;
							
							case 2 :
								if ( attributeName.equals( "ID" ) )
									channel = value ;
								break ;
						}
					}
				}
				if ( ev.isCharacters() )
				{
					String value = ev.asCharacters().getData();
					if ( stack.equals( this.pathID ) )
					{
						if ( ! value.matches( "\\d+" ) )
							throw new ErrorClass( ev, "Format error: Unexpected ID format from service" ) ;
						id = Long.valueOf( value ) ;
					}
					else if ( stack.equals( this.pathDescr ) )
					{
						title += value ;
						//System.out.println(title) ;
					}
				}					
				if( ev.isEndElement() )
				{
					if ( enable && stack.equals( this.pathTimer ) )
					{
						if ( id < 0 || channel == null || dateString == null || startString == null || endString == null || title == null )
							throw new ErrorClass( ev, "Incomplete timer entry from service" ) ;
						long start ;
						long end ;
						try {
							start = Conversions.svcTimeToLong( startString, dateString ) ;
							end   = Conversions.svcTimeToLong( endString,   dateString ) ;
						} catch ( ParseException e ) {
							throw new ErrorClass( ev, "Format error: Unexpected time format from service" ) ;
						}
						if ( start > end )
							end += Constants.DAYMILLSEC ;
						DVBViewerEntry entry = new DVBViewerEntry( id, channel, start, end, title, true ) ;
						result.add(entry) ;
					}
					stack.pop() ;
				}
			}
			reader.close();
		} catch (XMLStreamException e) {
			throw new ErrorClass( e,   "Error on reading the Service data." 
	                 + " Position: Line = " + Integer.toString( e.getLocation().getLineNumber() )
	                 +        ", column = " + Integer.toString(e.getLocation().getColumnNumber()) ) ;
		}
		return result ;
		
		/*
        <?xml version="1.0" encoding="iso-8859-1"?>
        <Timers>
           <Timer Type="1" Enabled="0" Priority="50" Date="05.07.2999" Start="23:39:00" End="00:09:00" Days="TT-----" Action="0">
               <Descr>Bayerisches FS Süd (deu)</Descr>
               <Options AdjustPAT="-1" AllAudio="-1" DVBSubs="-1" Teletext="-1"/>
               <Format>2</Format>
               <Folder>Auto</Folder>
               <NameScheme>%event_%date_%time</NameScheme>
               <Log Enabled="-1" Extended="0"/>
               <Channel ID="550137291|Bayerisches FS Süd (deu)"/>
               <Executeable>0</Executeable>
               <Recording>0</Recording>
               <ID>0</ID>
           </Timer>
           <Timer Type="1" Enabled="-1" Priority="50" Date="05.07.2009" Start="18:35:00" End="19:50:00" Action="0">
               <Descr>Lindenstrasse</Descr>
               <Options AdjustPAT="-1"/>
               <Format>2</Format>
               <Folder>Auto</Folder>
               <NameScheme>%event_%date_%time</NameScheme>
               <Log Enabled="-1" Extended="0"/>
               <Channel ID="543583690|Das Erste (deu)"/>
               <Executeable>-1</Executeable>
               <Recording>-1</Recording>
               <ID>1</ID>
               <Recordstat StartTime="05.07.2009 18:40:33">G:\Neue Aufnahmen\Lindenstrasse_07-05_18-40-33.ts</Recordstat>
           </Timer>
        </Timers>
		 */
	}
}
