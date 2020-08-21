// $LastChangedDate: 2010-05-01 08:01:55 +0200 (Sa, 01. Mai 2010) $
// $LastChangedRevision: 5 $
// $LastChangedBy: Stefan Gollmer $

#pragma once

#include <jni.h>
#include <string>

class Log
{
private :
	static jclass logClass ;
	static jmethodID outID ;
	static JNIEnv * env ;

public :
	static void init( JNIEnv * ev ) ;

	static void log( bool verbose, bool error, const std::wstring & out, bool toDisplay ) ;
	static void log( bool verbose, const std::wstring & out ) ;
	static void log( const std::wstring & out, bool toDisplay ) ;
	static void error( const std::wstring &out ) ;
	static void setJNIEnv( JNIEnv * ev ) ;
}
;
