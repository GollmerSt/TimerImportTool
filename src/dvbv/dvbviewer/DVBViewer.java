// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.dvbviewer ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import dvbv.xml.StackXML;
import dvbv.control.ChannelSet;
import dvbv.control.TimeOffsets;
import dvbv.gui.GUIStrings.ActionAfterItems;
import dvbv.javanet.staxutils.IndentingXMLStreamWriter;
import dvbv.misc.* ;
import dvbv.provider.Provider;

public class DVBViewer {
	private static final String NAME_USERMODE_FILE            = "usermode.ini" ;
	private static final String NAME_XML_PROCESSED_RECORDINGS = "DVBVTimerImportPrcd.xml" ;

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

	private DVBViewerService service = null ;
	private ArrayList<DVBViewerEntry> recordEntries = null;
	private MaxID maxID = new MaxID() ;
	private ArrayList< HashMap< String, Channel> > channelsLists 
	        = new ArrayList< HashMap< String, Channel> >( dvbv.provider.Provider.getProviders().size() ) ;
	private final String exePath ;
	private final String dataPath ;
	private final String exeName ;
	private final String pluginConfPath ;
	private String separator      = ",," ;
	private ActionAfterItems afterRecordingAction = ActionAfterItems.NONE ;
	public DVBViewer( String dataPath, String exeName )
	{
		this.exeName = exeName + ".jar" ;
		this.exePath = determineExePath( dataPath ) ;
		if ( dataPath != null )
			this.dataPath = dataPath ;
		else
			this.dataPath = this.determineDataPath() ;
		this.pluginConfPath = this.dataPath + File.separator + "Plugins" ;
	}
	public void setProvider()
	{
		if ( this.channelsLists.size() == 0)
			for ( int ix = 0 ; ix < dvbv.provider.Provider.getProviders().size() ; ix++ )
				channelsLists.add( new HashMap< String, Channel>() ) ;
	}
	private String determineExePath( String dataPath )
	{
		String exePath = System.getProperty("user.dir") ;
		String iniFile =   exePath + File.separator + NAME_USERMODE_FILE ;
		File f = new File( iniFile ) ;
		if ( ! f.exists() && dataPath == null )
		{
			File jarFile = null ;
			try {
				jarFile = new File(dvbv.main.TimerImportTool.class.getProtectionDomain()
						.getCodeSource().getLocation().toURI());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			exePath = jarFile.getParent() ;
			//System.out.println( this.exePath ) ;
		}
		return exePath ;
	}
	private String determineDataPath()
	{
		String iniFile = this.exePath + File.separator + NAME_USERMODE_FILE ;
		File f = new File( iniFile ) ;
		BufferedReader bR;
		try {
			bR = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			throw new ErrorClass( e, NAME_USERMODE_FILE + " not found. The importer must be located in the DVBViewer directory.");
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
								path = exePath + path ;
							else if ( value.equals( "1" ) )
								path = System.getenv( "APPDATA") + path ;
							else if ( value.equals( "2" ) )
							{
								String temp = System.getenv( "APPDATA") ;
								temp = temp.substring(temp.lastIndexOf(File.separator)) ;
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
		} catch (IOException e) {
			throw new ErrorClass( e, "Error on reading the file \"" + iniFile + "\"." );
		}
		if ( !userMode && ! root )
			throw new ErrorClass( "Illegal format of the file \"" + iniFile + "\"." ) ;
		File directory = new File( path ) ;
		if ( !directory.isDirectory() )
			throw new ErrorClass( "Directory \"" + path + "\" not found. The File \"" + iniFile + "\" should be checked." ) ;
		Log.setFile(path) ;
		return path ;
	}
	public void updateDVBViewer()
	{
		if ( this.service != null && this.service.isEnabled() )
		{
			this.recordEntries = this.service.readTimers() ;
			this.mergeXMLWithServiceData() ;
			try {
				this.setDVBViewerTimers() ;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.writeXML() ;
		}
	}
	public void prepare()
	{
		if ( this.recordEntries == null )
		{
			if ( this.service != null && this.service.isEnabled() )
			{
				this.recordEntries = this.service.readTimers() ;
				this.mergeXMLWithServiceData() ;
			}
			else
				this.recordEntries = this.readXML() ;
		}
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
				
		for ( Iterator< DVBViewerEntry > it = this.recordEntries.iterator() ; it.hasNext() ; )
		{
			DVBViewerEntry e = it.next() ;
			if ( e.getProvider() == provider && ! e.isMergeElement() )
			{
				e.setMissing() ;
			}
		}
	}
	private void removeOutdatedProviderEntries()
	{		
		for ( Iterator< DVBViewerEntry > it = this.recordEntries.iterator() ; it.hasNext() ; )
		{
			DVBViewerEntry e = it.next() ;
			if ( e.isOutdatedByProvider() )
				e.setToDelete() ;
		}
	}
	
	public void addNewEntry( Provider provider,
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
											   start,
											   end,
											   startOrg,
											   endOrg,
											   title,
											   c.getMerge( provider.getMerge() ),
											   provider ) ;

		for ( Iterator< DVBViewerEntry > it = this.recordEntries.iterator() ; it.hasNext() ; )
		{
			DVBViewerEntry co = it.next() ; 
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
											   start,
											   end,
											   start,
											   end,
											   title,
											   c.getMerge( provider.getMerge() ),
											   provider ) ;

		for ( Iterator< DVBViewerEntry > it = this.recordEntries.iterator() ; it.hasNext() ; )
		{
			DVBViewerEntry co = it.next() ; 
			if ( e.isOrgEqual( co ) )
			{
				co.setToDelete() ;
				break ;
			}
		}
	}
	public String getExePath()        { return this.exePath ; } ;
	public String getExeName()        { return this.exeName ; } ;
	public String getDataPath()       { return this.dataPath ; } ;
	public String getPluginConfPath() { return this.pluginConfPath ; } ;
	public void setService( DVBViewerService s ) { this.service = s ; }
	public DVBViewerService getService() { return this.service ; } ;
	public void setEnableWOL( boolean e ) { this.service.setEnableWOL( e ) ; } ;
	public void setBroadCastAddress( String b ) { this.service.setBroadCastAddress( b ) ; } ;
	public void setMacAddress( String m ) { this.service.setMacAddress( m ) ; } ;
	public void setWaitTimeAfterWOL( int w ) { this.service.setWaitTimeAfterWOL( w ) ; } ;
	public void setAfterRecordingAction( ActionAfterItems dvbViewerActionAfter ) { this.afterRecordingAction = dvbViewerActionAfter ; } ;
	public ActionAfterItems getAfterRecordingAction() { return this.afterRecordingAction ; } ;
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
		for ( Iterator<dvbv.control.Channel> it = channelSet.getChannels().iterator() ; it.hasNext() ; )
		{
			dvbv.control.Channel cC = it.next() ;
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
	public void setDVBViewerTimer( DVBViewerEntry d  ) throws InterruptedException
	{
		if (    d.getToDo() == DVBViewerEntry.ToDo.NEW )
		{
			String rs = this.exePath + File.separator + "dvbv_tvg.exe " ;
			rs += "-a0 -t0 " ;
			rs += "-d \"" + d.getTitle() + "\" " ;
			rs += "-c \"" + d.getChannel() + "\" " ;
			rs += "-e " + Conversions.longToSvcDayString(  d.getStart() ) + " ";
			rs += "-s " + Conversions.longToSvcTimeString( d.getStart() ) + " ";
			rs += "-p " + Conversions.longToSvcTimeString( d.getEnd() ) + " ";
			if ( this.afterRecordingAction != ActionAfterItems.DEFAULT )
			rs += "-a " + this.afterRecordingAction.getID() ;
			Log.out(true, rs) ;
			try {
				Runtime.getRuntime().exec( rs ).waitFor() ;
			} catch (IOException e) {
			throw new ErrorClass( e, "Error on executing the file \"dvbv_tvg.exe\". File missing?" );
			}
		}
	}
	public void setDVBViewerTimers() throws InterruptedException
	{
		if ( this.service != null && this.service.isEnabled() )
			this.removeOutdatedProviderEntries();
		DVBViewerEntry.beforeRecordingSettingProcces( this.recordEntries,
				                                      this.separator, this.maxID ) ;
		this.merge() ;

			
		int updatedEntries = 0 ;
		int newEntries = 0 ;
		String rsBase = this.exePath + File.separator + "dvbv_tvg.exe " ;
		rsBase += "-a0 -t0 " ;

		for ( Iterator<DVBViewerEntry> it = this.recordEntries.iterator() ; it.hasNext() ; )
		{
			DVBViewerEntry d = it.next();
			
			if ( d.mustDeleted() )
			{
				if ( this.service != null && this.service.isEnabled() )
					this.service.setTimerEntry( d, this.afterRecordingAction ) ;
				else
					this.setDVBViewerTimer( d  ) ;
			}			
		}
		for ( Iterator<DVBViewerEntry> it = this.recordEntries.iterator() ; it.hasNext() ; )
		{
			DVBViewerEntry d = it.next();
			if ( d.mustDeleted() )
				continue ;
			if ( d.mustUpdated() )
				updatedEntries++ ;
			else if ( ! d.mustIgnored() )
				newEntries++ ;

			if ( this.service != null && this.service.isEnabled() )
				this.service.setTimerEntry( d, this.afterRecordingAction ) ;
			else
				this.setDVBViewerTimer( d  ) ;
		}
		Log.out(false,     "Number of new entries:     " + Integer.toString( newEntries )
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
			Stack<String>   stack = new Stack<String>();
			
			while( reader.hasNext() ) {
				XMLEvent ev = reader.nextEvent();
				if( ev.isStartElement() )
				{
					stack.push( ev.asStartElement().getName().getLocalPart() );
					if ( ! stack.equals( DVBViewer.xmlPath ) )
						continue ;
					DVBViewerEntry entry = DVBViewerEntry.readXML( reader, ev, f ) ;
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
			try {
				writer = output.createXMLStreamWriter(
						new FileOutputStream( file), "ISO-8859-1");
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
		} catch (XMLStreamException e) {
			throw new ErrorClass( e,   "Error on writing XML file \"" + file.getAbsolutePath() 
	                 + ". Position: Line = " + Integer.toString( e.getLocation().getLineNumber() )
	                 +         ", column = " + Integer.toString(e.getLocation().getColumnNumber()) ) ;
		}
	}
	public void mergeXMLWithServiceData()
	{
		ArrayList<DVBViewerEntry> lastTimers = readXML() ;
		DVBViewerEntry.updateXMLDataByServiceData( lastTimers, this.recordEntries, this.separator, this.maxID ) ;
		this.recordEntries = lastTimers ;
	}
}
