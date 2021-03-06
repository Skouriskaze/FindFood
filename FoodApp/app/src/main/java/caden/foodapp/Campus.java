package caden.foodapp;

/**
 * Created by Jesse on 10/22/2016.
 */

public class Campus {
    private String name;
    private String address;

    public Campus() {

    }

    public Campus(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return this.name + ": " + this.address;
    }
}
