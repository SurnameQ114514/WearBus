package com.Sumeru.WearBus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Sumeru.WearBus.R;
import com.Sumeru.WearBus.models.RoutePlanResponse;

import java.util.ArrayList;
import java.util.List;

/** 单条路线内的分段列表（每一段公交/地铁/步行） */
public class RouteStepAdapter extends RecyclerView.Adapter<RouteStepAdapter.VH> {

    private final List<RoutePlanResponse.Segment> segments = new ArrayList<>();

    public void setSegments(List<RoutePlanResponse.Segment> newSegments) {
        segments.clear();
        if (newSegments != null) {
            segments.addAll(newSegments);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_step, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        RoutePlanResponse.Segment seg = segments.get(position);
        String startName = seg.stationStart != null && seg.stationStart.name != null
                ? seg.stationStart.name : "";
        String endName = seg.stationEnd != null && seg.stationEnd.name != null
                ? seg.stationEnd.name : "";

        StringBuilder sb = new StringBuilder();
        if (!startName.isEmpty() || !endName.isEmpty()) {
            if (!startName.isEmpty()) {
                sb.append(startName);
            } else {
                sb.append("起点");
            }
            sb.append(" → ");
            if (!endName.isEmpty()) {
                sb.append(endName);
            } else {
                sb.append("终点");
            }
        } else {
            sb.append("行驶一段");
        }
        if (seg.segmentTimes > 0) {
            sb.append("（约").append(seg.segmentTimes).append("分钟）");
        }

        holder.tvInstruction.setText(sb.toString());
    }

    @Override
    public int getItemCount() {
        return segments.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvInstruction;

        VH(View itemView) {
            super(itemView);
            tvInstruction = itemView.findViewById(R.id.tv_instruction);
        }
    }
}

