// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.importer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import dvbviewertimerimport.control.Channel;
import dvbviewertimerimport.control.ChannelSet;
import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.misc.Log;
import dvbviewertimerimport.misc.ResourceManager;
import dvbviewertimerimport.misc.TerminateClass;
import dvbviewertimerimport.provider.Provider;

public class UpdateImporter
{
	private static final String XML_PATH = "datafiles/DVBVTimerImportTool.xml" ;

	private final Control control ;
	private boolean warning = false ;
	
	public UpdateImporter( Control control )
	{
		this.control = control ;
	}
	public boolean importXML() throws TerminateClass
	{
		InputStream is = ResourceManager.createInputStream( XML_PATH ) ;

		Control controlReader = new Control( is, XML_PATH ) ;

		ArrayList< HashMap< String, ChannelSet > > pMap = new ArrayList< HashMap< String, ChannelSet > >() ;


		ArrayList<ChannelSet> updateChannels = control.getChannelSets() ;

		for ( int id = 0 ; id < Provider.getProviders().size() ; id++ )
		{
			HashMap< String, ChannelSet > map = new HashMap< String, ChannelSet >() ;
			
			for ( ChannelSet cs : updateChannels )
			{
				Channel c = cs.getChannel( id ) ;
				if ( c != null )
					map.put( new String( c.getName() ), cs ) ;
			}
			pMap.add( map ) ;
		}

		ArrayList<ChannelSet> newChannels = controlReader.getChannelSets() ;

		for ( Iterator< ChannelSet > it = newChannels.iterator() ; it.hasNext() ; )
		{
			ChannelSet cs = it.next() ;
			
			if ( this.update( cs, pMap ) )
				it.remove() ;
		}
		for ( ChannelSet cs : newChannels )
		{
			control.getChannelSets().add( cs ) ;
			Log.out( "Following channel set is added:\n\n" + cs.toString() + "\n" ) ;
		}
		return warning ;
	}
	private boolean update( ChannelSet cs , ArrayList< HashMap< String, ChannelSet > > pMap )
	{
		ChannelSet resultCS = null ;
		
		boolean mustUpdate = false ;

		for ( int id = 0 ; id < Provider.getProviders().size() ; id++ )
		{
			Channel c = cs.getChannel( id ) ;
			if ( c == null )
				continue ;
			HashMap< String, ChannelSet > map = pMap.get( id ) ;
			if ( map.containsKey( c.getName() ) )
			{
				ChannelSet oCS = map.get( c.getName() ) ;
				if (resultCS != oCS )
				{
					if ( resultCS == null )
					{
						resultCS = oCS ;
						continue ;
					}
					this.warning = true ;
					Log.out(   "Warning: Following channel sets are not updated in case of a different assignment:\n\nChannel set of the new version:\n"
							 + cs.toString() + "\n\nChannel set of the actual version:\n" + oCS.toString() + "\n" ) ;
				return true ;
			}
		}
		else
			mustUpdate = true ;
	}

	if ( mustUpdate && resultCS != null )
	{
		for ( int id = 0 ; id < Provider.getProviders().size() ; id++ )
		{
			Channel resultCh = resultCS.getChannel( id ) ;
			if ( resultCh == null )
				continue ;
			
			if ( !resultCh.getName().equals( cs.getChannel( id ).getName() ) )
			{
				Log.out(   "Warning: Following channel sets are not updated in case of a different assignment:\n\nChannel set of the new version:\n"
						 + cs.toString() + "\n\nChannel set of the actual version:\n" + resultCS.toString() + " \n" ) ;
			this.warning = true ;
			return true ;
		}
	}
	Log.out(   "Following channel set is updated:\n\n"
			 + resultCS.toString() + "\nto:\n" + cs.toString() + "\n\n" ) ;
	for ( int id = 0 ; id < Provider.getProviders().size() ; id++ )
	{
		HashMap< String, ChannelSet > map = pMap.get( id ) ;
		Channel c = cs.getChannel( id ) ;
		if ( c != null )
		{
			Channel resC = resultCS.getChannel( id ) ;
			if (  resC != null )
			{
				map.remove( resC.getName() ) ;
				resultCS.remove( id ) ;
			}
			resultCS.add( id, c.getName(), c.getTextID() ) ;
			map.put( new String( c.getName() ), resultCS ) ;
		}
	}
}
return resultCS != null ;
	}
}
