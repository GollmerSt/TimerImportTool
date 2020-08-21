// $LastChangedDate: 2010-05-01 08:01:55 +0200 (Sa, 01. Mai 2010) $
// $LastChangedRevision: 5 $
// $LastChangedBy: Stefan Gollmer $

#include "stdAfx.h"
#include "TimerItem.h"
#include "Misc.h"
#include <string>
#include <iostream>
#include "DVBViewerCOM.h"
#include "comutil.h"

long long TimerItem::javaOrigin = -1L ;


TimerItem::TimerItem(
		int _ix ,
		std::wstring _channelID ,
		long long _startTime ,
		long long _endTime ,
		std::wstring _description ,
		bool _enabled ,
		int _recAction ,
		int _afterRec ,
		std::wstring _days ,
		bool _mustDelete ) throw()
{
	ix = _ix ;
	channelID = _channelID ;
	startTime = _startTime ;
	endTime = _endTime ;
	description = _description ;
	enabled = _enabled ;
	recAction = _recAction ;
	afterRec = _afterRec ;
	days = _days ;
	mustDelete = _mustDelete ;
}
TimerItem::TimerItem( IDispatch * pItem, int _ix )
{
	DVBViewerCOM::Status status ;

	ix = _ix ;
	//ix = -1 ;

	status = DVBViewerCOM::getItemProperty( pItem, L"ChannelID", & TimerItem::DispID_ChannelID, &channelID ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get ChannelID" ) ;


	double date ;
	double start ;
	double end ;

	status = DVBViewerCOM::getItemProperty( pItem, L"Date", & TimerItem::DispID_Date, &date ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get Date" ) ;

	status = DVBViewerCOM::getItemProperty( pItem, L"StartTime", & TimerItem::DispID_StartTime, &start ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get StartTime" ) ;

	status = DVBViewerCOM::getItemProperty( pItem, L"EndTime", & TimerItem::DispID_EndTime, &end ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get EndTime" ) ;

	startTime = get( date, start, start ) ;
	endTime = get( date, end, start ) ;


	status = DVBViewerCOM::getItemProperty( pItem, L"Description", & TimerItem::DispID_Description , &description ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get Description" ) ;

	status = DVBViewerCOM::getItemProperty( pItem, L"Enabled", & TimerItem::DispID_Enabled, &enabled ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get Enabled" ) ;

	status = DVBViewerCOM::getItemProperty( pItem, L"TimerAction", & TimerItem::DispID_TimerAction, &recAction ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get TimerAction" ) ;

	status = DVBViewerCOM::getItemProperty( pItem, L"Shutdown", & TimerItem::DispID_Shutdown, &afterRec ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get Shutdown" ) ;

	status = DVBViewerCOM::getItemProperty( pItem, L"Days", & TimerItem::DispID_Days, &days ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get Days" ) ;

	mustDelete = false ;
}

IDispatch * TimerItem::setTimer( IDispatch * pItem ) const
{
	DVBViewerCOM::Status status ;

	status = DVBViewerCOM::setItemProperty( pItem, L"ChannelID", & TimerItem::DispID_ChannelID, VT_BSTR, &channelID ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get ChannelID" ) ;

	double date = getDate( startTime ) ;
	double start = getTime( startTime ) ;
	double end  = getTime( endTime ) ;

	status = DVBViewerCOM::setItemProperty( pItem, L"Date", & TimerItem::DispID_Date, VT_DATE, &date ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get Date" ) ;

	status = DVBViewerCOM::setItemProperty( pItem, L"StartTime", & TimerItem::DispID_StartTime, VT_DATE, &start ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get StartTime" ) ;

	status = DVBViewerCOM::setItemProperty( pItem, L"EndTime", & TimerItem::DispID_EndTime, VT_DATE, &end ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get EndTime" ) ;


	status = DVBViewerCOM::setItemProperty( pItem, L"Description", & TimerItem::DispID_Description, VT_BSTR, &description ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get Description" ) ;

	status = DVBViewerCOM::setItemProperty( pItem, L"Enabled", & TimerItem::DispID_Enabled, VT_BOOL, &enabled ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get Enabled" ) ;

	status = DVBViewerCOM::setItemProperty( pItem, L"TimerAction", & TimerItem::DispID_TimerAction, VT_I4, &recAction ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get TimerAction" ) ;

	status = DVBViewerCOM::setItemProperty( pItem, L"Shutdown", & TimerItem::DispID_Shutdown, VT_I4, &afterRec ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get Shutdown" ) ;

	status = DVBViewerCOM::setItemProperty( pItem, L"Days", & TimerItem::DispID_Days, VT_BSTR, &days ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get Days" ) ;

	return pItem ;
}


long long TimerItem::get( double date, double time, double start ) const
{
	double work = date + time ;
	if ( time < start )
		work += 1.0 ;
	return long long( work * 86400000 + time - 2209161600000 ) ; // 1000 * 60 * 60 * 24   )
}

double  TimerItem::getDate( long long time ) const
{
	long long work = ( time + 2209161600000 ) / 86400000 ;
	return double( work ) ;
}
double TimerItem::getTime( long long time ) const
{
	return double( time % 86400000 ) / 86400000.0 ;
}
std::vector< TimerItem > TimerItem::getItems( DVBViewerCOM & com )
{
	IDispatch * pCollection = com.getTimerCollection() ;

	int count ;

	DVBViewerCOM::Status status ;
	
	status = DVBViewerCOM::getItemProperty( com.getTimerCollection(), L"Count", & TimerItem::DispID_Count, &count ) ;
	if ( status != DVBViewerCOM::OK )
	{
		std::wcout << L"Error on reading the count of the timer items" << std::endl ;
		return std::vector< TimerItem >() ;
	}

	std::vector< TimerItem > items ;

	for ( int i = 0 ; i < count ; ++i )
	{
		VARIANTARG arg[] = { DVBViewerCOM::toVariant( i ) } ;

		DISPPARAMS param =
		{
			arg ,
			NULL,
			1,
			0
		};

		IDispatch * pItem ;
		status = DVBViewerCOM::getItem( com.getTimerCollection(), L"Item", & TimerItem::DispID_Item, DISPATCH_PROPERTYGET, &param, &pItem ) ;
		VariantClear( &arg[ 0 ] ) ;

		if ( status != DVBViewerCOM::OK )
		{
			std::wcout << L"Error on get the item of the timer items" << std::endl ;
			return std::vector< TimerItem >() ;
		}

		try
		{
			TimerItem item( pItem, i ) ;
			items.push_back( item ) ;
		} catch ( std::wstring e )
		{
			std::wcout << L"Error on reading a timer item, Error message: " << e << std::endl ;
			return std::vector< TimerItem >() ;
		}
		pItem->Release() ;
	}
	return items ;
}

DVBViewerCOM::Status TimerItem::setItems( DVBViewerCOM & com, const std::vector< TimerItem > & items )
{
	IDispatch * pCollection = com.getTimerCollection() ;

	DVBViewerCOM::Status status ;

	int numberOfDeletedItems = 0 ;

	for ( std::vector< TimerItem >::const_iterator it  = items.begin() ; it != items.end() ; ++it )
	{
		IDispatch * pItem = NULL ;

		it->decrIndex( numberOfDeletedItems ) ;

		if ( it->toDelete() )
			it->set( com ) ;
	}

	for ( std::vector< TimerItem >::const_iterator it  = items.begin() ; it != items.end() ; ++it )
	{
		if ( it->toUpdate() )
			it->set( com ) ;
	}

	for ( std::vector< TimerItem >::const_iterator it  = items.begin() ; it != items.end() ; ++it )
	{
		if ( it->toAdd() )
			it->set( com ) ;
	}
	return DVBViewerCOM::OK ;
}

DVBViewerCOM::Status TimerItem::set( DVBViewerCOM & com ) const
{
	DVBViewerCOM::Status status = DVBViewerCOM::OK ;
	IDispatch * pCollection = com.getTimerCollection() ;

	IDispatch * pItem = NULL ;

	int ix = getIndex() ;
	
	VARIANTARG arg[] = { DVBViewerCOM::toVariant( ix ) } ;

	DISPPARAMS param =
	{
		arg ,
		NULL,
		1,
		0
	};

	if ( toDelete() )
	{
		// Remove
		std::wcout << L"Description of removed entry " << ix << ": " << description << std::endl ;
		status = DVBViewerCOM::getItem( pCollection, L"Remove", & TimerItem::DispID_Remove, DISPATCH_METHOD, &param, NULL ) ;
	}
	else if ( toUpdate() )
	{
		// Update
		std::wcout << L"Description of updated entry " << ix << ": " << description << std::endl ;
		status = DVBViewerCOM::getItem( pCollection, L"Item", & TimerItem::DispID_Item, DISPATCH_PROPERTYGET, &param, &pItem ) ;
	}
	else 
		// New
		status = DVBViewerCOM::getItem( pCollection, L"NewItem", & TimerItem::DispID_NewItem, DISPATCH_METHOD, &pItem ) ;
	
	VariantClear( &arg[0] ) ;

	if ( status != DVBViewerCOM::OK )
		return status ;

	if ( pItem == NULL )
		return DVBViewerCOM::OK ;

	try
	{
		setTimer( pItem ) ;
	} catch ( std::wstring e ) {
		std::wcout << L"Error on set timer ítem. Error message: " << e << std::endl ;
		return DVBViewerCOM::INTERFACE_MEMBER_ACCESS_ERROR ;
	}
	if ( ! toUpdate() )
	{
		_variant_t var( pItem, false ) ; 
		VARIANTARG arg[] = { var.Detach() } ;

		DISPPARAMS param =
		{
			arg ,
			NULL,
			1,
			0
		};
		std::wcout << L"Description of new entry " << ix << ": " << description << std::endl ;

		status = DVBViewerCOM::getItem( pCollection, L"Add", & TimerItem::DispID_Add, DISPATCH_METHOD, & param,  NULL ) ;
		VariantClear( &arg[0] ) ;
	}
	else
		pItem->Release() ;
	
	return DVBViewerCOM::OK ;
}

bool TimerItem::isJavaInitialized = false ;
jfieldID TimerItem::ixFieldID ; ;
jfieldID TimerItem::channelIDFieldID ;
jfieldID TimerItem::startTimeFieldID ;
jfieldID TimerItem::endTimeFieldID ;
jfieldID TimerItem::descriptionFieldID ;
jfieldID TimerItem::enabledFieldID ;
jfieldID TimerItem::recActionFieldID ;
jfieldID TimerItem::afterRecFieldID ;
jfieldID TimerItem::mustDeleteFieldID ;

DISPID TimerItem::DispID_Count       = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_Item        = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_NewItem     = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_ChannelID   = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_Date        = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_StartTime   = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_EndTime     = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_Description = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_Enabled     = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_TimerAction = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_Shutdown    = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_Days        = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_Remove      = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_Add         = DISPID_UNKNOWN ;

bool TimerItem::initJava( JNIEnv *env )
{
	if ( isJavaInitialized )
		return true ;
	isJavaInitialized = true ;

	jclass dvbViewerEntryCOM = env->FindClass( "dvbv/dvbviewer/DVBViewerEntryCOM" ) ;
	if ( dvbViewerEntryCOM == NULL )
		return false ;

	ixFieldID          = env->GetFieldID( dvbViewerEntryCOM, "ix"         , "I" ) ;
	if ( ixFieldID == NULL )
		return false ;
	channelIDFieldID   = env->GetFieldID( dvbViewerEntryCOM, "channelID"  , "Ljava/lang/String;" ) ;
	if ( channelIDFieldID == NULL )
		return false ;
	startTimeFieldID   = env->GetFieldID( dvbViewerEntryCOM, "startTime"  , "J" ) ;
	if ( startTimeFieldID == NULL )
		return false ;
	endTimeFieldID     = env->GetFieldID( dvbViewerEntryCOM, "endTime"    , "J" ) ;
	if ( endTimeFieldID == NULL )
		return false ;
	descriptionFieldID = env->GetFieldID( dvbViewerEntryCOM, "description", "Ljava/lang/String;" ) ;
	if ( descriptionFieldID == NULL )
		return false ;
	enabledFieldID     = env->GetFieldID( dvbViewerEntryCOM, "enabled"    , "Z" ) ;
	if ( enabledFieldID == NULL )
		return false ;
	recActionFieldID   = env->GetFieldID( dvbViewerEntryCOM, "recAction"  , "I" ) ;
	if ( recActionFieldID == NULL )
		return false ;
	afterRecFieldID    = env->GetFieldID( dvbViewerEntryCOM, "afterRec"   , "I" ) ;
	if ( afterRecFieldID == NULL )
		return false ;
	mustDeleteFieldID  = env->GetFieldID( dvbViewerEntryCOM, "mustDelete" , "Z" ) ;
	if ( mustDeleteFieldID == NULL )
		return false ;

	return true ;
}

jobject TimerItem::toJava( JNIEnv *env, jclass clazz, jmethodID constructorID ) const
{	 
	return env->NewObject(
		clazz ,
		constructorID ,
		static_cast< jint >( ix ) ,
		Misc::convert( env, channelID ) ,
		static_cast< jlong >( startTime ) ,
		static_cast< jlong >( endTime ) ,
		Misc::convert( env, description ) ,
		static_cast< jboolean >( enabled ) ,
		static_cast< jint >( recAction ) ,
		static_cast< jint >( afterRec ) ,
		static_cast< jboolean >( mustDelete ) ) ;
}

TimerItem::TimerItem( JNIEnv *env, jobject o )
{
		if ( ! initJava( env ) )
			return ;
		ix          =     env->GetIntField( o, ixFieldID ) ;
		startTime   =    env->GetLongField( o, startTimeFieldID ) ;
		endTime     =    env->GetLongField( o, endTimeFieldID ) ;
		enabled     = env->GetBooleanField( o, enabledFieldID ) ;
		recAction   =     env->GetIntField( o, recActionFieldID ) ;
		afterRec    =     env->GetIntField( o, afterRecFieldID ) ;
		wchar_t w[] = { '-', '-', '-', '-', '-', '-', '-' } ;
		days        = std::wstring( w ) ;
		mustDelete  = env->GetBooleanField( o, mustDeleteFieldID ) ;
		channelID   = Misc::convert( env, static_cast<jstring>( env->GetObjectField( o, channelIDFieldID ) ) ) ;
		description = Misc::convert( env, static_cast<jstring>( env->GetObjectField( o, descriptionFieldID ) ) ) ;
}


