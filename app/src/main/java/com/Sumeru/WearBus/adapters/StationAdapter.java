package com.Sumeru.WearBus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.Sumeru.WearBus.models.Station;
import com.Sumeru.WearBus.R;
import java.util.ArrayList;
import java.util.List;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.ViewHolder> {
    private List<Station> stations;

    public void updateStations(List<Station> newStations) {
        if (newStations == null) {
            newStations = new ArrayList<>();
        }
        if (stations == null) {
            stations = new ArrayList<>(newStations);
            notifyDataSetChanged();
            return;
        }

        List<Station> finalNewStations = newStations;
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return stations.size();
            }

            @Override
            public int getNewListSize() {
                return finalNewStations.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                Station oldStation = stations.get(oldItemPosition);
                Station newStation = finalNewStations.get(newItemPosition);
                if (oldStation == null || newStation == null) return false;
                return oldStation.getName() != null && oldStation.getName().equals(newStation.getName());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Station oldStation = stations.get(oldItemPosition);
                Station newStation = finalNewStations.get(newItemPosition);
                if (oldStation == null || newStation == null) return false;
                return oldStation.getName() != null && oldStation.getName().equals(newStation.getName());
            }
        });

        stations = new ArrayList<>(newStations);
        diffResult.dispatchUpdatesTo(this);
    }

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