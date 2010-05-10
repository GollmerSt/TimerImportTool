package dvbviewertimerimport.dvbviewer;

public interface DVBViewerProvider {
  public void process( boolean getAll ) ;
  public void processEntry( Object args ) ;
}
