package com.chirag.restaurant.service;

import com.chirag.restaurant.model.Menu;
import com.chirag.restaurant.model.MenuItem;
import com.chirag.restaurant.model.Order;
import com.chirag.restaurant.model.Reservation;
import com.chirag.restaurant.model.ReservationStatus;
import com.chirag.restaurant.model.Waiter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class OrderService {

    private final Menu menu;
    private final ReservationService reservations;
    private final List<Waiter> waiters;
    private final Map<Long, List<Order>> ordersByReservation = new ConcurrentHashMap<>();
    private final Map<Long, Waiter> waiterByReservation = new ConcurrentHashMap<>();
    private final AtomicInteger nextWaiter = new AtomicInteger();
    private final AtomicLong ids = new AtomicLong();

    public OrderService(Menu menu, ReservationService reservations, List<Waiter> waiters) {
        if (waiters.isEmpty()) {
            throw new IllegalArgumentException("Need at least one waiter");
        }
        this.menu = menu;
        this.reservations = reservations;
        this.waiters = List.copyOf(waiters);
    }

    /** itemQuantities maps a menu item id to how many of it. */
    public Order placeOrder(long reservationId, Map<String, Integer> itemQuantities) {
        if (itemQuantities.isEmpty()) {
            throw new IllegalArgumentException("An order needs at least one item");
        }
        Map<MenuItem, Integer> items = new LinkedHashMap<>();
        itemQuantities.forEach((itemId, quantity) -> {
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for " + itemId);
            }
            items.merge(menu.get(itemId), quantity, Integer::sum);
        });

        Reservation reservation = reservations.get(reservationId);
        // Locked on the reservation so an order can't slip in while the bill is being made.
        synchronized (reservation) {
            if (reservation.status() != ReservationStatus.CONFIRMED) {
                throw new IllegalStateException(
                        "Can't order on reservation " + reservationId + ", it is " + reservation.status());
            }
            // The first order picks a waiter, and every later order on the same table keeps them.
            Waiter waiter = waiterByReservation.computeIfAbsent(reservationId,
                    id -> waiters.get(Math.floorMod(nextWaiter.getAndIncrement(), waiters.size())));
            Order order = new Order(ids.incrementAndGet(), reservation, waiter, items);
            ordersByReservation.computeIfAbsent(reservationId, id -> new ArrayList<>()).add(order);
            return order;
        }
    }

    public List<Order> ordersFor(long reservationId) {
        Reservation reservation = reservations.get(reservationId);
        synchronized (reservation) {
            return List.copyOf(ordersByReservation.getOrDefault(reservationId, List.of()));
        }
    }
}
