package com.Sumeru.WearBus.database;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Sumeru.WearBus.models.BusLineMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 从 assets/bus_lines.json 中初始化公交线路映射数据到 Room 数据库。
 *
 * JSON 格式示例：
 * [
 *   {
 *     "cityUuid": "city-uuid-1",
 *     "lineNumber": "1",
 *     "lineUuid": "line-uuid-1"
 *   },
 *   {
 *     "cityUuid": "city-uuid-1",
 *     "lineNumber": "2",
 *     "lineUuid": "line-uuid-2"
 *   }
 * ]
 */
public class BusLineInitializer {

    private static final String TAG = "BusLineInitializer";
    private static final String ASSET_FILE_NAME = "bus_lines.json";

    public static void initIfNeeded(Context context) {
        // 当前实现比较简单：每次启动尝试导入一遍，使用 REPLACE 策略，不会产生重复
        try {
            String json = readFromAssets(context, ASSET_FILE_NAME);
            if (json == null || json.isEmpty()) {
                Log.w(TAG, "bus_lines.json is empty or missing.");
                return;
            }

            JSONArray array = new JSONArray(json);
            BusLineDao dao = BusDatabase.getInstance(context).busLineDao();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String cityUuid = obj.optString("cityUuid");
                String lineNumber = obj.optString("lineNumber");
                String lineUuid = obj.optString("lineUuid");

                if (cityUuid.isEmpty() || lineNumber.isEmpty() || lineUuid.isEmpty()) {
                    Log.w(TAG, "Skip invalid item at index " + i);
                    continue;
                }

                BusLineMapping mapping = new BusLineMapping();
                mapping.cityUuid = cityUuid;
                mapping.lineNumber = lineNumber;
                mapping.lineUuid = lineUuid;
                dao.insert(mapping);
            }

            Log.i(TAG, "Bus line mappings initialized from assets, count=" + array.length());
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Failed to initialize bus line mappings", e);
        }
    }

    private static String readFromAssets(Context context, String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = context.getAssets().open(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}

