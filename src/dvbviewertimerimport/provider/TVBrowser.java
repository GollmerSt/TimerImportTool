// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


import dvbviewertimerimport.DVBViewerTimerImport;
import dvbviewertimerimport.control.Channel;
import dvbviewertimerimport.control.ChannelSet;
import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.provider.Provider;

public class TVBrowser extends Provider
{
	private HashSet< String > channelSet = null ;
	
	public TVBrowser(Control control )
	{
		super(control, false, false, "TV-Browser", false, false, false, false, false, false);
		this.canImport = true ;
		this.canModify = false ;
		this.canAddChannel = false ;
		this.isFunctional = false ;
		
	}
	
	@Override
	protected ArrayList< Channel > readChannels()
	{
		String [] channels = DVBViewerTimerImport.getTVBChannelNames() ;
		
		ArrayList< Channel > result = new ArrayList< Channel >() ;
		
		for ( String cS : channels )
		{
			Channel c = new Channel( getID(), (String) cS, -1  )
			{
				@Override
				public Object getIDKey()
				{
					return this.getName() ;
				}
				@Override
				public Object getIDKey( final Channel c ) { return c.getName() ; } ;  // ID of the provider, type is provider dependent
			};
			result.add( c ) ;
		}
		return result ;
	} ;
	@Override
	public int importChannels( boolean check )
	{
		if ( check )
			return 0 ;
		
		if ( ! isFunctional() )
			return -1 ;
		
		return this.assignChannels() ;
	}
	@Override
	public boolean containsChannel( final Channel channel, boolean ifList )
	{
		if ( ! isFunctional() )
			return true ;
		
		if (this.channelSet == null)
		{
			this.channelSet = new HashSet<String>();
			String[] channels = DVBViewerTimerImport.getTVBChannelNames();
			for (String name : channels)
				this.channelSet.add(name);
		}
		return this.channelSet.contains( channel.getName() ) ;
	}		
	@Override
	public void updateChannelMap()
	{
		this.channelSet = null ;
	}
	@Override
	public boolean isChannelMapAvailable()
	{
		return isFunctional() ;
	}
}
