package com.example.taller3;

public class User {

    private String name;
    private String lastname;
    private String email;
    private String pasword;
    private String urlProfilePicture;
    private long cc;
    private double lalitude;
    private double longitude;
    private boolean availability;

    public User() {
    }

    public User(String name, String lastname, String email, String pasword, String urlProfilePicture, long cc, double lalitude, double longitude, boolean avail) {
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.pasword = pasword;
        this.urlProfilePicture = urlProfilePicture;
        this.cc = cc;
        this.lalitude = lalitude;
        this.longitude = longitude;
        this.availability = avail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasword() {
        return pasword;
    }

    public void setPasword(String pasword) {
        this.pasword = pasword;
    }

    public String getUrlProfilePicture() {
        return urlProfilePicture;
    }

    public void setUrlProfilePicture(String urlProfilePicture) {
        this.urlProfilePicture = urlProfilePicture;
    }

    public long getCc() {
        return cc;
    }

    public void setCc(long cc) {
        this.cc = cc;
    }

    public double getLalitude() {
        return lalitude;
    }

    public void setLalitude(double lalitude) {
        this.lalitude = lalitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean getAvail() {
        return availability;
    }

    public void setAvail(boolean avail) {
        this.availability = avail;
    }
}
