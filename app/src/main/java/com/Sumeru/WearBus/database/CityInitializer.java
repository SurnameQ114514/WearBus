package com.Sumeru.WearBus.database;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Sumeru.WearBus.models.City;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 从 assets/cities.json 初始化城市表（cities）。
 *
 * JSON 格式示例：
 * [
 *   {
 *     "uuid": "beijing",
 *     "name": "北京市",
 *     "latitude": 39.910924547299565,
 *     "longitude": 116.4133836971231
 *   },
 *   {
 *     "uuid": "hangzhou",
 *     "name": "杭州市",
 *     "latitude": 30.25308298169347,
 *     "longitude": 120.21551180372168
 *   }
 * ]
 *
 * 注意：
 * - uuid 建议使用你刚才说的“常用叫法拼音”，例如 daxinganling、liangshan 等。
 * - uuid 只在本地用作主键和与 bus_lines.json 的 cityUuid 对应，格式可以自主约定。
 */
public class CityInitializer {

    private static final String TAG = "CityInitializer";
    private static final String ASSET_FILE_NAME = "cities.json";

    public static void initIfNeeded(Context context) {
        try {
            String json = readFromAssets(context, ASSET_FILE_NAME);
            if (json == null || json.isEmpty()) {
                Log.w(TAG, "cities.json is empty or missing.");
                return;
            }

            JSONArray array = new JSONArray(json);
            List<City> cityList = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String uuid = obj.optString("uuid");
                String name = obj.optString("name");
                double latitude = obj.optDouble("latitude", 0);
                double longitude = obj.optDouble("longitude", 0);

                if (uuid.isEmpty() || name.isEmpty()) {
                    Log.w(TAG, "Skip invalid city item at index " + i);
                    continue;
                }

                City city = new City();
                city.uuid = uuid;
                city.name = name;
                city.latitude = latitude;
                city.longitude = longitude;
                cityList.add(city);
            }

            if (!cityList.isEmpty()) {
                BusDatabase.getInstance(context)
                        .cityDao()
                        .insertAll(cityList);
                Log.i(TAG, "Cities initialized from assets, count=" + cityList.size());
            } else {
                Log.w(TAG, "No valid city items found in cities.json");
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Failed to initialize cities from assets", e);
        }
    }

    private static String readFromAssets(Context context, String fileName) throws IOException {
        InputStream is = context.getAssets().open(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        is.close();
        return sb.toString();
    }
}

