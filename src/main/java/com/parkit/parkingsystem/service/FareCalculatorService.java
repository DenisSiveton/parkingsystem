package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean isCustomerRegular){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }
        double parkingTimeDuration = calculateParkingDuration(ticket);

        applyFareRate(ticket, parkingTimeDuration);

        checkIfCustomerIsARegular(ticket, isCustomerRegular);

        roundToTheHundredth(ticket);
    }

    public void applyFareRate(Ticket ticket, double parkingTimeDuration) {
        //Verify that parking time duration is less than or equal to 30 minutes (0.5 hour)
        if(parkingTimeDuration * 60 <= 30) {
            ticket.setPrice(0.0);
        }
        else {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    ticket.setPrice(parkingTimeDuration * Fare.CAR_RATE_PER_HOUR);
                    break;
                }
                case BIKE: {
                    ticket.setPrice(parkingTimeDuration * Fare.BIKE_RATE_PER_HOUR);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown Parking Type");
            }
        }
    }

    private void roundToTheHundredth(Ticket ticket) {
        if (ticket.getPrice() > 0) {
            ticket.setPrice((double) Math.round(ticket.getPrice() * 100) / 100);
        }
    }

    private double calculateParkingDuration(Ticket ticket){
        double millisecondsToMinute = 60000;
        double inMinute = ticket.getInTime().getTime() / millisecondsToMinute;
        double outMinute = ticket.getOutTime().getTime() / millisecondsToMinute;

        //TODO: Some tests are failing here. Need to check if this logic is correct
        //TO-DO corrected and no more duration issues. Check reason below.
        //Change int into double so that decimals wouldn't be lost which are the minutes.

        return (outMinute - inMinute)/60;
    }

    public boolean checkIfCustomerIsARegular(Ticket ticket, boolean isCustomerRegular) {
        if (isCustomerRegular) {
            applyRegularCustomerDiscount(ticket);
        }
        return isCustomerRegular;
    }

    private void applyRegularCustomerDiscount(Ticket ticket) {
        ticket.setPrice(ticket.getPrice() * 0.95);
    }
}