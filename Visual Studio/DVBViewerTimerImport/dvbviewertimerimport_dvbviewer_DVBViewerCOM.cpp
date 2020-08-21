// $LastChangedDate: 2011-06-13 13:53:29 +0200 (Mo, 13 Jun 2011) $
// $LastChangedRevision: 33 $
// $LastChangedBy: Gottfried Gollmer $

#include "StdAfx.h"
#include "dvbviewertimerimport_dvbviewer_DVBViewerCOM.h"
#include "DVBViewerCOM.h"
#include "TimerItem.h"
#include "Misc.h"
#include "Log.h"
#include "Version.h"


DVBViewerCOM com ;


/* Header for class dvbviewertimerimport_dvbviewer_DVBViewerCOM */

/*
 * Class:     dvbviewertimerimport_dvbviewer_DVBViewerCOM
 * Method:    getItems
 * Signature: ()[Ldvbviewertimerimport/dvbviewer/DVBViewerEntryCOM;
 */
JNIEXPORT jobjectArray JNICALL Java_dvbviewertimerimport_dvbviewer_DVBViewerCOM_getItems
  (JNIEnv * env, jclass c)
{
	Log::setJNIEnv( env ) ;
	//printf ( "DVBViewer-getItems-Start\n" ) ;

	std::vector< TimerItem > items = com.getTimerItems() ;

	//printf ( "DVBViewer-getItems-Items read\n" ) ;


	jsize size = static_cast< jsize >( items.size() ) ;
	
	jclass dvbViewerEntryCOM = env->FindClass( "dvbviewertimerimport/dvbviewer/DVBViewerEntryCOM" ) ;
	if (dvbViewerEntryCOM == 0L)
		return NULL; /* exception thrown */

	//printf ( "dvbviewertimerimport/dvbviewer/DVBViewerEntryCOM searched\n" ) ;


	jobjectArray result = env->NewObjectArray( size, dvbViewerEntryCOM, 0L ) ;

	jmethodID constructorID = env->GetMethodID( dvbViewerEntryCOM, "<init>", "(ILjava/lang/String;JJLjava/lang/String;Ljava/lang/String;ZZIIZ)V");
	if (constructorID == 0L)
		return NULL; /* exception thrown */

	//printf ( "dvbViewerEntryCOM constructorID searched" ) ;


	int ix = 0 ;
	for ( std::vector< TimerItem >::const_iterator it = items.begin() ; it != items.end() ; ++it, ++ix )
	{
		jobject entry = it->toJava( env, dvbViewerEntryCOM, constructorID ) ;
		env->SetObjectArrayElement( result, ix, entry ) ;
		env->DeleteLocalRef(  entry ) ;
	}
	//printf ( "DVBViewer-getItems-End\n" ) ;

	Log::setJNIEnv( NULL ) ;
	return result ;
};

/*
 * Class:     dvbviewertimerimport_dvbviewer_DVBViewerCOM
 * Method:    setItems
 * Signature: ([Ldvbviewertimerimport/dvbviewer/DVBViewerEntryCOM;)V
 */
JNIEXPORT void JNICALL Java_dvbviewertimerimport_dvbviewer_DVBViewerCOM_setItems
  (JNIEnv * env, jclass c, jobjectArray oa)
{
	Log::setJNIEnv( env ) ;
	//printf ( "DVBViewer-setItems-Start\n" ) ;

	std::vector< TimerItem > items ;

	int size = env-> GetArrayLength( oa ) ;

	//printf ( "Number of items: %i\n", size ) ;

	for ( int ix = 0 ; ix < size ; ++ ix )
	{
		jobject entry = env->GetObjectArrayElement( oa, ix ) ;
		items.push_back( TimerItem( env, entry ) ) ;
	}
	//printf ( "Number of items: %i\n", items.size() ) ;
	com.setTimerItems( items ) ;
	Log::setJNIEnv( NULL ) ;
};

/*
 * Class:     dvbviewertimerimport_dvbviewer_DVBViewerCOM
 * Method:    connect
 * Signature: (Z)Z
 */
JNIEXPORT jboolean JNICALL Java_dvbviewertimerimport_dvbviewer_DVBViewerCOM_connect
  (JNIEnv * env, jclass c, jboolean f)
{
	Log::setJNIEnv( env ) ;
	return static_cast< jboolean >( com.connect( f == JNI_TRUE ) == DVBViewerCOM::OK ) ;
	Log::setJNIEnv( NULL ) ;
};

/*
 * Class:     dvbviewertimerimport_dvbviewer_DVBViewerCOM
 * Method:    disconnect
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_dvbviewertimerimport_dvbviewer_DVBViewerCOM_disconnect
  (JNIEnv * env, jclass c)
{
	Log::setJNIEnv( env ) ;
	com.disconnect() ;
	Log::setJNIEnv( NULL ) ;
};

/*
 * Class:     dvbviewertimerimport_dvbviewer_DVBViewerCOM
 * Method:    getSetupValue
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_dvbviewertimerimport_dvbviewer_DVBViewerCOM_getSetupValue
  (JNIEnv * env, jclass c , jstring jSection, jstring jName, jstring jDeflt)
{
	Log::setJNIEnv( env ) ;
	std::wstring section( Misc::convert( env, jSection ) ) ;
	std::wstring name( Misc::convert( env, jName ) ) ;
	std::wstring deflt( Misc::convert( env, jDeflt ) ) ;

	std::wstring result = com.getSetupValue( section, name, deflt ) ; 

	Log::setJNIEnv( NULL ) ;

	return Misc::convert(env, result); ;
};

/*
 * Class:     dvbviewertimerimport_dvbviewer_DVBViewerCOM
 * Method:    setCurrentChannel
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_dvbviewertimerimport_dvbviewer_DVBViewerCOM_setCurrentChannel
  (JNIEnv * env, jclass cls, jstring chID)
{
	Log::setJNIEnv( env ) ;
	std::wstring channelID( Misc::convert( env, chID ) ) ;

	return static_cast< jint >( com.setCurrentChannel( channelID ) ) ;
}

/*
 * Class:     dvbviewertimerimport_dvbviewer_DVBViewerCOM
 * Method:    getCurrentChannelNo
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_dvbviewertimerimport_dvbviewer_DVBViewerCOM_getCurrentChannelNo
  (JNIEnv * env, jclass cls)
{
	Log::setJNIEnv( env ) ;

	return static_cast< jint >( com.getCurrentChannelNo() ) ;
}

/*
 * Class:     dvbviewertimerimport_dvbviewer_DVBViewerCOM
 * Method:    initLog
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_dvbviewertimerimport_dvbviewer_DVBViewerCOM_initLog
  (JNIEnv * ev , jclass cls )
{
	Log::init( ev ) ;
	Log::setJNIEnv( NULL ) ;
}

/*
 * Class:     dvbviewertimerimport_dvbviewer_DVBViewerCOM
 * Method:    getVersion
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_dvbviewertimerimport_dvbviewer_DVBViewerCOM_getVersion
  (JNIEnv * env, jclass cls )
{
	return Misc::convert(env, Version::version );
}

