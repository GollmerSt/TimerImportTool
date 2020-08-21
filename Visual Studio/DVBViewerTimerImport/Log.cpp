// $LastChangedDate: 2010-05-06 15:55:05 +0200 (Do, 06. Mai 2010) $
// $LastChangedRevision: 14 $
// $LastChangedBy: Stefan Gollmer $

#include "StdAfx.h"
#include "Log.h"
#include "Misc.h"

#include <iostream>

jclass Log::logClass = NULL ;
jmethodID Log::outID = NULL ;
JNIEnv * Log::env    = NULL ;

void Log::init( JNIEnv * ev )
{
	logClass = ev->FindClass( "dvbviewertimerimport/misc/Log" ) ;
	if ( logClass == NULL )
	{
		std::wcout << "Class Log not found" << std::endl ;
		return ;
	}
	outID = ev->GetStaticMethodID( logClass, "out", "(ZZLjava/lang/String;Z)Ljava/lang/String;" ) ;
	if ( outID == NULL )
	{
		std::wcout << "Method Log.out not found" << std::endl ;
		return ;
	}
    env = ev ;
	//Log::log( L"Initialisation of Log successfull", true ) ;
}
void Log::log( bool verbose, bool error, const std::wstring & out, bool toDisplay )
{
	if ( env == NULL || logClass == NULL || outID == NULL )
		std::wcout << out << std::endl ;
	else
		env->CallStaticVoidMethod(
					logClass,
					outID,
					Misc::toJBoolean( verbose ),
					Misc::toJBoolean( error ),
					Misc::convert( env, out ),
					Misc::toJBoolean( toDisplay) );
}
void Log::log( bool verbose, const std::wstring & out )
{
	Log::log( verbose, false, out, false ) ;
}
void Log::log( const std::wstring & out, bool toDisplay )
{
	Log::log( false, false, out, toDisplay ) ;
}
void Log::error( const std::wstring & out )
{
	Log::log( false, true, out, false ) ;
}

void Log::setJNIEnv( JNIEnv * ev )
{
	env = ev ;
	if ( ev == NULL )
		return ;
	//Log::init( ev ) ;
} ;
