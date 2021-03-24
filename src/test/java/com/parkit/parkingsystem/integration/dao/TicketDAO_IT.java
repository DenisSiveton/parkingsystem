package com.parkit.parkingsystem.integration.dao;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class TicketDAO_IT {

    public static final String TEST_REG_NUMBER = "ABCDEF";
    private static Ticket ticket;
    private static TicketDAO ticketDAO;
    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static DataBasePrepareService dataBasePrepareService;


    @BeforeAll
    private static void setUp() {
        ticket = new Ticket();
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() {
        dataBasePrepareService.clearDataBaseEntries();
        ticket = new Ticket();

    }
    private void initTicket(Ticket ticket) {
        Date inTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setVehicleRegNumber(TEST_REG_NUMBER);
        ticket.setParkingSpot(parkingSpot);
        ticket.setPrice(0.0);
    }
    @Test
    public void saveTicket_databaseShouldHaveOneTicketSavedWithProperRegNumber() {
        //ARRANGE
        initTicket(ticket);

        //ACT
        ticketDAO.saveTicket(ticket);
        int numberOfTickets = returnNumberOfTicket();
        String regNumberOfTicket = returnRegNumberOfTicketWithFollowingRegNumber();

        //ASSERT
        assertEquals(1, numberOfTickets);
        assertEquals(regNumberOfTicket, TEST_REG_NUMBER);
    }

    @Test
    public void getTicket_databaseShouldReturnCorrectTicketWithProperRegNumber() {
        //ARRANGE
        initTicket(ticket);
        ticketDAO.saveTicket(ticket);

        //ACT
        Ticket ticketResult = ticketDAO.getTicket(TEST_REG_NUMBER);

        //ASSERT
        assertEquals(TEST_REG_NUMBER, ticketResult.getVehicleRegNumber());
    }

    @Test
    public void updateTicket_databaseShouldReturnOutTimeOfTicketWithValueNotNull() {
        //ARRANGE
        initTicket(ticket);
        ticketDAO.saveTicket(ticket);
        ticket = ticketDAO.getTicket(TEST_REG_NUMBER);
        Date outTime = new Date();
        ticket.setOutTime(outTime);

        //ACT
        ticketDAO.updateTicket(ticket);
        Date result = returnOutTimeOfTicketWithFollowingRegNumber();
        //ASSERT
        assertNotNull(result);
    }

    @Test
    public void checkIfCustomerHasHistory_CaseWhereCustomerIsRegular_ShouldReturnTrue(){
        //ARRANGE
        //Create first ticket with REG_NUMBER = "ABCDEF"
        initTicket(ticket);
        Date outTime = new Date();
        ticket.setOutTime(outTime);
        ticketDAO.saveTicket(ticket);

        //Create second ticket with REG_NUMBER = "ABCDEF"
        Ticket secondTicket = new Ticket();
        initTicket(secondTicket);


        //ACT
        boolean result = ticketDAO.checkIfCustomerHasHistory(secondTicket);

        //ASSERT
        assertTrue(result);
    }
    @Test
    public void checkIfCustomerHasHistory_CaseWhereCustomerIsNotRegular_ShouldReturnFalse(){
        //ARRANGE
        initTicket(ticket);
        //ACT
        boolean result = ticketDAO.checkIfCustomerHasHistory(ticket);

        //ASSERT
        assertFalse(result);
    }

    private int returnNumberOfTicket() {
        Connection con = null;
        int numberOfTicket = 0;
        try {
            con = ticketDAO.dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement("select * from ticket t");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ++numberOfTicket;
            }
            ticketDAO.dataBaseConfig.closePreparedStatement(ps);

        } catch (Exception ex) {
        } finally {
            ticketDAO.dataBaseConfig.closeConnection(con);
        }
        return numberOfTicket;
    }
    private String returnRegNumberOfTicketWithFollowingRegNumber() {
        Connection con = null;
        String regNumberOfTicket = "";
        try {
            con = ticketDAO.dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement("select t.VEHICLE_REG_NUMBER from ticket t where t.VEHICLE_REG_NUMBER = ?");
            ps.setString(1, TicketDAO_IT.TEST_REG_NUMBER);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                regNumberOfTicket = rs.getString(1);
            }
            ticketDAO.dataBaseConfig.closePreparedStatement(ps);

        } catch (Exception ex) {
        } finally {
            ticketDAO.dataBaseConfig.closeConnection(con);
        }
        return regNumberOfTicket;
    }

    private Date returnOutTimeOfTicketWithFollowingRegNumber() {
        Connection con = null;
        Date outTime = new Date();
        try {
            con = ticketDAO.dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement("select t.OUT_TIME from ticket t where t.VEHICLE_REG_NUMBER = ?");
            ps.setString(1, TicketDAO_IT.TEST_REG_NUMBER);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                outTime = rs.getTimestamp(1);
            }
            ticketDAO.dataBaseConfig.closePreparedStatement(ps);

        } catch (Exception ex) {
        } finally {
            ticketDAO.dataBaseConfig.closeConnection(con);
        }
        return outTime;
    }
}

