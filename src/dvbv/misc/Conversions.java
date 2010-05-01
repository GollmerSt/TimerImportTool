// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.misc ;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;
import java.text.ParseException;
import java.text.SimpleDateFormat;

 
public final class Conversions {
	private final static SimpleDateFormat tvInfoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); ;
	private final static SimpleDateFormat clickFinderoFormat = new SimpleDateFormat("yyyyMMddHHmm"); ;
	private final static SimpleDateFormat svcDayFormat = new SimpleDateFormat("dd.MM.yyyy"); ;
	private final static SimpleDateFormat svcTimeFormat = new SimpleDateFormat("HH:mm"); ;
	private final static SimpleDateFormat svcDayTimeFormat = new SimpleDateFormat("dd.MM.yyyyHH:mm"); ;
	private final static TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
	
	static long calcSvcTimeCorrection()
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
		Date d = new Date( tvInfoFormat.parse(time).getTime() ) ; //  + 60 *60 * 1000) ;
		//System.out.println(d.toString()) ;
		return d.getTime() ;
	}
	
	static public long clickFinderTimeToLong( String time ) throws ParseException
	{
		Date d = new Date( clickFinderoFormat.parse(time).getTime()) ;
		//System.out.println(d.toString()) ;
		return d.getTime() ;
	}
	
	public static String longToDateString( long d )
	{
		return longToSvcDayString( d ) ;
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
		long t = c.getTimeInMillis() + (long) timeZone.getOffset( d ) ;
		//System.out.println(t%(1000*60*60*24) ) ;
		return Long.toString( t  / 1000 / 60 / 60 / 24 + 25569 ) ;
	}
	public static String longToSvcMinutesString( long d )
	{
		GregorianCalendar c = new GregorianCalendar() ;
		c.setTime(new Date(d) ) ;
		int  minutes = c.get( java.util.Calendar.HOUR_OF_DAY ) * 60
		               + c.get( java.util.Calendar.MINUTE ) ;
		return Integer.toString( minutes ) ;
	}
	public static long timeToLong( String time, String date ) throws ParseException
	{
		return svcTimeToLong( time, date ) ;
	}
	public static long svcTimeToLong( String time, String date ) throws ParseException
	{
		return svcDayTimeFormat.parse( date + time ).getTime() ;
	}
	public static long javaToDVBViewerDate( long d )
	{
		long t = d + (long) timeZone.getOffset( d ) ;
		//System.out.println(t%(1000*60*60*24) ) ;
		return t ;
	}
	public static long dvbViewerToJavaDate( long d )
	{
		long t = d - (long) timeZone.getOffset( d ) ;
		if ( javaToDVBViewerDate( t ) == d )
			return t ;
		t += 1000*60*10 ;
		if ( javaToDVBViewerDate( t ) == d )
			return t ;
		t -= 1000*60*10*2 ;
		return t ;
	}
	public static String replaceDiacritical( final String s )
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
	public static <T> T getTheBestChoice( String search, Collection<T> list,
            int weightOfFirstChar, int charCount,
            Function rework )
	{
		ArrayList< T > objects = getTheBestChoices( search, list, weightOfFirstChar, charCount, rework ) ;
		
		return objects != null ? objects.get( 0 ) : null ;
	}
	public static <T> ArrayList< T > getTheBestChoices( String search, Collection< T > list,
			int weightOfFirstChar, int charCount,
			Function reworkFunc )
	{
		return getTheBestChoices( search, list, weightOfFirstChar, charCount, reworkFunc, null ) ;
	}
	public static <T> ArrayList< T > getTheBestChoices( String search, Collection< T > list,
			int weightOfFirstChar, int charCount,
			Function reworkFunc, Function weightFunc )
	{
		if ( reworkFunc == null )
			reworkFunc = new Function() ;
		if ( weightFunc == null )
			weightFunc = new Function() ;
		String string = reworkFunc.stringToString( search ) ;
		ArrayList< T > results = new ArrayList< T >() ;
		int weightMax = -1 ;
		int minDiff = 99999 ;
		for ( T choiceObject : list )
		{
			String choiceOrg = choiceObject.toString() ;
			String choice = reworkFunc.stringToString( choiceOrg ) ;
			int wiegthFirstChar = 0 ;
			if( string.trim().substring( 0, charCount).equalsIgnoreCase( choice.trim().substring( 0, charCount) ) )
				wiegthFirstChar = weightOfFirstChar ;
			
			
			ArrayList< Integer > partLength = getSplitedLength( string, choice, charCount ) ;
			
			int weight = weightFunc.arrayIntToInt( partLength, wiegthFirstChar, string, choiceOrg ) ;

			if ( weight > weightMax )
			{
				minDiff = 99999 ;
				results.clear() ;
				weightMax = weight ;
			}
			if ( weight == weightMax )
			{
				results.add( choiceObject ) ;
				int diff = Math.abs( choiceObject.toString().length() - string.length() ) ;
				if ( diff < minDiff )
					minDiff = diff ;
			}
			}
			for ( Iterator< T > it = results.iterator() ; it.hasNext() ; )
			{
				T o = it.next() ;
				int d = Math.abs( o.toString().length() - string.length() ) ;
				if ( minDiff != d )
					it.remove() ;
			}
		return results.size() == 0 ? null : results ;
	}
	private static ArrayList< Integer > getSplitedLength( final String left, final String right, int minChar )
	{
		ArrayList< Integer > result = new ArrayList< Integer >() ;
		
		int maxEqualLength = -1 ;
		int maxStart = -1 ;
		int maxEnd = -1 ;
		int maxPos = -1 ;
		
		int length = Math.min( left.length(), right.length() ) ;
		
		for ( int ib = 0 ; ib < left.length() ; ib++ )
		{
			int ie = ib + length > left.length() ? left.length() : ib + length ;
			for ( ; ie > ib ; ie-- )
			{
				if ( ie - ib < minChar )
					break ;
				int pos = right.indexOf( left.substring( ib, ie ) )  ;
				if ( pos >= 0 )
					if ( maxEqualLength < ie - ib )
					{
						if ( minChar > ie - ib )
							continue ;
						maxEqualLength = ie-ib ;
						maxStart = ib ;
						maxEnd = ie ;
						maxPos = pos ;
					}
			}
		}
		length = maxEnd - maxStart ;
		
		if ( maxStart > 0 && maxPos > 0 )
		{
			result.addAll( Conversions.getSplitedLength( 
					              left.substring( 0, maxStart ),
					              right.substring( 0, maxPos ),
					              minChar ) ) ;
		}
		if ( maxStart >= 0 )
		{
			result.add( length ) ;

			if ( maxEnd < left.length() && maxPos + length < right.length() )
				result.addAll( Conversions.getSplitedLength(
						          left.substring( maxEnd ), 
						          right.substring( maxPos + length), 
						          minChar ) ) ;
		}
		return result ;
	}

	
	public static long dayTimeToLong( String time ) throws ParseException
	{
		String [] parts = time.split(":") ;
		if ( parts.length != 2)
		{
			throw new ParseException( "Illegal time format: "+ time, 0 ) ;
		}
		long t = ( Long.valueOf( parts[0] ) * 60L + Long.valueOf( parts[1] ) ) * 60L * 1000L ;
		return t ;
	}
	//Only for offset!!!
	public static String longToDayTime( long d ) throws ParseException
	{
		long t = d / 1000L / 60L ;
		int h = (int) (t / 60L) ;
		int m = (int) (t % 60L) ;
		return String.format( "%02d:%02d", h, m ) ;
	}
}
