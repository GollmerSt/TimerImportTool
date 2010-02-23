// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbv.misc;


public final class Constants {
	public static final long   DAYMILLSEC                    = 24 * 60 * 60 * 1000 ;
	public static final long   LASTMINUTE                    = (24 * 60 - 1) * 60 * 1000 ;
	public static final String PROGRAM_NAME = "TimerImportTool" ;
	public final static long DAY_TIME_ORIGIN = Conversions.calcSvcTimeCorrection() ;
}
