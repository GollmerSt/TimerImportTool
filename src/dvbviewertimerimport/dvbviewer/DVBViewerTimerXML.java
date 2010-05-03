package dvbviewertimerimport.dvbviewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import dvbviewertimerimport.gui.GUIStrings.ActionAfterItems;
import dvbviewertimerimport.gui.GUIStrings.TimerActionItems;
import dvbviewertimerimport.javanet.staxutils.IndentingXMLStreamWriter;
import dvbviewertimerimport.misc.Constants;
import dvbviewertimerimport.misc.Conversions;
import dvbviewertimerimport.misc.ErrorClass;
import dvbviewertimerimport.misc.Log;
import dvbviewertimerimport.xml.StackXML;

public class DVBViewerTimerXML {
	
	private static final String NAME_XML_DVBVIEWER_TIMERS = "timers.xml" ;
	private static final String FIELD_SEPARATOR	= ";" ;
	
	private static final int FIELD_DESCRIPTION  = 0 ;
	private static final int FIELD_CHANNELID    = 1 ;
	private static final int FIELD_START_DATE   = 2 ;
	private static final int FIELD_START_TIME   = 3 ;
	private static final int FIELD_END_TIME     = 4 ;
	private static final int FIELD_SHUTDOWN     = 5 ;
	private static final int FIELD_DAYS   	    = 6 ;
	private static final int FIELD_TIMER_ACTION = 7 ;
	private static final int FIELD_ENABLED      = 8 ;
	@SuppressWarnings("unused")
	private static final int FIELD_DISABLE_AV   = 9 ;		// Not used anymore
	
	private  static final StackXML< String > entryPath = new StackXML< String >( "settings", "section", "entry" ) ;
	
	private final DVBViewer dvbViewer ;
	private HashMap< Long, DVBViewerEntry > entries ;
	private long maxRecordingNo = 0 ;
	
