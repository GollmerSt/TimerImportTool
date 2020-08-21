// $LastChangedDate: 2011-06-13 13:53:29 +0200 (Mo, 13 Jun 2011) $
// $LastChangedRevision: 33 $
// $LastChangedBy: Gottfried Gollmer $

#pragma once

#include <string>
#include <vector>
#include <iostream>
#include "jni.h"
#include <windows.h>
#include "DVBViewerCOM.h"

class TimerItem
{
private:
	static long long javaOrigin ;
	static long default_recAction ;
	static long default_afterRec ;

	static bool isJavaInitialized ;
	static jfieldID idFieldID ; ;
	static jfieldID channelIDFieldID ;
	static jfieldID startTimeFieldID ;
	static jfieldID endTimeFieldID ;
	static jfieldID daysFieldID ;
	static jfieldID descriptionFieldID ;
	static jfieldID enabledFieldID ;
	static jfieldID recordingFieldID ;
	static jfieldID recActionFieldID ;
	static jfieldID afterRecFieldID ;
	static jfieldID mustDeleteFieldID ;

	static DISPID DispID_Count ;
	static DISPID DispID_Item ;
	static DISPID TimerItem::DispID_NewItem ;
	static DISPID TimerItem::DispID_ID ;
	static DISPID DispID_ChannelID ;
	static DISPID DispID_Date ;
	static DISPID DispID_StartTime ;
	static DISPID DispID_EndTime ;
	static DISPID DispID_Description ;
	static DISPID DispID_Enabled ;
	static DISPID DispID_Recording ;
	static DISPID DispID_TimerAction ;
	static DISPID DispID_Shutdown ;
	static DISPID DispID_Days ;
	static DISPID DispID_Remove ;
	static DISPID DispID_Add ;

	long id ;
	std::wstring channelID ;
	long long startTime ;
	long long endTime ;
	std::wstring description ;
	bool enabled ;
	bool recording ;
	long recAction ;
	long afterRec ;
	std::wstring days ;
	bool mustDelete ;
	static void init() ;
	static bool initJava( JNIEnv *env ) ;
public:
	TimerItem(
		long _id ,
		std::wstring _channelID ,
		long long _startTime ,
		long long _endTime ,
		std::wstring _description ,
		bool _enabled ,
		long recAction ,
		long afterRec ,
		std::wstring _days,
		bool _mustDelete ) throw() ;

	TimerItem( IDispatch * pItem ) ;
	TimerItem( JNIEnv *env, jobject o ) ;
	IDispatch * setTimer( IDispatch * pItem ) const ;
	long long get( double date, double time, double start ) const ;
	double getDate( long long time ) const ;
	double getTime( long long time ) const ;
	bool toAdd() const { return id == -1 && ! mustDelete ; } ;
	bool toUpdate() const { return ! mustDelete && id != -1 ; } ;
	bool toDelete() const { return mustDelete && id != -1 ; } ;
	int getID() const { return id ; } ;
	jobject toJava( JNIEnv *env, jclass clazz, jmethodID constructorID ) const ;
	DVBViewerCOM::Status set( DVBViewerCOM & com, IDispatch * pItem, DISPPARAMS *pParam ) const ;
	static std::vector< TimerItem > getItems( DVBViewerCOM & com ) ;
	static DVBViewerCOM::Status  setItems( DVBViewerCOM & com, const std::vector< TimerItem > & items ) ;

	static void setDefaults( int _default_recAction, int _default_afterRec ) ;


	std::wstring toString() const ;
};
