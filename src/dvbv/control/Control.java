// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.control ;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;

import dvbv.javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import dvbv.dvbviewer.DVBViewer ;
import dvbv.dvbviewer.DVBViewerService ;
import dvbv.misc.* ;
import dvbv.provider.Provider;
import dvbv.tvinfo.TVInfo;

public class Control
{
	private static final String NAME_XML_CONTROLFILE          = "DVBVTimerImportTool.xml" ;
	
	private enum BlockType { INVALID , CHANNEL_PROVIDER, DVBSERVICE,
		                     GLOBAL_OFFSETS, CHANNEL_OFFSETS, CHANNEL, WOL } ;

	private DVBViewer dvbViewer = null ;
	private final Stack<String> pathProviders ;
	private final Stack<String> pathService ;
	private final Stack<String> pathGlobalOffsets ;
	private final Stack<String> pathChannel ;
	private final Stack<String> pathChannelOffsets ;
	private final Stack<String> pathChannelProvider ;
	private final Stack<String> pathChannelDVBViewer ;
	private final Stack<String> pathChannelMerge ;
	private final Stack<String> pathSeparator ;
	private final Stack<String> pathWOL ;
	private final Stack<String> pathDefaultProvider ;
	
	private String defaultProvider = null ;

	private ArrayList<ChannelSet> channelSets = new ArrayList<ChannelSet>() ;
	private String separator = null ;
	
