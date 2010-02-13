// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.control ;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import dvbv.javanet.staxutils.IndentingXMLStreamWriter;


public class TimeOffsets {
	private static TimeOffsets generalTimeOffsets = new TimeOffsets() ;
	private ArrayList<OffsetEntry> offsets  = null;
	public TimeOffsets()
	{
		this.offsets  = new ArrayList<OffsetEntry>() ;
	}
	public void add( String pre, String post, String weekdaysString, String startTime, String endTime )
	{
		OffsetEntry o = new OffsetEntry( pre, post, weekdaysString, startTime, endTime );
		if ( pre.length() > 0 || post.length() > 0 )
			this.offsets.add( o ) ;
	}
	private long getMax( long time, long offset, ArrayList<OffsetEntry> list, int ix )
	{
		long o = 0 ;
		if ( offset > 0 )
			o = offset ;
		for ( Iterator<OffsetEntry> i = list.iterator() ; i.hasNext(); )
		{
			OffsetEntry e = i.next();
			if ( ! e.isInTimeRange ( time ) )
				continue ;
			long[] n = e.getMinutes() ;
			if ( o < n[ix] )
				o = n[ix] ;
		}
		return o ;
	}
	public long getPreOffset( long time )
	{
		long offset = 0 ;
		if ( this != generalTimeOffsets )
			offset = this.getMax( time, 0, generalTimeOffsets.offsets, 0 ) ;
		return this.getMax( time, offset, this.offsets, 0 ) ;
	}
	public long getPostOffset( long time )
	{
		long offset = 0 ;
		if ( this != generalTimeOffsets )
			offset = this.getMax( time, 0, generalTimeOffsets.offsets, 1 ) ;
		return this.getMax( time, offset, offsets, 1 ) ;
	}
	public static TimeOffsets getGeneralTimeOffsets() { return generalTimeOffsets ; } ;
	public void writeXML( IndentingXMLStreamWriter sw ) throws XMLStreamException, ParseException
	{
		if ( this.offsets.size() == 0 )
			return ;
		
		sw.writeStartElement( "Offsets" ) ;
		
		for ( Iterator<OffsetEntry> it = this.offsets.iterator() ; it.hasNext() ; )
			it.next().writeXML( sw ) ;
		
		sw.writeEndElement() ;
	}
}