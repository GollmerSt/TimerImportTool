// $LastChangedDate: 2011-06-04 10:00:25 +0200 (Sa, 04. Jun 2011) $
// $LastChangedRevision: 26 $
// $LastChangedBy: Stefan Gollmer $

#include "StdAfx.h"
#include "DVBViewerCOM.h"
#include "Misc.h"
#include "comutil.h"
#include "TimerItem.h"
#include "Log.h"

#include <iostream>
#include <cwchar>
#include <string>


DVBViewerCOM::Status DVBViewerCOM::setItemProperty( IDispatch * pIDispatchItem, LPOLESTR property, DISPID * pDispID, VARTYPE vartype, const void * buffer )
{
	HRESULT  hr;
	DISPID   dispID = DISPID_UNKNOWN ;


	if ( pDispID == NULL )
		pDispID = &dispID ;

	if ( *pDispID == DISPID_UNKNOWN )
	{
		hr = pIDispatchItem->GetIDsOfNames( IID_NULL, & property, 1, LOCALE_SYSTEM_DEFAULT, pDispID );
		if ( FAILED( hr ) )
		{
//			std::wcout << L"INTERFACE_NAMES_NOT_FOUND" << std::endl ;
			return INTERFACE_NAMES_NOT_FOUND;
		}
	}


	_variant_t * pVa ;

	switch ( vartype )
	{
		case VT_BSTR :
			pVa = new _variant_t( (( std::wstring * ) buffer)->c_str() ) ;
			break ;
		case VT_I4 :
			pVa = new _variant_t( * ( ( long * ) buffer ) , VT_I4 ) ;
			break ;
		case VT_DATE:
			pVa = new _variant_t( * ( ( double * ) buffer ) , VT_DATE ) ;
			break;
		case VT_BOOL :
			pVa = new _variant_t( * ( ( bool * ) buffer ) ) ;
			break ;
		case VT_DISPATCH :
			pVa = new _variant_t( * ( ( IDispatch ** ) buffer ), false ) ;
			break ;
	}

	VARIANTARG arg[] = { pVa->Detach() } ;

	DISPID dispidNamed = DISPID_PROPERTYPUT;

	WORD  wFlags = DISPATCH_PROPERTYPUT ;

	if ( vartype == VT_DISPATCH && false )
	{
		dispidNamed = DISPATCH_PROPERTYPUTREF;
		wFlags = DISPATCH_PROPERTYPUTREF ;
	}

	DISPPARAMS param =
	{
		arg ,
		&dispidNamed,
		1,
		1
	};

	hr = pIDispatchItem->Invoke( *pDispID, IID_NULL, LOCALE_SYSTEM_DEFAULT, DISPATCH_PROPERTYPUT, &param, NULL, NULL, NULL );

	VariantClear( arg ) ;
	delete pVa ;

	if ( FAILED( hr ) )
	{
//		std::wcout << L"INTERFACE_MEMBER_ACCESS_ERROR, code = " << hr << std::endl ;
		return INTERFACE_MEMBER_ACCESS_ERROR;
	}
	return OK ;
}
DVBViewerCOM::Status DVBViewerCOM::getItemProperty( IDispatch * pIDispatchItem, LPOLESTR property, DISPID * pDispID, void * buffer )
{
	DISPPARAMS param = { NULL, NULL, 0, 0 };

	return DVBViewerCOM::getItem( pIDispatchItem, property, pDispID, DISPATCH_PROPERTYGET, & param, buffer ) ;
}
DVBViewerCOM::Status DVBViewerCOM::getItem( IDispatch * pIDispatchItem, LPOLESTR property, DISPID * pDispID, WORD  wFlags, void * buffer )
{
	DISPPARAMS param = { NULL, NULL, 0, 0 };

	return DVBViewerCOM::getItem( pIDispatchItem, property, pDispID, wFlags, & param, buffer ) ;
}
DVBViewerCOM::Status DVBViewerCOM::getItem( IDispatch * pIDispatchItem, LPOLESTR property, DISPID * pDispID, WORD  wFlags, DISPPARAMS * pParam, void * buffer )
{
	HRESULT  hr;
	DISPID   dispID = DISPID_UNKNOWN ;

	if ( pDispID == NULL )
		pDispID = &dispID ;

	if ( *pDispID == DISPID_UNKNOWN  )
	{
		hr = pIDispatchItem->GetIDsOfNames( IID_NULL, & property, 1, LOCALE_SYSTEM_DEFAULT, pDispID );
		if ( FAILED( hr ) )
			return INTERFACE_NAMES_NOT_FOUND;
	}

	VARIANT retVal;
	hr = pIDispatchItem->Invoke( *pDispID, IID_NULL, LOCALE_SYSTEM_DEFAULT, wFlags, pParam, & retVal, NULL, NULL );
	if ( FAILED( hr ) )
	{
//		std::wcout <<  "Fehlercode: " << hr << std::endl ;
		return INTERFACE_MEMBER_ACCESS_ERROR;
	}

	if ( buffer == NULL )
		return OK ;

	switch ( retVal.vt )
	{
		case VT_BSTR:
			{
				BSTR val = retVal.bstrVal;
				if ( val )
				{
					_bstr_t s( val ) ;
					int size = s.length() + 1 ;
					wchar_t * nbuffer = new wchar_t[ size ] ;
					wcscpy_s( nbuffer, size, s ) ;
					SysFreeString( val );
					* ( std::wstring * ) buffer = std::wstring( nbuffer ) ;
					delete nbuffer ;
				}
				else
					* ( std::wstring * ) buffer = std::wstring() ;
				break;
			}
    
		case VT_I4:
			* ( long * ) buffer = retVal.intVal;
			break;
    
		case VT_DATE:
			* ( double * ) buffer = retVal.date;
			break;
    
		case VT_DISPATCH:
			* ( IDispatch * * ) buffer = retVal.pdispVal;
			break;

		case VT_BOOL :
			* ( bool * ) buffer = retVal.boolVal == VARIANT_TRUE ;
			break ;
		case VT_EMPTY :
			break ;
	}
	return OK ;
}

