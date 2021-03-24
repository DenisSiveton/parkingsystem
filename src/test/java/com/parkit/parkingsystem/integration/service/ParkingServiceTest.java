package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ParkingServiceTest {

    private static ParkingService parkingService;
    private static final String TEST_REG_NUMBER = "ABCDEF";
    private static ParkingSpot parkingSpot;
    private static Ticket ticket;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;
    @Mock
    private static FareCalculatorService fareCalculatorService;

    @BeforeEach
    private void setUpPerTest() {
        try {

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber(TEST_REG_NUMBER);



        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void getNextParkingNumberIfAvailable_whenCarIsParked_ShouldReturnCorrectParkingTypeAndId(){

        //ARRANGE
        parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setParkingSpot(parkingSpot);
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        //ACT
        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();
        //ASSERT
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
        assertEquals(result.getParkingType(), parkingSpot.getParkingType());
        assertEquals(result.getId(),parkingSpot.getId());
    }
    @Test
    public void getNextParkingNumberIfAvailable_ForCar_FailsCauseRegNumberIsNotValid_Test() {

        //ARRANGE
        parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setParkingSpot(parkingSpot);
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);

        //ACT
        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();
        //ASSERT
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
    }
    @Test
    public void getNextParkingNumberIfAvailable_whenBikeIsParked_ShouldReturnCorrectParkingTypeAndId(){

        //ARRANGE
        parkingSpot = new ParkingSpot(4, ParkingType.BIKE,false);
        ticket.setParkingSpot(parkingSpot);
        when(inputReaderUtil.readSelection()).thenReturn(2);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(4);

        //ACT
        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();
        //ASSERT
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.BIKE);
        assertEquals(result.getParkingType(), parkingSpot.getParkingType());
        assertEquals(result.getId(),parkingSpot.getId());
    }
    @Test
    public void processIncomingVehicle_whenCarIsRegistered_ShouldCallAppropriateMethod() throws Exception {

    //ARRANGE
        parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setParkingSpot(parkingSpot);
        ParkingService spy = spy(parkingService);
        doReturn(parkingSpot).when(spy).getNextParkingNumberIfAvailable();
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(TEST_REG_NUMBER);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);



    //ACT
        spy.processIncomingVehicle();

    //ASSERT
        verify(parkingSpotDAO, times(1)).updateParking(parkingSpot);
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));

    }
    @Test
    public void processExitingVehicle_whenCarIsRegistered_ShouldCallAppropriateMethod() throws Exception {

        //ARRANGE
        parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setParkingSpot(parkingSpot);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(TEST_REG_NUMBER);
        when(ticketDAO.getTicket(TEST_REG_NUMBER)).thenReturn(ticket);
        when(ticketDAO.checkIfCustomerHasHistory(ticket)).thenReturn(false);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        doAnswer(invocation ->{
            ticket.setPrice(Fare.CAR_RATE_PER_HOUR);
            return null;
        }).when(fareCalculatorService).calculateFare(ticket, false);

        // ACT
        parkingService.processExitingVehicle();
        double priceTicket =ticket.getPrice();

        //ASSERT
        verify(ticketDAO, times(1)).updateTicket(ticket);
        verify(ticketDAO, times (1)).checkIfCustomerHasHistory(ticket);
        verify(parkingSpotDAO, times(1)).updateParking(parkingSpot);

        assertEquals(1.5, priceTicket , 0.01);
    }
    @Test
    public void processExitingVehicle_CaseWhereTicketUpdateFails_ShouldNotCallUpdateParkingMethod() throws Exception {

        //ARRANGE
        parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setParkingSpot(parkingSpot);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(TEST_REG_NUMBER);
        when(ticketDAO.getTicket(TEST_REG_NUMBER)).thenReturn(ticket);
        when(ticketDAO.checkIfCustomerHasHistory(ticket)).thenReturn(false);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false); //Negative Case For Unit Test
        doAnswer(invocation ->{
            ticket.setPrice(Fare.CAR_RATE_PER_HOUR);
            return null;
        }).when(fareCalculatorService).calculateFare(ticket, false);

        // ACT
        parkingService.processExitingVehicle();

        //ASSERT
        verify(ticketDAO, times(1)).updateTicket(ticket);
        verify(ticketDAO, times (1)).checkIfCustomerHasHistory(ticket);
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
    }

}
