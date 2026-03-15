package com.Sumeru.WearBus.utils;

import android.content.Context;
import android.util.Log;

/**
 * 安全的密钥管理器
 * 使用 Native 代码（JNI）获取 API 密钥
 * 避免在 BuildConfig 中存储明文密钥
 */
public class SecureKeyManager {
    
    private static final String TAG = "SecureKeyManager";
    
    /**
     * 获取 API Dev ID
     * 从 Native 代码获取（避免 BuildConfig 明文存储）
     */
    public static String getApiDevId(Context context) {
        // 从 Native 代码获取密钥
        if (NativeKeyProvider.isNativeLibraryAvailable()) {
            try {
                String nativeId = NativeKeyProvider.getApiDevId();
                if (nativeId != null && !nativeId.isEmpty()) {
                    Log.d(TAG, "使用 Native 代码获取 API ID");
                    return nativeId;
                }
            } catch (Exception e) {
                Log.e(TAG, "从 Native 获取 API ID 失败", e);
            }
        }
        
        // 如果 Native 不可用，抛出异常
        throw new RuntimeException("无法获取 API ID：Native 库不可用。请确保已正确编译 NDK 库。");
    }
    
    /**
     * 获取 API Dev Key
     * 从 Native 代码获取（避免 BuildConfig 明文存储）
     */
    public static String getApiDevKey(Context context) {
        // 从 Native 代码获取密钥
        if (NativeKeyProvider.isNativeLibraryAvailable()) {
            try {
                String nativeKey = NativeKeyProvider.getApiDevKey();
                if (nativeKey != null && !nativeKey.isEmpty()) {
                    Log.d(TAG, "使用 Native 代码获取 API Key");
                    return nativeKey;
                }
            } catch (Exception e) {
                Log.e(TAG, "从 Native 获取 API Key 失败", e);
            }
        }
        
        // 如果 Native 不可用，抛出异常
        throw new RuntimeException("无法获取 API Key：Native 库不可用。请确保已正确编译 NDK 库。");
    }
    
    /**
     * 检查是否使用 Native 密钥存储
     */
    public static boolean isUsingNativeStorage() {
        return NativeKeyProvider.isNativeLibraryAvailable();
    }
}