package com.Sumeru.WearBus.utils;

import android.util.Log;

/**
 * Native 密钥提供者
 * 使用 JNI 从 C++ 代码获取 API 密钥
 * 比 Java 代码更难反编译
 */
public class NativeKeyProvider {
    
    private static final String TAG = "NativeKeyProvider";
    private static boolean isLibraryLoaded = false;
    
    // 加载本地库
    static {
        try {
            System.loadLibrary("secure-keys");
            isLibraryLoaded = true;
            Log.d(TAG, "Native library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load native library", e);
            isLibraryLoaded = false;
        }
    }
    
    /**
     * 检查本地库是否加载成功
     */
    public static boolean isNativeLibraryAvailable() {
        return isLibraryLoaded;
    }
    
    /**
     * 获取 API Dev ID（从 Native 代码）
     */
    public static native String getApiDevId();
    
    /**
     * 获取 API Dev Key（从 Native 代码）
     */
    public static native String getApiDevKey();
}