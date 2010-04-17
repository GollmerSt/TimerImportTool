// $LastChangedDate: 2010-04-04 19:24:20 +0200 (So, 04 Apr 2010) $
// $LastChangedRevision: 230 $
// $LastChangedBy: Stefan $

package dvbv.dvbviewer;

import dvbv.gui.GUIStrings.ActionAfterItems;
import dvbv.gui.GUIStrings.TimerActionItems;

public class DVBViewerEntryCOM {
	final int ix ;
	final String channelID ;
	final long startTime ;
	final long endTime ;
	final String description ;
	final boolean enabled ;
	final int recAction ;
	final int afterRec ;
	final boolean mustDelete ;
	
	DVBViewerEntryCOM( DVBViewerEntry e )
	{
		this.ix          = (int) e.getID() ;
		this.channelID   = e.getChannel() ;
		this.startTime   = e.getStart() ;
		this.endTime     = e.getEnd() ;
		this.description = e.getTitle() ;
		this.enabled     = e.isEnabled() ;
		this.recAction   = e.getTimerAction().getID() ;
		this.afterRec    = e.getActionAfter().getID() ;
		switch ( e.getToDo() )
		{
			case DELETE :
			case DELETE_BY_PROVIDER :
				this.mustDelete = true ;
				break ;
			default :
				this.mustDelete = false ;
		}
	}
	DVBViewerEntryCOM(
			int ix ,
			String channelID ,
			long startTime ,
			long endTime ,
			String description ,
			boolean enabled ,
			int recAction ,
			int afterRec ,
			boolean mustDelete )
	{
		this.ix          = ix ;
		this.channelID   = channelID ;
		this.startTime   = startTime ;
		this.endTime     = endTime ;
		this.description = description ;
		this.enabled     = enabled ;
		this.recAction   = recAction ;
		this.afterRec    = afterRec ;
		this.mustDelete = mustDelete ;
	}
	public DVBViewerEntry createDVBViewerEntry()
	{
		DVBViewerEntry result = new DVBViewerEntry( this.enabled, this.ix,
				                                    this.channelID,
				                                    this.startTime, this.endTime,
				                                    this.description,
				                                    TimerActionItems.get(this.recAction),
				                                    ActionAfterItems.get( this.afterRec ) ) ;
		return result;
	}
}
