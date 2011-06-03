// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.main;

public class Versions {
	private static final int MAJOR_VERSION                  = 1 ;
	private static final int MINOR_VERSION                  = 2 ;
	private static final int SUB_VERSION                    = 7 ;
	private static final String DVBVIEWER_COM_DLL_VERSION   = "1.00.03" ;
	private static final boolean DVBVIEWER_BETA_STATUS      = false ;

	public static String getVersion() { return getVersion( false ) ; } ;


	@SuppressWarnings("unused")
	public static String getVersion( boolean betaStatus )
	{
		String last = null ;
		if ( betaStatus && DVBVIEWER_BETA_STATUS )
			last = " beta" ;
		else
			last = "" ;
		return          Integer.toString( MAJOR_VERSION )
				+ "." + Integer.toString( MINOR_VERSION )
	            + "." + Integer.toString( SUB_VERSION )
	            + last ;
	} ;
	public static String getDVBViewerCOMVersion() { return DVBVIEWER_COM_DLL_VERSION ; } ;

	/**
	 * @return triple containing the version numbers
	 */
	public static int[] getIntVersion()
	{
	  int[] version = { MAJOR_VERSION, MINOR_VERSION, SUB_VERSION, DVBVIEWER_BETA_STATUS?1:0 } ;
	  return version ;
	}
}
