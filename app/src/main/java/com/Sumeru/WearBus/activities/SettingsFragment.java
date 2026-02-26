package com.Sumeru.WearBus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.Sumeru.WearBus.database.BusDatabase;
import com.Sumeru.WearBus.models.City;
import com.Sumeru.WearBus.utils.CityManager;
import com.Sumeru.WearBus.R;
public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.preferences,rootKey);

        // “重新选择城市”入口，并显示当前所在城市
        Preference reselectCityPref = findPreference("pref_reselect_city");
        if (reselectCityPref != null) {
            updateCurrentCitySummary(reselectCityPref);
            reselectCityPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(requireContext(), SelectCityActivity.class);
                startActivity(intent);
                return true;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 从选择城市页返回时刷新当前城市显示
        Preference reselectCityPref = findPreference("pref_reselect_city");
        if (reselectCityPref != null) {
            updateCurrentCitySummary(reselectCityPref);
        }
    }

    private void updateCurrentCitySummary(Preference pref) {
        CityManager cityManager = new CityManager(requireContext());
        String uuid = cityManager.getCurrentCityUuid();
        if (!TextUtils.isEmpty(uuid)) {
            City city = BusDatabase.getInstance(requireContext()).cityDao().getCityByUuid(uuid);
            if (city != null) {
                pref.setSummary("当前：" + city.name + "，点击可修改");
                return;
            }
        }
        pref.setSummary("修改当前所在城市，将影响公交线路号查询");
    }
} 