/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_migrsoft_nbk_NbkCore */

#ifndef _Included_com_migrsoft_nbk_NbkCore
#define _Included_com_migrsoft_nbk_NbkCore
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_migrsoft_nbk_NbkCore
 * Method:    nbkCoreInit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_migrsoft_nbk_NbkCore_nbkCoreInit
  (JNIEnv *, jobject);

/*
 * Class:     com_migrsoft_nbk_NbkCore
 * Method:    nbkCoreDestroy
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_migrsoft_nbk_NbkCore_nbkCoreDestroy
  (JNIEnv *, jobject);

/*
 * Class:     com_migrsoft_nbk_NbkCore
 * Method:    nbkSetPageWidth
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_migrsoft_nbk_NbkCore_nbkSetPageWidth
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_migrsoft_nbk_NbkCore
 * Method:    nbkHandleEvent
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_migrsoft_nbk_NbkCore_nbkHandleEvent
  (JNIEnv *, jobject);

/*
 * Class:     com_migrsoft_nbk_NbkCore
 * Method:    nbkHandleInputEvent
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_com_migrsoft_nbk_NbkCore_nbkHandleInputEvent
  (JNIEnv *, jobject, jint, jint, jint, jint);

/*
 * Class:     com_migrsoft_nbk_NbkCore
 * Method:    nbkOnTimer
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_migrsoft_nbk_NbkCore_nbkOnTimer
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_migrsoft_nbk_NbkCore
 * Method:    nbkHandleCall
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_migrsoft_nbk_NbkCore_nbkHandleCall
  (JNIEnv *, jobject);

/*
 * Class:     com_migrsoft_nbk_NbkCore
 * Method:    nbkLoadUrl
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_migrsoft_nbk_NbkCore_nbkLoadUrl
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_migrsoft_nbk_NbkCore
 * Method:    nbkHistoryPrev
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_migrsoft_nbk_NbkCore_nbkHistoryPrev
  (JNIEnv *, jobject);

/*
 * Class:     com_migrsoft_nbk_NbkCore
 * Method:    nbkHistoryNext
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_migrsoft_nbk_NbkCore_nbkHistoryNext
  (JNIEnv *, jobject);

/*
 * Class:     com_migrsoft_nbk_NbkCore
 * Method:    nbkScrollTo
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_migrsoft_nbk_NbkCore_nbkScrollTo
  (JNIEnv *, jobject, jint, jint);

#ifdef __cplusplus
}
#endif
#endif
