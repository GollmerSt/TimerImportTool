// DVBViewerTest.cpp : Definiert den Einstiegspunkt für die Konsolenanwendung.
//

#include "stdafx.h"
#include "DVBViewerCOM.h"

#include "TimerItem.h"

int _tmain(int argc, _TCHAR* argv[])
{
	DVBViewerCOM com ;
	if ( DVBViewerCOM::OK != com.connect() )
		return 1 ;
	std::wstring setup = com.getSetupValue(std::wstring(L"General"), std::wstring(L"DefRecAction"), std::wstring(L"0") );
	printf( "General/DefRecAction = %s\n", setup.c_str() ) ;

	com.setCurrentChannel( L"740191815|SWR Fernsehen RP (ger)" ) ;

	std::vector< TimerItem > vec = com.getTimerItems() ;
	com.setTimerItems( vec ) ; 
	/* Voraussetzung: aktiver DVBV
	DVBViewerServer::DVBViewer^ dvbv = DVBViewerCOM ::get() ;
	DVBViewerServer::ITimerCollection^ timers = dvbv->TimerManager;
	System::Object ^ list ;
	DVBViewerServer::ITimerItem ^ item = nullptr ;
	item = timers->AddItem(
		gcnew String("Das Erste"),
		DateTime( 799264352000000000 ),
		DateTime( 599264452000000000 ),
		DateTime( 599264552000000000 ),
		gcnew String("Testaufnahme"),
		false,
		true,
		3,
		4,
		"-------"
		) ;
	item = timers->NewItem() ;

	std::vector<TimerItem> items ;

	items = DVBViewerCOM ::getItems() ;
	DVBViewerCOM ::setItems( items ) ;

	DVBViewerCOM ::connect() ;
	DVBViewerCOM ::disconnect() ;

*/
	com.disconnect() ;

	return 0;
}

