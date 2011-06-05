// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.misc;


public final class Constants {
	public static final long   YEARMILLSEC                   = 365 * 24 * 60 * 60 * 1000 ;
	public static final long   DAYMILLSEC                    = 24 * 60 * 60 * 1000 ;
	public static final long   LASTMINUTE                    = (24 * 60 - 1) * 60 * 1000 ;
	public static final String PROGRAM_NAME                  = "DVBViewerTimerImport" ;
	public static final String SYSTEM_LOOK_AND_FEEL_NAME     = "System" ;
		
	public static final boolean IS_WINDOWS ;
	
	static
	{
		String osName = System.getProperty( "os.name", ""  ).toLowerCase() ;
		if ( osName.contains( "windows") )
			IS_WINDOWS = true ;
		else
			IS_WINDOWS = false ;
	}

}
