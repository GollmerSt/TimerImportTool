// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.main;

public class Versions {
	private static final int MAJOR_VERSION					= 1 ;
  private static final int MINOR_VERSION					= 0 ;
  private static final int SUB_VERSION						= 6 ;
	private static final String DVBVIEWER_COM_DLL_VERSION	= "1.00.00" ;
	
	public static String getVersion() { return Integer.toString( MAJOR_VERSION ) 
	  + "." + Integer.toString( MINOR_VERSION )
	  + "." + Integer.toString( SUB_VERSION ) ; } ;
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
