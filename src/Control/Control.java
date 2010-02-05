// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package Control ;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import DVBViewer.DVBViewer ;
import DVBViewer.DVBViewerService ;
import Misc.* ;
import TVInfo.TVInfoRecording;

public class Control {
	private static final String NAME_XML_CONTROLFILE          = "DVBVTimerImportTool.xml" ;

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
	private String tvInfoMD5      = null ;
	private int tvInfoTriggerAction = 0 ;
	private String tvInfoURL = null ;

	private ArrayList<ChannelSet> channelSets = new ArrayList<ChannelSet>() ;
	private String separator = null ;
	private Merge generalMerge = new Merge( false ) ;
	
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
		
		this.read() ;
	}
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
		
		ChannelSet channelSet = null ;
		
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
					channelSet = new ChannelSet() ;
					channelOffsets = channelSet.getTimeOffsets() ;
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
	            		if (      attributeName.equals( "username" ) )
	            			this.tvInfoUsername      = value ;
		            	else if ( attributeName.equals( "password" ) )
		            		this.tvInfoPassword      = value ;
		            	else if ( attributeName.equals( "md5" ) )
		            		this.tvInfoMD5           = value ;
		            	else if ( attributeName.equals( "triggeraction" ) )
		            	{
		            		if ( !value.matches("\\d+") )
		            			throw new ErrorClass ( ev, "Wrong triggeraction format in file \"" + f.getName() + "\"" ) ;
		            		this.tvInfoTriggerAction = Integer.valueOf( value ) ;
		            	}
	            		break ;
	            	case 1 :
	            		if      ( attributeName.equals( "url" ) )
	            			dvbServiceURL      = value ;
	            		else if ( attributeName.equals( "username" ) )
	            			dvbServiceName     = value ;
		            	else if ( attributeName.equals( "password" ) )
		            		dvbServicePassword = value ;
	            		break ;
	            	case 2 :
	            	case 3 :
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
	            	case 5 :
	            		if      ( attributeName.equals( "enable" ) )
	            		{
	            			if      ( value.equalsIgnoreCase( "true" ) )
	            				dvbServiceEnableWOL = true ;
	            			else if ( value.equalsIgnoreCase( "false" ) )
	            				dvbServiceEnableWOL = false ;
	            			else
	            				throw new ErrorClass ( ev, "Wrong WOL enable format in file \"" + f.getName() + "\"" ) ;
	            		}
	            		else if ( attributeName.equals( "broadCastaddress" ) )
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
					Merge merge = null ;
					if      ( stack.equals( this.pathTVInfoURL ) )
						this.tvInfoURL = data ;
					else if ( stack.equals( this.pathChannelTVInfo ) )
						channelSet.add(Channel.Type.TVINFO, data) ;
					else if ( stack.equals( this.pathChannelClickFinder ) )
						channelSet.add(Channel.Type.CLICKFINDER, data) ;
					else if ( stack.equals( this.pathChannelDVBViewer ) )
						channelSet.setDVBViewerChannel( data ) ;
					else if ( stack.equals( this.pathGlobalCombine ) )
						merge = this.generalMerge ;
					else if ( stack.equals( this.pathChannelCombine ) )
						merge = channelSet.getMerge() ;
					else if ( stack.equals( this.pathSeparator) )
						this.separator = data ;
					if ( merge != null )
					{
						merge.setValid() ;
						if      ( data.equalsIgnoreCase( "false" ) )
							merge.set(false) ;
						else if ( data.equalsIgnoreCase( "true" ) )
							merge.set(true ) ;
						else
							throw new ErrorClass( ev, "Illegal boolean error in file \"" + f.getName() + "\"" ) ;
					}
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
		try {
			reader.close() ;
		} catch (XMLStreamException e) {
			throw new ErrorClass( e, "Unexpected error on closing the file \"" + f.getName() + "\"" );
		}
	}
	public void write()
	{
		XMLOutputFactory output = XMLOutputFactory.newInstance ();        
        
		String fileName = this.dvbViewer.getDataPath() + File.separator + NAME_XML_CONTROLFILE ;
		XMLStreamWriter  writer = null ;
		try {
			try {
				writer = output.createXMLStreamWriter(
						new FileOutputStream( new File( fileName )), "ISO-8859-1");
			} catch (FileNotFoundException e) {
				throw new ErrorClass( e, "Unexpecting error on writing to file \"" + fileName + "\". Write protected?" ) ;
			}
			IndentingXMLStreamWriter sw = new IndentingXMLStreamWriter(writer);
	        sw.setIndent( "    " );
			sw.writeStartDocument( "ISO-8859-1","1.0" ) ;
			sw.writeStartElement( "Importer" ) ;
		    sw.writeNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance") ;
		    sw.writeAttribute("xsi:noNamespaceSchemaLocation","DVBVTimerImportTool.xsd");
			  sw.writeStartElement( "TVInfo" ) ;
			    sw.writeAttribute( "username", this.tvInfoUsername ) ;
			    if ( this.tvInfoPassword != null )
				    sw.writeAttribute( "password", this.tvInfoPassword ) ;
  			    if ( this.tvInfoMD5 != null )
				    sw.writeAttribute( "md5"     , this.tvInfoMD5 ) ;
			    if ( this.tvInfoTriggerAction >= 0 )
				    sw.writeAttribute( "triggeraction" , Integer.toString( this.tvInfoTriggerAction ) ) ;
				sw.writeStartElement( "Url" ) ;
			      sw.writeCharacters( this.tvInfoURL ) ;
				  sw.writeEndElement();
			  sw.writeEndElement();

			  sw.writeStartElement( "DVBService" ) ;
			    sw.writeAttribute( "url",      this.dvbViewer.getService().getURL() ) ;
			    sw.writeAttribute( "username", this.dvbViewer.getService().getUserName() ) ;
			    sw.writeAttribute( "password", this.dvbViewer.getService().getPassword() ) ;
			    sw.writeAttribute( "timeZone", "Europe/Berlin" ) ;
			  sw.writeEndElement();
			  
			  TimeOffsets.getGeneralTimeOffsets().writeXML( sw ) ;
			  
			  this.generalMerge.writeXML( sw ) ;
			  
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
			throw new ErrorClass( e,   "Error on writing XML file \"" + fileName ) ;
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
	public void setDVBViewerEntries()
	{
		this.dvbViewer.setSeparator( this.separator ) ;
		this.dvbViewer.setMerge( this.generalMerge ) ;
		for ( Iterator<ChannelSet> it = this.channelSets.iterator() ; it.hasNext() ;)
		{
			this.dvbViewer.addChannel( it.next() ) ;
		}
	}
	public String getTVInfoURL() { return this.tvInfoURL ; } ;
	public String getTVInfoUserName() { return this.tvInfoUsername ; } ;
	public String getTVInfoMD5()
	{
		if ( this.tvInfoMD5 == null )
			return TVInfo.TVInfo.translateToMD5( this.tvInfoPassword ) ;
		return this.tvInfoMD5 ;
	} ; 
	public int getTriggerAction()     { return this.tvInfoTriggerAction ; } ;
}
