// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package Control ;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import DVBViewer.DVBViewer ;
import DVBViewer.TimeOffsets ;
import DVBViewer.Merge ;
import DVBViewer.DVBViewerService ;
import Misc.* ;


public class Control {
	private DVBViewer dvbViewer = null ;
	private final Stack<String> pathTVInfo ;
	private final Stack<String> pathTVInfoURL ;
	private final Stack<String> pathService ;
	private final Stack<String> pathGlobalOffsets ;
	private final Stack<String> pathGlobalCombine ;
	private final Stack<String> pathChannel ;
	private final Stack<String> pathChannelOffsets ;
	private final Stack<String> pathChannelTVInfo ;
	private final Stack<String> pathChannelClickFinder ;
	private final Stack<String> pathChannelDVBViewer ;
	private final Stack<String> pathChannelCombine ;
	private final Stack<String> pathSeparator ;
	private final Stack<String> pathWOL ;
	
	private String tvInfoUsername = null ;
	private String tvInfoPassword = null ;
	private int tvInfoTriggerAction = 0 ;
	private String tvInfoURL = null ;
	@SuppressWarnings("unchecked")
	public Control( DVBViewer dvbViewer )
	{
		this.dvbViewer = dvbViewer ;
		Stack<String> p1 = new Stack<String>() ;
		Collections.addAll( p1, "Importer", "TVInfo" ) ;
		this.pathTVInfo = p1 ;
		
		Stack<String> p2 = (Stack<String>)p1.clone();
		p2.push( "Url" ) ;
		this.pathTVInfoURL = p2 ;
		
		Stack<String> p3 = new Stack<String>() ;
		Collections.addAll( p3, "Importer", "DVBService" ) ;
		this.pathService = p3 ;

		Stack<String> p4 = new Stack<String>() ;
		Collections.addAll( p4, "Importer", "Offsets", "Offset" ) ;
		this.pathGlobalOffsets = p4 ;

		Stack<String> p5 = new Stack<String>() ;
		Collections.addAll( p5, "Importer", "Channels", "Channel" ) ;
		this.pathChannel = p5 ;

		Stack<String> p6 = new Stack<String>() ;
		Collections.addAll( p6, "Importer", "Channels", "Channel", "Offsets", "Offset" ) ;
		this.pathChannelOffsets = p6 ;

		Stack<String> p7 = new Stack<String>() ;
		Collections.addAll( p7, "Importer", "Channels", "Channel", "TVInfo" ) ;
		this.pathChannelTVInfo = p7 ;

		Stack<String> p8 = new Stack<String>() ;
		Collections.addAll( p8, "Importer", "Channels", "Channel", "ClickFinder" ) ;
		this.pathChannelClickFinder = p8 ;

		Stack<String> p9 = new Stack<String>() ;
		Collections.addAll( p9, "Importer", "Channels", "Channel", "DVBViewer" ) ;
		this.pathChannelDVBViewer = p9 ;
		
		Stack<String> p10 = new Stack<String>() ;
		Collections.addAll( p10, "Importer", "Combine" ) ;
		this.pathGlobalCombine = p10 ;
		
		Stack<String> p11 = new Stack<String>() ;
		Collections.addAll( p11, "Importer", "Channels", "Channel", "Combine" ) ;
		this.pathChannelCombine = p11 ;
		
		Stack<String> p12 = new Stack<String>() ;
		Collections.addAll( p12, "Importer",  "Separator" ) ;
		this.pathSeparator = p12 ;
		
		Stack<String> p13 = new Stack<String>() ;
		Collections.addAll( p13, "Importer",  "DVBService", "WakeOnLAN" ) ;
		this.pathWOL = p13 ;
		
		try {
			this.read() ;
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void read() throws XMLStreamException
	{	
		File f = new File( this.dvbViewer.getPluginConfPath()
		          + File.separator
		          + Constants.NAME_XML_CONTROLFILE ) ;
		if ( ! f.canRead() )
			throw new ErrorClass( "File \"" + f.getAbsolutePath() + "\" not found" ) ;
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader  reader = inputFactory.createXMLEventReader( new StreamSource( f ) );
		Stack<String>   stack = new Stack<String>();
		
		TimeOffsets offsets = null ;
		TimeOffsets channelOffsets = null ; 
		String offsetAfter  = null ;
		String offsetBefore = null ;
		String offsetDays   = null ;
		String offsetBegin  = null ;
		String offsetEnd    = null ;
		
		String tvInfoChannel      = null ;
		String clickFinderChannel = null ;
		String dvbViewerChannel   = null ;
		Merge channelMerge    = null ;
		
		String  dvbServiceURL              = null ;
		String  dvbServiceName             = null ;
		String  dvbServicePassword         = null ;
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
				stack.push( ev.asStartElement().getName().getLocalPart() );
				Iterator<Attribute> iter = ev.asStartElement().getAttributes();
				int type = 0 ;
				if ( stack.equals( this.pathTVInfo ) )
					type = 0;
				else if ( stack.equals( this.pathService ) )
					type = 1 ;
				else if ( stack.equals( this.pathGlobalOffsets ) )
				{
					type = 2 ;
					offsets = TimeOffsets.getGeneralTimeOffsets() ;
				}
				else if ( stack.equals( this.pathChannelOffsets ) )
				{
					type = 3 ;
					offsets = channelOffsets ;
				}
				else if ( stack.equals( this.pathChannel ) )
				{
					type = 4 ;
					channelOffsets = new TimeOffsets() ;
					tvInfoChannel      = "" ;
					clickFinderChannel = "" ;
					dvbViewerChannel   = "" ;
					channelMerge = new Merge( false ) ;
				}
				else if ( stack.equals( this.pathWOL ) )
					type = 5 ;
				else
					continue ;
				if ( type == 2 || type == 3 )
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
	            	case 0 :
	            		if ( attributeName == "username")           this.tvInfoUsername      = value ;
		            	else if ( attributeName == "password")      this.tvInfoPassword      = value ;
		            	else if ( attributeName == "triggeraction")
		            	{
		            		if ( !value.matches("\\d+") )
		            			throw new ErrorClass ( ev, "Wrong triggeraction format in file \"" + f.getName() + "\"" ) ;
		            		this.tvInfoTriggerAction = Integer.valueOf( value ) ;
		            	}
	            		break ;
	            	case 1 :
	            		if      ( attributeName == "url" )     dvbServiceURL      = value ;
	            		else if ( attributeName == "username") dvbServiceName     = value ;
		            	else if ( attributeName == "password") dvbServicePassword = value ;
	            		break ;
	            	case 2 :
	            	case 3 :
	            		if      ( attributeName == "before" ) offsetBefore = value ;
	            		else if ( attributeName == "after"  ) offsetAfter  = value ;
	            		else if ( attributeName == "days"   ) offsetDays   = value ;
	            		else if ( attributeName == "begin"  ) offsetBegin  = value ;
	            		else if ( attributeName == "end"    ) offsetEnd    = value ;
	            		break ;
	            	case 5 :
	            		if      ( attributeName == "enable"            )
	            		{
	            			if      ( value.equalsIgnoreCase( "true" ) )
	            				dvbServiceEnableWOL = true ;
	            			else if ( value.equalsIgnoreCase( "false" ) )
	            				dvbServiceEnableWOL = false ;
	            			else
	            				throw new ErrorClass ( ev, "Wrong WOL enable format in file \"" + f.getName() + "\"" ) ;
	            		}
	            		else if ( attributeName == "broadCastaddress"  )
	            		{
	            			if ( ! value.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"))
	            				throw new ErrorClass ( ev, "Wrong broadcast address format in file \"" + f.getName() + "\"" ) ;
	            			dvbServiceBroadCastAddress = value ;
	            		}
	            		else if ( attributeName == "macAddress" )
	            		{
	            			if ( ! value.matches("([\\dA-Fa-f]+[\\:\\-])+[\\dA-Fa-f]+"))
	            				throw new ErrorClass ( ev, "Wrong mac address format in file \"" + f.getName() + "\"" ) ;
	            			dvbServiceMacAddress = value ;
	            		}
	            		else if ( attributeName == "waitTimeAfterWOL" )
	            		{
	            			if ( ! value.matches("\\d+"))
	            				throw new ErrorClass ( ev, "Wrong waitTimeAfterWOL format in file \"" + f.getName() + "\"" ) ;
	            			dvbServiceWaitTimeAfterWOL = Integer.valueOf( value ) ;
	            		}
	            	}
	            }
	            if ( type == 2 || type == 3 )
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
					Merge combine = null ;
					if      ( stack.equals( this.pathTVInfoURL ) )
						this.tvInfoURL = data ;
					else if ( stack.equals( this.pathChannelTVInfo ) )
						tvInfoChannel = data ;
					else if ( stack.equals( this.pathChannelClickFinder ) )
						clickFinderChannel = data ;
					else if ( stack.equals( this.pathChannelDVBViewer ) )
						dvbViewerChannel = data ;
					else if ( stack.equals( this.pathGlobalCombine ) )
						combine = this.dvbViewer.getMerge() ;
					else if ( stack.equals( this.pathChannelCombine ) )
						combine = channelMerge ;
					else if ( stack.equals( this.pathSeparator) )
						dvbViewer.setSeparator( data ) ;
					if ( combine != null )
					{
						combine.setValid() ;
						if      ( data.equalsIgnoreCase( "false" ) )
							combine.set(false) ;
						else if ( data.equalsIgnoreCase( "true" ) )
							combine.set(true ) ;
						else
							throw new ErrorClass( ev, "Illegal boolean error in file \"" + f.getName() + "\"" ) ;
					}
				}					
			}					
	        if( ev.isEndElement() )
	        {
	        	if      ( stack.equals( this.pathChannel ) )
	        		this.dvbViewer.addChannel(dvbViewerChannel, tvInfoChannel, clickFinderChannel, channelOffsets, channelMerge ) ;
	        	else if ( stack.equals( this.pathService ) )
	        	{
	        		this.dvbViewer.setService(
	        				new DVBViewerService( dvbServiceURL, dvbServiceName, dvbServicePassword )
	        		) ;
	        		this.dvbViewer.setEnableWOL( dvbServiceEnableWOL ) ;
	        		if ( dvbServiceEnableWOL && ( dvbServiceBroadCastAddress == null || dvbServiceMacAddress == null ) )
	        			throw new ErrorClass( ev, "Broadcast address or mac addres not given in file \"" + f.getName() + "\"" ) ;
	        		this.dvbViewer.setBroadCastAddress( dvbServiceBroadCastAddress ) ;
	        		this.dvbViewer.setMacAddress( dvbServiceMacAddress ) ;
	        		this.dvbViewer.setWaitTimeAfterWOL( dvbServiceWaitTimeAfterWOL ) ;
	        	}
	        	stack.pop();
	        }
		}
		reader.close() ;
	}
	public String getTVInfoURL() { return this.tvInfoURL ; } ;
	public String getTVInfoUserName() { return this.tvInfoUsername ; } ;
	public String getTVInfoPassword() { return this.tvInfoPassword ; } ; 
	public int getTriggerAction()     { return this.tvInfoTriggerAction ; } ;
}
