package com.parkit.parkingsystem.model;

import java.util.Calendar;
import java.util.Date;

public class Ticket {
    private int id;
    private ParkingSpot parkingSpot;
    private String vehicleRegNumber;
    private double price;
    private Date inTime;
    private Date outTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    public void setParkingSpot(ParkingSpot parkingSpot) {
        this.parkingSpot = parkingSpot;
    }

    public String getVehicleRegNumber() {
        return vehicleRegNumber;
    }

    public void setVehicleRegNumber(String vehicleRegNumber) {
        this.vehicleRegNumber = vehicleRegNumber;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getInTime() {
        Date dateInTimeClone = this.inTime;
        return dateInTimeClone;
    }

    public void setInTime(Date inTime) {
        Date dateInTimeClone = inTime;
        this.inTime = dateInTimeClone;
    }

    public Date getOutTime() {
        Date dateOutTimeClone = this.outTime;
        return dateOutTimeClone;
    }

    public void setOutTime(Date outTime) {
        Date dateOutTimeClone = outTime;
        this.outTime = dateOutTimeClone;
    }
}
