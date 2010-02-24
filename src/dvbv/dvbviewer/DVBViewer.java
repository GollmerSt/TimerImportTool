// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.dvbviewer ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import dvbv.tvinfo.TVInfoRecording ;
import dvbv.control.ChannelSet;
import dvbv.control.TimeOffsets;
import dvbv.misc.* ;

public class DVBViewer {
	private static final String NAME_USERMODE_FILE            = "usermode.ini" ;

	private DVBViewerService service = null ;
	private ArrayList<DVBViewerEntry> recordEntries = null;
	@SuppressWarnings("unused")
	private ArrayList<TVInfoRecording> deletedRecodings = null;
	private ArrayList< HashMap< String, Channel> > channelsLists 
	        = new ArrayList< HashMap< String, Channel> >( dvbv.provider.Provider.getProviders().size() ) ;
	private final String exePath ;
	private final String dataPath ;
	private final String exeName ;
	private final String pluginConfPath ;
	private boolean generalMerge = false ;
	private String separator      = ",," ;
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
	public void setGeneralMerge( boolean merge ) { this.generalMerge = merge ; } ;
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
	private void checkRecordingEntries()
	{
		if ( this.recordEntries == null )
		{
			if ( this.service != null && this.service.isEnabled() )
				this.recordEntries = this.service.readTimers() ;
			else
				this.recordEntries = new ArrayList<DVBViewerEntry>() ;
		}
		
	}
	public void addNewEntry( int type,
							 String channel, 
							 long start, 
							 long end,
							 String title )
	{
		this.checkRecordingEntries();
		HashMap< String, Channel > channelMap = this.channelsLists.get( type ) ;
		if ( ! channelMap.containsKey( channel ) )
			throw new ErrorClass( "Channel \"" + channel + "\" not found in channel list" ) ;
		Channel c =  channelMap.get( channel ) ;
		TimeOffsets o = c.getOffsets() ;
		start -= o.getPreOffset(start)*60000 ;
		end  += o.getPostOffset(end)*60000 ;
		String dvbViewerChannel = c.getDVBViewer() ;
		if ( dvbViewerChannel == null || dvbViewerChannel.length() == 0 )
			throw new ErrorClass( "DVBViewer entry of channel \"" + channel + "\" not defined in channel list" ) ;
		DVBViewerEntry e = new DVBViewerEntry( c.getDVBViewer(),
											   start,
											   end,
											   title,
											   c.getMerge( this.generalMerge ) ) ;
		this.recordEntries.add( e ) ;
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
		this.checkRecordingEntries();
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
							this.recordEntries.add( newEntry ) ;
						changed = true ;
						break ;
					}
				}
			}
		}
	}
	public void setDVBViewerTimers() throws InterruptedException
	{
		int updatedEntries = 0 ;
		int newEntries = 0 ;
		String rsBase = this.exePath + File.separator + "dvbv_tvg.exe " ;
		rsBase += "-a0 -t0 " ;

		for ( Iterator<DVBViewerEntry> it = this.recordEntries.iterator() ; it.hasNext() ; )
		{
			DVBViewerEntry d = it.next();
			
			if ( d.mustUpdated() )
				updatedEntries++ ;
			else if ( ! d.mustIgnored() )
				newEntries++ ;

			if ( this.service != null && this.service.isEnabled() )
			{
				this.service.setTimerEntry( d ) ;
			}
			else if (    d.getToDo() == DVBViewerEntry.ToDo.NEW
					  || d.getToDo() == DVBViewerEntry.ToDo.NEW_UPDATED )
			{
				String rs = rsBase ;
				rs += "-d \"" + d.getTitle() + "\" " ;
				rs += "-c \"" + d.getChannel() + "\" " ;
				rs += "-e " + Conversions.longToSvcDayString(  d.getStart() ) + " ";
				rs += "-s " + Conversions.longToSvcTimeString( d.getStart() ) + " ";
				rs += "-p " + Conversions.longToSvcTimeString( d.getEnd() ) + " ";
				Log.out(true, rs) ;
				try {
					Runtime.getRuntime().exec( rs ).waitFor() ;
				} catch (IOException e) {
					throw new ErrorClass( e, "Error on executing the file \"dvbv_tvg.exe\". File missing?" );
				}
			}
		}
		Log.out(false,     "Number of new entries:     " + Integer.toString( newEntries )
				       + "\nNumber of updated entries: " + Integer.toString( updatedEntries ) ) ;
	}
	public void setDeletedRecordings( ArrayList<TVInfoRecording> l ){ this.deletedRecodings = l ; } ;
	public void setSeparator( String s ) { this.separator = s ; } ;
}
