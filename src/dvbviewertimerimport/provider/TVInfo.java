// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.provider ;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TimeZone;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import dvbviewertimerimport.control.Channel;
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
	private final SimpleDateFormat htmlDateFormat ;
	
	private ArrayList< MyEntry > unresolvedEntries= null ;
	private HashSet< String > solvedChannels = new HashSet< String >() ;
	
	private HashSet< String >   userSender = null ;
	private ArrayList< Channel > allSender  = null ;

	
	public Provider getTVInfo() { return this ; } ;

	public TVInfo( Control control )
	{
		super( control, true, true, "TVInfo", true, true, true, false, true, true ) ;
		this.canImport = true ;
		this.dvbViewer = this.control.getDVBViewer() ;
		this.timeZone = TimeZone.getTimeZone("Europe/Berlin") ;
		this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
		this.dateFormat.setTimeZone( timeZone ) ;
		this.htmlDateFormat = new SimpleDateFormat("EE d.M.y H:m") ;
		this.htmlDateFormat.setTimeZone( timeZone ) ;
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
		return Html.getStream( completeURL, "TVInfo XML" ) ;
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
	private class MyEntry
	{
		String channel = null ;
		String providerID = null ;
		long  start = 0;
		long  end   = 0 ;
		String title = null ;
		
		public void setTitle( final String title )
		{
			this.title = title ;
		}
		
		public boolean add()
		{
			if ( channel == null || channel.length() == 0 )
				return false ;
			dvbViewer.addNewEntry( getTVInfo(), providerID, channel, start, end, title) ;
			solvedChannels.add( channel ) ;
			return true ;
		}
		public void readAttributes( XMLEvent ev )
		{
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
				} catch ( ParseException e ) {
					throw new ErrorClass( ev, "Illegal TVinfo time" ) ;
				}
			}
			if ( ( start & end ) == 0 )
				throw new ErrorClass( ev, "Error in  TVinfo data, start or end time not given" ) ; 
		}
		public long getKey() { return start ; } ;
		@Override
		public String toString() { return this.title ; } ;
	}
	@Override
	public boolean process( boolean all, DVBViewer.Command command )
	{
		this.unresolvedEntries = null ;
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
			MyEntry entry = null ;
			while( reader.hasNext() ) {
				XMLEvent ev = reader.nextEvent();
				if( ev.isStartElement() )
				{
					stack.push( ev.asStartElement().getName().getLocalPart() );
					if ( ! stack.equals( this.xmlPathTVinfoEntry ) )
						continue ;
					entry = new MyEntry() ;
					entry.readAttributes( ev ) ;
				}
				if ( ev.isCharacters() )
				{
					if ( stack.equals( this.xmlPathTVinfoTitle ) )
					{
						entry.setTitle( ev.asCharacters().getData() ) ;
						boolean isAdded = false ;
						
						try
						{
							isAdded = entry.add() ;
						} catch ( ErrorClass e ) {
							Log.out( e.getErrorString() + " Entry ignored" ) ;
						}
						
						if ( ! isAdded )
						{
							if ( this.unresolvedEntries == null )
								this.unresolvedEntries = new ArrayList< MyEntry >() ;
							this.unresolvedEntries.add( entry ) ;
							Log.out( "Empty channelname in TVInfo data at line " + Integer.toString( ev.getLocation().getLineNumber() ) + ", title: " + entry.title ) ;
						}
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
		if ( this.unresolvedEntries != null )
			this.readMerklisteAndAddUnresolverEntries();
		return true ;
	}
	
	public class MerkzettelParserCallback extends HTMLEditorKit.ParserCallback
	{
		private int tableDiv = -1 ;
		private boolean isTableRead = false ;
		private boolean isA = false ;
		private int column = -1 ;
		private int row = -1 ;
		private HashMap< Long, ArrayList< MyEntry > > entries= null ;
		private MyEntry actEntry = null ;
		private char[] date = null ;
		private boolean ignore = false ;
		
		public HashMap< Long, ArrayList< MyEntry > > getResult() { return this.entries ; } ;
		
		public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos)
		{
			if ( this.isTableRead )
				return ;
			if ( t == HTML.Tag.DIV )
			{
				if ( a.containsAttribute( HTML.Attribute.CLASS, "Box4 col1" ) )
					this.tableDiv = 0 ;
				else if ( tableDiv >= 0 )
					this.tableDiv++ ;
			}
			else if ( this.tableDiv >= 0 )
			{
				if ( t == HTML.Tag.TR )
				{
					this.column = -1 ;
					this.row++ ;
					this.ignore = false ;
					if ( this.row > 0 )
						this.actEntry = new MyEntry() ;
				}
				else if ( t == HTML.Tag.TD )
					if ( this.column < 0 && a.containsAttribute( HTML.Attribute.COLSPAN, "5"))
						this.ignore = true ;
					else
						this.column++ ;
				else if ( t == HTML.Tag.A )
					this.isA = true ;
			}
		}
	    public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos)
	    {
			if ( this.isTableRead )
				return ;
			if ( this.tableDiv >= 0 )
			{
				if ( this.row > 0 && t == HTML.Tag.IMG && column == 1 )
					this.actEntry.channel = (String) a.getAttribute( HTML.Attribute.ALT ) ;
			}
	    }
		public void handleEndTag(HTML.Tag t, int pos)
		{
			if ( t == HTML.Tag.DIV )
			{
				if ( this.tableDiv > 0 )
					this.tableDiv-- ;
				else if ( this.tableDiv == 0 )
					this.isTableRead = true ;
			}
			else if ( this.tableDiv >= 0 && t == HTML.Tag.TR )
			{
				if ( actEntry != null  && ! this.ignore )
				{
					if ( actEntry.title != null && actEntry.channel != null )
					{
						
						if ( this.entries == null )
							this.entries = new HashMap< Long, ArrayList< MyEntry > >() ;
						if ( ! this.entries.containsKey( actEntry.getKey() ) )
							this.entries.put( actEntry.getKey() , new ArrayList< MyEntry >() ) ;
						ArrayList< MyEntry > list = this.entries.get( actEntry.getKey() ) ;
						list.add( actEntry ) ;
					}
					else
					{
						this.ignore = true ;
					}
				}
			}
			else if ( this.tableDiv >= 0 && t == HTML.Tag.A )
				this.isA = false ;
		}
		public void handleText(char[] data, int pos)
		{
			if ( this.isTableRead || this.ignore )
				return ;
			if ( this.tableDiv >= 0 && this.row > 0 )
			{
				switch ( column )
				{
				case 1 :
					this.actEntry.channel = new String( data ) ;
					if ( solvedChannels.contains( this.actEntry.channel ) )
						this.ignore = true ;
					break ;
				case 2 :
					this.date  = data ;
					break ;
				case 3 :
					if ( this.date == null )
					{
						this.ignore = true ;
						break ;
					}
					try {
						this.actEntry.start = htmlTimeToLong( new String( this.date ), new String( data ) ) ;
					} catch (ParseException e) {
						this.ignore = true ;
					}
					break ;
				case 4 :
				case 5 :
					if ( this.isA && data != null && this.actEntry.title == null )
						this.actEntry.title = new String( data ) ;
				}
			}
		}
	}
	
	public void readMerklisteAndAddUnresolverEntries()
	{
		String completeURL = "http://www.tvinfo.de/merkzettel?LIMIT=200&user=" + this.username + "&pass=" + this.getMD5() ;
		
		InputStream stream = null ;
		
		try
		{
			stream = Html.getStream( completeURL, "TVInfo Merkzettel" ) ;
		} catch ( ErrorClass e ) {
			Log.out( e.getErrorString() ) ;
			return ;
		}
		
		MerkzettelParserCallback myCallBack = new MerkzettelParserCallback() ;
		
		try {
			new ParserDelegator().parse( new  InputStreamReader( stream ) , myCallBack, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HashMap< Long, ArrayList< MyEntry > > hashMap = myCallBack.getResult() ;
		
		if ( hashMap == null )
		{
			Log.out( "Empty channelnames can't assigned. Merkzettel format changed? Entries ignored"  ) ;
			return ;
		}
		
		for ( MyEntry e : this.unresolvedEntries )
		{
			if (hashMap.containsKey( e.getKey() ) )
			{
				ArrayList< MyEntry > entries = hashMap.get( e.getKey() ) ;
				
				entries = dvbviewertimerimport.misc.Conversions.getTheBestChoices(
								e.toString(),
								entries ,
								0, 1, null ) ;
				if ( entries.size() > 1 )
				{
					Log.out( "Empty channelnname can't assigned to a entry of the Merkzettel (not unique), entry ignored"  ) ;
					continue ;
				}
				e.channel = entries.get( 0 ).channel ;
				Log.out( true, "The entry with the empty channel name containig the title \"" + e.title + "\" is assigned to: " + e.channel ) ;
				try
				{
					e.add() ;
				} catch ( ErrorClass e1 ) {
					Log.out( e1.getErrorString() + " Entry ignored" ) ;
				}
			}
			else
			{
				Log.out( "Empty channelname can't assigned. Entry ignored"  ) ;
			}
		}
	}
	public long timeToLong( String time ) throws ParseException
	{
		//Workaround in case of a wrong time zone of the TVInfo output
		// must be checked on summer time
		Date d = new Date( dateFormat.parse(time).getTime() ) ; //  + 60 *60 * 1000) ;
		//System.out.println(d.toString()) ;
		return d.getTime() ;
	}
	public long htmlTimeToLong( String date, String time ) throws ParseException
	{
		int year = new GregorianCalendar( this.timeZone ).get( Calendar.YEAR ) ;
		Date now = new Date() ;
		Date d1 = new Date( htmlDateFormat.parse(date+Integer.toString( year )+" "+time).getTime() ) ;
		Date d2 = htmlDateFormat.parse(date+Integer.toString( year+1 )+" "+time) ;
		
		if ( d1.before( now ))
			return d2.getTime() ;
		else
			return d1.getTime() ;
	}
	public class System_EditSenderParserCallback extends HTMLEditorKit.ParserCallback
	{
		private boolean isListUserSenderStarted = false ;
		private boolean isUserSenderRead = false ;
		private boolean isAllSenderRead = false ;
		
		private boolean isH1 = false ;
		private boolean isH3 = false ;
		private boolean isLI = false ;
		private boolean isSender = false ;
		private boolean isSenderAuswahl = false ;
		private String channel = null ;
				
		public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos)
		{
			if ( this.isUserSenderRead && this.isAllSenderRead )
				return ;
			if ( t == HTML.Tag.H1 )
				this.isH1 = true ;
			else if ( t == HTML.Tag.H3 )
				this.isH3 = true ;
			else if ( t == HTML.Tag.UL && this.isSender )
			{
				this.isSender = false ;
				this.isListUserSenderStarted = true ;
			}
			else if ( t == HTML.Tag.LI && this.isListUserSenderStarted )
				this.isLI = true ;
		}
	    public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos)
	    {
			if ( ! this.isSenderAuswahl )
				return ;
			if ( t == HTML.Tag.IMG )
			{
				
				Channel c = new Channel( getID(), (String) a.getAttribute( HTML.Attribute.ALT ), -1  )
				{
					@Override
					public Object getIDKey()
					{
						return this.getName() ;
					}
					@Override
					public Object getIDKey( final Channel c ) { return c.getName() ; } ;  // ID of the provider, type is provider dependent
				};
				allSender.add( c ) ;
			}
			if ( t == HTML.Tag.INPUT )
				if ( a.containsAttribute( HTML.Attribute.VALUE, "Änderungen speichern" ) )
				{
					this.isSenderAuswahl = false ;
					this.isAllSenderRead = true ;
				}
			
	    }
		public void handleEndTag(HTML.Tag t, int pos)
		{
			if ( this.isUserSenderRead && this.isAllSenderRead )
				return ;
			if ( t == HTML.Tag.H1 )
				this.isH1 = false ;
			else if ( t == HTML.Tag.H3 )
				this.isH3 = false ;
			if ( t == HTML.Tag.UL && this.isListUserSenderStarted )
			{
				this.isListUserSenderStarted = false ;
				this.isUserSenderRead = true ;
			}
			if ( t == HTML.Tag.LI && this.isListUserSenderStarted )
			{
				if ( this.channel != null )
					userSender.add( channel ) ;
				this.isLI = false ;
				this.channel = null ;
			}
		}
		public void handleText(char[] data, int pos)
		{
			if ( this.isUserSenderRead && this.isAllSenderRead )
				return ;
			if ( this.isH3 )
			{
				if ( new String(data).equals( "Sender:") )
					this.isSender = true ;
			}
			else if ( this.isH1 )
			{
				if ( new String(data).equals( "Senderauswahl") )
					this.isSenderAuswahl = true ;
			}
			else if ( this.isListUserSenderStarted && this.isLI )
			{
				this.channel = new String( data ) ;
				if ( this.channel.equals( ">>") )
					this.channel = null ;
			}
		}
	}
	public void readSystem_EditSenderPage()
	{
		this.userSender = new HashSet< String >() ;
		this.allSender = new ArrayList< Channel >() ;
		
		String completeURL = "http://www.tvinfo.de/system/_editSender.php?user=" + this.username + "&pass=" + this.getMD5() ;
		
		InputStream stream = null ;
		
		try
		{
			stream = Html.getStream( completeURL, "TVInfo Sender/Bearbeiten" ) ;
		} catch ( ErrorClass e ) {
			Log.out( e.getErrorString() ) ;
			return ;
		}
		
		System_EditSenderParserCallback myCallBack = new System_EditSenderParserCallback() ;
		
		try {
			new ParserDelegator().parse( new  InputStreamReader( stream ) , myCallBack, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public int importChannels( boolean check )
	{
		if ( check )
			return 0 ;
		
		return this.assignChannels() ;
	}
	@Override
	protected ArrayList< Channel > readChannels()
	{
		if ( this.allSender == null )
			this.readSystem_EditSenderPage() ;
		return this.allSender ;
	} ;
	@Override
	public boolean containsChannel( final Channel channel, boolean ifList )
	{
		if ( ifList )
			return true ;
		
		if (this.userSender == null)
			this.readSystem_EditSenderPage() ;
		return this.userSender.contains( channel.getName() ) ;
	}
	@Override
	public boolean isChannelMapAvailable()
	{
		return true ;
	}
}
