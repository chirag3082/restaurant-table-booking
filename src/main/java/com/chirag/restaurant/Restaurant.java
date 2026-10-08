package com.chirag.restaurant;

import com.chirag.restaurant.model.Bill;
import com.chirag.restaurant.model.Menu;
import com.chirag.restaurant.model.Order;
import com.chirag.restaurant.model.Payment;
import com.chirag.restaurant.model.Reservation;
import com.chirag.restaurant.model.Slot;
import com.chirag.restaurant.model.Table;
import com.chirag.restaurant.model.User;
import com.chirag.restaurant.model.Waiter;
import com.chirag.restaurant.service.OrderService;
import com.chirag.restaurant.service.PaymentService;
import com.chirag.restaurant.service.ReservationService;
import com.chirag.restaurant.service.TableAvailability;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The one class a caller talks to. Each public method maps to something the interviewer asked for. */
public final class Restaurant {

    private final String name;
    private final Menu menu;
    private final TableAvailability availability;
    private final ReservationService reservations;
    private final OrderService orders;
    private final PaymentService payments;

    public Restaurant(String name, List<Table> tables, Menu menu, List<Waiter> waiters) {
        this(name, tables, menu, waiters, Clock.systemDefaultZone());
    }

    public Restaurant(String name, List<Table> tables, Menu menu, List<Waiter> waiters, Clock clock) {
        this.name = name;
        this.menu = menu;
        this.availability = new TableAvailability(tables);
        this.reservations = new ReservationService(availability);
        this.orders = new OrderService(menu, reservations, waiters);
        this.payments = new PaymentService(reservations, orders, clock);
    }

    // 1. Users can make reservations
    public Reservation book(User user, int partySize, LocalDateTime from, LocalDateTime to) {
        return reservations.reserve(user, partySize, from, to);
    }

    public void cancel(long reservationId) {
        reservations.cancel(reservationId);
    }

    // 2. Users can place an order
    public Order placeOrder(long reservationId, Map<String, Integer> itemQuantities) {
        return orders.placeOrder(reservationId, itemQuantities);
    }

    // 3. Users can make a payment
    public Bill requestBill(long reservationId) {
        return payments.generateBill(reservationId);
    }

    public Payment pay(long billId, int amount) {
        return payments.pay(billId, amount);
    }

    public Set<Table> freeTablesAt(LocalDateTime slotStart) {
        return availability.freeAt(new Slot(slotStart));
    }

    public Reservation reservation(long reservationId) {
        return reservations.get(reservationId);
    }

    public Menu menu() { return menu; }
    public String name() { return name; }
}
