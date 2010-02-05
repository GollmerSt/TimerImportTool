// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package DVBViewer ;

import java.util.ArrayList;
import java.util.Iterator;


public class TimeOffsets {
	private static TimeOffsets generalTimeOffsets = new TimeOffsets() ;
	private ArrayList<OffsetEntry> preOffsets  = null;
	private ArrayList<OffsetEntry> postOffsets = null;
	public TimeOffsets()
	{
		this.preOffsets  = new ArrayList<OffsetEntry>() ;
		this.postOffsets = new ArrayList<OffsetEntry>() ;
	}
	public void add( String pre, String post, String weekdaysString, String startTime, String endTime )
	{
		if ( pre.length() > 0)
			this.addPreOffset( pre, weekdaysString, startTime, endTime ) ;
		if (post.length() > 0 )
			this.addPostOffset( post, weekdaysString, startTime, endTime ) ;
	}
	private void addPreOffset( String minuteString, String weekdaysString, String startTime, String endTime )
	{
		OffsetEntry o = new OffsetEntry( minuteString, weekdaysString, startTime, endTime );
		this.preOffsets.add( o ) ;
	}
	private void addPostOffset( String minuteString, String weekdaysString, String startTime, String endTime )
	{
		OffsetEntry o = new OffsetEntry( minuteString, weekdaysString, startTime, endTime );
		this.postOffsets.add( o ) ;
	}
	private long getMax( long time, long offset, ArrayList<OffsetEntry> list )
	{
		long o = offset ;
		for ( Iterator<OffsetEntry> i = list.iterator() ; i.hasNext(); )
		{
			OffsetEntry e = i.next();
			if ( ! e.isInTimeRange ( time ) )
				continue ;
			long n = e.getMinutes() ;
			if ( o < n )
				o = n ;
		}
		return o ;
	}
	public long getPreOffset( long time )
	{
		long offset = 0 ;
		if ( this != generalTimeOffsets )
			offset = this.getMax( time, 0, generalTimeOffsets.preOffsets ) ;
		return this.getMax( time, offset, preOffsets ) ;
	}
	public long getPostOffset( long time )
	{
		long offset = 0 ;
		if ( this != generalTimeOffsets )
			offset = this.getMax( time, 0, generalTimeOffsets.postOffsets ) ;
		return this.getMax( time, offset, postOffsets ) ;
	}
	static public TimeOffsets getGeneralTimeOffsets() { return generalTimeOffsets ; } ;
}