package com.chirag.restaurant.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** A 30 minute block that starts on the hour or the half hour. Bookings are made of whole slots. */
public record Slot(LocalDateTime start) implements Comparable<Slot> {

    public static final Duration LENGTH = Duration.ofMinutes(30);

    public Slot {
        Objects.requireNonNull(start, "start");
        if (start.getMinute() % 30 != 0 || start.getSecond() != 0 || start.getNano() != 0) {
            throw new IllegalArgumentException("Slots start on the hour or half hour, got " + start);
        }
    }

    public LocalDateTime end() {
        return start.plus(LENGTH);
    }

    /** Every slot covering [from, to). Both ends have to sit on a slot boundary. */
    public static List<Slot> between(LocalDateTime from, LocalDateTime to) {
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("from must be before to: " + from + " to " + to);
        }
        new Slot(to); // only here to validate that 'to' is on a boundary too
        List<Slot> slots = new ArrayList<>();
        for (Slot s = new Slot(from); s.start().isBefore(to); s = new Slot(s.end())) {
            slots.add(s);
        }
        return slots;
    }

    @Override
    public int compareTo(Slot other) {
        return start.compareTo(other.start);
    }
}
