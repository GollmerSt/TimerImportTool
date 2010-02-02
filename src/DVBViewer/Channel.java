// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package DVBViewer ;

public class Channel {
	private final String dvbViewer ;
	private final String tvInfo ;
	private final String clickFinder ;
	private final TimeOffsets offsets ;
	private final Merge merge ;

	public Channel( String dvbViewer,
			        String tvInfo, 
			        String clickFinder, 
			        TimeOffsets offsets,
			        Merge merge )
	{
		this.dvbViewer   = dvbViewer ;
		this.tvInfo      = tvInfo ;
		this.clickFinder = clickFinder ;
		this.offsets     = offsets ;
		this.merge       = merge ;
	}
	public String getDVBViewer()     { return this.dvbViewer ; } ;
	public String getTVInfo()        { return this.tvInfo ; } ;
	public String getClickFinder()   { return this.clickFinder; } ;
	public TimeOffsets  getOffsets() { return this.offsets ; } ;
	public Merge getMerge()        { return this.merge ; } ;
}
