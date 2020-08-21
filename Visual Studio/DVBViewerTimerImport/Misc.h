// $LastChangedDate: 2010-05-02 14:45:34 +0200 (So, 02. Mai 2010) $
// $LastChangedRevision: 11 $
// $LastChangedBy: Stefan Gollmer $

#pragma once

#include <string>
#include <jni.h>

class Misc
{
public:
	Misc(void);
	~Misc(void);

	static jstring convert( JNIEnv * env, std::wstring s ) ;
	static jboolean toJBoolean( bool isTrue ) { return isTrue? JNI_TRUE : JNI_FALSE ; } ;
	static std::wstring convert( JNIEnv * env, jstring s ) ;
};