DVBViewerCOM::Status DVBViewerCOM::connect( bool force )
{
//	std::wcout << "Number = " << numberOfConnections << std::endl ;
	if ( numberOfConnections > 0 )
	{
		++numberOfConnections ;
		return OK ;
	}
	CoInitialize( NULL ) ;

	HRESULT hr ;
	CLSID clsid;
	hr = CLSIDFromProgID( L"DVBViewerServer.DVBViewer", & clsid );

	if ( FAILED( hr ) )
	{
		Log::error( L"Error on connect DVBViewer: COM interface not known. The DVBViewer installation must be checked." ) ;
		return DVBVIEWER_INSTALLATION_FAIL ;
	}

	IUnknown * pDVBViewerObject = NULL ;

	hr = GetActiveObject( clsid, NULL, & pDVBViewerObject ) ;

	if ( FAILED( hr ) && ! force )
		return DVBVIEWER_NOT_FOUND ;		// DVBViewer not active

	if ( FAILED( hr ) )
	{
		hr = CoCreateInstance( clsid, NULL, CLSCTX_SERVER, IID_IUnknown, reinterpret_cast<void**>( & pDVBViewerObject ) );
		if ( FAILED( hr ) && !! force )
			return DVBVIEWER_NOT_FOUND ;		// DVBViewer not active
	}

	hr = pDVBViewerObject->QueryInterface( IID_IDispatch, ( void** ) &pIDVBViewer ) ;
	pDVBViewerObject->Release() ;

	if ( FAILED( hr ) )
	{
		Log::error( L"Error on connect DVBViewer: Unexpected error" ) ;
		return COULD_NOT_CONNECT ;
	}
	Status status = OK ;

	status = getItemProperty(  pIDVBViewer, L"TimerManager", &DispID_TimerManager, & pITimerCollection  ) ;
	if ( OK != status )
	{
		Log::error( L"Error on connect DVBViewer: Unexpected error" ) ;
		pIDVBViewer -> Release() ;
		return status ;
	}

	++numberOfConnections ;

	Log::log( true, std::wstring( L"DVBViewer is connected"  ) ) ;

	return OK ;
}
void DVBViewerCOM::disconnect()
{
//	std::wcout << "Number = " << numberOfConnections << std::endl ;
	if ( pIDVBViewer == NULL )
	{
		Log::error( L"Error on disconnect DVBViewer: DVBViewer isn't connected" ) ;
		return ;
	}
	if ( --numberOfConnections > 0 )
		return ;
	
	pITimerCollection->Release() ;
	pIDVBViewer -> Release() ;
	pIDVBViewer = NULL ;

	CoUninitialize() ;

	Log::log( true, std::wstring( L"DVBViewer is disconnected" ) ) ;
}

