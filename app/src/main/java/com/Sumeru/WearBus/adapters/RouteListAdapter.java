package com.Sumeru.WearBus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Sumeru.WearBus.R;
import com.Sumeru.WearBus.models.RoutePlanResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.text.SimpleDateFormat;

/** 公交规划结果：多条方案列表（apihz gongjiao.php） */
public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.VH> {

    private List<RoutePlanResponse.RoutePlan> routes = new ArrayList<>();

    public void setRoutes(List<RoutePlanResponse.RoutePlan> list) {
        routes = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        RoutePlanResponse.RoutePlan route = routes.get(position);
        int index = position + 1;
        holder.tvRouteIndex.setText("方案" + index);

        int totalMinutes = 0;
        int segmentCount = 0;
        if (route.segments != null) {
            segmentCount = route.segments.size();
            for (RoutePlanResponse.Segment seg : route.segments) {
                if (seg != null && seg.segmentTimes > 0) {
                    totalMinutes += seg.segmentTimes;
                }
            }
        }

        holder.tvDuration.setText("约" + totalMinutes + "分钟");

        // 计算预计到达时间
        String estimatedArrivalTime = calculateEstimatedArrivalTime(totalMinutes);
        holder.tvArrive.setText(estimatedArrivalTime);

        String lineName = route.lineName != null ? route.lineName : "";
        if (!lineName.isEmpty()) {
            holder.tvDistance.setText(lineName);
        } else {
            holder.tvDistance.setText("共" + segmentCount + "段");
        }

        // 显示到达时间，隐藏票价
        holder.tvArrive.setVisibility(View.VISIBLE);
        holder.tvPrice.setVisibility(View.GONE);

        holder.stepAdapter.setSegments(route.segments);
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    /**
     * 计算预计到达时间
     * @param totalMinutes 预计总时间（分钟）
     * @return 格式化的预计到达时间
     */
    private String calculateEstimatedArrivalTime(int totalMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, totalMinutes);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(calendar.getTime());
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvRouteIndex, tvDuration, tvDistance, tvArrive, tvPrice;
        RecyclerView rvSteps;
        RouteStepAdapter stepAdapter;

        VH(View itemView) {
            super(itemView);
            tvRouteIndex = itemView.findViewById(R.id.tv_route_index);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            tvArrive = itemView.findViewById(R.id.tv_arrive);
            tvPrice = itemView.findViewById(R.id.tv_price);
            rvSteps = itemView.findViewById(R.id.rv_steps);
            rvSteps.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            rvSteps.setNestedScrollingEnabled(false);
            stepAdapter = new RouteStepAdapter();
            rvSteps.setAdapter(stepAdapter);
        }
    }
}

