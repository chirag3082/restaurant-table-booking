package com.chirag.restaurant.model;

import java.time.LocalDateTime;
import java.util.List;

public final class Reservation {

    private final long id;
    private final User user;
    private final Table table;
    private final int partySize;
    private final List<Slot> slots;
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    public Reservation(long id, User user, Table table, int partySize, List<Slot> slots) {
        this.id = id;
        this.user = user;
        this.table = table;
        this.partySize = partySize;
        this.slots = List.copyOf(slots);
    }

    /** Moves from one status to the next, or fails if the reservation isn't in the expected status. */
    public synchronized void transition(ReservationStatus from, ReservationStatus to) {
        if (status != from) {
            throw new IllegalStateException(
                    "Reservation " + id + " is " + status + ", expected " + from + " to move to " + to);
        }
        status = to;
    }

    public long id() { return id; }
    public User user() { return user; }
    public Table table() { return table; }
    public int partySize() { return partySize; }
    public List<Slot> slots() { return slots; }
    public LocalDateTime from() { return slots.getFirst().start(); }
    public LocalDateTime to() { return slots.getLast().end(); }
    public synchronized ReservationStatus status() { return status; }

    @Override
    public String toString() {
        return "Reservation#" + id + " " + user.name() + " x" + partySize + " at " + table.id()
                + " " + from().toLocalTime() + "-" + to().toLocalTime() + " " + status();
    }
}