std::wstring DVBViewerCOM::getSetupValue( const std::wstring & section, const std::wstring & name, const std::wstring & default )
{
	if ( pIDVBViewer == NULL )
	{
		Log::error( L"Error on getSetupValue DVBViewer: DVBViewer isn't connected" ) ;
		return std::wstring() ;
	}

	VARIANTARG arg[] = { toVariant( default ), toVariant( name ),toVariant( section ) } ;

	DISPPARAMS param = 
	{
		arg ,
		NULL,
		3,
		0
	};

	std::wstring res ;

	Status status = getItem( pIDVBViewer, L"GetSetupValue", &DispID_GetSetupValue, DISPATCH_METHOD, & param, & res  ) ;

	VariantClear( & arg[0] ) ;
	VariantClear( & arg[1] ) ;
	VariantClear( & arg[2] ) ;

	if ( status != OK )
	{
		Log::error( L"Error on getSetupValue DVBViewer." ) ;
		return res ;
	}

	return res ;
//	return Misc::get( dvbViewer->GetSetupValue( Misc::get( section ), Misc::get( name ), Misc::get( default ) ) ) ;
}
long DVBViewerCOM::setCurrentChannel( const std::wstring & channelID )
{
	if ( pIDVBViewer == NULL )
	{
		Log::error( L"Error on getTimerItems DVBViewer: DVBViewer isn't connected" ) ;
		return -1 ;
	}

	IDispatch * pIChannelCollection ;

	Status status = getItemProperty(  pIDVBViewer, L"ChannelManager", &DispID_ChannelManager, & pIChannelCollection  ) ;
	if ( status != OK )
	{
		Log::error( L"Error on setCurrentChannel DVBViewer: \"IDVBViewer/ChannelManager\" not found." ) ;
		return -1 ;
	}
	 
	VARIANTARG arg[] = { toVariant( channelID ),  } ;

	DISPPARAMS param = 
	{
		arg ,
		NULL,
		1,
		0
	};

	long channelNr ;
	status = getItem( pIChannelCollection, L"GetNr", &DispID_GetNr, DISPATCH_METHOD, & param, & channelNr  ) ;
	if ( status != OK )
	{
		Log::error( L"Error on setCurrentChannel DVBViewer: \"IDVBViewer/GetNr\" not found." ) ;
		return -1 ;
	}

	VariantClear( & arg[0] ) ;
	//std::wcerr << "CurrentChannelNr = " << channelNr << std::endl ;

	status = setItemProperty( pIDVBViewer, L"CurrentChannelNr", &DispID_CurrentChannelNr, VT_I4, &channelNr ) ;
	if ( status != OK )
	{
		Log::error( L"Error on setCurrentChannel DVBViewer: \"IChannelCollection/CurrentChannelNr\" not found." ) ;
		return -1 ;
	}
	return channelNr ;
}

long DVBViewerCOM::getCurrentChannelNo()
{
	if ( pIDVBViewer == NULL )
	{
		Log::error( L"Error on getTimerItems DVBViewer: DVBViewer isn't connected" ) ;
		return -1 ;
	}

	long channelNr ;
	Status status = getItemProperty( pIDVBViewer, L"CurrentChannelNr", &DispID_CurrentChannelNr, &channelNr ) ;
	if ( status != OK )
	{
		Log::error( L"Error on getCurrentChannelNo DVBViewer: \"IDVBViewer/CurrentChannelNr\" not found." ) ;
		return -1 ;
	}
	return channelNr ;
}

std::vector< TimerItem > DVBViewerCOM::getTimerItems()
{
	if ( pIDVBViewer == NULL )
	{
		Log::error( L"Error on getTimerItems DVBViewer: DVBViewer isn't connected" ) ;
		return std::vector< TimerItem >() ;
	}
	return TimerItem::getItems( *this ) ;
}
bool DVBViewerCOM::setTimerItems( const std::vector< TimerItem > & items )
{
	if ( pIDVBViewer == NULL )
	{
		Log::error( L"Error on setTimerItems DVBViewer: DVBViewer isn't connected" ) ;
		return false ;
	}
	std::wstring defRecAction   = getSetupValue(  L"General", L"DefRecAction", L"0" ) ; 
	std::wstring defAfterRecord = getSetupValue( L"General", L"DefAfterRecord", L"0" ) ;

	TimerItem::setDefaults( _wtoi( defRecAction.c_str() ), _wtoi( defAfterRecord.c_str() ) ) ;

	TimerItem::setItems( *this, items ) ;
	return true ;
}
VARIANTARG DVBViewerCOM::toVariant( std::wstring s )
{
	_bstr_t bs( s.c_str() ) ;
	_variant_t res( bs ) ;
	return res.Detach() ; 
}
VARIANTARG DVBViewerCOM::toVariant( long i )
{
	_variant_t res( i ) ;
	return res.Detach() ; 
}