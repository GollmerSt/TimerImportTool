// $LastChangedDate: 2010-04-04 19:24:20 +0200 (So, 04 Apr 2010) $
// $LastChangedRevision: 230 $
// $LastChangedBy: Stefan $

package dvbviewertimerimport.dvbviewer;

import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Calendar;

import dvbviewertimerimport.misc.Enums.ActionAfterItems;
import dvbviewertimerimport.misc.Enums.TimerActionItems;

public class DVBViewerEntryCOM {

	private static TimeZone timeZone = null ;
	private static GregorianCalendar calendar0 = new GregorianCalendar( TimeZone.getTimeZone("GMT0") ) ;
	private static GregorianCalendar calendar  = null ;
	
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
	
	static
	{
		timeZone = DVBViewer.getTimeZone() ;
		calendar = new GregorianCalendar( timeZone ) ;
	}
	
	DVBViewerEntryCOM( DVBViewerEntry e )
	{
		this.channelID   = e.getChannel() ;
		this.startTime   = fromJavaDate( e.getStart() ) ;
		this.endTime     = fromJavaDate( e.getEnd() ) ;
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
				toJavaDate( this.startTime ),
				toJavaDate( this.endTime ),
				this.days,
				this.description,
				TimerActionItems.get( this.recAction ),
				ActionAfterItems.get(this.afterRec ) ) ;
	}

	private static void checkAndUpdateTimeZone()
	{
		if ( timeZone != DVBViewer.getTimeZone() )
		{
			timeZone = DVBViewer.getTimeZone() ;
			calendar.setTimeZone( timeZone ) ;
		}
	}
	public static long fromJavaDate( long d )
	{
		long t = d + (long) timeZone.getOffset( d ) ;
		return t ;
	}
	public static long toJavaDate( long d )
	{
		checkAndUpdateTimeZone() ;
		calendar0.clear() ;
		calendar.clear() ;
		calendar0.setTimeInMillis( d ) ;
		calendar.set(
				calendar0.get( Calendar.YEAR ),
				calendar0.get( Calendar.MONTH ),
				calendar0.get( Calendar.DATE ),
				calendar0.get( Calendar.HOUR_OF_DAY  ),
				calendar0.get( Calendar.MINUTE  ),
				calendar0.get( Calendar.SECOND  ) ) ;
		return calendar.getTimeInMillis() ;
	}

}
