package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FareCalculatorService_IT {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    private void testInit(int i, ParkingType parkingType) {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (i * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, parkingType, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
    }

    @Test
    public void calculateFare_whenACarIsParkedForAnHour_ShouldReturnCarRatePerHour(){
        testInit(60, ParkingType.CAR);
        fareCalculatorService.calculateFare(ticket, false);
        assertEquals(1.0 * Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    public void calculateFare_whenCarIsParkedForAnHourAndClientIsRegular_ShouldReturnDiscountFareForAnHour(){
        testInit(60, ParkingType.CAR);
        fareCalculatorService.calculateFare(ticket, true);
        double fareExpected = (double) Math.round(1.0 * 0.95 * Fare.CAR_RATE_PER_HOUR*100)/100;
        assertEquals(fareExpected, ticket.getPrice());
    }

    @Test
    public void calculateFare_whenCarIsParkedForThirtyMinutes_ShouldReturnFreeFare(){
        testInit(30, ParkingType.CAR);
        fareCalculatorService.calculateFare(ticket, false);
        assertEquals((0 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

}
