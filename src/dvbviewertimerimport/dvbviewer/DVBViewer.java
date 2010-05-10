// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.dvbviewer ;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import dvbviewertimerimport.xml.StackXML;
import dvbviewertimerimport.control.ChannelSet;
import dvbviewertimerimport.control.TimeOffsets;
//import dvbviewertimerimport.dvbviewer.channels.Channels;
import dvbviewertimerimport.misc.Enums.ActionAfterItems;
import dvbviewertimerimport.misc.Enums.TimerActionItems;
import dvbviewertimerimport.javanet.staxutils.IndentingXMLStreamWriter;
import dvbviewertimerimport.main.Versions;
import dvbviewertimerimport.misc.* ;
import dvbviewertimerimport.provider.Provider;

public class DVBViewer {
	
	private static TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin") ;
	
	public static enum Command { SET, DELETE, FIND, UPDATE } ;
	
	private static final String NAME_USERMODE_FILE            = "usermode.ini" ;
	private static final String NAME_CONFIG_PATH              = "Plugins" ;
	private static final String NAME_PATH_REMOVE              = "\\Roaming" ;
	private static final String NAME_IMPORT_INI_FILE          = "timerimporttool.ini" ;
	private static final String NAME_XML_PROCESSED_RECORDINGS = "DVBVTimerImportPrcd.xml" ;

	public static final String NAME_DVBVIEWER_COM_DLL         = "TimerImportToolCOM" ;
	
	private static boolean isDLLloaded = false ;

	private static final StackXML<String> xmlPath = new StackXML< String >( "Processed", "Entry" ) ;

	public class MaxID
	{
		long maxID = -1 ;
		public long increment()
		{
			maxID++ ;
			if ( maxID < 0L )
				maxID = 0 ;			// wrap around
			return( maxID ) ;
		}
		private void reset() { maxID = -1 ; } ;
	}

	public static TimeZone getTimeZone() { return timeZone ; } ;
	public static void setTimeZone( TimeZone timeZone ) { DVBViewer.timeZone = timeZone ; } ;
	private DVBViewerService service = null ;
	private final DVBViewerTimerXML timersXML ;
	
	private boolean isDVBViewerConnected = false ;
	
	private dvbviewertimerimport.dvbviewer.channels.Channels channels = new dvbviewertimerimport.dvbviewer.channels.Channels( this ) ;

