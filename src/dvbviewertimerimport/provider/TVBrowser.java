// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.provider;

import java.util.ArrayList;
import java.util.HashSet;


import dvbviewertimerimport.DVBViewerTimerImport;
import dvbviewertimerimport.control.Channel;
import dvbviewertimerimport.control.Control;
import dvbviewertimerimport.dvbviewer.DVBViewerEntry;
import dvbviewertimerimport.misc.Helper;
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
	public Channel createChannel( String name, String id )
	{
	  return new Channel( this.getID(), name, id )
	  {
      @Override
      public Object getIDKey()
      {
        return this.getTextID() ;
      }
      @Override
      public Object getIDKey( final Channel c ) { return c.getTextID() ; } ;  // ID of the provider, type is provider dependent
	  } ;
	}
	
	@Override
	protected ArrayList< Channel > readChannels()
	{
		ProviderChannel< String > [] channels = DVBViewerTimerImport.getTVBChannelNames() ;
		
		ArrayList< Channel > result = new ArrayList< Channel >() ;
		
		for ( ProviderChannel< String > cP : channels )
		{
			Channel c = this.createChannel( cP.getName() , cP.getKey()  ) ;
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
			ProviderChannel< String >[] channels = DVBViewerTimerImport.getTVBChannelNames();
			for (ProviderChannel< String > c : channels)
				this.channelSet.add( c.getKey() );
		}
		return this.channelSet.contains( channel.getIDKey() ) ;
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
	@Override
	public void updateRecordings( ArrayList< DVBViewerEntry > entries )
	{
		DVBViewerTimerImport.updateRecordings( entries ) ;
	}
}
