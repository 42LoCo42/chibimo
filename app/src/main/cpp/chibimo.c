#define _POSIX_C_SOURCE 200112L
#include <jni.h>
#include <errno.h>
#include <netdb.h>
#include <sodium.h>
#include <string.h>
#include <syslog.h>
#include <unistd.h>

#define concat(a, b) a ## b

#define export(type, name, ...)                             \
	JNIEXPORT type JNICALL                                  \
	concat(Java_forty_two_chibimo_zeolite_ZeoliteKt_, name) \
	(JNIEnv* env, jobject obj __VA_ARGS__)

#define log(...) syslog(LOG_ALERT, __VA_ARGS__);

#define safe(cond, ...)                     \
	if(cond) {                              \
		log("%s:%d: %s: %s",                \
			__FILE__, __LINE__,             \
			__VA_ARGS__, strerror(errno)    \
		);                                  \
		return 0;                           \
	}

#define skip(cond, ...)                     \
	if(cond) {                              \
		log("%s:%d: %s: %s",                \
			__FILE__, __LINE__,             \
			__VA_ARGS__, strerror(errno)    \
		);                                  \
		close(sock);                        \
		continue;                           \
	}

typedef unsigned char zeolite_sign_pk[crypto_sign_PUBLICKEYBYTES];
typedef unsigned char zeolite_sign_sk[crypto_sign_SECRETKEYBYTES];
typedef unsigned char zeolite_eph_pk[crypto_box_PUBLICKEYBYTES];
typedef unsigned char zeolite_eph_sk[crypto_box_SECRETKEYBYTES];
typedef unsigned char zeolite_sym_k[crypto_secretstream_xchacha20poly1305_KEYBYTES];

static struct {
	zeolite_sign_pk sign_pk;
	zeolite_sign_sk sign_sk;

	int fd;
	zeolite_sign_pk other_pk;
	crypto_secretstream_xchacha20poly1305_state send_state;
	crypto_secretstream_xchacha20poly1305_state recv_state;
} state;

