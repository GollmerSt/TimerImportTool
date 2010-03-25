// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.control ;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import dvbv.gui.GUIStrings;
import dvbv.gui.GUIStrings.ActionAfterItems;
import dvbv.javanet.staxutils.IndentingXMLStreamWriter;

import javax.swing.JOptionPane;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import dvbv.Resources.ResourceManager;
import dvbv.clickfinder.ClickFinder;
import dvbv.dvbviewer.DVBViewer ;
import dvbv.dvbviewer.DVBViewerService ;
import dvbv.misc.* ;
import dvbv.provider.Provider;
import dvbv.tvgenial.TVGenial;
import dvbv.tvinfo.TVInfo;
import dvbv.xml.StackXML;

public class Control
{
	private static final String NAME_XML_CONTROLFILE          = "DVBVTimerImportTool.xml" ;
	
	private enum BlockType { INVALID , CHANNEL_PROVIDER, DVBSERVICE, DVBVIEWER, 
		                     GLOBAL_OFFSETS, CHANNEL_OFFSETS, CHANNEL, WOL } ;

	private DVBViewer dvbViewer = null ;
	private final StackXML<String> pathProviders		= new StackXML<String>( "Importer", "Providers" ) ;
	private final StackXML<String> pathService			= new StackXML<String>( "Importer", "DVBService" ) ;
	private final StackXML<String> pathGlobalOffsets	= new StackXML<String>( "Importer", "Offsets", "Offset" ) ;
	private final StackXML<String> pathChannel			= new StackXML<String>( "Importer", "Channels", "Channel" ) ;
	private final StackXML<String> pathChannelOffsets	= new StackXML<String>( "Importer", "Channels", "Channel", "Offsets", "Offset" ) ;
	private final StackXML<String> pathChannelProvider	= new StackXML<String>( "Importer", "Channels", "Channel", "Provider" ) ;
	private final StackXML<String> pathChannelDVBViewer	= new StackXML<String>( "Importer", "Channels", "Channel", "DVBViewer" ) ;
	private final StackXML<String> pathChannelMerge		= new StackXML<String>( "Importer", "Channels", "Channel", "Merge" ) ;
	private final StackXML<String> pathSeparator		= new StackXML<String>( "Importer",  "Separator" ) ;
	private final StackXML<String> pathWOL				= new StackXML<String>( "Importer",  "DVBService", "WakeOnLAN") ;
	private final StackXML<String> pathDefaultProvider	= new StackXML<String>( "Importer",  "GUI", "DefaultProvider" ) ;
	private final StackXML<String> pathLanguage			= new StackXML<String>( "Importer",  "GUI", "Language" ) ;
	private final StackXML<String> pathLookAndFeel	    = new StackXML<String>( "Importer",  "GUI", "LookAndFeel" ) ;
	private final StackXML<String> pathDVBViewer		= new StackXML<String>( "Importer",  "DVBViewer" ) ;
	
	private String defaultProvider = null ;
	private String language = "" ;
	private String lookAndFeelName = Constants.SYSTEM_LOOK_AND_FEEL_NAME ;

	private ArrayList<ChannelSet> channelSets = new ArrayList<ChannelSet>() ;
	private String separator = null ;
		
