package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.*;

class TicketServiceImplTest {

    @Test
    @DisplayName("Invalid account ID test")
    public void testValidAccountId() throws InvalidPurchaseException {
        TicketPaymentService ticketPaymentService = Mockito.mock(TicketPaymentService.class);
        SeatReservationService seatReservationService = Mockito.mock(SeatReservationService.class);
        TicketServiceImpl ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);

        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2)
        };

        ticketService.purchaseTickets(2L, ticketTypeRequests);
    }

    @Test
    @DisplayName("Child and Infant tickets cannot be purchased without purchasing an Adult test")
    public void testHasAdultTicket() throws InvalidPurchaseException {
        TicketPaymentService ticketPaymentService = Mockito.mock(TicketPaymentService.class);
        SeatReservationService seatReservationService = Mockito.mock(SeatReservationService.class);
        TicketServiceImpl ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);

        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3)
        };

        ticketService.purchaseTickets(1L, ticketTypeRequests);
    }

    @Test
    @DisplayName("Adults must be grater than or equals to Infants test")
    public void testMoreAdultsThanInfantTickets() throws InvalidPurchaseException {
        TicketPaymentService ticketPaymentService = Mockito.mock(TicketPaymentService.class);
        SeatReservationService seatReservationService = Mockito.mock(SeatReservationService.class);
        TicketServiceImpl ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);

        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2)
        };

        ticketService.purchaseTickets(1L, ticketTypeRequests);
    }

    @Test
    @DisplayName("Only maximum of 20 tickets allowed at a time test")
    public void testMaxAllowedTicketsAtATime() throws InvalidPurchaseException {
        TicketPaymentService ticketPaymentService = Mockito.mock(TicketPaymentService.class);
        SeatReservationService seatReservationService = Mockito.mock(SeatReservationService.class);
        TicketServiceImpl ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);

        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 12),
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 8)
        };

        ticketService.purchaseTickets(2L, ticketTypeRequests);
    }

    @Test
    @DisplayName("Do not allocate seats for Infants test")
    public void testNotAssigningSeatsForInfant() throws InvalidPurchaseException {
        TicketPaymentService ticketPaymentService = Mockito.mock(TicketPaymentService.class);
        SeatReservationService seatReservationService = Mockito.mock(SeatReservationService.class);
        TicketServiceImpl ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);

        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 5),
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20)
        };

        assertEquals(20, ticketService.calculateTotalNofSeatsToAllocate(ticketTypeRequests));
    }

    @Test
    @DisplayName("Infants do not pay for a ticket test")
    public void testInfantDoNotPayForTickets() throws InvalidPurchaseException {

        TicketPaymentService ticketPaymentService = Mockito.mock(TicketPaymentService.class);
        SeatReservationService seatReservationService = Mockito.mock(SeatReservationService.class);
        TicketServiceImpl ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);

        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 5)
        };

        /*int totalAmountToPay = 10 * 20 + 3 * 10;*/
        assertEquals(230, ticketService.calculateTotalAmountToPay(ticketTypeRequests));

    }
}