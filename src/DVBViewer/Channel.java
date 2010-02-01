// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$


public class Channel {
	private final String dvbViewer ;
	private final String tvInfo ;
	private final String clickFinder ;
	private final TimeOffsets offsets ;
	private final Combine combine ;

	public Channel( String dvbViewer,
			        String tvInfo, 
			        String clickFinder, 
			        TimeOffsets offsets,
			        Combine combine )
	{
		this.dvbViewer   = dvbViewer ;
		this.tvInfo      = tvInfo ;
		this.clickFinder = clickFinder ;
		this.offsets     = offsets ;
		this.combine     = combine ;
	}
	public String getDVBViewer()     { return this.dvbViewer ; } ;
	public String getTVInfo()        { return this.tvInfo ; } ;
	public String getClickFinder()   { return this.clickFinder; } ;
	public TimeOffsets  getOffsets() { return this.offsets ; } ;
	public Combine getCombine()      { return this.combine ; } ;
}
