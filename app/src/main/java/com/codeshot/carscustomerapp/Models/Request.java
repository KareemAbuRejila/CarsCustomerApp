package com.codeshot.carscustomerapp.Models;

import com.firebase.geofire.GeoFire;

import java.io.Serializable;

public class Request implements Serializable {
    private String id,from,to,time,date,status;

    private String customerId;


    public Request() {
    }

    public Request(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public Request(String from, String to, String time, String date, String status) {
        this.from = from;
        this.to = to;
        this.time = time;
        this.date = date;
        this.status = status;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
