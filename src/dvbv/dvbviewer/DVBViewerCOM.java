// $LastChangedDate: 2010-02-13 14:49:56 +0100 (Sa, 13. Feb 2010) $
// $LastChangedRevision: 137 $
// $LastChangedBy: Stefan Gollmer $

package dvbv.dvbviewer;

import java.util.ArrayList;

public class DVBViewerCOM
{
	public static native DVBViewerEntryCOM[] getItems() ;
	public static native void setItems( DVBViewerEntryCOM[] entries ) ;
	public static native boolean connect() ;
	public static native void disconnect() ;
	public static native String getSetupValue( String section, String name, String deflt ) ;

	static
	{
		System.out.println( "Library wird geladen" ) ;
		System.loadLibrary("TimerImportToolJNI");
		System.out.println( "Library ist geladen" ) ;
	}
	public static ArrayList<DVBViewerEntry> readTimers()
	{
		ArrayList<DVBViewerEntry> result = new ArrayList<DVBViewerEntry>() ;
		return result ;
	}
}
