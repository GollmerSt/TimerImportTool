// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.provider;

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
	public int importChannels( boolean check )
	{
		if ( check )
			return 0 ;
		
		if ( ! isFunctional() )
			return -1 ;
    
		String [] channels = DVBViewerTimerImport.getTVBChannelNames() ;
    
		if ( channels == null )
			return -1 ;
    
		int count = 0 ;
    
		HashMap< String, ChannelSet > mapByName = new HashMap< String, ChannelSet >() ;
    
		int pid = this.getID() ;
    
		for ( ChannelSet cs : this.control.getChannelSets() )
		{
			Channel c = cs.getChannel( pid ) ;
			if ( c == null )
				continue ;
			mapByName.put( c.getName(), cs ) ;
		}
    
		for ( String channel : channels )
		{
			if ( ! mapByName.containsKey( channel ) )
			{
				ChannelSet cs = new ChannelSet() ;
				cs.add( pid, channel, -1 ) ;
				control.getChannelSets().add( cs ) ;
				mapByName.put( channel, cs ) ;
				count++ ;
			}
		}
		return count ;
	}
	@Override
	public boolean containsChannel( final Channel channel )
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
