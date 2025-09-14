package com.Sumeru.WearBus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.ViewHolder> {
    private final List<Station> stations;

    public StationAdapter(List<Station> stations) {
        this.stations = stations != null ? stations : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_station, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Station station = stations.get(position);
        holder.stationName.setText(station.getName());
        holder.stationOrder.setText(String.valueOf(position + 1));
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView stationName;
        TextView stationOrder;

        ViewHolder(View itemView) {
            super(itemView);
            stationName = itemView.findViewById(R.id.tv_station_name);
            stationOrder = itemView.findViewById(R.id.tv_station_order);
        }
    }
}