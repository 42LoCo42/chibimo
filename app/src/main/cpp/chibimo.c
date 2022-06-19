#include <jni.h>
#include <string.h>
#include <syslog.h>
#include <zeolite.h>

JNIEXPORT int JNICALL Java_forty_two_chibimo_zeolite_ZeoliteKt_zeoliteInit(
	JNIEnv* env, jobject obj
) {
	return zeolite_init();
}

JNIEXPORT jlong JNICALL Java_forty_two_chibimo_zeolite_ZeoliteKt_zeoliteCreate(
	JNIEnv* env, jobject obj
) {
	size_t size = sizeof(zeolite);
	zeolite* z = malloc(size);
	memset(z, 0, size);
	if(zeolite_create(z) != SUCCESS) {
		free(z);
		return 0;
	}
	return (jlong) z;
}

zeolite_error trustAll(zeolite_sign_pk pk) {
	return SUCCESS;
}

JNIEXPORT jlong JNICALL Java_forty_two_chibimo_zeolite_ZeoliteKt_zeoliteCreateChannel(
	JNIEnv* env, jobject obj, jlong z, int fd
) {
	size_t size = sizeof(zeolite_channel);
	zeolite_channel* c = malloc(size);
	memset(c, 0, size);
	zeolite_error e = zeolite_create_channel((zeolite*) z, c, fd, trustAll);
	if(e != SUCCESS) return e;
	return (jlong) c;
}

JNIEXPORT zeolite_error JNICALL Java_forty_two_chibimo_zeolite_ZeoliteKt_zeoliteChannelSend(
	JNIEnv* env, jobject obj, jlong c, jstring jstr
) {
	zeolite_channel* channel = c;
	const char* str = (*env)->GetStringUTFChars(env, jstr, NULL);
	const size_t len = (*env)->GetStringUTFLength(env, jstr);
	return zeolite_channel_send(c, str, len);
}

JNIEXPORT jstring JNICALL Java_forty_two_chibimo_zeolite_ZeoliteKt_zeoliteChannelRecv(
	JNIEnv* env, jobject obj, jlong c
) {
	zeolite_channel* channel = c;
	size_t len = 0;
	char* buf = NULL;
	zeolite_error e = zeolite_channel_recv(c, &buf, &len);
	syslog(LOG_CRIT, "%s %lu", zeolite_error_str(e), len);
	if(e != SUCCESS) return NULL;
	return (*env)->NewStringUTF(env, buf);
}