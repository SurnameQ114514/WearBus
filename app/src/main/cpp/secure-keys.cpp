#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "SecureKeys"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

// 简单的 XOR 加密密钥（增加一点破解难度）
static const char XOR_KEY = 0x5A;

// 每个字符与 XOR_KEY 异或
static const unsigned char ENCRYPTED_API_ID[] = {
    '1' ^ XOR_KEY, '0' ^ XOR_KEY, '0' ^ XOR_KEY, '0' ^ XOR_KEY,
    '8' ^ XOR_KEY, '0' ^ XOR_KEY, '9' ^ XOR_KEY, '7' ^ XOR_KEY,
    '\0'
};

static const unsigned char ENCRYPTED_API_KEY[] = {
    '3' ^ XOR_KEY, 'a' ^ XOR_KEY, '2' ^ XOR_KEY, '6' ^ XOR_KEY,
    'b' ^ XOR_KEY, '8' ^ XOR_KEY, '4' ^ XOR_KEY, '0' ^ XOR_KEY,
    '6' ^ XOR_KEY, '0' ^ XOR_KEY, 'a' ^ XOR_KEY, '6' ^ XOR_KEY,
    'e' ^ XOR_KEY, '0' ^ XOR_KEY, '8' ^ XOR_KEY, '2' ^ XOR_KEY,
    '9' ^ XOR_KEY, '0' ^ XOR_KEY, 'e' ^ XOR_KEY, '0' ^ XOR_KEY,
    '4' ^ XOR_KEY, '4' ^ XOR_KEY, '1' ^ XOR_KEY, '0' ^ XOR_KEY,
    '2' ^ XOR_KEY, '4' ^ XOR_KEY, '6' ^ XOR_KEY, 'c' ^ XOR_KEY,
    '4' ^ XOR_KEY, 'f' ^ XOR_KEY, '9' ^ XOR_KEY, '5' ^ XOR_KEY,
    '\0'
};

// 解密函数
std::string decrypt(const unsigned char* encrypted) {
    std::string result;
    for (int i = 0; encrypted[i] != '\0'; i++) {
        result += (encrypted[i] ^ XOR_KEY);
    }
    return result;
}

extern "C" {

// 获取 API ID
JNIEXPORT jstring JNICALL
Java_com_Sumeru_WearBus_utils_NativeKeyProvider_getApiDevId(JNIEnv* env, jobject /* this */) {
    std::string apiId = decrypt(ENCRYPTED_API_ID);
    LOGD("API ID 已解密");
    return env->NewStringUTF(apiId.c_str());
}

// 获取 API Key
JNIEXPORT jstring JNICALL
Java_com_Sumeru_WearBus_utils_NativeKeyProvider_getApiDevKey(JNIEnv* env, jobject /* this */) {
    std::string apiKey = decrypt(ENCRYPTED_API_KEY);
    LOGD("API Key 已解密");
    return env->NewStringUTF(apiKey.c_str());
}

}