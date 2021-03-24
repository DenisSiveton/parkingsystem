package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingService_IT {

    private static final String TEST_REG_NUMBER = "ABCDEF";

    private static ParkingService parkingService;
    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static DataBasePrepareService dataBasePrepareService;
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp(){
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() {
        try {

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            dataBasePrepareService.clearDataBaseEntries();

        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void getNextParkingNumberIfAvailable_ForCar_ShouldReturnParkingSpotNumberOneWithCorrectType(){

        //ARRANGE
        when(inputReaderUtil.readSelection()).thenReturn(1);

        //ACT
        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();
        //ASSERT
        assertEquals(result.getParkingType(), ParkingType.CAR);
        assertEquals(result.getId(),1);
    }
    @Test
    public void getNextParkingNumberIfAvailable_ForBike_ShouldReturnParkingSpotNumberFourWithCorrectType(){

        //ARRANGE
        when(inputReaderUtil.readSelection()).thenReturn(2);

        //ACT
        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();
        //ASSERT
        assertEquals(result.getParkingType(), ParkingType.BIKE);
        assertEquals(result.getId(),4);
    }

    @Test
    public void processIncomingVehicle_ForACar_ShouldReturnCorrectNumberOfTicketWithCorrectRegulationVehicleNumber() throws Exception {
        //ARRANGE
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(TEST_REG_NUMBER);

        //ACT
        parkingService.processIncomingVehicle();
        int numberOfTicket = getNumberOfTicketWithSpecifiedRegulationVehicleNumber(TEST_REG_NUMBER);
        boolean isParkingSpotAvailable = getParkingSpotAvailabilityWithRegulationVehicleNumber(TEST_REG_NUMBER);
        //ASSERT

        assertEquals(1, numberOfTicket);
        assertFalse(isParkingSpotAvailable);
        //TODO: check that a ticket is actually saved in DB and Parking table is updated with availability
    }

    @Test
    public void processExitingVehicle_ForACar_ShouldReturnCorrectNumberOfCompleteTicketWithCorrectRegulationVehicleNumberAndAvailableParkingSpot() throws Exception {
        //ARRANGE
        //Firstly : Create fake ticket and update parking accordingly.
        createFictiveTicketWithRegulationVehicleNumber(TEST_REG_NUMBER);
        //Secondly :  Exit the Car in ParkingSpot 1
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(TEST_REG_NUMBER);

        // ACT
        parkingService.processExitingVehicle();
        int numberOfTicketComplete = getNumberOfTicketCompleteWithSpecifiedRegulationVehicleNumber(TEST_REG_NUMBER);
        boolean isParkingSpotAvailable = getParkingSpotAvailabilityWithRegulationVehicleNumber(TEST_REG_NUMBER);

        //ASSERT
        assertEquals(1, numberOfTicketComplete);
        assertTrue(isParkingSpotAvailable);
        //TODO: check that the fare generated and out time are populated correctly in the database
    }

    private boolean getParkingSpotAvailabilityWithRegulationVehicleNumber(String testRegNumber) {
        Connection con = null;
        boolean parkingSpotAvailable=true;
        try {
            con = parkingSpotDAO.dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement("select p.AVAILABLE from ticket t,parking p where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=?");
            ps.setString(1, testRegNumber);
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

    private int getNumberOfTicketWithSpecifiedRegulationVehicleNumber(String testRegNumber) {
        Connection con = null;
        int numberOfTicket = 0;
        try {
            con = parkingSpotDAO.dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement("select * from ticket t where t.VEHICLE_REG_NUMBER = ?");
            ps.setString(1, testRegNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ++numberOfTicket;
            }
            parkingSpotDAO.dataBaseConfig.closePreparedStatement(ps);

        } catch (Exception ex) {
        } finally {
            parkingSpotDAO.dataBaseConfig.closeConnection(con);
        }
        return numberOfTicket;
    }

    private void createFictiveTicketWithRegulationVehicleNumber(String testRegNumber) {
        Ticket fictiveTicket = initiateFictiveTicket(testRegNumber);
        ticketDAO.saveTicket(fictiveTicket);
        parkingSpotDAO.updateParking(fictiveTicket.getParkingSpot());
    }

    private Ticket initiateFictiveTicket(String testRegNumber) {
        ParkingSpot fictiveParkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));

        Ticket fictiveTicket = new Ticket();
        fictiveTicket.setVehicleRegNumber(testRegNumber);
        fictiveTicket.setPrice(0);
        fictiveTicket.setInTime(inTime);
        fictiveTicket.setOutTime(null);
        fictiveTicket.setParkingSpot(fictiveParkingSpot);

        return fictiveTicket;
    }

    private int getNumberOfTicketCompleteWithSpecifiedRegulationVehicleNumber(String testRegNumber) {
        Connection con = null;
        int numberOfTicket = 0;
        try {
            con = parkingSpotDAO.dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement("select * from ticket t where t.OUT_TIME IS NOT NULL and t.VEHICLE_REG_NUMBER = ?");
            ps.setString(1, testRegNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ++numberOfTicket;
            }
            parkingSpotDAO.dataBaseConfig.closePreparedStatement(ps);

        } catch (Exception ex) {
        } finally {
            parkingSpotDAO.dataBaseConfig.closeConnection(con);
        }
        return numberOfTicket;
    }
}
