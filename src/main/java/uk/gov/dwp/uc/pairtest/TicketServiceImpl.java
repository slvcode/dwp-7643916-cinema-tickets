package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {

    private final TicketPaymentService  ticketPaymentService;
    private final SeatReservationService seatReservationService;
    private int MAX_NOF_ALLOWED_TICKETS = 20;
    private int INFANT_TICKET_PRICE = 0;
    private int CHILD_TICKET_PRICE = 10;
    private int ADULT_TICKET_PRICE = 20;


    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        try {
            if (accountId == null || accountId == 0) {
                throw new InvalidPurchaseException("Invalid account ID");
            } else {
                validateTicketRequests(ticketTypeRequests);
                int totalAmountToPay = calculateTotalAmountToPay(ticketTypeRequests);
                int totalSeatsToAllocate = calculateTotalNofSeatsToAllocate(ticketTypeRequests);

                ticketPaymentService.makePayment(accountId, totalAmountToPay);
                seatReservationService.reserveSeat(accountId, totalSeatsToAllocate);
            }
        } catch (InvalidPurchaseException e) {
            throw e;
        }
    }

    /**
     * This method verifies:
     * 1. Whether the ticketTypeRequests parameter is null or empty.
     * 2. If the number of seats exceeds the maximum allowed number of seats.
     * 3. If the ticketTypeRequest includes at least one adult seat.
     * 4. If number of infant not exceed number of adults.
   */
    public void validateTicketRequests(TicketTypeRequest[] ticketTypeRequests) {

        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("Ticket requests cannot be null or empty");
        }

        int totalSeatsToAllocate = calculateTotalNofSeatsToAllocate(ticketTypeRequests);
        if (totalSeatsToAllocate > MAX_NOF_ALLOWED_TICKETS) {
            throw new InvalidPurchaseException("Cannot purchase more than " + MAX_NOF_ALLOWED_TICKETS + " tickets at a time");
        }

        boolean hasInfantOrChild = false;
        boolean hasAdult = false;
        int nofInfantTickets = 0;
        int nofAdultTickets = 0;
        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            TicketTypeRequest.Type ticketType = ticketTypeRequest.getTicketType();
            int nofTickets = ticketTypeRequest.getNoOfTickets();

            switch (ticketType) {
                case INFANT, CHILD ->  hasInfantOrChild = true;
                case ADULT -> hasAdult = true;
            }

            switch (ticketType) {
                case INFANT ->  nofInfantTickets = nofTickets;
                case ADULT -> nofAdultTickets = nofTickets;
            }
        }

        if (hasInfantOrChild && !hasAdult) {
            throw new InvalidPurchaseException("Cannot purchase child or infant tickets without an adult tickets");
        }

        if (nofInfantTickets > nofAdultTickets) {
            throw new InvalidPurchaseException("Infant ticket count must not exceed adult ticket count");
        }
    }

    /**
     * This function calculates the total amount that needs to be paid.
     * @returns totalAmountToPay
     */
    public int calculateTotalAmountToPay(TicketTypeRequest[] ticketTypeRequests) {
        int totalAmountToPay = 0;
        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            TicketTypeRequest.Type ticketType = ticketTypeRequest.getTicketType();
            int nofTickets = ticketTypeRequest.getNoOfTickets();

            int ticketPrice = 0;
            switch (ticketType) {
                case INFANT -> ticketPrice = INFANT_TICKET_PRICE;
                case CHILD -> ticketPrice = CHILD_TICKET_PRICE;
                case ADULT -> ticketPrice = ADULT_TICKET_PRICE;
            }
            totalAmountToPay += ticketPrice * nofTickets;
        }
        return totalAmountToPay;
    }

    /**
     * This function calculates the total quantity of seats to be assigned.
     * @returns totalSeatsToAllocate
    */
    public int calculateTotalNofSeatsToAllocate(TicketTypeRequest[] ticketTypeRequests) {
        int totalNofSeatsToAllocate = 0;
        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            TicketTypeRequest.Type ticketType = ticketTypeRequest.getTicketType();
            int nofTickets = ticketTypeRequest.getNoOfTickets();

            int seats = 0;
            switch (ticketType) {
                case INFANT-> seats = 0;
                case CHILD, ADULT -> seats = 1;
            }
            totalNofSeatsToAllocate += seats * nofTickets;
        }
        return totalNofSeatsToAllocate;
    }
}
