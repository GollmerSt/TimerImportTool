// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.control ;

import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;

import dvbviewertimerimport.javanet.staxutils.IndentingXMLStreamWriter;


public class TimeOffsets  implements Cloneable {
	private static TimeOffsets generalTimeOffsets = new TimeOffsets() ;
	private ArrayList<OffsetEntry> offsets  = null;
	public TimeOffsets()
	{
		this.offsets  = new ArrayList<OffsetEntry>() ;
	}
	@Override
	public TimeOffsets clone()
	{
		TimeOffsets offsets = new TimeOffsets() ;
		for ( OffsetEntry oe : this.offsets )
			offsets.offsets.add( oe.clone() ) ;
		return offsets ;
	}
	public void assign( TimeOffsets offsets )
	{
		this.offsets = offsets.offsets ;
	}
	public void add( String pre, String post, String weekdaysString, String startTime, String endTime )
	{
		OffsetEntry o = new OffsetEntry( pre, post, weekdaysString, startTime, endTime );
		if ( pre.length() > 0 || post.length() > 0 )
			this.offsets.add( o ) ;
	}
	public void addEmpty()
	{
		OffsetEntry o = new OffsetEntry();
		this.offsets.add( o ) ;
	}
	public void delete( int ix )
	{
		this.offsets.remove( ix ) ;
	}
	private long getMax( long time, long offset, ArrayList<OffsetEntry> list, int ix )
	{
		long o = 0 ;
		if ( offset > 0 )
			o = offset ;
		for ( OffsetEntry e : list )
		{
			if ( ! e.isInTimeRange ( time ) )
				continue ;
			int[] n = e.getMinutes() ;
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
		
		for ( OffsetEntry e : this.offsets )
			e.writeXML( sw ) ;
		
		sw.writeEndElement() ;
	}
	public OffsetEntry getOffset( int ix ) { return offsets.get( ix ) ; } ;
	public int size() { return offsets.size() ; } ;
}