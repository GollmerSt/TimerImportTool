// $LastChangedDate: 2010-04-04 19:24:20 +0200 (So, 04 Apr 2010) $
// $LastChangedRevision: 230 $
// $LastChangedBy: Stefan $

package dvbviewertimerimport.dvbviewer;

import dvbviewertimerimport.misc.Enums.ActionAfterItems;
import dvbviewertimerimport.misc.Enums.TimerActionItems;
import dvbviewertimerimport.misc.Conversions;

public class DVBViewerEntryCOM {
	final int id ;
	final String channelID ;
	final long startTime ;
	final long endTime ;
	final String days ;
	final String description ;
	final boolean enabled ;
	final int recAction ;
	final int afterRec ;
	final boolean mustDelete ;
	
	DVBViewerEntryCOM( DVBViewerEntry e )
	{
		this.channelID   = e.getChannel() ;
		this.startTime   = Conversions.javaToDVBViewerDate( e.getStart() ) ;
		this.endTime     = Conversions.javaToDVBViewerDate( e.getEnd() ) ;
		this.days        = e.getDays() ;
		this.description = e.getTitle() ;
		this.enabled     = e.isEnabled() ;
		this.recAction   = e.getTimerAction().getID() ;
		this.afterRec    = e.getActionAfter().getID() ;
		switch ( e.getToDo() )
		{
			case DELETE :
			case DELETE_BY_PROVIDER :
				this.id = (int) e.getServiceID() ;
				this.mustDelete = true ;
				break ;
			case NEW :
				this.id = -1 ;
				this.mustDelete = false ;
				break ;
			default :
				this.id = (int) e.getServiceID() ;
				this.mustDelete = false ;
		}
	}
	DVBViewerEntryCOM(
			int id ,
			String channelID ,
			long startTime ,
			long endTime ,
			String days ,
			String description ,
			boolean enabled ,
			int recAction ,
			int afterRec ,
			boolean mustDelete )
	{
		this.id          = id ;
		this.channelID   = channelID ;
		this.startTime   = startTime ;
		this.endTime     = endTime ;
		this.days        = days ;
		this.description = description ;
		this.enabled     = enabled ;
		this.recAction   = recAction ;
		this.afterRec    = afterRec ;
		this.mustDelete = mustDelete ;
	}
/*	public DVBViewerEntry createDVBViewerEntry()
	{
		DVBViewerEntry result = new DVBViewerEntry( this.enabled, this.id,
				                                    this.channelID,
				                                    this.startTime, this.endTime,
				                                    this.days, this.description,
				                                    TimerActionItems.get(this.recAction),
				                                    ActionAfterItems.get( this.afterRec ) ) ;
		return result;
	}*/
	public DVBViewerEntry createDVBViewerEntry() {
		
		return new DVBViewerEntry(
				this.enabled,
				this.id,
				this.channelID,
				Conversions.dvbViewerToJavaDate( this.startTime ),
				Conversions.dvbViewerToJavaDate( this.endTime ),
				this.days,
				this.description,
				TimerActionItems.get( this.recAction ),
				ActionAfterItems.get(this.afterRec ) ) ;
	}
}
