package com.chirag.restaurant.model;

import java.util.List;

/** One bill per visit, covering every order placed on the reservation. */
public final class Bill {

    private final long id;
    private final Reservation reservation;
    private final List<Order> orders;
    private final int total;
    private BillStatus status = BillStatus.OPEN;

    public Bill(long id, Reservation reservation, List<Order> orders) {
        this.id = id;
        this.reservation = reservation;
        this.orders = List.copyOf(orders);
        this.total = this.orders.stream().mapToInt(Order::total).sum();
    }

    public synchronized void markPaid() {
        if (status == BillStatus.PAID) {
            throw new IllegalStateException("Bill " + id + " is already paid");
        }
        status = BillStatus.PAID;
    }

    public long id() { return id; }
    public Reservation reservation() { return reservation; }
    public List<Order> orders() { return orders; }
    public int total() { return total; }
    public synchronized BillStatus status() { return status; }
}
