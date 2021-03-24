package com.parkit.parkingsystem.integration.dao;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TicketDAOTest {

    public static final String TEST_REG_NUMBER = "ABCDEF";
    private static Ticket ticket;
    private static TicketDAO ticketDAO;


    @BeforeAll
    private static void setUp() {
        ticketDAO = new TicketDAO();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
        ticketDAO.dataBaseConfig = Mockito.mock(DataBaseTestConfig.class);
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
    public void saveTicket_verifyCorrectMethodsAreCalled() throws SQLException, IOException, ClassNotFoundException {
        //ARRANGE
        initTicket(ticket);

        Connection mockConnection = Mockito.mock(Connection.class);
        PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);

        when(ticketDAO.dataBaseConfig.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(any())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.execute()).thenReturn(false);

        //ACT
        boolean result = ticketDAO.saveTicket(ticket);

        //ASSERT
        verify(mockPreparedStatement, times(1)).execute();
        verify(ticketDAO.dataBaseConfig, times(1)).getConnection();
        verify(ticketDAO.dataBaseConfig, times(1)).closePreparedStatement(mockPreparedStatement);
        verify(ticketDAO.dataBaseConfig, times(1)).closeConnection(mockConnection);
        assertFalse(result);
    }

    @Test
    public void getTicket_verifyCorrectMethodsAreCalled() throws SQLException, IOException, ClassNotFoundException {
        //ARRANGE
        Connection mockConnection = Mockito.mock(Connection.class);
        PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);

        when(ticketDAO.dataBaseConfig.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(any())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        //ACT
        Ticket ticketResult = ticketDAO.getTicket(TEST_REG_NUMBER);

        //ASSERT
        verify(ticketDAO.dataBaseConfig, times(1)).getConnection();
        verify(ticketDAO.dataBaseConfig, times(1)).closePreparedStatement(mockPreparedStatement);
        verify(ticketDAO.dataBaseConfig, times(1)).closeResultSet(mockResultSet);
        verify(ticketDAO.dataBaseConfig, times(1)).closeConnection(mockConnection);
        assertNull(ticketResult);

    }

    @Test
    public void updateTicket_verifyCorrectMethodsAreCalled() throws SQLException, IOException, ClassNotFoundException {
        //ARRANGE
        initTicket(ticket);
        ticket.setOutTime(new Date());
        Connection mockConnection = Mockito.mock(Connection.class);
        PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);

        when(ticketDAO.dataBaseConfig.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(any())).thenReturn(mockPreparedStatement);

        //ACT
        boolean result = ticketDAO.updateTicket(ticket);

        //ASSERT
        verify(ticketDAO.dataBaseConfig, times(1)).getConnection();
        verify(ticketDAO.dataBaseConfig, times(1)).closePreparedStatement(mockPreparedStatement);
        verify(ticketDAO.dataBaseConfig, times(1)).closeConnection(mockConnection);
        assertTrue(result);
    }

    @Test
    public void checkIfCustomerHasHistory_verifyCorrectMethodsAreCalled() throws SQLException, IOException, ClassNotFoundException {
        //ARRANGE
        initTicket(ticket);

        Connection mockConnection = Mockito.mock(Connection.class);
        PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);

        when(ticketDAO.dataBaseConfig.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(any())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        //ACT
        boolean result = ticketDAO.checkIfCustomerHasHistory(ticket);

        //ASSERT
        verify(ticketDAO.dataBaseConfig, times(1)).getConnection();
        verify(ticketDAO.dataBaseConfig, times(1)).closePreparedStatement(mockPreparedStatement);
        verify(ticketDAO.dataBaseConfig, times(1)).closeResultSet(mockResultSet);
        verify(ticketDAO.dataBaseConfig, times(1)).closeConnection(mockConnection);
        assertFalse(result);
    }
}

