package com.chirag.restaurant.service;

import com.chirag.restaurant.model.Bill;
import com.chirag.restaurant.model.Order;
import com.chirag.restaurant.model.Payment;
import com.chirag.restaurant.model.Reservation;
import com.chirag.restaurant.model.ReservationStatus;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class PaymentService {

    private final ReservationService reservations;
    private final OrderService orders;
    private final Clock clock;
    private final Map<Long, Bill> bills = new ConcurrentHashMap<>();
    private final Map<Long, Bill> billByReservation = new ConcurrentHashMap<>();
    private final AtomicLong billIds = new AtomicLong();
    private final AtomicLong paymentIds = new AtomicLong();

    public PaymentService(ReservationService reservations, OrderService orders, Clock clock) {
        this.reservations = reservations;
        this.orders = orders;
        this.clock = clock;
    }

    /** Asking twice returns the same bill, so a double tap on "get bill" is harmless. */
    public Bill generateBill(long reservationId) {
        Reservation reservation = reservations.get(reservationId);
        synchronized (reservation) {
            Bill existing = billByReservation.get(reservationId);
            if (existing != null) {
                return existing;
            }
            List<Order> placed = orders.ordersFor(reservationId);
            if (placed.isEmpty()) {
                throw new IllegalStateException("Nothing was ordered on reservation " + reservationId);
            }
            reservation.transition(ReservationStatus.CONFIRMED, ReservationStatus.BILLED);

            Bill bill = new Bill(billIds.incrementAndGet(), reservation, placed);
            bills.put(bill.id(), bill);
            billByReservation.put(reservationId, bill);
            return bill;
        }
    }

    public Payment pay(long billId, int amount) {
        Bill bill = getBill(billId);
        if (amount != bill.total()) {
            throw new IllegalArgumentException("Bill " + billId + " is " + bill.total() + ", got " + amount);
        }
        bill.markPaid(); // throws if someone already paid it
        bill.reservation().transition(ReservationStatus.BILLED, ReservationStatus.COMPLETED);
        return new Payment(paymentIds.incrementAndGet(), bill, amount, clock.instant());
    }

    public Bill getBill(long billId) {
        Bill bill = bills.get(billId);
        if (bill == null) {
            throw new NoSuchElementException("No bill " + billId);
        }
        return bill;
    }
}
