// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.control ;

import java.text.ParseException;
import java.util.GregorianCalendar;

import javax.xml.stream.XMLStreamException;

import dvbv.javanet.staxutils.IndentingXMLStreamWriter;

import dvbv.misc.* ;


public class OffsetEntry implements Cloneable {
	private static final String[] ATTRIBUTES = { "before", "after" } ;
	private final int[] minutes ;
	private final boolean[] weekdays ;
	private final String[] times  = new String[ 2 ] ;
	
	public OffsetEntry()
	{
		this.minutes = new int[2] ;
		this.minutes[0] = -1 ;
		this.minutes[1] = -1 ;
		this.weekdays = new boolean[7] ;
		this.times[0] = "00:00";
		this.times[1] = "23:59" ;
	}
	public OffsetEntry( String preMinuteString, 
						String postMinuteString, 
						String weekdaysString,
						String startTime,
						String endTime )
	{
		int[] minutes = { -1, -1 } ;
		String [] minuteStrings = { preMinuteString, postMinuteString } ;
		for ( int ix = 0 ; ix < 2 ; ix++ )
		{
			if ( minuteStrings[ix] == null || minuteStrings[ix].length() == 0 )
				continue ;
			if ( !minuteStrings[ix].matches("\\d*"))
				throw new ErrorClass( "Illegal minute format" ) ;

			minutes[ ix ] = Integer.valueOf( minuteStrings[ix] ) ;
		}
		this.minutes = minutes ;

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
			this.times[0] = startTime ;
		else
			this.times[0] = "00:00" ;

		if ( endTime.length() != 0 )
			this.times[1] = endTime ;
		else
			this.times[1] = "23:59" ;
	}
	@Override
	public OffsetEntry clone()
	{
		OffsetEntry clonedOffset = new OffsetEntry() ;
		clonedOffset.minutes[0] = this.minutes[0] ;
		clonedOffset.minutes[1] = this.minutes[1] ;
		for ( int ix = 0 ; ix < 7 ; ix++ )
			clonedOffset.weekdays[ ix ] = this.weekdays[ ix ] ;
		clonedOffset.times[0] = this.times[0] ;
		clonedOffset.times[1] = this.times[1] ;
		return clonedOffset ;
	}
	public int[] getMinutes() { return this.minutes ; } ;
	public boolean isInTimeRange( long time )
	{
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis( time ) ;
		int d = cal.get( java.util.Calendar.DAY_OF_WEEK ) + 6 ;
		d = d % 7 ;
		if ( ! this.weekdays[ d ] )
			return false ;
		String day = Conversions.longToDateString( time ) ;
		long lowerTime = 0L;
		long upperTime = 0L;
		try {
			lowerTime = Conversions.timeToLong( this.times[0], day );
			upperTime = Conversions.timeToLong( this.times[1], day );
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(  dayTime ) ;
		return time >= lowerTime && time <= upperTime ;
	}
	public void writeXML( IndentingXMLStreamWriter sw ) throws XMLStreamException, ParseException
	{
		sw.writeStartElement( "Offset" ) ;
		  for ( int ix = 0 ; ix < 2 ; ix++ )
			  if ( this.minutes[ ix ] >= 0 )
				  sw.writeAttribute( ATTRIBUTES[ ix ], Long.toString( this.minutes[ ix ] ) ) ;

		  String weekdays = "" ;

		  for ( int ix = 0 ; ix < 7 ; ix ++ )
			  if ( this.weekdays[ ix ])
				  weekdays += "1" ;
			  else
				  weekdays += "0" ;
		  if ( ! weekdays.equals( "1111111" ) )
			  sw.writeAttribute( "days", weekdays) ;
		  
		  if ( ! this.times[0].equals( "00:00") )
			  sw.writeAttribute( "begin", this.times[0] ) ;
		  if ( ! this.times[1].equals( "23:59") )
			  sw.writeAttribute( "end",   this.times[1] ) ;
		sw.writeEndElement() ;
	}
	public boolean[] getWeekDays() { return this.weekdays ; } ;
	public String[] getDayTimes() { return this.times ; } ;
}
	
