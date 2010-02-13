// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.tvinfo ;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.DigestException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import dvbv.dvbviewer.DVBViewer ;
import dvbv.misc.* ;


public final class AllTVInfoRecordings
{
	private static final String NAME_XML_PROCESSED_RECORDINGS = "DVBVTimerImportPrcd.xml" ;

	private final Stack<String> xmlPath ;
	private final Stack<String> xmlPathTVinfoEntry ;
	private final Stack<String> xmlPathTVinfoTitle ;
	private HashMap<String,TVInfoRecording> map = null ;
	private TVInfo tvInfo = null ;
	private int days = 0 ;
	private int syncs = 0;
	private String dataDirectory = null ;
	private DVBViewer dvbViewer = null ;
	@SuppressWarnings("unchecked")
	public AllTVInfoRecordings( DVBViewer dvbViewer, int days, int syncs )
	{
		this.map           = new HashMap<String,TVInfoRecording> ();
		this.tvInfo        = (TVInfo) dvbv.provider.Provider.getProvider( "TVInfo") ;
		this.dvbViewer     = dvbViewer ;
		this.days          = days ;
		this.syncs         = syncs ;
		this.dataDirectory = dvbViewer.getDataPath() ;
		
		Stack<String> p1 = new Stack<String>() ;
		Collections.addAll( p1, "TVInfoProcessed", "entry" ) ;
		this.xmlPath = p1 ;
		Stack<String> p2 = new Stack<String>() ;
		Collections.addAll( p2, "epg_schedule", "epg_schedule_entry" ) ;
		this.xmlPathTVinfoEntry = p2 ;
		Stack<String> p3 = (Stack<String>)p2.clone();
		p3.push("title");
		this.xmlPathTVinfoTitle = p3 ;
	}
	private boolean add( String tvInfoID,
			         String channel,
			         long start,
			         long end,
			         boolean setUpdated )
	{
		boolean result ;
		TVInfoRecording t2 = null ;
		TVInfoRecording t1 = new TVInfoRecording( tvInfoID, channel, start, end ) ;
		if ( ! this.map.containsKey( t1.getHash() ) )
		{
			t2 = t1 ;
			this.map.put(t1.getHash(), t1) ;
			result = true ;
		}
		else
		{
			t2 = this.map.get( t1.getHash() ) ;
			result = false ;
		}
		if ( setUpdated )
			t2.setUpdated() ;
		return result ;

	}
	public ArrayList<TVInfoRecording> finish( long now )
	{
		ArrayList<TVInfoRecording> result = new ArrayList<TVInfoRecording>() ;
		ArrayList<String> toDelete = new ArrayList<String>() ;
		for ( TVInfoRecording r : map.values() )
		{
			TVInfoRecording.DeleteMode m = r.toDelete(this.days, this.syncs, now);
			if ( m != TVInfoRecording.DeleteMode.KEEP)
			{
				if ( m == TVInfoRecording.DeleteMode.REMOVE )
					result.add( r ) ;
				toDelete.add(r.getHash()) ;
				
			}
		}
		for ( String s : toDelete )
			map.remove( s ) ;
		return result ;
	}
	@SuppressWarnings("unchecked")
	private void read()
	{	
		File f = new File( dataDirectory
		          + File.separator
		          + NAME_XML_PROCESSED_RECORDINGS ) ;
		if ( ! f.exists() )
			return ;
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader  reader = inputFactory.createXMLEventReader( new StreamSource( f ) );
			Stack<String>   stack = new Stack<String>();
			
			while( reader.hasNext() ) {
				XMLEvent ev = reader.nextEvent();
				if( ev.isStartElement() )
				{
					stack.push( ev.asStartElement().getName().getLocalPart() );
					if ( ! stack.equals( this.xmlPath ) ) continue ;
					String tvInfoID = null ;
					String channel = null;
					String start = null ;
					String end = null ;
					String missingSince = null ;
					String missingSyncSince = null ;
					Iterator<Attribute> iter = ev.asStartElement().getAttributes();
			        while( iter.hasNext() )
			        {
			        	Attribute a = iter.next();
			        	String attributeName = a.getName().getLocalPart() ;
			        	String value = a.getValue() ;
			        	if (      attributeName.equals( "uid" ) )
			        		tvInfoID = value ;
			        	else if ( attributeName.equals( "channel" ) )
			        		channel = value ;
			        	else if ( attributeName.equals( "start" ) )
			        		start = value ;
			        	else if ( attributeName.equals( "end") )
			        		end = value ;
			        	else if ( attributeName.equals( "missingSince") )
			        		missingSince = value ;
			        	else if ( attributeName.equals( "missingSyncSince") )
			        		missingSyncSince = value ;
			        }
			        TVInfoRecording t = new TVInfoRecording( tvInfoID, channel, start, end, missingSince, missingSyncSince ) ;
			        this.map.put(t.getHash(), t) ;
				}
			    if( ev.isEndElement() ) stack.pop();
			}
			reader.close();
		} catch (XMLStreamException e) {
			throw new ErrorClass( e,   "Error on readin XML file \"" + f.getName() 
					                 + ". Position: Line = " + Integer.toString( e.getLocation().getLineNumber() )
					                 +         ", column = " + Integer.toString(e.getLocation().getColumnNumber()) ) ;
		}
		catch (Exception  e) {
			throw new ErrorClass( e, "Unexpected error on read XML file \"" + f.getName() ) ;
		}
	}
	public void write()
	{
		XMLOutputFactory output = XMLOutputFactory.newInstance ();
		 
		String fileName = dataDirectory + File.separator + NAME_XML_PROCESSED_RECORDINGS ;
		XMLStreamWriter writer = null ;
		try {
			try {
				writer = output.createXMLStreamWriter(
						new FileOutputStream( new File( fileName )), "ISO-8859-1");
			} catch (FileNotFoundException e) {
				throw new ErrorClass( e, "Unexpecting error on writing to file \"" + fileName + "\". Write protected?" ) ;
			}
			writer.writeStartDocument("ISO-8859-1","1.0");
			writer.writeStartElement("TVInfoProcessed");
			for ( TVInfoRecording r : map.values() )
			{
				writer.writeStartElement("entry");
				writer.writeAttribute("uid", r.getID() ) ;
				writer.writeAttribute("channel", r.getChannel() ) ;
				writer.writeAttribute("start", r.getStart() ) ;
				writer.writeAttribute("end", r.getEnd() ) ;
				writer.writeAttribute("missingSince", r.getMissingSince() ) ;
				writer.writeAttribute("missingSyncSince", r.getMissingSyncSince() ) ;
				writer.writeAttribute("hashCode", r.getHash() ) ;
				writer.writeEndElement();
			}
			writer.writeEndElement();
			writer.writeEndDocument();
			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			throw new ErrorClass( e,   "Error on writing XML file \"" + fileName 
	                 + ". Position: Line = " + Integer.toString( e.getLocation().getLineNumber() )
	                 +         ", column = " + Integer.toString(e.getLocation().getColumnNumber()) ) ;
		}
	}
	@SuppressWarnings("unchecked")
	public void process( boolean all )
	{
		int providerType = dvbv.provider.Provider.getProviderID( "TVInfo" ) ;

		if ( ! all )
			this.read() ;
		
		InputStream iS = null ;
		try {
			iS = this.tvInfo.connect();
		} catch (DigestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader  reader = inputFactory.createXMLEventReader( new StreamSource( iS ) );
			Stack<String>   stack = new Stack<String>() ;
			String tvInfoID = null ;
			String channel = null;
			long  start = 0;
			long  end   = 0 ;
			String title = null ;
			boolean getTitle = false ;
			while( reader.hasNext() ) {
				XMLEvent ev = reader.nextEvent();
				if( ev.isStartElement() )
				{
					stack.push( ev.asStartElement().getName().getLocalPart() );
					if ( ! stack.equals( this.xmlPathTVinfoEntry ) ) continue ;
					tvInfoID = null ;
					channel = null;
					start = 0 ;
					end   = 0 ;
					title = null ;
					Iterator<Attribute> iter = ev.asStartElement().getAttributes();
					while( iter.hasNext() )
					{
						Attribute a = iter.next();
						String attributeName = a.getName().getLocalPart() ;
						String value = a.getValue() ;
						try
						{
							if ( attributeName.equals( "uid" ) )
								tvInfoID = value ;
							else if ( attributeName.equals( "channel" ) )
								channel = value ;
							else if ( attributeName.equals( "starttime" ) )
								start = Conversions.tvInfoTimeToLong( value ) ;
							else if ( attributeName.equals( "endtime" ) )
								end  = Conversions.tvInfoTimeToLong( value ) ;
							else if ( attributeName.equals( "title" ) )
								title = value ;
						} catch (ParseException e)
						{
							throw new ErrorClass( ev, "Illegal TVinfo time" ) ;
						}
					}
					if ( ( start & end ) == 0 )
						throw new ErrorClass( ev, "Error in  TVinfo data, start or end time not given" ) ; 
					if ( ( tvInfoID.length() | channel.length() ) == 0 )
						throw new ErrorClass( ev, "Error in  TVinfo data, channel or title not given" ) ; 
					if ( this.add( tvInfoID, channel, start, end, true ) )
						getTitle = true ;
					else
						getTitle = false ;
				}
				if ( getTitle && ev.isCharacters() )
				{
					if ( stack.equals( this.xmlPathTVinfoTitle ) )
					{
						title = ev.asCharacters().getData() ;
						this.dvbViewer.addNewEntry( providerType, channel, start, end, title) ;
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
			
		//} catch (ErrorClass e) {
		//	// TODO Auto-generated catch block
		//	e.printStackTrace();
		}
		this.dvbViewer.setDeletedRecordings( this.finish(System.currentTimeMillis()) ) ;
		dvbViewer.merge() ;
	}
}
