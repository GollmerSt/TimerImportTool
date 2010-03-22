// $LastChangedDate: 2010-03-21 22:47:26 +0100 (So, 21. Mrz 2010) $
// $LastChangedRevision: 200 $
// $LastChangedBy: Stefan Gollmer $

package dvbv.tvgenial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import dvbv.Resources.ResourceManager;
import dvbv.control.Channel;
import dvbv.control.ChannelSet;
import dvbv.control.Control;
import dvbv.misc.Conversions;
import dvbv.misc.ErrorClass;
import dvbv.misc.Registry;
import dvbv.provider.Provider;

public class TVGenial extends Provider {

	private static final String NAME_PLUGIN_PATH   = "DVBViewer" ;
	private static final String NAME_CHANNEL_FILE  = "tvuid.txt" ;
	private static final String PATH_SCRIPT_FILE   = "TVGenial/DVBViewer.txt" ;
	private static final String PATH_LOGO_FILE     = "TVGenial/Logo.png" ;
	private static final String PATH_RECORDER_FILE = "TVGenial/recorder.ini" ;
	private static final String PATH_SETUP_FILE    = "TVGenial/Setup.ini" ;
	
	public TVGenial( Control control ) {
		super( control, false, false, "TVGenial", false, false, false, true, true, false);
	}
	@Override
	public boolean install()
	{
		String programDir = Registry.getValue( "HKEY_LOCAL_MACHINE\\SOFTWARE\\ARAKON-Systems\\TVgenial", "InstallDir" ) ;
		if ( programDir == null )
			return false ;
		
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
			return false ;
		
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
	@Override
	public int importChannels( boolean check )
	{
		if ( check )
			return 0 ;
		
		String dataPath = Registry.getValue( "HKEY_LOCAL_MACHINE\\SOFTWARE\\ARAKON-Systems\\TVgenial", "PublicDataRoot") ;
		if ( dataPath == null )
			return -1 ;

		HashMap< Long, ChannelSet > mapByID = new HashMap< Long, ChannelSet >() ;
		HashMap< String, ChannelSet > mapByName = new HashMap< String, ChannelSet >() ;
		
		int pid = this.getID() ;
		
		for ( Iterator< ChannelSet > it = this.control.getChannelSets().iterator() ; it.hasNext() ; )
		{
			ChannelSet cs = it.next() ;
			Channel c = cs.getChannel( pid ) ;
			if ( c == null )
				continue ;
			mapByID.put( c.getID(), cs ) ;
			mapByName.put( c.getName(), cs ) ;
		}

		File f = new File( dataPath + File.separator + NAME_CHANNEL_FILE ) ;
		
		if ( ! f.canRead() )
			throw new ErrorClass( "File \"" + f.getAbsolutePath() + "\" not found" ) ;

		FileReader fr;
		try {
			fr = new FileReader( f );
		} catch (FileNotFoundException e) {
			return -1 ;
		}
			
		BufferedReader br = new BufferedReader( fr ) ;
			
		String line ;
		
		int count = 0 ;
			
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
				
				if ( ! mapByID.containsKey( tvuid ) )
				{
					int countR = 0 ;
					while ( mapByName.containsKey( nameLong ) )
						nameLong = name + Integer.valueOf( countR++ ) ;
					ChannelSet cs = new ChannelSet() ;
					cs.add( pid, nameLong, tvuid ) ;
					control.getChannelSets().add( cs ) ;
					mapByID.put( tvuid, cs ) ;
					mapByName.put( nameLong, cs ) ;
					count++ ;
				}
			}
		} catch (IOException e) {
			return -1 ;
		}
	return count ;
	}
	private String getParaInfo()
	{
		return 
		", necessary parameters:\n   -TVGenial TVUID=ccc Begin=yyyyMMddHHmm Dauer=nnn Sendung=cccccc" ;
	}
	@Override
	public void processEntry( String[] args )
	{		
		long tvuid = -1 ;
		String startTime = null ;
		long milliSeconds = -1 ;
		String title = null ;
		
		boolean mustDelete = false ;
		
		for ( int i = 0 ; i < args.length ; i++ )
		{
			String p = args[i] ;
			int pos = p.indexOf('=') ;
			if ( pos < 0 )
				continue ;
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
			else if ( key.equalsIgnoreCase("-delete"))
				mustDelete = true ;
		}
		if ( tvuid < 0 || startTime == null || milliSeconds < 0 || title == null )
		{
			String errorString = this.getParaInfo() ;
			throw new ErrorClass( "Missing parameter" + errorString ) ;
		}
		long start = 0 ;
		try {
			start = Conversions.clickFinderTimeToLong( startTime ) ;
		} catch (ParseException e) {
			String errorString = this.getParaInfo() ;
			throw new ErrorClass( e, "Syntax error in the parameter \"Begin\"" + errorString ) ;
		}
		long end = start + milliSeconds ;
		
		String channel = null ; ;
				
		for ( Iterator< ChannelSet > it = control.getChannelSets().iterator() ; it.hasNext() ; )
		{
			Channel c = it.next().getChannel( this.getID() ) ;
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
			this.control.getDVBViewer().addNewEntry( this, channel, start, end, title ) ;
	}
}