//#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <stdint.h>
#include <termios.h>
#include <android/log.h>
#include <sys/ioctl.h>
#include "example.h"
#include "MyClient.h"
#include "hello-uart.h"
#include <math.h>

#undef	TCSAFLUSH
#define	TCSAFLUSH	TCSETSF
#ifndef	_TERMIOS_H_
#define	_TERMIOS_H_
#endif


static const char *classPathName = "org/doubango/imsdroid/UartCmd";
#define LOG_TAG "hello"
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)


using namespace android;

extern "C"
{

	JNIEXPORT jbyteArray JNICALL Native_Combine(JNIEnv *env,jobject mc ,
			jobject nanoq, jobject encodq)
	{
		 jbyteArray beSendMsg = env->NewByteArray (13);

		 return  beSendMsg;
	}


	JNIEXPORT jint JNICALL Native_StartCal(JNIEnv *env,jobject mc)
	{
		  return 0;
	}

	JNIEXPORT jint JNICALL Native_WriteDemoData(JNIEnv *env,jobject mc, jintArray data, jint size)
	{
		  return 0;
	}

	JNIEXPORT jint JNICALL Native_OpenUart(JNIEnv *env,jobject mc, jstring s )
	{
		return 0;
	}

	JNIEXPORT jint JNICALL Native_CloseUart(JNIEnv *env,jobject mc, jint fdnum)
	{
		return 0;
	}

	JNIEXPORT jint JNICALL Native_SetUart(JNIEnv *env,jobject mc, jint fdnum, jint baudrate)
	{
		return 0;
	}

	JNIEXPORT jint JNICALL Native_SendMsgUart(JNIEnv *env,jobject mc,  jint fdnum , jbyteArray inByte)
	{
		return 0;
	}

	JNIEXPORT jstring JNICALL Native_ReceiveDW1000Uart(JNIEnv *env,jobject mc, jint fdnum)
	{
		return NULL;
	}


	JNIEXPORT jbyteArray JNICALL Native_ReceiveByteMsgUart(JNIEnv *env,jobject mc, jint fdnum)
	{
		jbyteArray array = env->NewByteArray(5);

		return array;
	}

	JNIEXPORT jint JNICALL Native_WeightSet(JNIEnv *env,jobject mc, jfloat dwWeight , jfloat encoderWeight)
	{
		return 0;
	}

	///------EKF calculation--------------------------------------------------------------------------
	JNIEXPORT jfloatArray JNICALL Native_EKF(JNIEnv *env,jobject mc,jfloat a,jfloat b,jfloat c,jint left,jint right,jint degree)
	{
		jfloatArray result;

		result = env->NewFloatArray(4);
		return result;
	}

	static JNINativeMethod gMethods[] = {
		//Java Name			(Input Arg) return arg   JNI Name
		{"ReceiveDW1000Uart",   "(I)Ljava/lang/String;",(void *)Native_ReceiveDW1000Uart},
		{"ReceiveByteMsgUart",   "(I)[B",(void *)Native_ReceiveByteMsgUart},
		{"SendMsgUart",   "(I[B)I",  (void *)Native_SendMsgUart},
		{"SetUart",   "(II)I",   					(void *)Native_SetUart},
		{"OpenUart",   "(Ljava/lang/String;)I",   	(void *)Native_OpenUart},
		{"WriteDemoData",   "([II)I",   	(void *)Native_WriteDemoData},
		{"StartCal",   "()I",   	(void *)Native_StartCal},
		{"CloseUart",   "(I)I",   	(void *)Native_CloseUart},
		{"Combine",   "(Ljava/util/ArrayList;Ljava/util/ArrayList;)[B",   	(void *)Native_Combine},
		{"EKF", "(FFFIII)[F"	,(void *)Native_EKF},
		{"WeightSet", "(FF)I"	,(void *)Native_WeightSet},
	};

	static int registerNativeMethods(JNIEnv* env, const char* className,
		JNINativeMethod* gMethods, int numMethods)
	{
		jclass clazz;
		clazz = env->FindClass(className);
		if (clazz == NULL)
		{
			LOGI("can't find className=%s  \n",className);
			return JNI_FALSE;
		}

		if (env->RegisterNatives(clazz, gMethods, numMethods) < 0)
		{
		LOGE("register nativers error");
			return JNI_FALSE;
		}

		return JNI_TRUE;
	}

	static int register_android_native_uart(JNIEnv *env){

		 if (!registerNativeMethods(env, classPathName,
				 gMethods, sizeof(gMethods) / sizeof(gMethods[0]))) {
			return JNI_FALSE;
		  }
		  return JNI_TRUE;
	}


	JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved){
		JNIEnv* env = NULL;
		jint result = -1;

		LOGI("Entering JNI_OnLoad \n");

		if (vm->GetEnv((void**)&env,JNI_VERSION_1_4) != JNI_OK)
			goto bail;

		if (!register_android_native_uart(env))
			goto bail;

		/* success -- return valid version number */
		result = JNI_VERSION_1_4;

		bail:
			LOGI("Leaving JNI_OnLoad (result=0x%x)\n", result);
			return result;
	}
}