	public Control( DVBViewer dvbViewer )
	{
		this.dvbViewer = dvbViewer ;
		
		new TVInfo( this ) ;
		new TVGenial( this ) ;
		new ClickFinder( this ) ;
		dvbViewer.setProvider() ;
		
		this.read() ;
	}
	public void read()
	{	
		String path = this.dvbViewer.getPluginConfPath() ;
		File f = new File( path + File.separator + NAME_XML_CONTROLFILE ) ;
		if ( ! f.exists() )
		{
			int answer = JOptionPane.showConfirmDialog( null,
					GUIStrings.copyDefaultControlFile(), 
			        Constants.PROGRAM_NAME, 
			        JOptionPane.OK_CANCEL_OPTION );
			if ( answer == JOptionPane.CANCEL_OPTION )
				System.exit( 1 ) ;
			ResourceManager.copyFile( path, "datafiles/DVBVTimerImportTool.xml" ) ;
			ResourceManager.copyFile( path, "datafiles/DVBVTimerImportTool.xsd" ) ;
		}
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
		long channelID    = -1L ;
		
		ChannelSet channelSet = null ;
		
		boolean dvbServiceEnable		   = false ;
		String  dvbServiceURL              = "" ;
		String  dvbServiceName             = "" ;
		String  dvbServicePassword         = "" ;
		boolean dvbServiceEnableWOL        = false ;
		String  dvbServiceBroadCastAddress = null ;
		String  dvbServiceMacAddress       = null ;
		int     dvbServiceWaitTimeAfterWOL = 15 ;
		ActionAfterItems dvbViewerActionAfter = ActionAfterItems.NONE ;
		

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
					channelID = -1L ;
					channelSet = new ChannelSet() ;
					channelOffsets = channelSet.getTimeOffsets() ;
				}
				else if ( stack.equals( this.pathWOL ) )
					type = BlockType.WOL ;
				else if ( stack.equals( this.pathDVBViewer ) )
					type = BlockType.DVBVIEWER ;
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
				@SuppressWarnings("unchecked")
				Iterator<Attribute> iter = ev.asStartElement().getAttributes();
	            while( iter.hasNext() )
	            {
	            	Attribute a = iter.next();
	            	String attributeName = a.getName().getLocalPart() ;
	            	String value = a.getValue() ;
	            	switch ( type )
	            	{
	            	case CHANNEL_PROVIDER :
	            		if      ( attributeName.equals( "name" ) )
	            		{
	            			provider = Provider.getProvider( value ) ;
	            			if ( provider == null )
	            				throw new ErrorClass ( ev, "Unknown provider name in file \"" + f.getName() + "\"" ) ;
	            		}
	            		else if ( attributeName.equals( "channelID" ) )
	            		{
	            			if ( ! value.matches("\\d+"))
	            				throw new ErrorClass ( ev, "Wrong cahnne id format in file \"" + f.getName() + "\"" ) ;
	            			channelID = Long.valueOf( value ) ;
	            		}
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
	            		break ;
	            	case DVBVIEWER :
	            		if      ( attributeName.equals( "afterRecordingAction" ) )
	            		{
	            			try {
	            				dvbViewerActionAfter = ActionAfterItems.valueOf( value ) ;
	            			} catch ( IllegalArgumentException e ) {
	            				throw new ErrorClass ( ev, "Wrong afterAction format in file \"" + f.getName() + "\"" ) ;
	            			}
	            			
	            		}
	            		break ;
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
						channelSet.add( provider.getID(), data, channelID ) ;
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
					else if ( stack.equals( this.pathLanguage ) )
					{
						this.language = data ;
						GUIStrings.setLanguage( data ) ;
					}
					else if ( stack.equals( this.pathLookAndFeel) )
						this.lookAndFeelName = data ;
				}					
			}					
	        if( ev.isEndElement() )
	        {
	        	if      ( stack.equals( this.pathChannel ) )
					try {
						this.channelSets.add( channelSet ) ;
					} catch (ErrorClass e) {
						throw new ErrorClass( ev, e.getErrorString() + " in file \"" + f.getName() + "\"" ) ;
					}
	        	stack.pop();
	        }
		}
		this.dvbViewer.setService(
				new DVBViewerService( dvbServiceEnable, dvbServiceURL, dvbServiceName, dvbServicePassword )
		) ;
		this.dvbViewer.setEnableWOL( dvbServiceEnableWOL ) ;
		if ( dvbServiceEnableWOL && ( dvbServiceBroadCastAddress == null || dvbServiceMacAddress == null ) )
			throw new ErrorClass( "Broadcast address or mac addres not given in file \"" + f.getName() + "\"" ) ;
		this.dvbViewer.setBroadCastAddress( dvbServiceBroadCastAddress ) ;
		this.dvbViewer.setMacAddress( dvbServiceMacAddress ) ;
		this.dvbViewer.setWaitTimeAfterWOL( dvbServiceWaitTimeAfterWOL ) ;
		this.dvbViewer.setAfterRecordingAction( dvbViewerActionAfter ) ;
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
		      sw.writeStartElement( "DVBViewer" ) ;
			    sw.writeAttribute( "afterRecordingAction", dvbViewer.getAfterRecordingAction().name() ) ;
			  sw.writeEndElement();
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
			  if ( ! this.language.equals( "" ) )
			  {
				  sw.writeStartElement( "Language" ) ;
				  sw.writeCharacters( this.language ) ;
				  sw.writeEndElement() ;
			  }
			    sw.writeStartElement( "LookAndFeel" ) ;
			    sw.writeCharacters( this.lookAndFeelName ) ;
			    sw.writeEndElement() ;
			  sw.writeEndElement() ;
			  
			  TimeOffsets.getGeneralTimeOffsets().writeXML( sw ) ;
			  
			  if ( this.separator.length() != 0 )
			  {
				  sw.writeStartElement( "Separator" ) ;
				  sw.writeCharacters( this.separator ) ;
				  sw.writeEndElement() ;
			  }
			  sw.writeStartElement( "Channels" ) ;
			    for ( ChannelSet cs : this.channelSets )
			    	cs.writeXML( sw ) ;
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
	public void setDVBViewerEntries()
	{
		this.dvbViewer.setSeparator( this.separator ) ;
		for ( ChannelSet cs : this.channelSets )
		{
			this.dvbViewer.addChannel( cs ) ;
		}
	}
	public String getDefaultProvider() { return this.defaultProvider ; } ;
	public void setDefaultProvider( String defaultProvider ) { this.defaultProvider = defaultProvider ; } ;
	public String getLanguage() { return this.language ; } ;
	public void setLanguage( String language ) { this.language = language ; } ;
	public String getLookAndFeelName() { return this.lookAndFeelName ; } ;
	public void setLookAndFeelName( String name ) { this.lookAndFeelName = name ; } ;
	public String getSeparator() { return this.separator ; } ;
	public void setSeparator( String separator ) { this.separator = separator ; } ;
	public ArrayList<ChannelSet> getChannelSets() { return this.channelSets ; } ;
	public DVBViewer getDVBViewer() { return this.dvbViewer ; } ;
}
