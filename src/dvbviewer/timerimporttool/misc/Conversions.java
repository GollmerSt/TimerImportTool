// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package Misc ;

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
	private final static long dayTimeOrigin = Conversions.calcSvcTimeCorrection() ;
	
	private static long calcSvcTimeCorrection()
	{
		long result = 0 ;
		try {
			result = svcTimeFormat.parse("00:00").getTime() ;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result ;
	}
	
	static byte[] intToBytes( int n )
	{
		byte[] result = new byte[4];
		result[ 3 ] = (byte) (   n         & 0xff ) ;
		result[ 2 ] = (byte) ( ( n >>  8 ) & 0xff ) ;
		result[ 1 ] = (byte) ( ( n >> 16 ) & 0xff ) ;
		result[ 0 ] = (byte) ( ( n >> 24 ) & 0xff ) ;
		return result ;
	}
	
	public static byte[] longToBytes( long n )
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
	
	public static String bytesToString( byte[] b )
	{
		String hex = "" ;
		for (int i = 0; i < b.length; i++)
		{
			hex += String.format("%02x", (int)b[i]& 0xff ) ;
		}
		return hex ;
	}
	
	public static long tvInfoTimeToLong( String time ) throws ParseException
	{
		//Workaround in case of a wrong time zone of the TVInfo output
		// must be checked on summer time
		Date d = new Date( tvInfoFormat.parse(time).getTime()  + 60 *60 * 1000) ;
		//System.out.println(d.toString()) ;
		return d.getTime() ;
	}
	
	static public long clickFinderTimeToLong( String time ) throws ParseException
	{
		Date d = new Date( clickFinderoFormat.parse(time).getTime()) ;
		//System.out.println(d.toString()) ;
		return d.getTime() ;
	}
	
	public static long dayTimeToLong( String time ) throws ParseException
	{
		long t = dayTimeFormat.parse(time).getTime() - dayTimeOrigin ;
		//System.out.println( "dayTimeToLong: "+ Long.toString( t ) ) ;
		return t ;
	}
	public static String longTodayTime( long d ) throws ParseException
	{
		Date dt = new Date( d + dayTimeOrigin ) ;
		//System.out.println( "dayTimeToLong: "+ Long.toString( t ) ) ;
		return Conversions.dayTimeFormat.format(  dt ) ;
	}
	public static String longToSvcDayString( long d )
	{
		Date dt = new Date( d ) ;
		return Conversions.svcDayFormat.format( dt ) ;
	}
	public static String longToSvcTimeString( long d )
	{
		Date dt = new Date( d ) ;
		return Conversions.svcTimeFormat.format( dt ) ;
	}
	public static String longToSvcDateString( long d )
	{
		GregorianCalendar c = new GregorianCalendar() ;
		c.setTime(new Date(d) ) ;
		long t = c.getTimeInMillis() + (long)c.get(java.util.Calendar.ZONE_OFFSET) ;
		//System.out.println(t%(1000*60*60*24) ) ;
		return Long.toString( t  / 1000 / 60 / 60 / 24 +  + 25569 ) ;
	}
	public static String longToSvcMinutesString( long d )
	{
		GregorianCalendar c = new GregorianCalendar() ;
		c.setTime(new Date(d) ) ;
		int  minutes = c.get( java.util.Calendar.HOUR_OF_DAY ) * 60
		               + c.get( java.util.Calendar.MINUTE ) ;
		return Integer.toString( minutes ) ;
	}
	public static long svcTimeToLong( String time, String date ) throws ParseException
	{
		
		long result = svcTimeFormat.parse( time ).getTime() - Conversions.dayTimeOrigin ;
		result += svcDayFormat.parse( date ).getTime() ;
		return result ;
	}
	public static String replaceDiacritical( String s )
	{
		if ( ! s.matches(".*[äöüÄÖÜß].*") ) //\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df]") )
			return s ;
		
		String result = s ;
		
		result = result.replaceAll( "\u00e4", "ae" ) ;
		result = result.replaceAll( "\u00f6", "oe" ) ;
		result = result.replaceAll( "\u00fc", "ue" ) ;
		result = result.replaceAll( "\u00c4", "Ae" ) ;
		result = result.replaceAll( "\u00d6", "Oe" ) ;
		result = result.replaceAll( "\u00dc", "Ue" ) ;
		result = result.replaceAll( "\u00df", "ss" ) ;
		
		//System.out.println( result ) ;
		
		return result ;
	}
}
