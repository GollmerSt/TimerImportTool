// $LastChangedDate: 2010-01-28 09:11:48 +0100 (Do, 28 Jan 2010) $
// $LastChangedRevision: 15 $
// $LastChangedBy: Stefan Gollmer $


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class DVBViewer {
	private DVBViewerService service = null ;
	private ArrayList<DVBViewerEntry> newEntries = null;
	@SuppressWarnings("unused")
	private ArrayList<TVInfoRecording> deletedRecodings = null;
	private HashMap< String, Channel> channelsByTVInfo      = null;
	private HashMap< String, Channel> channelsByClickFinder = null;
	private final String exePath ;
	private final String dataPath ;
	private final String exeName ;
	private final String pluginConfPath ;
	private Combine combine = new Combine( true );
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
		this.newEntries = new ArrayList<DVBViewerEntry>() ;
		this.channelsByTVInfo      = new HashMap< String, Channel>() ;
		this.channelsByClickFinder = new HashMap< String, Channel>() ;
	}
	private String determineExePath( String dataPath )
	{
		String exePath = System.getProperty("user.dir") ;
		String iniFile =   exePath + File.separator + Constants.NAME_USERMODE_FILE ;
		File f = new File( iniFile ) ;
		if ( ! f.exists() && dataPath == null )
		{
			File jarFile = null ;
			try {
				jarFile = new File(TimerImportTool.class.getProtectionDomain()
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
		String iniFile = this.exePath + File.separator + Constants.NAME_USERMODE_FILE ;
		File f = new File( iniFile ) ;
		BufferedReader bR;
		try {
			bR = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			throw new ErrorClass( e, Constants.NAME_USERMODE_FILE + " not found. The importer must be located in the DVBViewer directory.");
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
	private void addNewEntry( HashMap< String, Channel> channelMap,
			                           String channel, 
			                           long start, 
			                           long end, 
			                           String title )
	{
		if ( ! channelMap.containsKey( channel ) )
			throw new ErrorClass( "Channel \"" + channel + "\" not found in channel list" ) ;
		Channel c =  channelMap.get( channel ) ;
		TimeOffsets o = c.getOffsets() ;
		start -= o.getPreOffset(start)*60000 ;
		end  += o.getPostOffset(end)*60000 ;
		String dvbViewerChannel = c.getDVBViewer() ;
		if ( dvbViewerChannel.length() == 0 || dvbViewerChannel.equalsIgnoreCase("none") )
			throw new ErrorClass( "DVBViewer entry of channel \"" + channel + "\" not defined in channel list" ) ;
		boolean combine = this.combine.toCombine() ;
		if ( c.getCombine().isValid() )
			combine = c.getCombine().toCombine() ;
		DVBViewerEntry e = new DVBViewerEntry( c.getDVBViewer(), start, end, title, combine ) ;
		this.newEntries.add( e ) ;
	}
	public void addNewTVInfoEntry( String channel, long start, long end, String title )
	{
		this.addNewEntry(this.channelsByTVInfo, channel, start, end, title ) ;
	} ;
	public void addNewClickFinderEntry( String channel, long start, long end, String title )
	{
		this.addNewEntry(this.channelsByClickFinder, channel, start, end, title ) ;
	} ;
	public String getExePath()        { return this.exePath ; } ;
	public String getExeName()        { return this.exeName ; } ;
	public String getDataPath()       { return this.dataPath ; } ;
	public String getPluginConfPath() { return this.pluginConfPath ; } ;
	public void setService( DVBViewerService s ) { this.service = s ; } ;
	public void setEnableWOL( boolean e ) { this.service.setEnableWOL( e ) ; } ;
	public void setBroadCastAddress( String b ) { this.service.setBroadCastAddress( b ) ; } ;
	public void setMacAddress( String m ) { this.service.setMacAddress( m ) ; } ;
	public void setWaitTimeAfterWOL( int w ) { this.service.setWaitTimeAfterWOL( w ) ; } ;
	public void addChannel( String dvbViewer,
			        		String tvInfo, 
			        		String clickFinder, 
			        		TimeOffsets offsets,
			        		Combine combine )
	{
		Channel c = new Channel( dvbViewer, tvInfo, clickFinder, offsets, combine ) ;
		channelsByTVInfo.put( new String( tvInfo ) , c ) ;
		channelsByClickFinder.put( new String( clickFinder ), c ) ;
	}
	public void combine()
	{
		for ( int iO = 0 ; iO < this.newEntries.size() ; iO++ )
		{
			if ( ! this.newEntries.get( iO ).toCombine() )
				continue ;
			boolean changed = true ;
			while ( changed )
			{
				changed = false ;
				DVBViewerEntry o = this.newEntries.get( iO ) ;
				String channel = o.getChannel() ;
				long start = o.getStart() ;
				long end   = o.getEnd() ;
			
				for ( int iI = iO+1 ; iI < this.newEntries.size() ; iI++)
				{
					DVBViewerEntry i = this.newEntries.get( iI ) ;
					if ( !channel.equals( i.getChannel() ) )
						continue ;
					long startI = i.getStart() ;
					long endI   = i.getEnd() ;
					if ( o.isInRange(startI, endI) )
					{
						start = Math.min(start, startI) ;
						end  = Math.max( end, endI ) ;
						String title = i.getTitle();
						if ( start < startI )
							title += this.separator + i.getTitle() ;
						else
							title = i.getTitle() + this.separator + title ;
						DVBViewerEntry e = new DVBViewerEntry( channel, start, end, title, o.toCombine() ) ;
						this.newEntries.remove( iO ) ;
						this.newEntries.add( iO, e ) ;
						this.newEntries.remove( iI ) ;
						changed = true ;
						break ;
					}
				}
			}
		}
	}
	public void setDVBViewerTimers() throws IOException, InterruptedException
	{
		String rsBase = this.exePath + File.separator + "dvbv_tvg.exe " ;
		rsBase += "-a0 -t0 " ;

		for ( Iterator<DVBViewerEntry> it = this.newEntries.iterator() ; it.hasNext() ; )
		{
			DVBViewerEntry d = it.next();
			if ( this.service != null )
				this.service.setTimerEntry( d.getChannel(), d.getTitle(), d.getStart(), d.getEnd()) ;
			else
			{
				String rs = rsBase ;
				rs += "-d \"" + d.getTitle() + "\" " ;
				rs += "-c \"" + d.getChannel() + "\" " ;
				rs += "-e " + Conversions.longToSvcDayString(  d.getStart() ) + " ";
				rs += "-s " + Conversions.longToSvcTimeString( d.getStart() ) + " ";
				rs += "-p " + Conversions.longToSvcTimeString( d.getEnd() ) + " ";
				Runtime.getRuntime().exec( rs ).waitFor() ;
				Log.out(true, rs) ;
			}
		}
	}
	public void setDeletedRecordings( ArrayList<TVInfoRecording> l ){ this.deletedRecodings = l ; } ;
	public Combine getCombine() { return combine ; } ;
	public void setSeparator( String s ) { this.separator = s ; } ;
}
