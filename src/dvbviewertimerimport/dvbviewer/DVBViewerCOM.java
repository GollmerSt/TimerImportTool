// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$

package dvbviewertimerimport.dvbviewer;

import java.util.ArrayList;


public class DVBViewerCOM
{

	public static native String getVersion() ;
	public static native boolean connect( boolean force) ;
	public static native void disconnect() ;
	public static native DVBViewerEntryCOM[] getItems() ;
	public static native void setItems( DVBViewerEntryCOM[] entries ) ;
	public static native String getSetupValue( String section, String name, String deflt ) ;
	public static native int setCurrentChannel( String channelID ) ;
	public static native int getCurrentChannelNo() ;
	public static native void initLog() ;

	
	
	static
	{
		DVBViewer.loadDVBViewerCOMDll() ;

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
		{
			if ( entry.mustDVBViewerDeleted() )
				items.add( new DVBViewerEntryCOM( entry, true ) ) ;
			if ( entry.mustUpdated() )
				items.add( new DVBViewerEntryCOM( entry, false ) ) ;
			if ( entry.mustDVBViewerCreated() )
				items.add( new DVBViewerEntryCOM( entry, false ) ) ;
		}
		DVBViewerEntryCOM[] array = (DVBViewerEntryCOM[]) items.toArray( new DVBViewerEntryCOM[0] ) ;
		
		if ( array.length == 0 )
			return true ;
		
		if ( ! DVBViewerCOM.connect() )
			return false ;
		
		DVBViewerCOM.setItems( array ) ;
		
		DVBViewerCOM.disconnect() ;
		
		return true ;
	}
}
