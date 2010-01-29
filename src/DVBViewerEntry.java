// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$


public final class DVBViewerEntry {
	public enum ToDo { NONE, NEW, NEW_UPDATED, UPDATE, DISABLE, UPDATE_AND_DISABLE } ;
	private final long id ;
	private final String channel ;
	private long start ;
	private long end ;
	private String title ;
	private ToDo toDo ;
	private boolean combine;
	
	public DVBViewerEntry( long id, String channel, long start, long end, String title, boolean combine )
	{
		this.id = id ;
		this.channel = channel ;
		this.start = start;
		this.end = end;
		this.title = title ;
		this.combine = combine ;
		this.toDo = ToDo.NONE ;
	}
	public DVBViewerEntry( String channel, long start, long end, String title, boolean combine )
	{
		this.id = -1 ;
		this.channel = channel ;
		this.start = start;
		this.end = end;
		this.title = title ;
		this.combine = combine ;
		this.toDo = ToDo.NEW ;
	}
	public void update( long start, long end, String title )
	{
		this.start = start ;
		this.end = end ;
		this.title = title ;
		this.toDo = ToDo.UPDATE ;
	}
	public DVBViewerEntry update( DVBViewerEntry dE, String separator )
	{
		DVBViewerEntry result = null ;
		
		if ( this.toDo == ToDo.NONE || this.toDo == ToDo.NEW )
		{
			result = this.clone() ;
			result.toDo = ToDo.DISABLE ;
		}
		this.start = Math.min(this.start, dE.getStart() ) ;
		this.end   = Math.max( this.end,  dE.getEnd() ) ;
		String title = this.getTitle();
		if ( this.start < dE.getStart() )
			title += separator + dE.getTitle() ;
		else
			title = dE.getTitle() + separator + title ;
		this.title = title ;
		if ( this.toDo == ToDo.NEW )
			this.toDo = ToDo.NEW_UPDATED ;
		else if ( this.toDo == ToDo.NONE )
			this.toDo = ToDo.UPDATE ;
		else if ( this.toDo == ToDo.DISABLE || this.toDo == ToDo.UPDATE_AND_DISABLE )
			throw new ErrorClass( "Unexpected error in DVBViewerEntry.update" ) ;
		dE.disable() ;
		return result ;
	}
	public void disable ()
	{
		if ( this.toDo == ToDo.NEW  )
			this.toDo = ToDo.DISABLE ;
		else if ( this.toDo == ToDo.NONE || this.toDo == ToDo.UPDATE )
			this.toDo = ToDo.UPDATE_AND_DISABLE ;
	}
	public DVBViewerEntry clone()
	{
		DVBViewerEntry result = new DVBViewerEntry( this.id, this.channel, this.start, this.end, this.title, this.combine ) ;
		result.toDo = this.toDo ;
		return result ;
	}
	public long getID() { return this.id ; } ;
	public String getChannel() { return this.channel ; } ;
	public String getTitle() {return this.title ; } ;
	public long getStart() { return this.start ; } ;
	public long getEnd()   { return this.end ; } ;
	public 	boolean isInRange( long start, long end )
	{
		if ( this.start <= start && this.end >= start )
			return true ;
		if ( this.start <= end && this.end >= end )
			return true ;
		return false ;
	}
	public boolean toCombine() { return this.combine ; } ;
	public boolean mustCombine( DVBViewerEntry dE )
	{
		if ( this.toDo == ToDo.NONE && dE.toDo == ToDo.NONE )
			return false ;
		if ( ! this.combine || ! dE.combine )
			return false ;
		if ( this.isDisabled() || dE.isDisabled() )
			return false ;
		if ( ! this.channel.equals( dE.channel ) )
			return false ;
		return this.isInRange( dE.start, dE.end ) ;
	}
	public void setToDo( ToDo t ) { this.toDo = t ; } ;
	public ToDo getToDo() { return this.toDo ; } ;
	public boolean isDisabled() { return this.toDo == ToDo.DISABLE || toDo == ToDo.UPDATE_AND_DISABLE ; } ;
	public boolean isEnabled()
	{
		return    this.toDo == ToDo.NEW || this.toDo == ToDo.NEW_UPDATED
		       || this.toDo == ToDo.UPDATE ;
	}
	public boolean mustIgnored() { return this.toDo == ToDo.NONE ; } ;
	public boolean mustUpdated() { return this.toDo == ToDo.UPDATE || this.toDo == ToDo.UPDATE_AND_DISABLE ; } ;
}
