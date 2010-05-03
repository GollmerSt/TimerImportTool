// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


import dvbviewertimerimport.control.Channel;
import dvbviewertimerimport.control.ChannelSet;
import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.misc.ErrorClass;
import dvbviewertimerimport.provider.Provider;

public class TVInfoDVBV {
	public final static String NAME_IMPORTFILE = "tvinfoDVBV.ini" ;
	private final Control control ;
	public TVInfoDVBV( Control control )
	{
		this.control = control ;
		this.importer() ;
	}
	public void importer()
	{
		HashMap< String, ChannelSet > map = new HashMap< String, ChannelSet >() ;
		
		int pid = Provider.getProviderID( "TVInfo" ) ;
		
		for ( ChannelSet cs :  control.getChannelSets() )
		{
			Channel c = cs.getChannel( pid ) ;
			if ( c == null )
				continue ;
			map.put( cs.getChannel( pid ).getName(), cs ) ;
		}
		
		File f = new File( control.getDVBViewer().getPluginConfPath()
		          + File.separator
		          + NAME_IMPORTFILE ) ;
		if ( ! f.canRead() )
			throw new ErrorClass( f.getAbsolutePath() ) ;
		FileReader fr;
		try {
			fr = new FileReader( f );
		} catch (FileNotFoundException e) {
			throw new ErrorClass( f.getAbsolutePath() ) ;
		}
		
		BufferedReader br = new BufferedReader( fr ) ;
		
		boolean channelsFound = false ;
		String line ;
		
		try {
			while ( (line = br.readLine() ) != null )
			{
				line = line.trim();
				if ( ! channelsFound )
				{
					if ( line.equalsIgnoreCase( "[Channels]" ) )
						channelsFound = true ;
					continue ;
				}
				else
				{
					String [] parts = line.split( "=", 2 ) ;
					if ( parts.length < 2 )
						continue ;
					String program = parts[0] ;
					String channel = parts[1] ;
					
					if ( channel.equalsIgnoreCase( "none" ) )
						continue ;
					
					if ( map.containsKey( program ) )
					{
						map.get( program ).setDVBViewerChannel( channel ) ;
					}
				}
			}
		} catch (IOException e) {
			throw new ErrorClass( f.getAbsolutePath() ) ;
		}
	}
}
