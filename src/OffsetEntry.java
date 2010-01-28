// $LastChangedDate: 2010-01-28 09:11:48 +0100 (Do, 28 Jan 2010) $
// $LastChangedRevision: 15 $
// $LastChangedBy: Stefan Gollmer $


import java.text.ParseException;
import java.util.GregorianCalendar;


public class OffsetEntry {
	private final long minutes ;
	private final boolean[] weekdays ;
	private final long start ;
	private final long end ;
	
	public OffsetEntry( String minuteString, String weekdaysString, String startTime, String endTime )
	{
		if ( !minuteString.matches("\\d+") )
			throw new ErrorClass( "Illegal minute format" ) ;
		this.minutes = Integer.valueOf(minuteString) ;
		boolean[] w = new boolean[7] ;
		String ws  ;
		if ( weekdaysString.length() == 0 )
			ws ="1111111" ;
		else
			ws =weekdaysString ;
		for ( int i = 0 ; i < ws.length() ; i++ )
		{
			char c = ws.charAt(i) ;
			if ( c == '0' )
				w[i] = false ;
			else if ( c == '1' )
				w[i] = true ;
			else
				throw new ErrorClass("Illegal weekday string") ;
		}
		this.weekdays = w ;
		if ( startTime.length() != 0 )
			try {
				this.start = Conversions.dayTimeToLong( startTime ) ;
			} catch (ParseException e) {
				throw new ErrorClass("Illegal begin time (must be hh:mm)") ;
			}
		else
			this.start = 0 ;

		if ( endTime.length() != 0 )
			try {
				this.end = Conversions.dayTimeToLong( endTime ) ;
			} catch (ParseException e) {
				throw new ErrorClass("Illegal end time (must be hh:mm)") ;
			}
		else
			this.end = Constants.DAYMILLSEC ;
	}
	public long getMinutes() { return this.minutes ; } ;
	public boolean isInTimeRange( long time )
	{
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis( time ) ;
		int d = cal.get( java.util.Calendar.DAY_OF_WEEK ) + 6 ;
		d = d % 7 ;
		if ( ! this.weekdays[ d ] )
			return false ;
		cal.set( java.util.Calendar.HOUR_OF_DAY , 0) ;
		cal.set( java.util.Calendar.MINUTE  , 0) ;
		cal.set( java.util.Calendar.SECOND  , 0) ;
		cal.set( java.util.Calendar.MILLISECOND  , 0) ;
		long dayTime = time - cal.getTime().getTime();
		return dayTime >= start && dayTime <= end ;
	}
}
