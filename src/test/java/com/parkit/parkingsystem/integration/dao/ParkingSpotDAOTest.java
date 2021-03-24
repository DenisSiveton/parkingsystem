package com.parkit.parkingsystem.integration.dao;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ParkingSpotDAOTest {

    private static ParkingSpotDAO parkingSpotDAO;


    //@TestInstance(LifeCycle.PER_CLASS)

    @BeforeAll
    private static void setUp() {
        parkingSpotDAO = new ParkingSpotDAO();

    }


    @Test
    public void getNextAvailableParkingSlotForCar_verifyCorrectMethodsAreCalled() throws SQLException, ClassNotFoundException {
        //ARRANGE
        DataBaseTestConfig dataBaseMockTestConfig = Mockito.mock(DataBaseTestConfig.class);
        parkingSpotDAO.dataBaseConfig = dataBaseMockTestConfig;
        Connection mockConnection = Mockito.mock(Connection.class);
        PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);

        when(dataBaseMockTestConfig.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(any())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        //ACT
        parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        //ASSERT

        verify(dataBaseMockTestConfig, times(1)).getConnection();
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();
        verify(dataBaseMockTestConfig, times(1)).closeResultSet(mockResultSet);
        verify(dataBaseMockTestConfig, times(1)).closePreparedStatement(mockPreparedStatement);
        verify(dataBaseMockTestConfig, times(1)).closeConnection(mockConnection);
    }

    @Test
    public void updateParking_verifyCorrectMethodsAreCalled() throws SQLException, ClassNotFoundException {
        //ARRANGE
        DataBaseTestConfig dataBaseMockTestConfig = Mockito.mock(DataBaseTestConfig.class);
        parkingSpotDAO.dataBaseConfig = dataBaseMockTestConfig;
        Connection mockConnection = Mockito.mock(Connection.class);
        PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);

        when(dataBaseMockTestConfig.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(any())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);


        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        //ACT

        parkingSpotDAO.updateParking(parkingSpot);

        //ASSERT

        verify(mockPreparedStatement, times(1)).executeUpdate();
        verify(dataBaseMockTestConfig, times(1)).getConnection();
        verify(dataBaseMockTestConfig, times(1)).closePreparedStatement(mockPreparedStatement);
        verify(dataBaseMockTestConfig, times(1)).closeConnection(mockConnection);
    }
}
