package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;


public class FareCalculatorServiceTest {

    private Ticket ticket;

    private static FareCalculatorService fareCalculatorService;


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
        //ARRANGE
        testInit(60, ParkingType.CAR);
        double parkingTimeDuration = 1.0;
        //ACT
        fareCalculatorService.applyFareRate(ticket, parkingTimeDuration);
        //ASSERT
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
    }

    @Test
    public void calculateFare_whenABikeIsParkedForAnHour_ShouldReturnBikeRatePerHour(){
        testInit(60, ParkingType.BIKE);
        double parkingTimeDuration = 1.0;
        //ACT
        fareCalculatorService.applyFareRate(ticket, parkingTimeDuration);
        //ASSERT
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
    }

    @Test
    public void applyFareRate_whenUnknownVehicleType_ShouldReturnAnException(){
        //ARRANGE
        testInit(60, null);
        double parkingTimeDuration = 1.0;
        //ACT & ASSERT
        assertThrows(NullPointerException.class, () -> fareCalculatorService.applyFareRate(ticket, parkingTimeDuration));
    }

    @Test
    public void calculateFare_whenVehicleInTimeIsLaterThanOutTime_ShouldReturnAnException(){
        //ARRANGE
        testInit(-60, ParkingType.BIKE);
        //ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket, false));
    }

    @Test
    public void applyFareRate_whenBikeIsParkedForLessThanOneHour_ShouldReturnProportionalFareParkingTime(){
        //ARRANGE
        testInit(45, ParkingType.BIKE);
        double parkingTimeDuration = 0.75;
        //ACT
        fareCalculatorService.applyFareRate(ticket, parkingTimeDuration);
        //ASSERT
        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice() );
    }

    @Test
    public void applyFareRate_whenCarIsParkedForLessThanOneHour_ShouldReturnProportionalFareParkingTime(){
        //ARRANGE
        testInit(45, ParkingType.CAR);
        double parkingTimeDuration = 0.75;
        //ACT
        fareCalculatorService.applyFareRate(ticket, parkingTimeDuration);
        //ASSERT
        assertEquals( (0.75 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @Test
    public void applyFareRate_whenCarIsParkedForADay_ShouldReturnProportionalFareParkingTime(){
        //ARRANGE
        testInit(24*60, ParkingType.CAR);
        double parkingTimeDuration = 24;
        //ACT
        fareCalculatorService.applyFareRate(ticket, parkingTimeDuration);
        //ASSERT
        assertEquals( (24 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @Test
    public void applyFareRate_whenCarIsParkedForThirtyMinutes_ShouldReturnFreeFare(){
        //ARRANGE
        testInit(30, ParkingType.CAR);
        double parkingTimeDuration = 0.5;
        //ACT
        fareCalculatorService.applyFareRate(ticket, parkingTimeDuration);
        //ASSERT
        assertEquals((0 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @Test
    public void applyFareRate_whenCarIsParkedForMoreThanThirtyMinutes_ShouldReturnProportionalFareParkingTime(){
        //ARRANGE
        testInit(36, ParkingType.CAR);
        double parkingTimeDuration = 0.6;
        //ACT
        fareCalculatorService.applyFareRate(ticket, parkingTimeDuration);
        //ASSERT
        assertEquals((0.6 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice() );
    }

    @Test
    public void applyFareRate_whenBikeIsParkedForThirtyMinutes_ShouldReturnFreeFare(){
        //ARRANGE
        testInit(30, ParkingType.BIKE);
        double parkingTimeDuration = 0.5;
        //ACT
        fareCalculatorService.applyFareRate(ticket, parkingTimeDuration);
        //ASSERT
        assertEquals((0 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice() );
    }

    @Test
    public void applyFareRate_whenBikeIsParkedForMoreThanThirtyMinutes_ShouldReturnProportionalFareParkingTime(){
        //ARRANGE
        testInit(36, ParkingType.BIKE);
        double parkingTimeDuration = 0.6;
        //ACT
        fareCalculatorService.applyFareRate(ticket, parkingTimeDuration);
        //ASSERT
        assertEquals((0.6 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void applyFareRate_whenVehicleTypeIsNotCorrect_ShouldReturnAnException(){
        //ARRANGE
        testInit(60, ParkingType.OTHER);
        double parkingTimeDuration = 3600*1000;
        //ACT
        Executable executable = () -> fareCalculatorService.applyFareRate(ticket, parkingTimeDuration);
        //ASSERT
        assertThrows(IllegalArgumentException.class, executable);
    }

    @Test
    public void checkIfCustomerIsARegular_whenCustomerIsNotRegular_ShouldReturnFalse(){
        //ARRANGE
        testInit(60, ParkingType.CAR);
        //ACT
        boolean result = fareCalculatorService.checkIfCustomerIsARegular(ticket, false);
        //ASSERT
        assertEquals(false, result);
    }

    @Test
    public void checkIfCustomerIsARegular_whenCustomerIsRegular_ShouldReturnTrue(){
        //ARRANGE
        testInit(60, ParkingType.CAR);
        //ACT
        boolean result = fareCalculatorService.checkIfCustomerIsARegular(ticket, true);
        //ASSERT
        assertEquals(true, result);
    }
}
