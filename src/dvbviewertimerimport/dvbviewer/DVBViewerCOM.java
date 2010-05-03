// $LastChangedDate: 2010-02-13 14:49:56 +0100 (Sa, 13. Feb 2010) $
// $LastChangedRevision: 137 $
// $LastChangedBy: Stefan Gollmer $

package dvbviewertimerimport.dvbviewer;

import java.io.File;
import java.util.ArrayList;


public class DVBViewerCOM
{

	public static native String getVersion() ;
	public static native boolean connect( boolean force) ;
	public static native void disconnect() ;
	public static native DVBViewerEntryCOM[] getItems() ;
	public static native void setItems( DVBViewerEntryCOM[] entries ) ;
	public static native String getSetupValue( String section, String name, String deflt ) ;
	public static native void setCurrentChannel( String channelID ) ;
	public static native void initLog() ;

	
	
	static
	{
		File f = new File( DVBViewer.determineExePath() + File.separator
				           + DVBViewer.NAME_DVBVIEWER_COM_DLL + ".dll" ) ;
		if ( ! f.canExecute() )
			DVBViewer.getDVBViewerCOMDll() ;
		System.loadLibrary( DVBViewer.NAME_DVBVIEWER_COM_DLL );

		DVBViewerCOM.initLog() ;
	}
	
	public static boolean connect()
	{
		return DVBViewerCOM.connect( false ) ;
		
	}

	
	public static  ArrayList<DVBViewerEntry> readTimers()
	{
		if ( ! DVBViewerCOM.connect() )
			return null ;
		DVBViewerEntryCOM[] items = DVBViewerCOM.getItems() ;
		DVBViewerCOM.disconnect() ;

		ArrayList<DVBViewerEntry> result = new ArrayList<DVBViewerEntry>() ;
		
		for ( DVBViewerEntryCOM item : items )
		{
			result.add( item.createDVBViewerEntry() ) ;
		}
		return result ;
	}
	public static boolean setTimers( ArrayList<DVBViewerEntry> entries )
	{
		ArrayList<DVBViewerEntryCOM> items = new ArrayList<DVBViewerEntryCOM>() ;
		
		for ( DVBViewerEntry entry : entries )
			if ( ! entry.mustIgnored() )
				items.add( new DVBViewerEntryCOM( entry ) ) ;
		
		DVBViewerEntryCOM[] array = (DVBViewerEntryCOM[]) items.toArray( new DVBViewerEntryCOM[0] ) ;
		
		if ( ! DVBViewerCOM.connect() )
			return false ;
		
		DVBViewerCOM.setItems( array ) ;
		
		DVBViewerCOM.disconnect() ;
		
		return true ;
	}
}