	public DVBViewerTimerXML( DVBViewer dvbViewer )
	{
		this.dvbViewer = dvbViewer ;
	}
	private DVBViewerEntry addDVBViewerEntry( final String xmlEntry, long ix )
	{
		String field[] = xmlEntry.split( FIELD_SEPARATOR ) ;
		long start = 0 ;
		long end   = 0 ;
		try {
			start = Conversions.svcTimeToLong(field[FIELD_START_TIME], field[FIELD_START_DATE]);
			end   = Conversions.svcTimeToLong(field[FIELD_END_TIME],   field[FIELD_START_DATE]);
		} catch (Exception e) {
			// TODO: handle exception
		}
		if ( end < start )
			end += Constants.DAYMILLSEC ;
		if ( ix < 0 )
			ix = this.maxRecordingNo++ ;
		else if ( ix > maxRecordingNo + 1 )
			maxRecordingNo = ix + 1 ;
		
		DVBViewerEntry entry = new DVBViewerEntry(
				field[ FIELD_ENABLED ].equalsIgnoreCase( "true" ) ,
				ix,
				field[ FIELD_CHANNELID ] ,
				start ,
				end ,
				field[ FIELD_DAYS ] ,
				field[ FIELD_DESCRIPTION ] ,
				TimerActionItems.get( Integer.valueOf( field[ FIELD_TIMER_ACTION ] ) ) ,
				ActionAfterItems.get( Integer.valueOf( field[ FIELD_SHUTDOWN ] ) ) ) ;
		entries.put( ix, entry ) ;
		return entry ;
	}
	private static String createString( DVBViewerEntry e )
	{
		return createString( e, null, null ) ;
	}
	private static String createString( DVBViewerEntry e, String defRecAction, String defAfterRecord )
	{
		StringBuilder out = new StringBuilder( e.getTitle().replace( ";", "") ) ;
		out.append( ';' ) ;
		out.append( e.getChannel() ) ;
		out.append( ';' ) ;
		out.append( Conversions.longToSvcDayString( e.getStart() ) ) ;
		out.append( ';' ) ;
		out.append( Conversions.longToSvcTimeString( e.getStart() ) ) ;
		out.append( ';' ) ;
		out.append( Conversions.longToSvcTimeString( e.getEnd() ) ) ;
		out.append( ';' ) ;
		if ( e.getActionAfter() == ActionAfterItems.DEFAULT && defAfterRecord != null)
			out.append( defAfterRecord ) ;
		else
			out.append( Integer.toString( e.getActionAfter().getID() ) ) ;
		out.append( ';' ) ;
		out.append( e.getDays() ) ;
		out.append( ';' ) ;
		if ( e.getTimerAction() == TimerActionItems.DEFAULT && defRecAction != null )
			out.append( defRecAction ) ;
		else
			out.append( Integer.toString( e.getTimerAction().getID() ) ) ;
		out.append( ';' ) ;
		out.append( e.isEnabled() ) ;
		
		return out.toString() ;
	}
	public ArrayList<DVBViewerEntry> readTimers()
	{
		ArrayList<DVBViewerEntry> result = new ArrayList<DVBViewerEntry>() ;
		entries = new HashMap< Long, DVBViewerEntry >() ;
		
		File f = new File( dvbViewer.getDataPath() + File.separator + NAME_XML_DVBVIEWER_TIMERS ) ;
		if ( ! f.exists() )
			return result ;
		
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader  reader = inputFactory.createXMLEventReader( new StreamSource( f ) );
			StackXML<String>   stack = new StackXML<String>();
			
			String data = "" ;
			long ix = -1 ;
			
			while( reader.hasNext() ) {
				XMLEvent ev = reader.nextEvent();
				if( ev.isStartElement() )
				{
					stack.push( ev.asStartElement().getName().getLocalPart() );
					if ( stack.equals( DVBViewerTimerXML.entryPath ) )
					{
						data = "" ;
						ix = -1 ;
						@SuppressWarnings("unchecked")
						Iterator<Attribute> iter = ev.asStartElement().getAttributes();
						while( iter.hasNext() )
						{
							Attribute attr = iter.next() ;
							if ( attr.getName().getLocalPart().equals( "name" ) )
							{
								ix = Integer.valueOf( attr.getValue() ) ;
							}
						}
					}
				}
			    else if( ev.isEndElement() )
			    {
					if ( stack.equals( DVBViewerTimerXML.entryPath ) )
					{
						DVBViewerEntry entry = this.addDVBViewerEntry( data, ix ) ;
						result.add( entry ) ;
					}
			    	stack.pop();
			    }
				
			    else if ( ev.isCharacters() )
			    {
					if ( stack.equals( DVBViewerTimerXML.entryPath ) )
						data += ev.asCharacters().getData() ;
			    }
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
		return result ;
	}
	private void writeXML()
	{
		DVBViewerSetupXML setup = new DVBViewerSetupXML( dvbViewer ) ;
		
		String defRecAction   = setup.getSetupValue( "General", "DefRecAction",   "0" ) ; 
		String defAfterRecord = setup.getSetupValue( "General", "DefAfterRecord", "0" ) ;

		XMLOutputFactory output = XMLOutputFactory.newInstance ();
		 
		XMLStreamWriter writer = null ;
		FileOutputStream os = null ;
		File file = new File( dvbViewer.getDataPath() + File.separator + NAME_XML_DVBVIEWER_TIMERS ) ;
		try {
			try {
				writer = output.createXMLStreamWriter(
						(os = new FileOutputStream( file) ), "ISO-8859-1");
			} catch (FileNotFoundException e) {
				throw new ErrorClass( e, "Unexpecting error on writing to file \"" + file.getPath() + "\". Write protected?" ) ;
			}
			IndentingXMLStreamWriter sw = new IndentingXMLStreamWriter(writer);
	        sw.setIndent( "  " );
			sw.writeStartDocument("ISO-8859-1","1.0");
			sw.writeStartElement("settings");
		    sw.writeNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance") ;
		    sw.writeAttribute("xsi:noNamespaceSchemaLocation","timers.xsd");
			  sw.writeStartElement("section");
			    sw.writeAttribute( "name", "VCR" ) ;
			    int count = 0 ;
			    for ( DVBViewerEntry e : entries.values() )
			    {
			    	if ( ! e.mustWrite() )
			    		continue ;
			    	sw.writeStartElement("entry");
			    	  sw.writeAttribute( "name", Integer.toString( count++ ) ) ;
			    	  sw.writeCharacters( DVBViewerTimerXML.createString( e, defRecAction, defAfterRecord ) ) ;
					sw.writeEndElement();
			    }
				sw.writeEndElement();
			  sw.writeEndElement();
			sw.writeEndDocument();
			sw.flush();
			sw.close();
			os.close() ;
		} catch (XMLStreamException e) {
			throw new ErrorClass( e,   "Error on writing XML file \"" + file.getAbsolutePath() 
	                 + ". Position: Line = " + Integer.toString( e.getLocation().getLineNumber() )
	                 +         ", column = " + Integer.toString(e.getLocation().getColumnNumber()) ) ;
		} catch (IOException e) {
			throw new ErrorClass( e,   "Error on writing XML file \"" + file.getAbsolutePath() ) ;
		}
	}
	public void setTimers( ArrayList<DVBViewerEntry> entries )
	{
		for ( DVBViewerEntry d : entries )
		{
			if ( d.mustDeleted() )
			{
				this.entries.remove( d.getServiceID() ) ;
				Log.out( true, "Entry \"" + DVBViewerTimerXML.createString( d ) +"\" removed." ) ;
			}
			else if ( d.mustUpdated() )
			{
				this.entries.put( d.getServiceID(), d ) ;
				Log.out( true, "Entry \"" + DVBViewerTimerXML.createString( d ) +"\" updated." ) ;

			}
			else if ( ! d.mustIgnored() )
			{
				d.setServiceID( this.maxRecordingNo ++ ) ;
				this.entries.put( d.getServiceID(), d ) ;
				Log.out( true, "Entry \"" + DVBViewerTimerXML.createString( d ) +"\" added." ) ;
			}
		}
		this.writeXML() ;
	}
}
