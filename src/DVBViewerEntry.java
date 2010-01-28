// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$


public final class DVBViewerEntry {
	private final String channel ;
	private final long start ;
	private final long end ;
	private final String title ;
	private final boolean toCombine ;
	
	public DVBViewerEntry( String channel, long start, long end, String title, boolean combine )
	{
		this.channel = channel ;
		this.start = start;
		this.end = end;
		this.title = title ;
		this.toCombine = combine ;
	}
	String getChannel() { return this.channel ; } ;
	String getTitle() {return this.title ; } ;
	long getStart() { return this.start ; } ;
	long getEnd()   { return this.end ; } ;
	public 	boolean isInRange( long start, long end )
	{
		if ( this.start <= start && this.end >= start )
			return true ;
		if ( this.start <= end && this.end >= end )
			return true ;
		return false ;
	}
	public boolean toCombine() { return this.toCombine ; } ;
}
