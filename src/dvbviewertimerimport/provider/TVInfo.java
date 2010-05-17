// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.provider ;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.misc.* ;
import dvbviewertimerimport.provider.Provider;
import dvbviewertimerimport.xml.StackXML;

public final class TVInfo extends Provider {	

	private final StackXML<String> xmlPathTVinfoEntry = new StackXML<String>( "epg_schedule", "epg_schedule_entry" ) ;
	private final StackXML<String> xmlPathTVinfoTitle = new StackXML<String>( "epg_schedule", "epg_schedule_entry", "title" ) ;

	private final DVBViewer dvbViewer ;
	private final SimpleDateFormat dateFormat ;

	public TVInfo( Control control )
	{
		super( control, true, true, "TVInfo", true, true, true, false, true, true ) ;
		this.dvbViewer = this.control.getDVBViewer() ;
		this.timeZone = TimeZone.getTimeZone("Europe/Berlin") ;
		this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
		this.dateFormat.setTimeZone( timeZone ) ;
	}
	private String getMD5()
	{
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
			throw new ErrorClass( e2, "TVInfo URL not correct, the TVInfo should be checked." ) ;
		}

		URLConnection tvInfo = null ;
		InputStream result = null ;
		try {
			tvInfo = tvInfoURL.openConnection();
			result = tvInfo.getInputStream() ;
		} catch (IOException e) {
			throw new ErrorClass( e, "TVInfo data not available." );
		}
		return result ;
	}
	
	@Override
	public boolean test()
	{
		InputStream i = null ;
		try {
			i = this.connect() ;
		} catch (ErrorClass e ) {
			return false ;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String comp = "<?xml version=\"1.0" ;
		byte [] buffer = new byte[ 100 ] ; 
		try {
			i.read( buffer ) ;
			i.close();
		} catch (IOException e) {
			return false ;
		}
		String content = new String( buffer, 0, comp.length() ) ;
		if ( content.length() < comp.length())
			return false ;
		if ( ! content.equals( comp ) )
			return false ;
		return true ;
	}
	@Override
	public boolean process( boolean all, DVBViewer.Command command )
	{
		InputStream iS = null ;
		try {
			iS = this.connect();
		} catch (DigestException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader  reader = inputFactory.createXMLEventReader( new StreamSource( iS ) );
			StackXML<String>   stack = new StackXML<String>() ;
			String channel = null;
			String providerID = null ;
			long  start = 0;
			long  end   = 0 ;
			String title = null ;
			while( reader.hasNext() ) {
				XMLEvent ev = reader.nextEvent();
				if( ev.isStartElement() )
				{
					stack.push( ev.asStartElement().getName().getLocalPart() );
					if ( ! stack.equals( this.xmlPathTVinfoEntry ) ) continue ;
					channel = null;
					providerID = null ;
					start = 0 ;
					end   = 0 ;
					title = null ;
					@SuppressWarnings("unchecked")
					Iterator<Attribute> iter = ev.asStartElement().getAttributes();
					while( iter.hasNext() )
					{
						Attribute a = iter.next();
						String attributeName = a.getName().getLocalPart() ;
						String value = a.getValue() ;
						try
						{
							if      ( attributeName.equals( "channel" ) )
								channel = value ;
							else if ( attributeName.equals( "uid" ) )
								providerID = value ;
							else if ( attributeName.equals( "starttime" ) )
								start = timeToLong( value ) ;
							else if ( attributeName.equals( "endtime" ) )
								end  = timeToLong( value ) ;
							else if ( attributeName.equals( "title" ) )
								title = value ;
						} catch (ParseException e)
						{
							throw new ErrorClass( ev, "Illegal TVinfo time" ) ;
						}
					}
					if ( ( start & end ) == 0 )
						throw new ErrorClass( ev, "Error in  TVinfo data, start or end time not given" ) ; 
					if ( channel.length() == 0 )
						throw new ErrorClass( ev, "Error in  TVinfo data, channel not given" ) ; 
				}
				if ( ev.isCharacters() )
				{
					if ( stack.equals( this.xmlPathTVinfoTitle ) )
					{
						title = ev.asCharacters().getData() ;
						this.dvbViewer.addNewEntry( this, providerID, channel, start, end, title) ;
					}
				}					
				if( ev.isEndElement() ) stack.pop();
			}
			reader.close();
		} catch (XMLStreamException e) {
			if ( e.getLocation().getLineNumber() == 1 && e.getLocation().getColumnNumber() == 1 )
				throw new ErrorClass( "No data available from TVInfo, account data should be checked." ) ;
			else
				throw new ErrorClass( e,   "Error on reading TVInfo data."
	                 + " Position: Line = " + Integer.toString( e.getLocation().getLineNumber() )
	                 +        ", column = " + Integer.toString(e.getLocation().getColumnNumber()) ) ;
			
		}
		return true ;
	}
	public long timeToLong( String time ) throws ParseException
	{
		//Workaround in case of a wrong time zone of the TVInfo output
		// must be checked on summer time
		Date d = new Date( dateFormat.parse(time).getTime() ) ; //  + 60 *60 * 1000) ;
		//System.out.println(d.toString()) ;
		return d.getTime() ;
	}
	

}
