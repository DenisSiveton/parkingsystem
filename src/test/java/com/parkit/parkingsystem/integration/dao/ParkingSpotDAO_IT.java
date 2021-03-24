package com.parkit.parkingsystem.integration.dao;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ParkingSpotDAO_IT {

    private static ParkingSpotDAO parkingSpotDAO;
    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static DataBasePrepareService dataBasePrepareService;


    @BeforeAll
    private static void setUp() {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() {
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    public void getNextAvailableParkingSlotForCar_shouldReturn_1() {
        //ARRANGE
        //ACT
        int parking_spot_number_available = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        //ASSERT
        assertEquals(1, parking_spot_number_available);
    }
    @Test
    public void getNextAvailableParkingSlotForBike_shouldReturn_4() {
        //ARRANGE
        //ACT
        int parking_spot_number_available = parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE);

        //ASSERT
        assertEquals(4, parking_spot_number_available);
    }

    @Test
    public void updateParking_whenVehicleParksInSlotOne_parkingSlotShouldBeNotAvailable() {
        //ARRANGE

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        //ACT

        parkingSpotDAO.updateParking(parkingSpot);
        boolean parkingSpotAvailable = isParkingSpotAvailable();

        //ASSERT

        assertFalse(parkingSpotAvailable);
    }

    private boolean isParkingSpotAvailable() {
        Connection con = null;
        boolean parkingSpotAvailable = false;
        try {
            con = parkingSpotDAO.dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement("select p.AVAILABLE from parking p where p.PARKING_NUMBER = 1");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                parkingSpotAvailable = rs.getBoolean(1);
            }
            parkingSpotDAO.dataBaseConfig.closePreparedStatement(ps);

        } catch (Exception ex) {
        } finally {
            parkingSpotDAO.dataBaseConfig.closeConnection(con);
        }
        return parkingSpotAvailable;
    }

}
