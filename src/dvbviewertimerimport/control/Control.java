// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.control ;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;


import dvbviewertimerimport.misc.Enums.TimerActionItems;
import dvbviewertimerimport.javanet.staxutils.IndentingXMLStreamWriter;
import dvbviewertimerimport.dvbviewer.DVBViewer ;
import dvbviewertimerimport.dvbviewer.DVBViewerEntry;
import dvbviewertimerimport.dvbviewer.DVBViewerService ;
import dvbviewertimerimport.main.Versions;
import dvbviewertimerimport.misc.* ;
import dvbviewertimerimport.misc.Enums.ActionAfterItems;
import dvbviewertimerimport.provider.ClickFinder;
import dvbviewertimerimport.provider.Provider;
import dvbviewertimerimport.provider.TVBrowser;
import dvbviewertimerimport.provider.TVGenial;
import dvbviewertimerimport.provider.TVInfo;
import dvbviewertimerimport.xml.StackXML;



public class Control
{
	private static final String NAME_XML_CONTROLFILE          = "DVBVTimerImportTool.xml" ;
	private static final String NAME_XML_CONTROLFILE_BACKUP   = "DVBVTimerImportTool.bak" ;
	private static final String NAME_XML_CONTROLFILE_IMPORTED = "DVBVTimerImportTool.imp" ;
	
	private enum BlockType { INVALID, CHANNEL_PROVIDER, DVBSERVICE,
							 DVBVIEWER, OFFSETS, OFFSET_ENTRY, CHANNEL, WOL } ;

	private final DVBViewer dvbViewer ;
	private final StackXML<String> pathProgramVersion	  = new StackXML<String>( "Importer", "ProgramVersion" ) ;
	private final StackXML<String> pathProviders		  = new StackXML<String>( "Importer", "Providers" ) ;
	private final StackXML<String> pathService			  = new StackXML<String>( "Importer", "DVBService" ) ;
	private final StackXML<String> pathGlobalOffsetEntry  = new StackXML<String>( "Importer", "Offsets", "Offset" ) ;
	private final StackXML<String> pathChannel			  = new StackXML<String>( "Importer", "Channels", "Channel" ) ;
	private final StackXML<String> pathChannelOffsets     = new StackXML<String>( "Importer", "Channels", "Channel", "Offsets" ) ;
	private final StackXML<String> pathChannelOffsetEntry = new StackXML<String>( "Importer", "Channels", "Channel", "Offsets", "Offset" ) ;
	private final StackXML<String> pathChannelProvider	  = new StackXML<String>( "Importer", "Channels", "Channel", "Provider" ) ;
	private final StackXML<String> pathChannelDVBViewer	  = new StackXML<String>( "Importer", "Channels", "Channel", "DVBViewer" ) ;
	private final StackXML<String> pathChannelMerge		  = new StackXML<String>( "Importer", "Channels", "Channel", "Merge" ) ;
	private final StackXML<String> pathSeparator		  = new StackXML<String>( "Importer",  "Separator" ) ;
	private final StackXML<String> pathMaxTitleLength     = new StackXML<String>( "Importer",  "MaxTitleLength" ) ;
	private final StackXML<String> pathWOL				  = new StackXML<String>( "Importer",  "DVBService", "WakeOnLAN") ;
	private final StackXML<String> pathDefaultProvider	  = new StackXML<String>( "Importer",  "GUI", "DefaultProvider" ) ;
	private final StackXML<String> pathLanguage			  = new StackXML<String>( "Importer",  "GUI", "Language" ) ;
	private final StackXML<String> pathLookAndFeel	      = new StackXML<String>( "Importer",  "GUI", "LookAndFeel" ) ;
	private final StackXML<String> pathDVBViewer		  = new StackXML<String>( "Importer",  "DVBViewer" ) ;
	private final StackXML<String> pathDVBViewerChannels  = new StackXML<String>( "Importer",  "DVBViewer", "Channels" ) ;
	
	private String defaultProvider = null ;
	private Locale language = Locale.getDefault() ;
	private String lookAndFeelName = Constants.SYSTEM_LOOK_AND_FEEL_NAME ;

	private ArrayList<ChannelSet> channelSets = new ArrayList<ChannelSet>() ;
	private String separator = null ;
	private int maxTitleLength = -1 ;
	
