package com.Sumeru.WearBus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Sumeru.WearBus.R;
import com.Sumeru.WearBus.models.NearbyPoi;

import java.util.ArrayList;
import java.util.List;

public class NearbyStationAdapter extends RecyclerView.Adapter<NearbyStationAdapter.VH> {

    public interface OnItemClick {
        void onClick(NearbyPoi poi);
    }

    private List<NearbyPoi> data;
    private final OnItemClick onItemClick;

    public NearbyStationAdapter(List<NearbyPoi> data, OnItemClick onItemClick) {
        this.data = data != null ? data : new ArrayList<>();
        this.onItemClick = onItemClick;
    }

    public void update(List<NearbyPoi> newData) {
        this.data = newData != null ? newData : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void append(List<NearbyPoi> more) {
        if (more == null || more.isEmpty()) return;
        int start = data.size();
        data.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nearby_station, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        NearbyPoi p = data.get(position);
        holder.tvName.setText(p.getSafeName());
        holder.tvDistance.setText(p.getSafeDistance());
        holder.tvAddress.setText(p.getSafeAddress());
        holder.tvLines.setText("线路：" + p.getLineSummary(5));
        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvDistance, tvAddress, tvLines;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvLines = itemView.findViewById(R.id.tv_lines);
        }
    }
}

