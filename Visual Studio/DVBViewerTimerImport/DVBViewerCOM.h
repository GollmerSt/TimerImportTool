// $LastChangedDate: 2011-06-04 10:00:25 +0200 (Sa, 04. Jun 2011) $
// $LastChangedRevision: 26 $
// $LastChangedBy: Stefan Gollmer $

#pragma once

#include <windows.h>
//#include <time.h> 

#include <string>
#include <vector>
//#include <iostream>

class TimerItem ;

class DVBViewerCOM
{
public:
	enum Status {
			OK ,
			DVBVIEWER_INSTALLATION_FAIL ,
			DVBVIEWER_NOT_FOUND ,
			COULD_NOT_CONNECT ,
			INTERFACE_NAMES_NOT_FOUND,
			INTERFACE_MEMBER_ACCESS_ERROR,
	} ;
private:
	int numberOfConnections ;
	IDispatch * pIDVBViewer ;
	IDispatch * pITimerCollection ;

	DISPID DispID_TimerManager ;
	DISPID DispID_GetSetupValue ;
	DISPID DispID_ChannelManager ;
	DISPID DispID_CurrentChannelNr ;
	DISPID DispID_GetNr ;

public:
	DVBViewerCOM()
	{
		//std::wcout << L"Hier wird COM initialisiert" << std::endl ;
		numberOfConnections = 0 ;
		pIDVBViewer = NULL ;
		pITimerCollection = NULL ;

		DispID_TimerManager     = DISPID_UNKNOWN ;
		DispID_GetSetupValue    = DISPID_UNKNOWN ;
		DispID_ChannelManager   = DISPID_UNKNOWN ;
		DispID_CurrentChannelNr = DISPID_UNKNOWN ;
		DispID_GetNr            = DISPID_UNKNOWN ;
	}



	static Status DVBViewerCOM::setItemProperty( IDispatch * pIDispatchItem, LPOLESTR property, DISPID * pDispID, VARTYPE vartype, const void * buffer ) ;
	static Status DVBViewerCOM::getItemProperty( IDispatch * pIDispatchItem, LPOLESTR property, DISPID * pDispID, void * buffer ) ;
	static Status DVBViewerCOM::getItem( IDispatch * pIDispatchItem, LPOLESTR property, DISPID * pDispID, WORD  wFlags, DISPPARAMS * pParam, void * buffer ) ;
	static Status DVBViewerCOM::getItem( IDispatch * pIDispatchItem, LPOLESTR property, DISPID * pDispID, WORD  wFlags, void * buffer ) ;
public :
	Status connect( bool force = false ) ;
	void disconnect() ;
	std::wstring getSetupValue( const std::wstring & section, const std::wstring & name, const std::wstring & default ) ;
	long setCurrentChannel( const std::wstring & channelID ) ;
	long getCurrentChannelNo() ;

	static VARIANTARG toVariant( std::wstring s ) ;
	static VARIANTARG toVariant( long i ) ;
	std::vector< TimerItem > getTimerItems() ;
	bool setTimerItems( const std::vector< TimerItem > & items ) ;

	IDispatch * getTimerCollection() { return pITimerCollection ; } ;

} ;