	private enum ImportStatus { FALSE, PENDING, IMPORTED } ;
	private ImportStatus importStatus = ImportStatus.FALSE ;
	
	public Control( DVBViewer dvbViewer )
	{
		this.dvbViewer = dvbViewer ;
		
		this.installProvider() ;
		this.dvbViewer.setProvider() ;
		
		this.read() ;
	}
	public Control( InputStream is, String name )   // only for preparing XML read
	{
		this.dvbViewer = null ;
		Provider.push() ;
		
		this.installProvider() ;
		
		this.read( is, name ) ;
		
		Provider.pop();
	}
	private void installProvider()
	{
		new TVInfo( this ) ;
		new TVGenial( this ) ;
		new ClickFinder( this ) ;
		new TVBrowser( this ) ;
	}
	
	public void read() throws TerminateClass { this.read( null, null ) ; } ;

	public void read( InputStream inputStream, String name ) throws TerminateClass
	{	
		boolean versionChanged = true ;
		
		StreamSource source = null ;
		if ( inputStream == null )
		{
			String path = this.dvbViewer.getXMLFilePath() ;
			File f = new File( path + File.separator + NAME_XML_CONTROLFILE ) ;
			if ( ! f.exists() )
			{
				ResourceManager.copyFile( path, "datafiles/DVBVTimerImportTool.xml" ) ;
				ResourceManager.copyFile( path, "datafiles/DVBVTimerImportTool.xsd" ) ;
			}
			name = f.getName() ;
			if ( ! f.canRead() )
				throw new ErrorClass( "File \"" + f.getAbsolutePath() + "\" not found" ) ;
			source = new StreamSource( f ) ;
		}
		else
			source = new StreamSource( inputStream ) ;
		XMLEventReader reader = null ;
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try {
			reader = inputFactory.createXMLEventReader( source );
		} catch (XMLStreamException e2) {
			throw new ErrorClass( e2, "Unexpected error on opening the file \"" + name + "\"" );
		}
		StackXML<String>   stack = new StackXML<String>();
		
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
		
		String dvbViewerPath = null ;
		String dvbExePath = null ;
		String viewParameters = null ;
		String recordingParameters = null ;
		boolean startIfRecording = false ;
		ActionAfterItems dvbViewerActionAfter = ActionAfterItems.NONE ;
		TimerActionItems dvbViewerTimerAction = TimerActionItems.RECORD ;
		int dvbChannelChangeTime = 0 ;
		boolean inActiveIfMerged = true ;
		

		while( reader.hasNext() )
		{
			XMLEvent ev = null ;
			try {
				ev = reader.nextEvent();
			} catch (XMLStreamException e1) {
				throw new ErrorClass( e1, "XML syntax error in file \"" + name + "\"" );
			}
			if( ev.isStartElement() )
			{
				stack.push(  ev.asStartElement().getName().getLocalPart() ) ;
				BlockType type = BlockType.INVALID ;
				if ( stack.equals( this.pathDVBViewerChannels ) )
				{
					if ( this.dvbViewer != null )
					{
						this.dvbViewer.getChannels().readXML( reader, ev, name ) ;
						stack.pop() ;
					}
					continue ;
				}
				else if ( stack.equals( this.pathProviders ) )
					try {
						Provider.readXML( reader, name ) ;
						stack.pop() ;
						continue ;
					} catch (XMLStreamException e1) {
						throw new ErrorClass( e1, "XML syntax error in file \"" + name + "\"" );
					}
				else if ( stack.equals( this.pathChannelProvider ) )
					type = BlockType.CHANNEL_PROVIDER ;
				else if ( stack.equals( this.pathService ) )
					type = BlockType.DVBSERVICE ;
				else if ( stack.equals( this.pathChannelOffsets ) )
				{
					type = BlockType.OFFSETS ;
					offsets = channelOffsets ;
				}
				else if ( stack.equals( this.pathGlobalOffsetEntry ) )
				{
					type = BlockType.OFFSET_ENTRY ;
					offsets = TimeOffsets.getGeneralTimeOffsets() ;
				}
				else if ( stack.equals( this.pathChannelOffsetEntry ) )
				{
					type = BlockType.OFFSET_ENTRY ;
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
				if (    type == BlockType.OFFSET_ENTRY )
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
								throw new ErrorClass ( ev, "Unknown provider name in file \"" + name + "\"" ) ;
						}
						else if ( attributeName.equals( "channelID" ) )
						{
							if ( ! value.matches("\\d+"))
								throw new ErrorClass ( ev, "Wrong cahnne id format in file \"" + name + "\"" ) ;
							channelID = Long.valueOf( value ) ;
						}
						break ;
					case DVBSERVICE :
						if      ( attributeName.equals( "enable" ) )
							dvbServiceEnable   = dvbviewertimerimport.xml.Conversions.getBoolean( value, ev, name ) ;
						else if ( attributeName.equals( "url" ) )
							dvbServiceURL      = value ;
						else if ( attributeName.equals( "username" ) )
							dvbServiceName     = value ;
						else if ( attributeName.equals( "password" ) )
							dvbServicePassword = value ;
						break ;
					case OFFSETS :
						if      ( attributeName.equals( "useGlobal" ) )
						{
							if      ( value.equalsIgnoreCase( "true" ) )
								offsets.setUseGlobal( true ) ;
							else if ( value.equalsIgnoreCase( "false" ) )
								offsets.setUseGlobal( false ) ;
							else
								throw new ErrorClass ( ev, "Wrong WOL enable format in file \"" + name + "\"" ) ;
						}
						break ;
					case OFFSET_ENTRY :
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
								throw new ErrorClass ( ev, "Wrong WOL enable format in file \"" + name + "\"" ) ;
						}
						else if ( attributeName.equals( "broadCastAddress" ) )
						{
							if ( ! value.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"))
								throw new ErrorClass ( ev, "Wrong broadcast address format in file \"" + name + "\"" ) ;
							dvbServiceBroadCastAddress = value ;
						}
						else if ( attributeName.equals( "macAddress" ) )
						{
							if ( ! value.matches("([\\dA-Fa-f]+[\\:\\-])+[\\dA-Fa-f]+"))
								throw new ErrorClass ( ev, "Wrong mac address format in file \"" + name + "\"" ) ;
							dvbServiceMacAddress = value ;
						}
						else if ( attributeName.equals( "waitTimeAfterWOL" ) )
						{
							if ( ! value.matches("\\d+"))
								throw new ErrorClass ( ev, "Wrong waitTimeAfterWOL format in file \"" + name + "\"" ) ;
							dvbServiceWaitTimeAfterWOL = Integer.valueOf( value ) ;
						}
						break ;
					case DVBVIEWER :
						
						if      ( attributeName.equals( "dvbViewerPath" ) )
							dvbViewerPath = value ;
						if      ( attributeName.equals( "dvbExePath" ) )
							dvbExePath = value ;
						else if ( attributeName.equals( "viewParameters" ) )
							viewParameters = value ;
						else if ( attributeName.equals( "recordingParameters" ) )
							recordingParameters = value ;
						else if ( attributeName.equals( "startIfRecording" ) )
						{
							if      ( value.equalsIgnoreCase( "true" ) )
								startIfRecording = true ;
							else if ( value.equalsIgnoreCase( "false" ) )
								startIfRecording = false ;
							else
								throw new ErrorClass ( ev, "Wrong startIfRecording format in file \"" + name + "\"" ) ;
						}
						else if ( attributeName.equals( "inActiveIfMerged" ) )
						{
							if      ( value.equalsIgnoreCase( "true" ) )
								inActiveIfMerged = true ;
							else if ( value.equalsIgnoreCase( "false" ) )
								inActiveIfMerged = false ;
							else
								throw new ErrorClass ( ev, "Wrong inActiveIfMerged format in file \"" + name + "\"" ) ;
						}
						else if ( attributeName.equals( "afterRecordingAction" ) )
						{
							try {
								dvbViewerActionAfter = ActionAfterItems.valueOf( value ) ;
							} catch ( IllegalArgumentException e ) {
								throw new ErrorClass ( ev, "Wrong afterAction format in file \"" + name + "\"" ) ;
							}
						}
						else if ( attributeName.equals( "timerAction" ) )
						{
							try {
								dvbViewerTimerAction = TimerActionItems.valueOf( value ) ;
							} catch ( IllegalArgumentException e ) {
								throw new ErrorClass ( ev, "Wrong timerAction format in file \"" + name + "\"" ) ;
							}
							
						}
						else if ( attributeName.equals( "timeZone" ) )
							DVBViewer.setTimeZone( TimeZone.getTimeZone( value )) ;
						else if ( attributeName.equals( "channelChangeTime" ) )
						{
							if ( ! value.matches("\\d+"))
								throw new ErrorClass ( ev, "Wrong channelChangeTime format in file \"" + name + "\"" ) ;
							dvbChannelChangeTime = Integer.valueOf( value ) ;
						}
					}
				}
				if (    type == BlockType.OFFSET_ENTRY )
					try {
						offsets.add(offsetBefore, offsetAfter, offsetDays, offsetBegin, offsetEnd) ;
					} catch (ErrorClass e) {
						throw new ErrorClass( ev, e.getErrorString() + " in file \"" + name + "\"" );
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
						channelSet.setMerge( dvbviewertimerimport.xml.Conversions.getBoolean( data, ev, name ) ) ;
					}
					else if ( stack.equals( this.pathSeparator) )
						this.separator = data ;
					else if ( stack.equals( this.pathMaxTitleLength ) )
					{
						if ( ! data.matches("\\d+"))
							throw new ErrorClass ( ev, "Wrong MaxTitleLength format in file \"" + name + "\"" ) ;
						this.maxTitleLength= Integer.valueOf( data ) ;
					}
					else if ( stack.equals( this.pathDefaultProvider ) )
						this.defaultProvider = data ;
					else if ( stack.equals( this.pathLanguage ) )
					{
						String [] parts = data.split( "_", -1 ) ;
						if ( parts.length == 3 )
							this.language = new Locale( parts[0], parts[1], parts[2] ) ;
						else
							this.language = Locale.getDefault() ;
					}
					else if ( stack.equals( this.pathLookAndFeel) )
						this.lookAndFeelName = data ;
					else if ( stack.equals( this.pathProgramVersion ) )
					{
						if ( Versions.getVersion().equals( data ) )
							versionChanged = false ;
					}
				}					
			}					
			if( ev.isEndElement() )
			{
				if      ( stack.equals( this.pathChannel ) )
					try {
						this.channelSets.add( channelSet ) ;
					} catch (ErrorClass e) {
						throw new ErrorClass( ev, e.getErrorString() + " in file \"" + name + "\"" ) ;
					}
					stack.pop();
			}
		}
		if ( this.dvbViewer != null )
		{
			if ( dvbViewerPath != null )
				this.dvbViewer.setDVBViewerPath( dvbViewerPath ) ;
			if ( dvbExePath != null )
				this.dvbViewer.setDVBExePath( dvbExePath ) ;
			this.dvbViewer.setChannelChangeTime( dvbChannelChangeTime ) ;
			if ( viewParameters != null )
				this.dvbViewer.setViewParameters( viewParameters ) ;
			if ( recordingParameters != null )
				this.dvbViewer.setRecordingParameters( recordingParameters ) ;
			this.dvbViewer.setStartIfRecording( startIfRecording ) ;
			
			this.dvbViewer.setService(new DVBViewerService(dvbServiceEnable,
					dvbServiceURL, dvbServiceName, dvbServicePassword));
			this.dvbViewer.setEnableWOL(dvbServiceEnableWOL);
			if (dvbServiceEnableWOL
					&& (dvbServiceBroadCastAddress == null || dvbServiceMacAddress == null))
				throw new ErrorClass(
						"Broadcast address or mac addres not given in file \""
								+ name + "\"");
			this.dvbViewer.setBroadCastAddress(dvbServiceBroadCastAddress);
			this.dvbViewer.setMacAddress(dvbServiceMacAddress);
			this.dvbViewer.setWaitTimeAfterWOL(dvbServiceWaitTimeAfterWOL);
			this.dvbViewer.setAfterRecordingAction(dvbViewerActionAfter);
			this.dvbViewer.setTimerAction(dvbViewerTimerAction);
			DVBViewerEntry.setInActiveIfMerged( inActiveIfMerged ) ;
		}
		try {
			reader.close() ;
		} catch (XMLStreamException e) {
			throw new ErrorClass( e, "Unexpected error on closing the file \"" + name + "\"" );
		}
		if ( versionChanged )
		{
			DVBViewer.getDVBViewerCOMDllAndCheckVersion() ;
			this.write( null ) ;
		}
	}
	public boolean write( final File xmlFile )
	{
		if ( this.dvbViewer == null || this.importStatus != ImportStatus.FALSE )
			return false ;
		File file ;
		if ( xmlFile == null  )
		{
			File toFile = new File(   this.dvbViewer.getXMLFilePath()
	                         + File.separator
	                         + NAME_XML_CONTROLFILE_BACKUP ) ;

			file = new File(   this.dvbViewer.getXMLFilePath()
                    + File.separator
                    + NAME_XML_CONTROLFILE ) ;
			
			toFile.delete() ;
			file.renameTo( toFile ) ;
		}
		else
			file = xmlFile ;
		

		XMLOutputFactory output = XMLOutputFactory.newInstance ();        
        
		XMLStreamWriter  writer = null ;
		FileOutputStream  fos = null ;
		try {
			try {
				writer = output.createXMLStreamWriter(
						fos =new FileOutputStream( file), "ISO-8859-1");
			} catch (FileNotFoundException e) {
				throw new ErrorClass( e, "Unexpecting error on writing to file \"" + file.getPath() + "\". Write protected?" ) ;
			}
			IndentingXMLStreamWriter sw = new IndentingXMLStreamWriter(writer);
			sw.setIndent( "    " );
			sw.writeStartDocument( "ISO-8859-1","1.0" ) ;
			sw.writeStartElement( "Importer" ) ;
			sw.writeNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance") ;
			sw.writeAttribute("xsi:noNamespaceSchemaLocation","DVBVTimerImportTool.xsd");
			if ( xmlFile == null )
			  if ( xmlFile == null )
			  {
			    sw.writeStartElement( "ProgramVersion" ) ;
				sw.writeCharacters( Versions.getVersion() ) ;
			    sw.writeEndElement();

			  }
			  Provider.writeXML( sw ) ;
			  sw.writeStartElement( "DVBViewer" ) ;
		        if ( dvbViewer.getDVBViewerPath() != null && dvbViewer.isDVBViewerPathSetExternal() )
			    	sw.writeAttribute( "dvbViewerPath", dvbViewer.getDVBViewerPath() ) ;
		        if ( dvbViewer.isDVBExePathSetExternal() )
			    	sw.writeAttribute( "dvbExePath", dvbViewer.getDVBExePath() ) ;
			    sw.writeAttribute( "waitTimeBeforeCOM", Integer.toString( dvbViewer.getChannelChangeTime() ) ) ;
		        if ( ! dvbViewer.getViewParameters().equals("") )
					sw.writeAttribute( "viewParameters", dvbViewer.getViewParameters() ) ;
				if ( ! dvbViewer.getRecordingParameters().equals("" ) )
					sw.writeAttribute( "recordingParameters", dvbViewer.getRecordingParameters() ) ;
				if ( dvbViewer.getStartIfRecording() )
					sw.writeAttribute( "startIfRecording", true ) ;
			    sw.writeAttribute( "afterRecordingAction", dvbViewer.getAfterRecordingAction().name() ) ;
			    sw.writeAttribute( "timerAction", dvbViewer.getTimerAction().name() ) ;
			    sw.writeAttribute( "timeZone", DVBViewer.getTimeZone().getID() ) ;
			    if ( ! DVBViewerEntry.getInActiveIfMerged() )
			    	sw.writeAttribute( "inActiveIfMerged", DVBViewerEntry.getInActiveIfMerged() ) ;
			    sw.writeStartElement( "Channels" ) ;
			      this.dvbViewer.getChannels().writeXML( sw, file ) ;
			    sw.writeEndElement();
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
			  sw.writeStartElement( "Language" ) ;
			  sw.writeCharacters(
					  this.language.getLanguage()
					  + "_" + this.language.getCountry()
					  + "_" + this.language.getVariant() ) ;
			  sw.writeEndElement() ;

			  sw.writeStartElement( "LookAndFeel" ) ;
			    sw.writeCharacters( this.lookAndFeelName ) ;
			    sw.writeEndElement() ;
			  sw.writeEndElement() ;
			  
			  TimeOffsets.getGeneralTimeOffsets().writeXML( sw ) ;
			  
			  if ( this.separator != null && this.separator.length() != 0 )
			  {
				  sw.writeStartElement( "Separator" ) ;
				  sw.writeCharacters( this.separator ) ;
				  sw.writeEndElement() ;
			  }
			  if ( this.maxTitleLength >= 0 )
			  {
				  sw.writeStartElement( "MaxTitleLength" ) ;
				  sw.writeCharacters( Integer.toString( this.maxTitleLength ) ) ;
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
			fos.close();
		} catch (XMLStreamException e) {
			throw new ErrorClass( e,   "Error on writing XML file \"" + file.getPath() ) ;
		} catch (IOException e) {
			throw new ErrorClass( e,   "Error on writing XML file \"" + file.getPath() ) ;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true ;
	}
	public void copyControlFile( File file, boolean from )
	{
		File source, destination ;
		
		File controlFile = new File(   this.dvbViewer.getXMLFilePath()
                + File.separator
                + NAME_XML_CONTROLFILE_IMPORTED ) ;
		
		if ( from )
		{
			source = file ;
			destination = controlFile ;
		}
		else
		{
			source = controlFile ;
			destination = file ;

		}	
		FileInputStream istream;
		try {
			istream = new FileInputStream( source );
		} catch (FileNotFoundException e2) {
			throw new ErrorClass(   "Unexpected error on reading file \""
		              + source.getAbsolutePath()
		              + "\"." ) ;
		}

		FileOutputStream ostream = null ;
		try {
			ostream = new FileOutputStream( destination );
		} catch (FileNotFoundException e1) {
			throw new ErrorClass(   "Unexpected error on writing file \""
		              + destination.getAbsolutePath()
		              + "\"." ) ;
		}
		try {
			byte [] buffer = new byte[2048] ;

			int length = 0 ;

			while ( ( length = istream.read( buffer ) ) > 0 )
				ostream.write( buffer, 0, length ) ;

			istream.close() ;
			ostream.close() ;
		} catch (IOException e) {
			throw new ErrorClass(   "Unexpected error on writing file \""
					              + destination.getAbsolutePath()
					              + "\"." ) ;
		}

    }
	
	public void renameImportedFile()
	{
		if ( this.importStatus != ImportStatus.PENDING )
			return ;
		
		this.importStatus = ImportStatus.IMPORTED ;

		File controlFile = new File(   this.dvbViewer.getXMLFilePath()
                + File.separator
                + NAME_XML_CONTROLFILE ) ;
		
		File importedFile = new File(   this.dvbViewer.getXMLFilePath()
                + File.separator
                + NAME_XML_CONTROLFILE_IMPORTED ) ;
		
		controlFile.delete() ;
		importedFile.renameTo( controlFile ) ;
	}

	public void setDVBViewerEntries()
	{
		if ( this.dvbViewer == null)
			return ;
		this.dvbViewer.clearChannelLists() ;
		this.dvbViewer.setSeparator( this.separator ) ;
		this.dvbViewer.setMaxTitleLength( this.maxTitleLength ) ;
		for ( ChannelSet cs : this.channelSets )
		{
			this.dvbViewer.addChannel( cs ) ;
		}
	}
	public String getDefaultProvider() { return this.defaultProvider ; } ;
	public void setDefaultProvider( String defaultProvider ) { this.defaultProvider = defaultProvider ; } ;
	public Locale getLanguage() { return this.language ; } ;
	public void setLanguage( Locale language ) { this.language = language ; } ;
	public String getLookAndFeelName() { return this.lookAndFeelName ; } ;
	public void setLookAndFeelName( String name ) { this.lookAndFeelName = name ; } ;
	public String getSeparator() { return this.separator ; } ;
	public void setSeparator( String separator ) { this.separator = separator ; } ;
	public int getMaxTitleLength() { return this.maxTitleLength ; } ;
	public void setMaxTitleLength( int l ) { this.maxTitleLength = l ; } ;
	public void setIsImported() { this.importStatus = ImportStatus.PENDING ; } ;
	public ArrayList<ChannelSet> getChannelSets() { return this.channelSets ; } ;
	public DVBViewer getDVBViewer() { return this.dvbViewer ; } ;
}
