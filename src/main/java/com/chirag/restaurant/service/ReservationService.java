package com.chirag.restaurant.service;

import com.chirag.restaurant.exception.NoTableAvailableException;
import com.chirag.restaurant.model.Reservation;
import com.chirag.restaurant.model.ReservationStatus;
import com.chirag.restaurant.model.Slot;
import com.chirag.restaurant.model.Table;
import com.chirag.restaurant.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class ReservationService {

    private final TableAvailability availability;
    private final Map<Long, Reservation> reservations = new ConcurrentHashMap<>();
    private final AtomicLong ids = new AtomicLong();

    public ReservationService(TableAvailability availability) {
        this.availability = availability;
    }

    public Reservation reserve(User user, int partySize, LocalDateTime from, LocalDateTime to) {
        if (partySize <= 0) {
            throw new IllegalArgumentException("Party size must be positive: " + partySize);
        }
        List<Slot> slots = Slot.between(from, to);
        Table table = availability.claimSmallestFit(slots, partySize)
                .orElseThrow(() -> new NoTableAvailableException(partySize, from, to));

        Reservation reservation = new Reservation(ids.incrementAndGet(), user, table, partySize, slots);
        reservations.put(reservation.id(), reservation);
        return reservation;
    }

    public void cancel(long reservationId) {
        Reservation reservation = get(reservationId);
        reservation.transition(ReservationStatus.CONFIRMED, ReservationStatus.CANCELLED);
        availability.release(reservation.table(), reservation.slots());
    }

    public Reservation get(long reservationId) {
        Reservation reservation = reservations.get(reservationId);
        if (reservation == null) {
            throw new NoSuchElementException("No reservation " + reservationId);
        }
        return reservation;
    }
}