	private ArrayList<DVBViewerEntry> recordEntries = null;
	private MaxID maxID = new MaxID() ;
	private ArrayList< HashMap< String, Channel> > channelsLists 
	        = new ArrayList< HashMap< String, Channel> >( dvbviewertimerimport.provider.Provider.getProviders().size() ) ;
	private final String exePath ;
	private final String iniPath ;
	@SuppressWarnings("unused")
	private final String dvbViewerPath ;
	private boolean usePathFile = false ;
	private boolean createPathFile = false ;
	private String dataPath = null ;
	private String savedDataPath = "" ;
	private String pluginConfPath = null ;
	private final String exeName ;
	private String separator      = ",," ;
	private ActionAfterItems afterRecordingAction = ActionAfterItems.NONE ;
	private TimerActionItems timerAction = TimerActionItems.RECORD ;
	public DVBViewer( final String iniPath, String exeName )
	{
		this.exeName = exeName + ".jar" ;
		this.exePath = determineExePath() ;
		if ( iniPath != null )
			this.iniPath = iniPath ;
		else
			this.iniPath = exePath ;
		this.dvbViewerPath = iniPath ;    // TODO:   Richtig oder nicht?
		this.timersXML = new DVBViewerTimerXML( this ) ;
	}
	public boolean initDataPath()
	{
		if ( this.dataPath != null )
			return true ;
		String path =this.readDataPathFromIni() ;
		if ( path == null )
		{
			this.dataPath = this.determineDataPath() ;
			if ( this.dataPath == null )
				return false ;
			this.pluginConfPath = this.dataPath + File.separator + NAME_CONFIG_PATH ;
		}
		else
		{
			this.savedDataPath = path ;
			this.usePathFile = true ;
			this.dataPath = path ;
			this.pluginConfPath = this.dataPath ;
		}
		Log.setFile( this.dataPath ) ;
		return true ;
	}
	public void setDataPath( String path )
	{
		createPathFile = true ;
		this.dataPath = path ;
		this.savedDataPath = path ;
		this.pluginConfPath = this.dataPath ;
		Log.setFile( this.dataPath ) ;
	}
	public void setProvider()
	{
		if ( this.channelsLists.size() == 0)
			for ( int ix = 0 ; ix < dvbviewertimerimport.provider.Provider.getProviders().size() ; ix++ )
				channelsLists.add( new HashMap< String, Channel>() ) ;
	}
	public static String determineExePath()
	{
		String exePath = System.getProperty("user.dir") ;
		
		File jarFile = new File(  exePath + File.separator + Constants.PROGRAM_NAME + ".jar" ) ;
		
		if ( jarFile.exists() )
			return exePath ;

		jarFile = null ;
		try {
			jarFile = new File(dvbviewertimerimport.main.TimerImportTool.class.getProtectionDomain()
					.getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if ( findIni( jarFile.getParent() ) )
			exePath = jarFile.getParent() ;
		//System.out.println( this.exePath ) ;
		return exePath ;
	}
	private static boolean findIni( String exePath )
	{
		String [] iniFiles = new String[ 2 ] ;
		iniFiles[ 0 ] = exePath + File.separator + NAME_USERMODE_FILE ;
		iniFiles[ 1 ] = exePath + File.separator + NAME_IMPORT_INI_FILE ;
		
		boolean found = false ;
		
		for ( String iniFile : iniFiles )
		{
			File f = new File( iniFile ) ;
			if ( f.exists() )
				found = true ;
		}
		return found ;
	}
	private String determineDataPath()
	{
		String iniFile = this.iniPath + File.separator + NAME_USERMODE_FILE ;
		File f = new File( iniFile ) ;
		BufferedReader bR;
		try {
			bR = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			return null ;
		}
		String line = null ;
		boolean modeBlock = false ;
		boolean userMode = false ;
		boolean root = false ;
		
		String path = "" ;
		try {
			while ((line = bR.readLine()) != null)
			{
				line = line.trim();
				if ( line.startsWith( "[" ) )
				{
					if ( line.equalsIgnoreCase("[Mode]"))
						modeBlock = true ;
					else if ( modeBlock )
						break ;
				}
				else 
				{
					int p = line.indexOf('=') ;
					if ( p >= 0 && modeBlock )
					{
						String key = line.substring(0,p).trim() ;
						String value = line.substring(p+1).trim() ;
						if ( key.equalsIgnoreCase( "UserMode" ) )
						{
							userMode = true ;
							if ( value.equals( "0" ) )
								path = iniPath + path ;
							else if ( value.equals( "1" ) )
								path = System.getenv( "APPDATA") + path ;
							else if ( value.equals( "2" ) )
							{
								String temp = System.getenv( "APPDATA") ;
								temp = temp.substring(temp.lastIndexOf(File.separator)) ;
								if ( temp.equalsIgnoreCase( DVBViewer.NAME_PATH_REMOVE ))
									temp = "" ;
								path =   System.getenv( "ALLUSERSPROFILE")
								       + temp + path ;
							}
							else
								throw new ErrorClass( "The file \"" + iniFile + "\" contains an illegal user mode.") ;
						}
						else if ( key.equalsIgnoreCase( "Root" ) )
						{
							root = true ;
							path += File.separator + value ;
						}
					}
				}
			}
			bR.close() ;
		} catch (IOException e) {
			throw new ErrorClass( e, "Error on reading the file \"" + iniFile + "\"." );
		}
		if ( !userMode && ! root )
			throw new ErrorClass( "Illegal format of the file \"" + iniFile + "\"." ) ;
		File directory = new File( path ) ;
		if ( !directory.isDirectory() )
			throw new ErrorClass( "Directory \"" + path + "\" not found. The File \"" + iniFile + "\" should be checked." ) ;
		return path ;
	}
	private void connectDVBViewerIfNecessary()
	{
		if ( ( this.service == null || ! this.service.isEnabled() ) && ! isDVBViewerConnected )
			isDVBViewerConnected = DVBViewerCOM.connect() ;
	}
	private void disconnectDVBViewer()
	{
		if ( isDVBViewerConnected )
			DVBViewerCOM.disconnect() ;
		isDVBViewerConnected = false ;
	}
	void readDVBViewerTimers()
	{
		this.recordEntries = null ;
		if ( this.service != null && this.service.isEnabled() )
			this.recordEntries =  this.service.readTimers() ;
		else
		{
			if ( DVBViewerCOM.connect() )
			{
				this.recordEntries = DVBViewerCOM.readTimers() ;
				DVBViewerCOM.disconnect() ;
			}
			else
				this.recordEntries = this.timersXML.readTimers() ;
		}
	}
	public void updateDVBViewer()
	{
		this.connectDVBViewerIfNecessary();
		readDVBViewerTimers() ;
		this.mergeXMLWithServiceData() ;
		try {
			this.setDVBViewerTimers() ;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.disconnectDVBViewer();
		this.writeXML() ;
	}
  public boolean  process( DVBViewerProvider provider, boolean getAll, Object args ) throws Exception
  {
    return process( provider, getAll, args, Command.SET ) ;
  }

  public boolean  process( DVBViewerProvider provider, boolean getAll, Object args, Command command ) throws Exception
	{
    boolean result = true ;
    if ( command == Command.FIND && this.recordEntries != null )
    {
      result  = provider.process(getAll, command ); ;
      result &= provider.processEntry( args, command ) ;
      return result ;
    }
		this.connectDVBViewerIfNecessary();
		try
		{
	    this.readDVBViewerTimers() ;
	    this.mergeXMLWithServiceData() ;
	    result  = provider.process(getAll, command ); ;
	    result &= provider.processEntry( args, command ) ;
	    if ( command != Command.FIND )
	      this.setDVBViewerTimers();
		} catch ( Exception e ) {
	    this.disconnectDVBViewer();
		  throw e ;
		}
		this.disconnectDVBViewer();
		return result ;
	}
	private void addRecordingEntry( DVBViewerEntry entry )
	{
		if ( entry.getID() < 0L )
		{
			entry.setID( this.maxID.increment() ) ;
		}
		this.recordEntries.add( entry ) ;
	}
	private void prepareProvider( Provider provider )
	{
		if ( provider.isPrepared() )
			return ;
		
		provider.setPrepared( true ) ;
				
		for ( DVBViewerEntry e : this.recordEntries )
		{
			if ( e.getProvider() == provider && ! e.isMergeElement() )
			{
				e.setMissing() ;
			}
		}
	}
	private void removeOutdatedProviderEntries()
	{		
		for ( DVBViewerEntry e : this.recordEntries )
		{
			if ( e.isOutdatedByProvider() )
				e.setToDelete() ;
		}
	}
	
	public void addNewEntry( Provider provider,
							 String providerID,
							 String channel, 
							 long start, 
							 long end,
							 String title )
	{
		this.prepareProvider( provider ) ;
		HashMap< String, Channel > channelMap = this.channelsLists.get( provider.getID() ) ;
		if ( ! channelMap.containsKey( channel ) )
			throw new ErrorClass( "Channel \"" + channel + "\" not found in channel list" ) ;
		Channel c =  channelMap.get( channel ) ;
		TimeOffsets o = c.getOffsets() ;
		long startOrg = start ;
		start -= o.getPreOffset(start)*60000 ;
		long endOrg = end ;
		end  += o.getPostOffset(end)*60000 ;
		String dvbViewerChannel = c.getDVBViewer() ;
		if ( dvbViewerChannel == null || dvbViewerChannel.length() == 0 )
			throw new ErrorClass( "DVBViewer entry of channel \"" + channel + "\" not defined in channel list" ) ;
		DVBViewerEntry e = new DVBViewerEntry( c.getDVBViewer(),
											   providerID,
											   start,
											   end,
											   startOrg,
											   endOrg,
											   "-------",
											   title,
											   this.timerAction ,
											   this.afterRecordingAction ,
											   c.getMerge( provider.getMerge() ),
											   provider ) ;

		for ( DVBViewerEntry co : this.recordEntries )
		{
			if ( co.isFilterElement() && e.isOrgEqual( co ) )
			{
				co.resetMissing() ;
				if ( provider.isFilterEnabled() )
					return ;
				else
				{
					co.setToDelete() ;
					break ;
				}
			}
		}
		this.addRecordingEntry( e ) ;
	}
	public void deleteEntry( Provider provider,
			 String channel, 
			 long start, 
			 long end,
			 String title )
	{
		HashMap< String, Channel > channelMap = this.channelsLists.get( provider.getID() ) ;
		if ( ! channelMap.containsKey( channel ) )
			throw new ErrorClass( "Channel \"" + channel + "\" not found in channel list" ) ;
		Channel c =  channelMap.get( channel ) ;
		String dvbViewerChannel = c.getDVBViewer() ;
		if ( dvbViewerChannel == null || dvbViewerChannel.length() == 0 )
			throw new ErrorClass( "DVBViewer entry of channel \"" + channel + "\" not defined in channel list" ) ;
		DVBViewerEntry e = new DVBViewerEntry( c.getDVBViewer(),
											   null,
											   start,
											   end,
											   start,
											   end,
											   "-------",
											   title,
											   this.timerAction,
											   this.afterRecordingAction,
											   c.getMerge( provider.getMerge() ),
											   provider ) ;

		for ( DVBViewerEntry co : this.recordEntries )
		{
			if ( e.isOrgEqual( co ) )
			{
				co.setToDelete() ;
				break ;
			}
		}
	}
  public void deleteEntry( DVBViewerEntry entry )
 {
   entry.setToDelete() ;
 }
	public ArrayList<DVBViewerEntry> getRecordEntries() { return this.recordEntries ; } ;
	public String getExePath()        { return this.exePath ; } ;
	public String getExeName()        { return this.exeName ; } ;
	public String getDataPath()       { return this.dataPath ; } ;
	public String getPluginConfPath() { return this.pluginConfPath ; } ;
	public dvbviewertimerimport.dvbviewer.channels.Channels getChannels()
	{
		return channels ;
	}
	public void setService( DVBViewerService s ) { this.service = s ; }
	public DVBViewerService getService() { return this.service ; } ;
	public void setEnableWOL( boolean e ) { this.service.setEnableWOL( e ) ; } ;
	public void setBroadCastAddress( String b ) { this.service.setBroadCastAddress( b ) ; } ;
	public void setMacAddress( String m ) { this.service.setMacAddress( m ) ; } ;
	public void setWaitTimeAfterWOL( int w ) { this.service.setWaitTimeAfterWOL( w ) ; } ;
	public void setAfterRecordingAction( ActionAfterItems dvbViewerActionAfter ) { this.afterRecordingAction = dvbViewerActionAfter ; } ;
	public ActionAfterItems getAfterRecordingAction() { return this.afterRecordingAction ; } ;
	public void setTimerAction( TimerActionItems timerAction ) { this.timerAction = timerAction ; } ;
	public TimerActionItems getTimerAction() { return this.timerAction ; } ;
	public void clearChannelLists()
	{
	  this.channelsLists 
    = new ArrayList< HashMap< String, Channel> >( dvbviewertimerimport.provider.Provider.getProviders().size() ) ;
	  this.setProvider() ;
	}
	private void addChannel( HashMap< String, Channel> channels, 
			                 String channelName, 
			                 Channel channel,
			                 String channelGroupName )
	{
		if ( channelName == null )
			return ;
		if ( channels.containsKey( channelName ) )
			throw new ErrorClass( "The " + channelGroupName + " channel \"" + channelName + "\" is not unique") ;
		channels.put( new String( channelName ), channel ) ;
	}
	public void addChannel( ChannelSet channelSet )
	{
		Channel c = new Channel( channelSet.getDVBViewerChannel(),
				                 channelSet.getTimeOffsets(),
				                 channelSet.getMerge() ) ;
		for ( dvbviewertimerimport.control.Channel cC : channelSet.getChannels() )
		{
			int type = cC.getType() ;
			this.addChannel( this.channelsLists.get(type ), cC.getName(), c, cC.getTypeName() ) ;
		}
	}
	public void merge()
	{
		for ( int iO = 0 ; iO < this.recordEntries.size() ; iO++ )
		{
			if ( ! this.recordEntries.get( iO ).toMerge() )
				continue ;
			boolean changed = true ;
			while ( changed )
			{
				changed = false ;
				DVBViewerEntry o = this.recordEntries.get( iO ) ;
			
				for ( int iI = iO+1 ; iI < this.recordEntries.size() ; iI++)
				{
					DVBViewerEntry i = this.recordEntries.get( iI ) ;
					if ( o.mustMerge( i ) )
					{
						DVBViewerEntry newEntry = o.update( i, this.separator ) ;
						if ( newEntry != null )
							this.addRecordingEntry( newEntry ) ;
						changed = true ;
						break ;
					}
				}
			}
		}
	}
	public void setDVBViewerTimers() throws InterruptedException
	{
		this.removeOutdatedProviderEntries();
		
		DVBViewerEntry.beforeRecordingSettingProcces(
				this.recordEntries,
				this.separator,
				this.maxID ) ;
		this.merge() ;
			
		int updatedEntries = 0 ;
		int newEntries = 0 ;
		int deletedEntries = 0 ;

		for ( DVBViewerEntry d : this.recordEntries )
		{
			if ( d.mustDeleted() )
				deletedEntries++ ;
			else if ( d.mustUpdated() )
				updatedEntries++ ;
			else if ( ! d.mustIgnored() )
				newEntries++ ;
		}

		if ( this.service != null && this.service.isEnabled() )
			this.service.setTimers( this.recordEntries ) ;
		else if ( ! DVBViewerCOM.setTimers( this.recordEntries ) )
			timersXML.setTimers( this.recordEntries ) ;
		
		Log.out(false,     "Number of new entries:     " + Integer.toString( newEntries )
			           + "\nNumber of deleted entries: " + Integer.toString( deletedEntries )
				       + "\nNumber of updated entries: " + Integer.toString( updatedEntries ) ) ;

		DVBViewerEntry.afterRecordingSettingProcces( this.recordEntries ) ;
	}
	public void setSeparator( String s ) { this.separator = s ; } ;
	public ArrayList<DVBViewerEntry> readXML()
	{
		maxID.reset() ;
		
		ArrayList<DVBViewerEntry> result = new ArrayList<DVBViewerEntry>() ;
		
		HashMap< Long, DVBViewerEntry > idMap = new HashMap< Long, DVBViewerEntry >() ;
		
		File f = new File( this.dataPath + File.separator + NAME_XML_PROCESSED_RECORDINGS ) ;
		if ( ! f.exists() )
			return result ;
		
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader  reader = inputFactory.createXMLEventReader( new StreamSource( f ) );
			StackXML<String>   stack = new StackXML<String>();
			
			while( reader.hasNext() ) {
				XMLEvent ev = reader.nextEvent();
				if( ev.isStartElement() )
				{
					stack.push( ev.asStartElement().getName().getLocalPart() );
					if ( ! stack.equals( DVBViewer.xmlPath ) )
						continue ;
					DVBViewerEntry entry = DVBViewerEntry.readXML( reader, ev, f.getName() ) ;
					idMap.put( entry.getID(), entry ) ;
					entry.setID( this.maxID.increment() ) ;
					result.add( entry ) ;
					stack.pop();
					continue ;
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
		
		DVBViewerEntry.assignMergedElements( idMap ) ;

		DVBViewerEntry.removeOutdatedEntries( result ) ;
		
		return result ;
	}
	public void writeXML()
	{
		XMLOutputFactory output = XMLOutputFactory.newInstance ();
		 
		XMLStreamWriter writer = null ;
		File file = new File( this.dataPath + File.separator + NAME_XML_PROCESSED_RECORDINGS ) ;

		try {
			FileOutputStream os = null ;
			try {
				writer = output.createXMLStreamWriter(
						( os = new FileOutputStream( file) ) , "ISO-8859-1");
			} catch (FileNotFoundException e) {
				throw new ErrorClass( e, "Unexpecting error on writing to file \"" + file.getPath() + "\". Write protected?" ) ;
			}
			IndentingXMLStreamWriter sw = new IndentingXMLStreamWriter(writer);
	        sw.setIndent( "    " );
			sw.writeStartDocument("ISO-8859-1","1.0");
			sw.writeStartElement("Processed");
		    sw.writeNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance") ;
		    sw.writeAttribute("xsi:noNamespaceSchemaLocation","DVBVTimerImportPrcd.xsd");
			for ( DVBViewerEntry e : recordEntries )
			{
				e.writeXML( sw, file ) ;
			}
			sw.writeEndElement();
			sw.writeEndDocument();
			sw.flush();
			sw.close();
			os.close();
		} catch (XMLStreamException e) {
			throw new ErrorClass( e,   "Error on writing XML file \"" + file.getAbsolutePath() 
	                 + ". Position: Line = " + Integer.toString( e.getLocation().getLineNumber() )
	                 +         ", column = " + Integer.toString(e.getLocation().getColumnNumber()) ) ;
		} catch (IOException e) {
			throw new ErrorClass( e,   "Error on writing XML file \"" + file.getAbsolutePath() ) ;
		}
	}
	public void mergeXMLWithServiceData()
	{
		ArrayList<DVBViewerEntry> lastTimers = readXML() ;
		DVBViewerEntry.updateXMLDataByServiceData( lastTimers, this.recordEntries, this.separator, this.maxID ) ;
		this.recordEntries = lastTimers ;
	}
	private String readDataPathFromIni()
	{
		String name = this.iniPath + File.separator + NAME_IMPORT_INI_FILE ;
		
		File f = new File( name ) ;
		
		if ( ! f.isFile() )
			return null ;
		
		BufferedReader bR;
		try {
			bR = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			throw new ErrorClass( e, NAME_IMPORT_INI_FILE + " not found. The importer must be located in the DVBViewer directory.");
		}
		String line = null ;
		boolean pathBlock = false ;
		
		String path = "" ;
		try {
			while ((line = bR.readLine()) != null)
			{
				line = line.trim() ;
				if ( !pathBlock )
				{
					if ( line.equalsIgnoreCase( "[Pathes]") )
						pathBlock = true ;
				}
				else
				{
					String [] parts = line.split( "=" ) ;
					if ( parts.length != 2)
						continue ;
					if ( parts[0].trim().equalsIgnoreCase( "Data" ))
					{
						path = parts[1].trim() ;
						break ;
					}
				}
			}
			bR.close() ;
		} catch (IOException e) {
			throw new ErrorClass( e, "Error on reading the file \"" + name + "\"." );
		}
		if ( path.length() == 0 )
			throw new ErrorClass( "Illegal format of the file \"" + name + "\"." ) ;
		File directory = new File( path ) ;
		if ( !directory.isDirectory() )
			throw new ErrorClass( "Directory \"" + path + "\" not found. The File \"" + name + "\" should be checked." ) ;
		
		File channelFile = new File( path + File.separator + dvbviewertimerimport.dvbviewer.channels.Channels.CHANNEL_FILE_NAME ) ;

		if ( ! channelFile.isFile() )
		{
			f.delete() ;
			return null ;
		}
		Log.setFile(path) ;
		
		return path ;
	}
	public void writeDataPathToIni()
	{
		String name = this.iniPath + File.separator + NAME_IMPORT_INI_FILE ;
		File f = new File( name ) ;
		
		String lineSeparator = System.getProperty("line.separator") ;

		
		if ( ! this.usePathFile )
		{
			if ( f.isFile() )
				f.delete() ;	// TODO   Admin-Mode?????????
			return ;
		}
		if ( ! this.createPathFile )
			return ;
		
		try {
			BufferedWriter bW = new BufferedWriter( new FileWriter( f, false ) ) ;
			bW.write( "[Pathes]" + lineSeparator ) ;
			bW.write( "Data=" + this.dataPath + lineSeparator ) ;
			bW.close() ;
		} catch (IOException e) {
			throw new ErrorClass( e, "Error on writing file " + name ) ;
		}
	}
	public boolean isPathFileUsed() { return this.usePathFile ; } ;
	public boolean setPathFileIsUsed( boolean use )
	{
		if ( use == this.usePathFile )
			return false ;
		
		if ( ! use )
		{
			String path = this.determineDataPath() ;
			if ( path == null )
				return false ;
			this.dataPath = path ;
			this.pluginConfPath = path + File.separator + NAME_CONFIG_PATH ;
		}
		else
			this.dataPath = this.savedDataPath ;
		this.usePathFile = use ;
		return true ;
	}
	public static void getDVBViewerCOMDll()
	{
		if ( isDLLloaded )
			return ;
		String exePath = DVBViewer.determineExePath() ;
		File f = new File ( exePath ) ;
		if ( ! f.canWrite() )
		{
			Log.error( ResourceManager.msg( "ADMINISTRATOR" ) ) ;
			System.exit( 1 ) ;
		}
		ResourceManager.copyBinaryFile( exePath, "datafiles/"
			                                    + NAME_DVBVIEWER_COM_DLL + ".dll" ) ;
	}	
	public static void getDVBViewerCOMDllAndCheckVersion()
	{
		getDVBViewerCOMDll() ;
		if ( ! DVBViewerCOM.getVersion().equals( Versions.getDVBViewerCOMVersion() ) )
		{
			Log.error( ResourceManager.msg( "PACKAGE" ) ) ;
			System.exit( 1 ) ;
		}
	}
	
	public static void setDLLisLoaded()
	{
		isDLLloaded = true ;
	}
}
