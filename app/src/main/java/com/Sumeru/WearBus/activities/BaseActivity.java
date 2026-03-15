package com.Sumeru.WearBus.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.Sumeru.WearBus.utils.SecureKeyManager;

/**
 * BaseActivity - 所有Activity的基类
 * 提供公共功能：API密钥获取等
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 获取API开发ID
     * 从安全存储中获取，避免硬编码
     */
    protected String getDevId() {
        return SecureKeyManager.getApiDevId(this);
    }

    /**
     * 获取API开发密钥
     * 从安全存储中获取，避免硬编码
     */
    protected String getDevKey() {
        return SecureKeyManager.getApiDevKey(this);
    }
}
