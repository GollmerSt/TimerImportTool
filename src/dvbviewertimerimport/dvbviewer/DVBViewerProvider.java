package dvbviewertimerimport.dvbviewer;

public interface DVBViewerProvider {
  public boolean process( boolean getAll, DVBViewer.Command command ) ;
  public boolean processEntry( Object args, DVBViewer.Command command ) ;
}
