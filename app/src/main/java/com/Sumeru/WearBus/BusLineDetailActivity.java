package com.Sumeru.WearBus;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BusLineDetailActivity extends AppCompatActivity {
    private static final String TAG = "BusLineDetail";

    private TextView tvLineName, tvCompany, tvPrice, tvInterval,
            tvTravelTime, tvOperateTime, tvSaleType,
            tvTicketRule, tvLineType, tvMonthTicket;
    private RecyclerView rvStations;
    private TextView tvEmptyStations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_line_detail);

        initViews();

        // 接收数据
        BusLineDetail detail = getIntent().getParcelableExtra("bus_detail");
        if (detail != null) {
            displayBusLineDetail(detail);
            Log.d(TAG, "成功显示线路详情");
        } else {
            Toast.makeText(this, "未能获取线路详情", Toast.LENGTH_SHORT).show();
            finish();
            Log.e(TAG, "未接收到线路详情数据");
        }
    }

    private void initViews() {
        tvLineName = findViewById(R.id.tv_line_name);
        tvCompany = findViewById(R.id.tv_company);
        tvPrice = findViewById(R.id.tv_price);
        tvInterval = findViewById(R.id.tv_interval);
        tvTravelTime = findViewById(R.id.tv_travel_time);
        tvOperateTime = findViewById(R.id.tv_operate_time);
        tvSaleType = findViewById(R.id.tv_sale_type);
        tvTicketRule = findViewById(R.id.tv_ticket_rule);
        tvLineType = findViewById(R.id.tv_line_type);
        tvMonthTicket = findViewById(R.id.tv_month_ticket);
        rvStations = findViewById(R.id.rv_stations);
        tvEmptyStations = findViewById(R.id.tv_empty_stations);

        // 修复点：设置LayoutManager
        rvStations.setLayoutManager(new LinearLayoutManager(this) {
            // 修复列表显示不全问题
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        // 禁用嵌套滚动
        rvStations.setNestedScrollingEnabled(false);
        rvStations.setHasFixedSize(false);
    }

    private void displayBusLineDetail(BusLineDetail detail) {
        // 使用安全方法获取数据
        tvLineName.setText(detail.getSafeLinename());
        tvCompany.setText(detail.getSafeCompany());
        tvPrice.setText(detail.getFormattedTicketPrice());
        tvInterval.setText("发车间隔: " + (detail.getInterval() > 0 ? detail.getInterval() + "分钟" : "未知"));
        tvTravelTime.setText("全程时间: " + (detail.getTotaltime() > 0 ? detail.getTotaltime() + "分钟" : "未知"));
        tvOperateTime.setText("运营时间: " + detail.getSafeStartTime() + " - " + detail.getSafeEndTime());
        tvSaleType.setText(detail.getFormattedSaleType());
        tvLineType.setText(detail.getFormattedLineType());

        // 票制规则显示
        switch (detail.getTicketcal()) {
            case 0:
                tvTicketRule.setText("单一票价");
                break;
            case 1:
                tvTicketRule.setText("按距离计费");
                break;
            case 2:
                tvTicketRule.setText("按站点计费");
                break;
            default:
                tvTicketRule.setText("票制规则未知");
        }

        // 月票信息显示
        tvMonthTicket.setText(detail.getIsmonticket() == 1 ? "支持月票" : "不支持月票");

        // 站点列表处理
        List<Station> stations = detail.getSafeStation();
        if (stations != null && !stations.isEmpty()) {
            StationAdapter adapter = new StationAdapter(stations);
            rvStations.setAdapter(adapter);

            // 修复点：请求布局重新计算高度
            rvStations.getAdapter().notifyDataSetChanged();
            rvStations.requestLayout();

            tvEmptyStations.setVisibility(View.GONE);
            rvStations.setVisibility(View.VISIBLE);
            Log.d(TAG, "显示站点数量: " + stations.size());
        } else {
            tvEmptyStations.setVisibility(View.VISIBLE);
            rvStations.setVisibility(View.GONE);
            Log.w(TAG, "无站点数据");
        }
    }
}