// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TimeZone;

import dvbviewertimerimport.control.Channel;
import dvbviewertimerimport.control.ChannelSet;
import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.dvbviewer.DVBViewer;
import dvbviewertimerimport.misc.ErrorClass;
import dvbviewertimerimport.misc.Log;
import dvbviewertimerimport.misc.Registry;
import dvbviewertimerimport.misc.ResourceManager;
import dvbviewertimerimport.provider.Provider;

public class TVGenial extends Provider {

	private static final String NAME_PLUGIN_PATH   = "DVBViewer" ;
	private static final String NAME_CHANNEL_FILE  = "tvuid.txt" ;
	private static final String PATH_SCRIPT_FILE   = "TVGenial/DVBViewer.txt" ;
	private static final String PATH_LOGO_FILE     = "TVGenial/Logo.png" ;
	private static final String PATH_RECORDER_FILE = "TVGenial/recorder.ini" ;
	private static final String PATH_SETUP_FILE    = "TVGenial/Setup.ini" ;

	private final SimpleDateFormat dateFormat ;
	
	private HashSet< Long > channelSet = null ;
	
	public TVGenial( Control control ) {
		super( control, false, false, "TVGenial", false, false, false, true, false, false);
		this.timeZone = TimeZone.getTimeZone("Europe/Berlin") ;
		this.dateFormat = new SimpleDateFormat("yyyyMMddHHmm") ;
		this.dateFormat.setTimeZone( timeZone ) ;
		this.canAddChannel = false ;
		this.canImport = true ;
		
		this.isFunctional = null != Registry.getValue( "HKEY_LOCAL_MACHINE\\SOFTWARE\\ARAKON-Systems\\TVgenial", "InstallDir" ) ;
	}
	@Override
	public boolean install()
	{
		String programDir = Registry.getValue( "HKEY_LOCAL_MACHINE\\SOFTWARE\\ARAKON-Systems\\TVgenial", "InstallDir" ) ;
		if ( programDir == null )
		{
			Log.out( "Registry entry of TVGenial not found. \nIt seems to be that installation of TVGenial is failed." ) ;
			return false ;
		}
		
		File file = new File( programDir + File.separator + "Interfaces" + File.separator + TVGenial.NAME_PLUGIN_PATH  ) ;
		file.mkdir() ;
		
		String jarFile =   this.control.getDVBViewer().getExePath()
		                 + File.separator 
		                 + this.control.getDVBViewer().getExeName() ;
		
		ArrayList< String[] > keyList = new ArrayList< String[] >() ;
		
		String [] stringSet = new String[ 2 ] ;
		
		stringSet[0] = "%JAR_File%" ;
		stringSet[1] = jarFile ;
		
		keyList.add( stringSet ) ;
				
		ResourceManager.copyFile( file.getPath(), PATH_SCRIPT_FILE, keyList, true ) ;
		ResourceManager.copyBinaryFile( file.getPath(), PATH_LOGO_FILE ) ;
		ResourceManager.copyFile( file.getPath(), PATH_RECORDER_FILE ) ;
		ResourceManager.copyFile( file.getPath(), PATH_SETUP_FILE ) ;
		
		return true ;
	}
	@Override
	public boolean uninstall()
	{
		String programDir = Registry.getValue( "HKEY_LOCAL_MACHINE\\SOFTWARE\\ARAKON-Systems\\TVgenial", "InstallDir" ) ;
		if ( programDir == null )
		{
			Log.out( "Registry entry of TVGenial not found. \nIt seems to be that installation of TVGenial is failed." ) ;
			return false ;
		}
		
		File dir = new File( programDir + File.separator + "Interfaces" + File.separator + TVGenial.NAME_PLUGIN_PATH  ) ;
		
		String [] files = { PATH_SCRIPT_FILE, PATH_LOGO_FILE, PATH_RECORDER_FILE, PATH_SETUP_FILE } ;
		
		for ( String fs : files )
		{
			File f = new File( dir.getPath() + File.separator + fs.split( "/" )[1] ) ;
			f.delete() ;
		}
		dir.delete() ;
		
		return true ;
	}
	private ArrayList< Channel > readChannels()
	{
		String dataPath = Registry.getValue( "HKEY_LOCAL_MACHINE\\SOFTWARE\\ARAKON-Systems\\TVgenial", "PublicDataRoot") ;
		if ( dataPath == null )
			return null ;
		
		ArrayList< Channel > list = new ArrayList< Channel >() ;

		File f = new File( dataPath + File.separator + NAME_CHANNEL_FILE ) ;
		
		if ( ! f.canRead() )
			throw new ErrorClass( "File \"" + f.getAbsolutePath() + "\" not found" ) ;

		FileReader fr;
		try {
			fr = new FileReader( f );
		} catch (FileNotFoundException e) {
			return null ;
		}
			
		BufferedReader br = new BufferedReader( fr ) ;
			
		String line ;
		
		HashMap< String, Long > nameMap = new HashMap< String, Long >() ;

		int pid = this.getID() ;
		
		for ( ChannelSet cs : this.control.getChannelSets() )
		{
			Channel c = cs.getChannel( pid ) ;
			if ( c == null )
				continue ;
			nameMap.put( c.getName(), c.getID() ) ;
		}
			
		try {
			while ( (line = br.readLine() ) != null )
			{
				line = line.trim();
				
				if ( line.substring( 0, 2 ).equals( "//" ) )
					continue ;
				String [] parts = line.split( "\\|" ) ;
				
				if ( parts.length < 2 )
					continue ;
				long tvuid = Long.valueOf( parts[0] ) ; 
				String name = parts[1] ;
				String nameLong = name ;
				
				
				int countR = 0 ;
				while ( nameMap.containsKey( nameLong ) )
				{
					if ( nameMap.get( nameLong) == tvuid )
						break ;
					nameLong = name + Integer.valueOf( countR++ ) ;
				}
				Channel c = new Channel( pid, nameLong, tvuid ) ;
				list.add( c ) ; ;
				nameMap.put( nameLong, tvuid ) ;
			}
		} catch (IOException e) {
			return null ;
		}
	return list ;
}
	@Override
	public int importChannels( boolean check )
	{
		if ( check )
			return 0 ;
		
		ArrayList< Channel > channels = readChannels() ;
		
		if ( channels == null )
			return -1 ;
		
		HashMap< Long, ChannelSet > mapByID = new HashMap< Long, ChannelSet >() ;

		int pid = this.getID() ;
		
		for ( ChannelSet cs : this.control.getChannelSets() )
		{
			Channel c = cs.getChannel( pid ) ;
			if ( c == null )
				continue ;
			mapByID.put( c.getID(), cs ) ;
		}
		
		int count = 0 ;

		for ( Channel c : channels )
		{
			if ( ! mapByID.containsKey( c.getID() ) )
			{
				ChannelSet cs = new ChannelSet() ;
				cs.add( c ) ;
				control.getChannelSets().add( cs ) ;
				count++ ;
			}
		}
	return count ;
	}
	private String getParaInfo()
	{
		return 
		", necessary parameters:\n   -TVGenial TVUID=ccc Beginn=yyyyMMddHHmm Dauer=nnn Sendung=cccccc" ;
	}
	@Override
	public boolean processEntry( Object args, DVBViewer.Command command )
	{		
		long tvuid = -1 ;
		String startTime = null ;
		long milliSeconds = -1 ;
		String title = null ;
		
		boolean mustDelete = false ;
		
		
		
		for ( String p : (String [])args )
		{			
			int pos = p.indexOf('=') ;
			if ( pos < 0 )
			{
				if ( p.trim().equalsIgnoreCase("-delete"))
					mustDelete = true ;
				continue ;
			}
			String key   = p.substring(0, pos).trim() ;
			String value = p.substring(pos+1).trim() ;
			
			if      ( key.equalsIgnoreCase("TVUID"))
			{
				if ( ! value.matches( "\\d+" ))
				{
					String errorString = this.getParaInfo() ;
					throw new ErrorClass( "Invalid parameter TVUID" + errorString ) ;
				}
				tvuid = Long.valueOf( value ) ;
			}
			else if ( key.equalsIgnoreCase("Beginn"))
				startTime = value ;
			else if ( key.equalsIgnoreCase("Dauer"))
			{
				if ( ! value.matches("\\d+") )
					throw new ErrorClass( "Undefined value of parameter \"Dauer\".") ;
				milliSeconds = Long.valueOf(value) * 60000 ;
			}
			else if ( key.equalsIgnoreCase("Sendung"))
				title = value ;
		}
		if ( tvuid < 0 || startTime == null || milliSeconds < 0 || title == null )
		{
			String errorString = this.getParaInfo() ;
			throw new ErrorClass( "Missing parameter" + errorString ) ;
		}
		long start = 0 ;
		try {
			start = timeToLong( startTime ) ;
		} catch (ParseException e) {
			String errorString = this.getParaInfo() ;
			throw new ErrorClass( e, "Syntax error in the parameter \"Begin\"" + errorString ) ;
		}
		long end = start + milliSeconds ;
		
		String channel = null ; ;
				
		for ( ChannelSet cs : control.getChannelSets() )
		{
			Channel c = cs.getChannel( this.getID() ) ;
			if ( c == null )
				continue ;
			if ( tvuid == c.getID() )
			{
				channel = c.getName() ;
				break ;
			}
		}
		if ( mustDelete )
			this.control.getDVBViewer().deleteEntry( this, channel, start, end, title) ;
		else
			this.control.getDVBViewer().addNewEntry( this, null, channel, start, end, title ) ;
		return true ;
	}
	private long timeToLong( String time ) throws ParseException
	{
		Date d = new Date( dateFormat.parse(time).getTime()) ;
		//System.out.println(d.toString()) ;
		return d.getTime() ;
	}
	
	@Override
	public boolean containsChannel( final Channel channel )
	{
		if ( ! isFunctional() )
			return true ;
		
		if (this.channelSet == null)
		{
			this.channelSet = new HashSet< Long >();
			ArrayList< Channel > channels = readChannels() ;
			for ( Channel c : channels)
				this.channelSet.add( c.getID() );
		}
		return this.channelSet.contains( channel.getID() ) ;
	}		
	@Override
	public void updateChannelMap()
	{
		this.channelSet = null ;
	}
	@Override
	public boolean isChannelMapAvailable()
	{
		return false ;
	}
}