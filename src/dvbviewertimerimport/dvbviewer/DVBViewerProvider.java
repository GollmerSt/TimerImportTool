// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.dvbviewer;

public interface DVBViewerProvider {
	public boolean process( boolean getAll, DVBViewer.Command command ) ;
	public boolean processEntry( Object arg, DVBViewer.Command command ) ;
}
