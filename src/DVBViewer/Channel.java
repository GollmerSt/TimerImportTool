// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package DVBViewer ;

import Control.Merge;
import Control.TimeOffsets;

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
	public Merge getMerge()        { return this.merge ; } ;
}