export(jboolean, init, ,jstring jaddr, jstring jport) {
	const char* addr = (*env)->GetStringUTFChars(env, jaddr, NULL);
	const char* port = (*env)->GetStringUTFChars(env, jport, NULL);

	safe(sodium_init() != 0, "Could not init libsodium");
	safe(crypto_sign_keypair(state.sign_pk, state.sign_sk) != 0,
		"Could not create identity");

	log("connect to %s:%s", addr, port);

	struct addrinfo hints = {0};
	hints.ai_family   = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_protocol = 0;
	struct addrinfo* results = NULL;
	safe(getaddrinfo(addr, port, &hints, &results) != 0,
		"Address lookup failed");

	for(struct addrinfo* i = results; i != NULL; i = i->ai_next) {
		int sock = socket(i->ai_family, i->ai_socktype, i->ai_protocol);
		skip(sock < 0, "Could not create socket");
		skip(connect(sock, i->ai_addr, i->ai_addrlen), "Could not connect");
		state.fd = sock;
		break;
	}
	safe(state.fd == 0, "No usable address found");
	freeaddrinfo(results);

	// exchange & check protocol
	const char*  protocol  = "zeolite1";
	const size_t protoLen  = 8;
	char other_protocol[8] = {0};
	safe(send(state.fd, protocol, protoLen, 0) != protoLen,
		"Could not send protocol");
	safe(recv(state.fd, other_protocol, sizeof(other_protocol), MSG_WAITALL)
		!= protoLen, "Could not recv protocol");
	safe(strncmp(protocol, other_protocol, protoLen) != 0,
		"Protocols differ!");

	// exchange public signing keys (client identification)
	safe(send(state.fd, state.sign_pk, sizeof(state.sign_pk), 0)
		!= sizeof(state.sign_pk), "Could not send signature public key");
	safe(recv(state.fd, state.other_pk, sizeof(state.other_pk), MSG_WAITALL)
		!= sizeof(state.other_pk), "Could not receive signature public key");

	// create, sign & exchange ephemeral keys (for shared key transfer)
	zeolite_eph_pk eph_pk;
	zeolite_eph_sk eph_sk;
	unsigned long long eph_msg_len = crypto_sign_BYTES + sizeof(eph_pk);
	unsigned char      eph_msg[eph_msg_len];

	safe(crypto_box_keypair(eph_pk, eph_sk) != 0,
		"Could not create ephemeral keypair");
	if(crypto_sign(eph_msg, NULL, eph_pk, sizeof(eph_pk), state.sign_sk) != 0,
		"Could not sign ephemeral public key");
	safe(send(state.fd, eph_msg, eph_msg_len, 0) != eph_msg_len,
		"Could not send signed ephemeral public key");

	// read & verify signed ephemeral public key
	zeolite_eph_sk other_eph_pk;

	safe(recv(state.fd, eph_msg, eph_msg_len, MSG_WAITALL) != eph_msg_len,
		"Could not receive other signed ephemeral public key");
	safe(crypto_sign_open(other_eph_pk, NULL, eph_msg, eph_msg_len, state.other_pk)
		!= 0, "Invalid signature in received ephemeral key");

	// create, encrypt & send symmetric sender key
	zeolite_sym_k  send_k;
	zeolite_sym_k  recv_k;
	unsigned char  full_sym_msg[crypto_box_NONCEBYTES
		+ crypto_box_MACBYTES + sizeof(send_k)];
	unsigned char* nonce = full_sym_msg;
	unsigned char* ciphertext = full_sym_msg + crypto_box_NONCEBYTES;

	crypto_secretstream_xchacha20poly1305_keygen(send_k);
	randombytes_buf(nonce, crypto_box_NONCEBYTES);
	safe(crypto_box_easy(ciphertext, send_k, sizeof(send_k),
		nonce, other_eph_pk, eph_sk) != 0, "Could not encrypt symmetric key");
	safe(send(state.fd, full_sym_msg, sizeof(full_sym_msg), 0)
		!= sizeof(full_sym_msg), "Could not send symmetric key");

	// receive & decrypt symmetric receiver key
	safe(recv(state.fd, full_sym_msg, sizeof(full_sym_msg), MSG_WAITALL)
		!= sizeof(full_sym_msg), "Could not receive symmetric key");
	safe(crypto_box_open_easy(recv_k,
		ciphertext, crypto_box_MACBYTES + sizeof(send_k),
		nonce, other_eph_pk, eph_sk) != 0, "Could not decrypt symmetrc key");

	// init stream states
	unsigned char header[crypto_secretstream_xchacha20poly1305_HEADERBYTES];

	safe(crypto_secretstream_xchacha20poly1305_init_push(
		&state.send_state, header, send_k) != 0, "Could not init send cipher");
	safe(send(state.fd, header, sizeof(header), 0) != sizeof(header),
		"Could not send cipher header");
	safe(recv(state.fd, header, sizeof(header), MSG_WAITALL) != sizeof(header),
		"Could not receive cipher header");
	safe(crypto_secretstream_xchacha20poly1305_init_pull(
		&state.recv_state, header, recv_k
	) != 0, "Could not init recv cipher");

	puts("init done");

	return 1;
}

export(jboolean, send, ,jstring jmsg) {
	const char* msg = (*env)->GetStringUTFChars(env, jmsg, NULL);
	uint32_t    len = (*env)->GetStringUTFLength(env, jmsg);

	uint32_t cipher_len = len + crypto_secretstream_xchacha20poly1305_ABYTES;
	unsigned char* cipher_storage = malloc(cipher_len);

	safe(send(state.fd, &len, sizeof(len), 0) != sizeof(len),
		"Could not send length");
	safe(crypto_secretstream_xchacha20poly1305_push(
		&state.send_state, cipher_storage, NULL, msg, len, NULL, 0, 0
	) != 0, "Could not encrypt data");
	safe(send(state.fd, cipher_storage, cipher_len, 0) != cipher_len,
		"Could not send data");
	free(cipher_storage);
	return 1;
}

export(jstring, recv) {
	uint32_t len = 0;
	safe(recv(state.fd, &len, sizeof(len), MSG_WAITALL) != sizeof(len),
		"Could not receive length");

	uint32_t cipher_len = len + crypto_secretstream_xchacha20poly1305_ABYTES;
	unsigned char* cipher_storage = malloc(cipher_len);
	unsigned char* msg_storage    = malloc(len + 1);
	msg_storage[len] = 0;

	safe(recv(state.fd, cipher_storage, cipher_len, MSG_WAITALL) != cipher_len,
		"Could not receive data");
	if(crypto_secretstream_xchacha20poly1305_pull(
		&state.recv_state, msg_storage, NULL,
		NULL, cipher_storage, cipher_len, NULL, 0
	) != 0) {
		free(cipher_storage);
		safe(0, "Could not decrypt data");
		return 0;
	}
	jstring ret = (*env)->NewStringUTF(env, msg_storage);
	free(msg_storage);
	free(cipher_storage);
	return ret;
}