// $LastChangedDate: 2011-06-04 10:00:25 +0200 (Sa, 04. Jun 2011) $
// $LastChangedRevision: 26 $
// $LastChangedBy: Stefan Gollmer $

#include "StdAfx.h"
#include "Misc.h"

Misc::Misc(void)
{
}

Misc::~Misc(void)
{
}


jstring Misc::convert( JNIEnv * env, std::wstring s )
{
//	printf( "fromString = %s Länge= %i\n", s.c_str(), s.length() ) ;
	return env->NewString((const jchar *)s.c_str(), static_cast< jsize >( s.length() ) ); ;
}
std::wstring Misc::convert( JNIEnv * env, jstring s )
{
	int len = env->GetStringLength( s ) ;
	wchar_t * outbuf = new wchar_t[ len ] ;
	env->GetStringRegion( s , 0, len, (jchar *)outbuf) ;
	std::wstring result( outbuf, len ) ;
	delete [] outbuf ;
//	printf( "toString = %s Länge= %i\n", result.c_str(), result.length() ) ;
	return result ;
}
