package com.Sumeru.WearBus;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Station implements Parcelable {
    @SerializedName("name")
    private String name;

    @SerializedName("uuid")
    private String uuid;

    @SerializedName("lat")
    private double lat;

    @SerializedName("lng")
    private double lng;

    protected Station(Parcel in) {
        name = in.readString();
        uuid = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(uuid);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Station> CREATOR = new Creator<Station>() {
        @Override
        public Station createFromParcel(Parcel in) {
            return new Station(in);
        }

        @Override
        public Station[] newArray(int size) {
            return new Station[size];
        }
    };

    // Getters with null safety
    public String getName() {
        return name != null ? name : "未知站点";
    }

    public String getUuid() {
        return uuid != null ? uuid : "";
    }

    public double getLat() { return lat; }
    public double getLng() { return lng; }
}