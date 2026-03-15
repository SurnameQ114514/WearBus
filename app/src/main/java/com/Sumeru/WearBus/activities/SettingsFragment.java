package com.Sumeru.WearBus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.Sumeru.WearBus.database.BusDatabase;
import com.Sumeru.WearBus.models.City;
import com.Sumeru.WearBus.utils.CityManager;
import com.Sumeru.WearBus.utils.DatabaseTask;
import com.Sumeru.WearBus.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.preferences,rootKey);

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
        Preference reselectCityPref = findPreference("pref_reselect_city");
        if (reselectCityPref != null) {
            updateCurrentCitySummary(reselectCityPref);
        }
    }

    private void updateCurrentCitySummary(Preference pref) {
        CityManager cityManager = new CityManager(requireContext());
        String uuid = cityManager.getCurrentCityUuid();
        if (!TextUtils.isEmpty(uuid)) {
            // 使用异步任务查询数据库
            DatabaseTask.execute(
                () -> BusDatabase.getInstance(requireContext()).cityDao().getCityByUuid(uuid),
                city -> {
                    if (city != null) {
                        pref.setSummary("当前：" + city.name + "，点击可修改");
                    } else {
                        pref.setSummary("修改当前所在城市，将影响公交线路号查询");
                    }
                }
            );
        } else {
            pref.setSummary("修改当前所在城市，将影响公交线路号查询");
        }
    }
}
