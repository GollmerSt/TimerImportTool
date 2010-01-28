// $LastChangedDate: 2010-01-28 09:11:48 +0100 (Do, 28 Jan 2010) $
// $LastChangedRevision: 15 $
// $LastChangedBy: Stefan Gollmer $


import java.util.Date;
import java.util.GregorianCalendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;

 
public final class Conversions {
	private final static SimpleDateFormat tvInfoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZZ"); ;
	private final static SimpleDateFormat clickFinderoFormat = new SimpleDateFormat("yyyyMMddHHmm"); ;
	private final static SimpleDateFormat dayTimeFormat = new SimpleDateFormat("HH:mm"); ;
	private final static SimpleDateFormat svcDayFormat = new SimpleDateFormat("dd.MM.yyyy"); ;
	private final static SimpleDateFormat svcTimeFormat = new SimpleDateFormat("HH:mm"); ;
	
	static byte[] intToBytes( int n )
	{
		byte[] result = new byte[4];
		result[ 3 ] = (byte) (   n         & 0xff ) ;
		result[ 2 ] = (byte) ( ( n >>  8 ) & 0xff ) ;
		result[ 1 ] = (byte) ( ( n >> 16 ) & 0xff ) ;
		result[ 0 ] = (byte) ( ( n >> 24 ) & 0xff ) ;
		return result ;
	}
	
	static byte[] longToBytes( long n )
	{
		byte[] result = new byte[8];
		result[ 7 ] = (byte) (   n         & 0xff ) ;
		result[ 6 ] = (byte) ( ( n >>  8 ) & 0xff ) ;
		result[ 5 ] = (byte) ( ( n >> 16 ) & 0xff ) ;
		result[ 4 ] = (byte) ( ( n >> 24 ) & 0xff ) ;
		result[ 3 ] = (byte) ( ( n >> 32 ) & 0xff ) ;
		result[ 2 ] = (byte) ( ( n >> 40 ) & 0xff ) ;
		result[ 1 ] = (byte) ( ( n >> 48 ) & 0xff ) ;
		result[ 0 ] = (byte) ( ( n >> 56 ) & 0xff ) ;
		return result ;
	}
	
	static String bytesToString( byte[] b )
	{
		String hex = "" ;
		for (int i = 0; i < b.length; i++)
		{
			hex += String.format("%02x", (int)b[i]& 0xff ) ;
		}
		return hex ;
	}
	
	static long tvInfoTimeToLong( String time ) throws ParseException
	{
		//Workaround in case of a wrong time zone of the TVInfo output
		// must be checked on summer time
		Date d = new Date( tvInfoFormat.parse(time).getTime()  + 60 *60 * 1000) ;
		//System.out.println(d.toString()) ;
		return d.getTime() ;
	}
	
	static long clickFinderTimeToLong( String time ) throws ParseException
	{
		Date d = new Date( clickFinderoFormat.parse(time).getTime()) ;
		//System.out.println(d.toString()) ;
		return d.getTime() ;
	}
	
	static long dayTimeToLong( String time ) throws ParseException
	{
		return dayTimeFormat.parse(time).getTime() ;
	}
	static String longToSvcDayString( long d )
	{
		Date dt = new Date( d ) ;
		return Conversions.svcDayFormat.format( dt ) ;
	}
	static String longToSvcTimeString( long d )
	{
		Date dt = new Date( d ) ;
		return Conversions.svcTimeFormat.format( dt ) ;
	}
	static String longToSvcDateString( long d )
	{
		GregorianCalendar c = new GregorianCalendar() ;
		c.setTime(new Date(d) ) ;
		long t = c.getTimeInMillis() + (long)c.get(java.util.Calendar.ZONE_OFFSET) ;
		//System.out.println(t%(1000*60*60*24) ) ;
		return Long.toString( t  / 1000 / 60 / 60 / 24 +  + 25569 ) ;
	}
	static String longToSvcMinutesString( long d )
	{
		GregorianCalendar c = new GregorianCalendar() ;
		c.setTime(new Date(d) ) ;
		int  minutes = c.get( java.util.Calendar.HOUR_OF_DAY ) * 60
		               + c.get( java.util.Calendar.MINUTE ) ;
		return Integer.toString( minutes ) ;
	}
}
