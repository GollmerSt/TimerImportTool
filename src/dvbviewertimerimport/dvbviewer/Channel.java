// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.dvbviewer ;

import dvbviewertimerimport.control.ChannelSet;
import dvbviewertimerimport.control.TimeOffsets;
import dvbviewertimerimport.misc.Enums.Merge;

public class Channel {
	private final ChannelSet channelSet ;
	private final TimeOffsets offsets ;
	private final Merge merge ;

	public Channel( ChannelSet channelSet,
			        TimeOffsets offsets,
			        Merge merge )
	{
		this.channelSet  = channelSet ;
		this.offsets     = offsets ;
		this.merge       = merge ;
	}
	public String getDVBViewer()     { return this.channelSet.getDVBViewerChannel() ; } ;
	public ChannelSet getChannelSet() { return this.channelSet ; } ;
	public TimeOffsets  getOffsets() { return this.offsets ; } ;
	public boolean getMerge( boolean generalMerge)
	{
		if (      merge == Merge.INVALID )
			return generalMerge ;
		else if ( merge == Merge.TRUE )
			return true ;
		return false ;
	}
}
