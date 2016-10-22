package caden.foodapp;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Jesse on 10/22/2016.
 */

public class Pin {

    private LatLng loc;
    private String desc;

    public Pin(LatLng loc, String desc) {
        this.loc = loc;
        this.desc = desc;
    }

    public LatLng getLocation() {
        return loc;
    }

    public void setLocation(LatLng loc) {
        this.loc = loc;
    }

    public String getDescription() {
        return desc;
    }

    public void setDescription(String desc) {
        this.desc = desc;
    }
}

