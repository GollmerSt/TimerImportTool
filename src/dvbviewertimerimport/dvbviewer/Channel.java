// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.dvbviewer ;

import dvbviewertimerimport.control.TimeOffsets;
import dvbviewertimerimport.misc.Enums.Merge;

public class Channel {
	private final String dvbViewer ;
//	private final String tvInfo ;
//	private final String clickFinder ;
	private final TimeOffsets offsets ;
	private final Merge merge ;

	public Channel( String dvbViewer,
			        TimeOffsets offsets,
			        Merge merge )
	{
		this.dvbViewer   = dvbViewer ;
		this.offsets     = offsets ;
		this.merge       = merge ;
	}
	public String getDVBViewer()     { return this.dvbViewer ; } ;
	public TimeOffsets  getOffsets() { return this.offsets ; } ;
	public boolean getMerge( boolean generalMerge)
	{
		if (      merge == Merge.INVALID )
			return generalMerge ;
		else if ( merge == Merge.TRUE )
			return true ;
		return false ;
	}
}
