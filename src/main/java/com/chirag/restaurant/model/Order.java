package com.chirag.restaurant.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Order {

    private final long id;
    private final Reservation reservation;
    private final Waiter waiter;
    private final Map<MenuItem, Integer> items;

    public Order(long id, Reservation reservation, Waiter waiter, Map<MenuItem, Integer> items) {
        this.id = id;
        this.reservation = reservation;
        this.waiter = waiter;
        this.items = Collections.unmodifiableMap(new LinkedHashMap<>(items));
    }

    public int total() {
        return items.entrySet().stream()
                .mapToInt(e -> e.getKey().price() * e.getValue())
                .sum();
    }

    public long id() { return id; }
    public Reservation reservation() { return reservation; }
    public Waiter waiter() { return waiter; }
    public Map<MenuItem, Integer> items() { return items; }
}
