package com.Sumeru.WearBus.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.Sumeru.WearBus.R;
import com.Sumeru.WearBus.models.NearbyPoi;
import com.Sumeru.WearBus.models.NearbyStationLine;

import java.util.ArrayList;
import java.util.List;
public class NearbyStationDetailActivity extends AppCompatActivity {

    private static final String EXTRA_POI = "extra_poi";

    public static void start(Context context, NearbyPoi poi) {
        Intent i = new Intent(context, NearbyStationDetailActivity.class);
        // 不再直接传 Serializable，避免 JsonElement 等非序列化字段导致异常
        i.putExtra("name", poi.getSafeName());
        i.putExtra("distance", poi.getSafeDistance());
        i.putExtra("address", poi.getSafeAddress());
        i.putExtra("province", poi.province);
        i.putExtra("city", poi.city);
        i.putExtra("county", poi.county);
        Double lon = poi.getLon();
        Double lat = poi.getLat();
        if (lon != null) i.putExtra("lon", lon);
        if (lat != null) i.putExtra("lat", lat);

        ArrayList<String> lineNames = new ArrayList<>();
        List<NearbyStationLine> lines = poi.getStationLines();
        for (NearbyStationLine l : lines) {
            if (l != null && !TextUtils.isEmpty(l.lineName)) {
                lineNames.add(l.lineName);
            }
        }
        i.putStringArrayListExtra("lines", lineNames);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_station_detail);

        String name = getIntent().getStringExtra("name");
        String distance = getIntent().getStringExtra("distance");
        String address = getIntent().getStringExtra("address");
        String province = getIntent().getStringExtra("province");
        String city = getIntent().getStringExtra("city");
        String county = getIntent().getStringExtra("county");
        double lon = getIntent().getDoubleExtra("lon", Double.NaN);
        double lat = getIntent().getDoubleExtra("lat", Double.NaN);
        ArrayList<String> lineNames = getIntent().getStringArrayListExtra("lines");

        TextView tvName = findViewById(R.id.tv_name);
        TextView tvDistance = findViewById(R.id.tv_distance);
        TextView tvAddress = findViewById(R.id.tv_address);
        TextView tvArea = findViewById(R.id.tv_area);
        TextView tvLines = findViewById(R.id.tv_lines);
        TextView tvCoords = findViewById(R.id.tv_coords);

        tvName.setText(!TextUtils.isEmpty(name) ? name : "未知站点");
        tvDistance.setText("距离：" + (distance != null ? distance : ""));
        tvAddress.setText("地址：" + (address != null ? address : ""));

        String area = joinNonEmpty(province, city, county);
        tvArea.setText("区域：" + (TextUtils.isEmpty(area) ? "未知" : area));

        if (lineNames == null || lineNames.isEmpty()) {
            tvLines.setText("线路：未知");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lineNames.size(); i++) {
                if (i > 0) sb.append("、");
                sb.append(lineNames.get(i));
            }
            tvLines.setText("线路：" + sb);
        }

        if (!Double.isNaN(lon) && !Double.isNaN(lat)) {
            tvCoords.setText("坐标：" + String.format("%.6f, %.6f", lat, lon));
        } else {
            tvCoords.setText("坐标：未知");
        }
    }

    private String joinNonEmpty(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.trim().isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(p.trim());
            }
        }
        return sb.toString();
    }
}

