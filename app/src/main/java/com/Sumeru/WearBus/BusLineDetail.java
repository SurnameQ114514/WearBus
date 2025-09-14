package com.Sumeru.WearBus;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class BusLineDetail implements Parcelable {
    @SerializedName("code")
    private int code;

    @SerializedName("msg")
    private String msg;

    @SerializedName("station")
    private List<Station> station;

    @SerializedName("ticketcal")
    private int ticketcal;

    @SerializedName("ismanual")
    private int ismanual;

    @SerializedName("linetype")
    private int linetype;

    @SerializedName("totalprice")
    private int totalprice;

    @SerializedName("byuuid")
    private String byuuid;

    @SerializedName("length")
    private int length;

    @SerializedName("ismonticket")
    private int ismonticket;

    @SerializedName("endtime")
    private String endtime;

    @SerializedName("increasedstep")
    private int increasedstep;

    @SerializedName("starttime")
    private String starttime;

    @SerializedName("startprice")
    private int startprice;

    @SerializedName("increasedprice")
    private int increasedprice;

    @SerializedName("stationnum")
    private int stationnum;

    @SerializedName("totaltime")
    private int totaltime;

    @SerializedName("isbidirectional")
    private int isbidirectional;

    @SerializedName("linename")
    private String linename;

    @SerializedName("interval")
    private int interval;

    @SerializedName("company")
    private String company;

    protected BusLineDetail(Parcel in) {
        code = in.readInt();
        msg = in.readString();
        station = in.createTypedArrayList(Station.CREATOR);
        ticketcal = in.readInt();
        ismanual = in.readInt();
        linetype = in.readInt();
        totalprice = in.readInt();
        byuuid = in.readString();
        length = in.readInt();
        ismonticket = in.readInt();
        endtime = in.readString();
        increasedstep = in.readInt();
        starttime = in.readString();
        startprice = in.readInt();
        increasedprice = in.readInt();
        stationnum = in.readInt();
        totaltime = in.readInt();
        isbidirectional = in.readInt();
        linename = in.readString();
        interval = in.readInt();
        company = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(code);
        dest.writeString(msg);
        dest.writeTypedList(station);
        dest.writeInt(ticketcal);
        dest.writeInt(ismanual);
        dest.writeInt(linetype);
        dest.writeInt(totalprice);
        dest.writeString(byuuid);
        dest.writeInt(length);
        dest.writeInt(ismonticket);
        dest.writeString(endtime);
        dest.writeInt(increasedstep);
        dest.writeString(starttime);
        dest.writeInt(startprice);
        dest.writeInt(increasedprice);
        dest.writeInt(stationnum);
        dest.writeInt(totaltime);
        dest.writeInt(isbidirectional);
        dest.writeString(linename);
        dest.writeInt(interval);
        dest.writeString(company);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BusLineDetail> CREATOR = new Creator<BusLineDetail>() {
        @Override
        public BusLineDetail createFromParcel(Parcel in) {
            return new BusLineDetail(in);
        }

        @Override
        public BusLineDetail[] newArray(int size) {
            return new BusLineDetail[size];
        }
    };

    // 安全获取方法
    public List<Station> getSafeStation() {
        return station != null ? station : new ArrayList<>();
    }

    public String getFormattedTicketPrice() {
        return totalprice > 0 ? "¥" + (totalprice / 100.0) + "元" : "票价未知";
    }

    public String getFormattedLineType() {
        switch (linetype) {
            case 1: return "公交线路";
            case 2: return "地铁线路";
            case 3: return "磁悬浮";
            default: return "其他类型";
        }
    }

    public String getFormattedSaleType() {
        return ismanual == 0 ? "有人售票" : "无人售票";
    }

    public String getSafeLinename() {
        return !TextUtils.isEmpty(linename) ? linename : "未知线路";
    }

    public String getSafeCompany() {
        return !TextUtils.isEmpty(company) ? company : "未知运营公司";
    }

    public String getSafeStartTime() {
        return !TextUtils.isEmpty(starttime) ? starttime : "--:--";
    }

    public String getSafeEndTime() {
        return !TextUtils.isEmpty(endtime) ? endtime : "--:--";
    }

    // Getters
    public int getCode() { return code; }
    public String getMsg() { return msg; }
    public List<Station> getStation() { return station; }
    public int getTicketcal() { return ticketcal; }
    public int getIsmanual() { return ismanual; }
    public int getLinetype() { return linetype; }
    public int getTotalprice() { return totalprice; }
    public String getByuuid() { return byuuid; }
    public int getLength() { return length; }
    public int getIsmonticket() { return ismonticket; }
    public String getEndtime() { return endtime; }
    public int getIncreasedstep() { return increasedstep; }
    public String getStarttime() { return starttime; }
    public int getStartprice() { return startprice; }
    public int getIncreasedprice() { return increasedprice; }
    public int getStationnum() { return stationnum; }
    public int getTotaltime() { return totaltime; }
    public int getIsbidirectional() { return isbidirectional; }
    public String getLinename() { return linename; }
    public int getInterval() { return interval; }
    public String getCompany() { return company; }
}