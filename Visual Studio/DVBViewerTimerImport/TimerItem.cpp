// $LastChangedDate: 2011-06-13 13:53:29 +0200 (Mo, 13 Jun 2011) $
// $LastChangedRevision: 33 $
// $LastChangedBy: Gottfried Gollmer $

#include "stdAfx.h"
#include "TimerItem.h"
#include "Misc.h"
#include "Log.h"

#include <string>
#include <map>
#include <iostream>
#include <sstream>
#include "DVBViewerCOM.h"
#include "comutil.h"

long long TimerItem::javaOrigin = -1L ;
long TimerItem::default_recAction = 0 ;
long TimerItem::default_afterRec  = 0 ;


TimerItem::TimerItem(
		long _id ,
		std::wstring _channelID ,
		long long _startTime ,
		long long _endTime ,
		std::wstring _description ,
		bool _enabled ,
		long _recAction ,
		long _afterRec ,
		std::wstring _days ,
		bool _mustDelete ) throw()
{
	id = _id ;
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
TimerItem::TimerItem( IDispatch * pItem )
{
	DVBViewerCOM::Status status ;

	status = DVBViewerCOM::getItemProperty( pItem, L"ID", & TimerItem::DispID_ID, &id ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get ChannelID" ) ;

//	id = -1 ;

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

	status = DVBViewerCOM::getItemProperty( pItem, L"Recording", & TimerItem::DispID_Recording, &recording ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get Recording" ) ;

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
		throw std::wstring( L"Error on set Date" ) ;

	status = DVBViewerCOM::setItemProperty( pItem, L"StartTime", & TimerItem::DispID_StartTime, VT_DATE, &start ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on set StartTime" ) ;

	status = DVBViewerCOM::setItemProperty( pItem, L"EndTime", & TimerItem::DispID_EndTime, VT_DATE, &end ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on set EndTime" ) ;


	status = DVBViewerCOM::setItemProperty( pItem, L"Description", & TimerItem::DispID_Description, VT_BSTR, &description ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on set Description" ) ;

	status = DVBViewerCOM::setItemProperty( pItem, L"Enabled", & TimerItem::DispID_Enabled, VT_BOOL, &enabled ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on set Enabled" ) ;

	long temp( recAction ) ;
	if ( recAction < 0 )
		temp = default_recAction ;

	//std::wcout << "recAction = " << temp << std::endl ;
	status = DVBViewerCOM::setItemProperty( pItem, L"TimerAction", & TimerItem::DispID_TimerAction, VT_I4, &temp ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on set TimerAction" ) ;


	if ( afterRec < 0 )
		temp = default_afterRec ;
	else
		temp = afterRec ;

	//std::wcout << "afterRec = " << temp << std::endl ;
	status = DVBViewerCOM::setItemProperty( pItem, L"Shutdown", & TimerItem::DispID_Shutdown, VT_I4, &temp ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on set Shutdown" ) ;

	status = DVBViewerCOM::setItemProperty( pItem, L"Days", & TimerItem::DispID_Days, VT_BSTR, &days ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on set Days" ) ;

	status = DVBViewerCOM::getItemProperty( pItem, L"Shutdown", & TimerItem::DispID_Shutdown, &temp ) ;
	if ( status != DVBViewerCOM::OK )
		throw std::wstring( L"Error on get Shutdown" ) ;
	//std::wcout << "afterRec = " << temp << std::endl ;

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
		Log::error( L"Error on getTimerItems DVBViewer: \"ITimerCollection/Count\" not known." ) ;
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
			Log::error( L"Error on getTimerItems DVBViewer: \"ITimerCollection/Item\" not known." ) ;
			return std::vector< TimerItem >() ;
		}

		try
		{
			TimerItem item( pItem ) ;
			items.push_back( item ) ;
		} catch ( std::wstring e )
		{
			Log::error( L"Error on reading a timer item: " + e ) ;
			return std::vector< TimerItem >() ;
		}
		pItem->Release() ;
	}
	return items ;
}

DVBViewerCOM::Status TimerItem::setItems( DVBViewerCOM & com, const std::vector< TimerItem > & items )
{
	std::map< long, const TimerItem * > itemMap ;
	for ( std::vector< TimerItem >::const_iterator it  = items.begin() ; it != items.end() ; ++it )
	{
		const TimerItem & item = *it ;
		if ( item.toDelete() || item.toUpdate() )
			itemMap[ item.getID() ] = &item ;
	}
	IDispatch * pCollection = com.getTimerCollection() ;

	DVBViewerCOM::Status status = DVBViewerCOM::OK ;

	int count = 0 ;

	boolean getCount = true ;

	int ix = 0 ;
	while ( itemMap.size() != 0 )
	{
		if ( getCount )
		{
			getCount = false ;
			status = DVBViewerCOM::getItemProperty( com.getTimerCollection(), L"Count", & TimerItem::DispID_Count, &count ) ;
			if ( status != DVBViewerCOM::OK )
			{
				Log::error( L"Error on setTimerItems DVBViewer: \"ITimerCollection/Count\" not known." ) ;
				return status ;
			}
		}

		if ( ix >= count )
			break ;

		IDispatch * pItem = NULL ;

		VARIANTARG arg[] = { DVBViewerCOM::toVariant( ix ) } ;

		DISPPARAMS param =
		{
			arg ,
			NULL,
			1,
			0
		};

		status = DVBViewerCOM::getItem( pCollection, L"Item", & TimerItem::DispID_Item, DISPATCH_PROPERTYGET, &param, &pItem ) ;
		

		if ( status != DVBViewerCOM::OK )
		{
			VariantClear( &arg[0] ) ;
			Log::error( L"Error on setTimerItems DVBViewer: \"ITimerCollection/Item\" not known." ) ;
			return status ;
		}

		TimerItem actItem( pItem ) ;

		std::map< long, const TimerItem * >::const_iterator itMap ;

		if ( ( itMap = itemMap.find( actItem.getID() ) ) == itemMap.end() )
		{
			++ix ;
			VariantClear( &arg[0] ) ;
			pItem->Release() ;
			continue ;
		}
		itMap->second->set( com, pItem, &param ) ;
		itemMap.erase( itMap ) ;
		VariantClear( &arg[0] ) ;
		getCount = true ;
	}

	if ( itemMap.size() > 0 )
		Log::log( false, std::wstring( L"Some timer items not found" ) ) ;

	for ( std::vector< TimerItem >::const_iterator it = items.begin() ; it != items.end() ; ++it )
	{
		if ( it->toAdd() )
			it->set( com, NULL, NULL ) ;
	}
//	std::wcout << L"---------------------" << std::endl ;
	return DVBViewerCOM::OK ;
}

DVBViewerCOM::Status TimerItem::set( DVBViewerCOM & com, IDispatch * pItem, DISPPARAMS * pParam ) const
{

	DVBViewerCOM::Status status = DVBViewerCOM::OK ;
	IDispatch * pCollection = com.getTimerCollection() ;

	IDispatch * pTimerItem = pItem ;

//	std::wcout << "Processing of entry \"" << toString() << "\"" << std::endl ;
	if ( toDelete() )
	{
		// Remove
//		std::wcout << "Removed" << std::endl ;
		Log::log( true, L"Entry \"" + toString() + L"\" removed" ) ;
		status = DVBViewerCOM::getItem( pCollection, L"Remove", & TimerItem::DispID_Remove, DISPATCH_METHOD, pParam, NULL ) ;
		pItem->Release() ;
		if ( status != DVBViewerCOM::OK )
			Log::error( L"Error on setTimerItem DVBViewer: \"ITimerCollection/Remove\" not known." ) ;
		return status ;
	}
	else if ( toUpdate() )
	{
		// Update
//		std::wcout << "Updated" << std::endl ;
		Log::log( true, L"Entry \"" + toString() + L"\" updated" ) ;
	}
	else 
	{
		// New
//		std::wcout << "Added" << std::endl ;
		Log::log( true, L"Entry \"" + toString() + L"\" added" ) ;
		status = DVBViewerCOM::getItem( pCollection, L"NewItem", & TimerItem::DispID_NewItem, DISPATCH_METHOD, &pTimerItem ) ;
	}
	if ( status != DVBViewerCOM::OK )
	{
		Log::error( L"Error on setTimerItem DVBViewer: \"ITimerCollection/Item\" not known." ) ;
		return status ;
	}
	try
	{
		setTimer( pTimerItem ) ;
	} catch ( std::wstring e ) {
		Log::error( L"Error on set timer ítem. Error message: " + e ) ;
		return DVBViewerCOM::INTERFACE_MEMBER_ACCESS_ERROR ;
	}
	if ( ! toUpdate() )
	{
		_variant_t var( pTimerItem, false ) ; 
		VARIANTARG arg[] = { var.Detach() } ;

		DISPPARAMS param =
		{
			arg ,
			NULL,
			1,
			0
		};
		status = DVBViewerCOM::getItem( pCollection, L"Add", & TimerItem::DispID_Add, DISPATCH_METHOD, & param,  NULL ) ;
		VariantClear( &arg[0] ) ;
	}
	else
		pTimerItem->Release() ;  // if new update Item will be released by VariantClear 
	
	return DVBViewerCOM::OK ;
}

bool TimerItem::isJavaInitialized = false ;
jfieldID TimerItem::idFieldID ; ;
jfieldID TimerItem::channelIDFieldID ;
jfieldID TimerItem::startTimeFieldID ;
jfieldID TimerItem::endTimeFieldID ;
jfieldID TimerItem::descriptionFieldID ;
jfieldID TimerItem::daysFieldID ;
jfieldID TimerItem::enabledFieldID ;
jfieldID TimerItem::recordingFieldID ;
jfieldID TimerItem::recActionFieldID ;
jfieldID TimerItem::afterRecFieldID ;
jfieldID TimerItem::mustDeleteFieldID ;

DISPID TimerItem::DispID_Count       = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_Item        = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_NewItem     = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_ID          = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_ChannelID   = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_Date        = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_StartTime   = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_EndTime     = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_Description = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_Enabled     = DISPID_UNKNOWN ;
DISPID TimerItem::DispID_Recording   = DISPID_UNKNOWN ;
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

	jclass dvbViewerEntryCOM = env->FindClass( "dvbviewertimerimport/dvbviewer/DVBViewerEntryCOM" ) ;
	if ( dvbViewerEntryCOM == NULL )
		return false ;

	idFieldID          = env->GetFieldID( dvbViewerEntryCOM, "id"         , "I" ) ;
	if ( idFieldID == NULL )
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
	daysFieldID        = env->GetFieldID( dvbViewerEntryCOM, "days",        "Ljava/lang/String;" ) ;
	if ( daysFieldID == NULL )
		return false ;
	enabledFieldID     = env->GetFieldID( dvbViewerEntryCOM, "enabled"    , "Z" ) ;
	if ( enabledFieldID == NULL )
		return false ;
	recordingFieldID     = env->GetFieldID( dvbViewerEntryCOM, "recording"    , "Z" ) ;
	if ( recordingFieldID == NULL )
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
		static_cast< jint >( id ) ,
		Misc::convert( env, channelID ) ,
		static_cast< jlong >( startTime ) ,
		static_cast< jlong >( endTime ) ,
		Misc::convert( env, days ) ,
		Misc::convert( env, description ) ,
		Misc::toJBoolean( enabled ) , 
		Misc::toJBoolean( recording ) , 
		static_cast< jint >( recAction ) ,
		static_cast< jint >( afterRec ) ,
		Misc::toJBoolean( mustDelete ) ) ;
}

TimerItem::TimerItem( JNIEnv *env, jobject o )
{
		if ( ! initJava( env ) )
			return ;
		id          =     env->GetIntField( o, idFieldID ) ;
		startTime   =    env->GetLongField( o, startTimeFieldID ) ;
		endTime     =    env->GetLongField( o, endTimeFieldID ) ;
		enabled     = env->GetBooleanField( o, enabledFieldID )  == JNI_TRUE ;
		recording   = false ;
		recAction   =     env->GetIntField( o, recActionFieldID ) ;
		afterRec    =     env->GetIntField( o, afterRecFieldID ) ;
		days        = Misc::convert( env, static_cast<jstring>( env->GetObjectField( o, daysFieldID ) ) ) ;
		mustDelete  = env->GetBooleanField( o, mustDeleteFieldID ) == JNI_TRUE ;
		channelID   = Misc::convert( env, static_cast<jstring>( env->GetObjectField( o, channelIDFieldID ) ) ) ;
		description = Misc::convert( env, static_cast<jstring>( env->GetObjectField( o, descriptionFieldID ) ) ) ;
}

void TimerItem::setDefaults( int _default_recAction, int _default_afterRec )
{
	default_recAction = _default_recAction ;
	default_afterRec  = _default_afterRec ;
}


std::wstring TimerItem::toString() const
{
	double date = getDate( startTime ) ;
	double start = getTime( startTime ) ;
	double end  = getTime( endTime ) ;

	std::wstringstream stream ;
	stream << id << ";" ;
	stream << description << ";" ;
	stream << channelID << ";" ;
	stream << date << ";" ;
	stream << start << ";" ;
	stream << end << ";" ;
	stream << afterRec << ";" ;
	stream << days << ";" ;
	stream << recAction << ";" ;
	stream << ( enabled ? std::wstring( L"true" ) : std::wstring( L"false" ) ) ;
	stream << ( recording ? std::wstring( L"true" ) : std::wstring( L"false" ) ) ;

	return std::wstring( stream.str() ) ;
}



