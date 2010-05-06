package dvbviewertimerimport.main;

public class Versions {
	private static final int MAJOR_VERSION                     = 0 ;
  private static final int MINOR_VERSION                     = 9 ;
  private static final int SUB_VERSION                     = 13 ;
	private static final String DVBVIEWER_COM_DLL_VERSION      = "0.9.12" ;
	
	public static String getVersion() { return Integer.toString( MAJOR_VERSION ) 
	  + "." + Integer.toString( MAJOR_VERSION )
	  + "." + Integer.toString( MAJOR_VERSION ) ; } ;
	public static String getDVBViewerCOMVersion() { return DVBVIEWER_COM_DLL_VERSION ; } ;
	
	/**
	 * @return triple containing the version numbers
	 */
	public static int[] getIntVersion()
	{
	  int[] version = { MAJOR_VERSION, MINOR_VERSION, SUB_VERSION } ;
	  return version ;
	}
}
