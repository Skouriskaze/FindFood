package caden.foodapp;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Jesse on 10/22/2016.
 */

public class Pin {

    private Campus campus;
    private double latitute;
    private double longitude;
    private String desription;
    private String type;
    private List<String> tags;

    public Pin() {

    }

    public Pin(Campus campus, LatLng loc, String desc, String type, List<String> tags) {
        this.campus = campus;
        this.latitute = loc.latitude;
        this.longitude = loc.longitude;
        this.desription = desc;
        this.type = type;
        this.tags = tags;
    }

    public double getLatitute() {
        return latitute;
    }

    public void setLatitute(double latitute) {
        this.latitute = latitute;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return desription;
    }

    public void setDescription(String desc) {
        this.desription = desc;
    }

    public Campus getCampus() {
        return campus;
    }

    public void setCampus(Campus campus) {
        this.campus = campus;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}