	public Control( DVBViewer dvbViewer )
	{
		this.dvbViewer = dvbViewer ;
		
		new TVInfo( ) ;
		new dvbv.clickfinder.ClickFinder( dvbViewer ) ;
		dvbViewer.setProvider() ;
		
		Stack<String> p = null ;
		
		p = new Stack<String>() ;
		Collections.addAll( p, "Importer", "Providers" ) ;
		this.pathProviders = p ;
		
		p = new Stack<String>() ;
		Collections.addAll( p, "Importer", "DVBService" ) ;
		this.pathService = p ;

		p = new Stack<String>() ;
		Collections.addAll( p, "Importer", "Offsets", "Offset" ) ;
		this.pathGlobalOffsets = p ;

		p = new Stack<String>() ;
		Collections.addAll( p, "Importer", "Channels", "Channel" ) ;
		this.pathChannel = p ;

		p = new Stack<String>() ;
		Collections.addAll( p, "Importer", "Channels", "Channel", "Offsets", "Offset" ) ;
		this.pathChannelOffsets = p ;

		p = new Stack<String>() ;
		Collections.addAll( p, "Importer", "Channels", "Channel", "Provider" ) ;
		this.pathChannelProvider = p ;

		p = new Stack<String>() ;
		Collections.addAll( p, "Importer", "Channels", "Channel", "DVBViewer" ) ;
		this.pathChannelDVBViewer = p ;
		
		p = new Stack<String>() ;
		Collections.addAll( p, "Importer", "Channels", "Channel", "Merge" ) ;
		this.pathChannelMerge = p ;
		
		p = new Stack<String>() ;
		Collections.addAll( p, "Importer",  "Separator" ) ;
		this.pathSeparator = p ;
		
		p = new Stack<String>() ;
		Collections.addAll( p, "Importer",  "DVBService", "WakeOnLAN" ) ;
		this.pathWOL = p ;
		
		p = new Stack<String>() ;
		Collections.addAll( p, "Importer",  "GUI", "DefaultProvider" ) ;
		this.pathDefaultProvider = p ;
		
		
		this.read() ;
	}
	@SuppressWarnings("unchecked")
	public void read()
	{	
		File f = new File( this.dvbViewer.getPluginConfPath()
		          + File.separator
		          + NAME_XML_CONTROLFILE ) ;
		if ( ! f.canRead() )
			throw new ErrorClass( "File \"" + f.getAbsolutePath() + "\" not found" ) ;
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader reader = null ;
		try {
			reader = inputFactory.createXMLEventReader( new StreamSource( f ) );
		} catch (XMLStreamException e2) {
			throw new ErrorClass( e2, "Unexpected error on opening the file \"" + f.getName() + "\"" );
		}
		Stack<String>   stack = new Stack<String>();
		
		TimeOffsets offsets = null ;
		TimeOffsets channelOffsets = null ; 
		String offsetAfter  = null ;
		String offsetBefore = null ;
		String offsetDays   = null ;
		String offsetBegin  = null ;
		String offsetEnd    = null ;
		
		Provider provider = null ;
		
		ChannelSet channelSet = null ;
		
		boolean dvbServiceEnable		   = false ;
		String  dvbServiceURL              = "" ;
		String  dvbServiceName             = "" ;
		String  dvbServicePassword         = "" ;
		boolean dvbServiceEnableWOL        = false ;
		String  dvbServiceBroadCastAddress = null ;
		String  dvbServiceMacAddress       = null ;
		int     dvbServiceWaitTimeAfterWOL = 15 ;
		

		while( reader.hasNext() )
		{
			XMLEvent ev = null ;
			try {
				ev = reader.nextEvent();
			} catch (XMLStreamException e1) {
				throw new ErrorClass( e1, "XML syntax error in file \"" + f.getName() + "\"" );
			}
			if( ev.isStartElement() )
			{
				stack.push(  ev.asStartElement().getName().getLocalPart() ) ;
				Iterator<Attribute> iter = ev.asStartElement().getAttributes();
				BlockType type = BlockType.INVALID ;
				if ( stack.equals( this.pathProviders ) )
					try {
						Provider.readXML( reader, f ) ;
						stack.pop() ;
						continue ;
					} catch (XMLStreamException e1) {
						throw new ErrorClass( e1, "XML syntax error in file \"" + f.getName() + "\"" );
					}
				if ( stack.equals( this.pathChannelProvider ) )
					type = BlockType.CHANNEL_PROVIDER ;
				else if ( stack.equals( this.pathService ) )
					type = BlockType.DVBSERVICE ;
				else if ( stack.equals( this.pathGlobalOffsets ) )
				{
					type = BlockType.GLOBAL_OFFSETS ;
					offsets = TimeOffsets.getGeneralTimeOffsets() ;
				}
				else if ( stack.equals( this.pathChannelOffsets ) )
				{
					type = BlockType.CHANNEL_OFFSETS ;
					offsets = channelOffsets ;
				}
				else if ( stack.equals( this.pathChannel ) )
				{
					type = BlockType.CHANNEL ;
					provider = null ;
					channelSet = new ChannelSet() ;
					channelOffsets = channelSet.getTimeOffsets() ;
				}
				else if ( stack.equals( this.pathWOL ) )
					type = BlockType.WOL ;
				else
					continue ;
				if (    type == BlockType.CHANNEL_OFFSETS
				     || type == BlockType.GLOBAL_OFFSETS )
				{
					offsetAfter  = "" ;
					offsetBefore = "" ;
					offsetDays   = "" ;
					offsetBegin  = "" ;
					offsetEnd    = "" ;
				}
	            while( iter.hasNext() )
	            {
	            	Attribute a = iter.next();
	            	String attributeName = a.getName().getLocalPart() ;
	            	String value = a.getValue() ;
	            	switch ( type )
	            	{
	            	case CHANNEL_PROVIDER :
	            		provider = Provider.getProvider( value ) ;
	            		if ( provider == null )
            				throw new ErrorClass ( ev, "Unknown provider name in file \"" + f.getName() + "\"" ) ;
	            		break ;
	            	case DVBSERVICE :
	            		if      ( attributeName.equals( "enable" ) )
	            			dvbServiceEnable   = dvbv.xml.Conversions.getBoolean( value, ev, f ) ;
	            		if      ( attributeName.equals( "url" ) )
	            			dvbServiceURL      = value ;
	            		else if ( attributeName.equals( "username" ) )
	            			dvbServiceName     = value ;
		            	else if ( attributeName.equals( "password" ) )
		            		dvbServicePassword = value ;
	            		break ;
	            	case GLOBAL_OFFSETS :
	            	case CHANNEL_OFFSETS :
	            		if      ( attributeName.equals( "before" ) )
	            			offsetBefore = value ;
	            		else if ( attributeName.equals( "after"  ) )
	            			offsetAfter  = value ;
	            		else if ( attributeName.equals( "days"   ) )
	            			offsetDays   = value ;
	            		else if ( attributeName.equals( "begin"  ) )
	            			offsetBegin  = value ;
	            		else if ( attributeName.equals( "end"    ) )
	            			offsetEnd    = value ;
	            		break ;
	            	case WOL :
	            		if      ( attributeName.equals( "enable" ) )
	            		{
	            			if      ( value.equalsIgnoreCase( "true" ) )
	            				dvbServiceEnableWOL = true ;
	            			else if ( value.equalsIgnoreCase( "false" ) )
	            				dvbServiceEnableWOL = false ;
	            			else
	            				throw new ErrorClass ( ev, "Wrong WOL enable format in file \"" + f.getName() + "\"" ) ;
	            		}
	            		else if ( attributeName.equals( "broadCastAddress" ) )
	            		{
	            			if ( ! value.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"))
	            				throw new ErrorClass ( ev, "Wrong broadcast address format in file \"" + f.getName() + "\"" ) ;
	            			dvbServiceBroadCastAddress = value ;
	            		}
	            		else if ( attributeName.equals( "macAddress" ) )
	            		{
	            			if ( ! value.matches("([\\dA-Fa-f]+[\\:\\-])+[\\dA-Fa-f]+"))
	            				throw new ErrorClass ( ev, "Wrong mac address format in file \"" + f.getName() + "\"" ) ;
	            			dvbServiceMacAddress = value ;
	            		}
	            		else if ( attributeName.equals( "waitTimeAfterWOL" ) )
	            		{
	            			if ( ! value.matches("\\d+"))
	            				throw new ErrorClass ( ev, "Wrong waitTimeAfterWOL format in file \"" + f.getName() + "\"" ) ;
	            			dvbServiceWaitTimeAfterWOL = Integer.valueOf( value ) ;
	            		}
	            	}
	            }
	            if (    type == BlockType.CHANNEL_OFFSETS
	                 || type == BlockType.GLOBAL_OFFSETS )
					try {
						offsets.add(offsetBefore, offsetAfter, offsetDays, offsetBegin, offsetEnd) ;
					} catch (ErrorClass e) {
						throw new ErrorClass( ev, e.getErrorString() + " in file \"" + f.getName() + "\"" );
					}
			}
			if ( ev.isCharacters() )
			{
				String data = ev.asCharacters().getData().trim() ;
				if ( data.length() > 0 )
				{
					if      ( stack.equals( this.pathChannelProvider ) )
						channelSet.add( provider.getID(), data ) ;
					else if ( stack.equals( this.pathChannelDVBViewer ) )
						channelSet.setDVBViewerChannel( data ) ;
					else if ( stack.equals( this.pathChannelMerge ) )
					{
						channelSet.setMerge( dvbv.xml.Conversions.getBoolean( data, ev, f ) ) ;
					}
					else if ( stack.equals( this.pathSeparator) )
						this.separator = data ;
					else if ( stack.equals( this.pathDefaultProvider ) )
						this.defaultProvider = data ;
				}					
			}					
	        if( ev.isEndElement() )
	        {
	        	if      ( stack.equals( this.pathChannel ) )
					try {
						this.channelSets.add( channelSet ) ;
//						this.dvbViewer.addChannel(dvbViewerChannel, tvInfoChannel, clickFinderChannel, channelOffsets, channelMerge ) ;
					} catch (ErrorClass e) {
						throw new ErrorClass( ev, e.getErrorString() + " in file \"" + f.getName() + "\"" ) ;
					}
	        	stack.pop();
	        }
    		this.dvbViewer.setService(
    				new DVBViewerService( dvbServiceEnable, dvbServiceURL, dvbServiceName, dvbServicePassword )
    		) ;
    		this.dvbViewer.setEnableWOL( dvbServiceEnableWOL ) ;
    		if ( dvbServiceEnableWOL && ( dvbServiceBroadCastAddress == null || dvbServiceMacAddress == null ) )
    			throw new ErrorClass( ev, "Broadcast address or mac addres not given in file \"" + f.getName() + "\"" ) ;
    		this.dvbViewer.setBroadCastAddress( dvbServiceBroadCastAddress ) ;
    		this.dvbViewer.setMacAddress( dvbServiceMacAddress ) ;
    		this.dvbViewer.setWaitTimeAfterWOL( dvbServiceWaitTimeAfterWOL ) ;
		}
		try {
			reader.close() ;
		} catch (XMLStreamException e) {
			throw new ErrorClass( e, "Unexpected error on closing the file \"" + f.getName() + "\"" );
		}
	}
	public void write()
	{
		XMLOutputFactory output = XMLOutputFactory.newInstance ();        
        
		File file = new File( this.dvbViewer.getPluginConfPath()
		          + File.separator
		          + NAME_XML_CONTROLFILE ) ;

		XMLStreamWriter  writer = null ;
		try {
			try {
				writer = output.createXMLStreamWriter(
						new FileOutputStream( file), "ISO-8859-1");
			} catch (FileNotFoundException e) {
				throw new ErrorClass( e, "Unexpecting error on writing to file \"" + file.getPath() + "\". Write protected?" ) ;
			}
			IndentingXMLStreamWriter sw = new IndentingXMLStreamWriter(writer);
	        sw.setIndent( "    " );
			sw.writeStartDocument( "ISO-8859-1","1.0" ) ;
			sw.writeStartElement( "Importer" ) ;
		    sw.writeNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance") ;
		    sw.writeAttribute("xsi:noNamespaceSchemaLocation","DVBVTimerImportTool.xsd");
		      Provider.writeXML( sw ) ;
			  sw.writeStartElement( "DVBService" ) ;
			  	DVBViewerService dvbs = this.dvbViewer.getService() ;
			    sw.writeAttribute( "enable",   dvbs.isEnabled() ) ;
			    sw.writeAttribute( "url",      dvbs.getURL() ) ;
			    sw.writeAttribute( "username", dvbs.getUserName() ) ;
			    sw.writeAttribute( "password", dvbs.getPassword() ) ;
			    sw.writeAttribute( "timeZone", "Europe/Berlin" ) ;
				sw.writeStartElement( "WakeOnLAN" ) ;
				  sw.writeAttribute( "enable", dvbs.getEnableWOL() ) ;
				  sw.writeAttribute( "broadCastAddress", dvbs.getBroadCastAddress() ) ;
				  sw.writeAttribute( "macAddress", dvbs.getMacAddress() ) ;
				  sw.writeAttribute( "waitTimeAfterWOL", Integer.toString( dvbs.getWaitTimeAfterWOL() ) ) ;
				  sw.writeEndElement();
			  sw.writeEndElement();
			  
			  sw.writeStartElement( "GUI" ) ;
			  if ( this.defaultProvider != null )
			  {
				  sw.writeStartElement( "DefaultProvider" ) ;
				  sw.writeCharacters( this.defaultProvider ) ;
				  sw.writeEndElement() ;
			  }
			  sw.writeEndElement() ;
			  
			  TimeOffsets.getGeneralTimeOffsets().writeXML( sw ) ;
			  
			  if ( this.separator.length() != 0 )
			  {
				  sw.writeStartElement( "Separator" ) ;
				  sw.writeCharacters( this.separator ) ;
				  sw.writeEndElement() ;
			  }
			  sw.writeStartElement( "Channels" ) ;
			    for ( Iterator< ChannelSet> it = this.channelSets.iterator() ; it.hasNext() ; )
			    	it.next().writeXML( sw ) ;
			  sw.writeEndElement();
			sw.writeEndElement();
			writer.writeEndDocument();
			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			throw new ErrorClass( e,   "Error on writing XML file \"" + file.getPath() ) ;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void writeOffsets( IndentingXMLStreamWriter sw, TimeOffsets offsets ) throws XMLStreamException
	{
		sw.writeStartElement( "Offsets" ) ;
		
//		for ( )
		
	}
	public void setDVBViewerEntries( Provider provider )
	{
		this.dvbViewer.setSeparator( this.separator ) ;
		this.dvbViewer.setGeneralMerge( provider.getMerge() ) ;
		for ( Iterator<ChannelSet> it = this.channelSets.iterator() ; it.hasNext() ;)
		{
			this.dvbViewer.addChannel( it.next() ) ;
		}
	}
	public String getDefaultProvider() { return this.defaultProvider ; } ;
	public void setDefaultProvider( String defaultProvider ) { this.defaultProvider = defaultProvider ; } ;
	public ArrayList<ChannelSet> getChannelSets() { return this.channelSets ; } ;
	public DVBViewer getDVBViewer() { return this.dvbViewer ; } ;
}