package com.codeshot.carscustomerapp.Models;

import com.firebase.geofire.GeoFire;

import java.io.Serializable;

public class User implements Serializable {
    String userName,email,phoneNumber,gender;
    private GeoFire geoFire;
    public User() {
    }

    public User(String userName, String email, String phoneNumber, String gender) {
        this.userName = userName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public GeoFire getGeoFire() {
        return geoFire;
    }

    public void setGeoFire(GeoFire geoFire) {
        this.geoFire = geoFire;
    }
}
