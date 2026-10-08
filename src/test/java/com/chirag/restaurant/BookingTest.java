package com.chirag.restaurant;

import com.chirag.restaurant.exception.NoTableAvailableException;
import com.chirag.restaurant.model.Reservation;
import com.chirag.restaurant.model.ReservationStatus;
import com.chirag.restaurant.model.Table;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.chirag.restaurant.TestData.USER;
import static com.chirag.restaurant.TestData.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingTest {

    @Test
    void picksTheSmallestTableThatFits() {
        Restaurant restaurant = TestData.restaurant();

        assertEquals("T2", restaurant.book(USER, 2, at("19:00"), at("20:00")).table().id());
        assertEquals("T1", restaurant.book(USER, 3, at("19:00"), at("20:00")).table().id());
        assertEquals("T3", restaurant.book(USER, 5, at("19:00"), at("20:00")).table().id());
    }

    @Test
    void tableHasToBeFreeInEverySlotOfTheBooking() {
        Restaurant restaurant = TestData.restaurant();
        // The whiteboard: T3 is taken at 1:00, T1 is taken at 2:00
        restaurant.book(USER, 5, at("13:00"), at("13:30"));
        restaurant.book(USER, 3, at("14:00"), at("14:30"));

        // 4 people from 1:00 to 2:00. T2 is too small and T3 is busy at 1:00, so only T1 works.
        Reservation r = restaurant.book(USER, 4, at("13:00"), at("14:00"));
        assertEquals("T1", r.table().id());

        // 4 people from 1:30 to 2:30. T1 is now taken until 2:30, so it has to be T3.
        assertEquals("T3", restaurant.book(USER, 4, at("13:30"), at("14:30")).table().id());
    }

    @Test
    void failsWhenNoTableIsBigEnough() {
        Restaurant restaurant = TestData.restaurant();

        assertThrows(NoTableAvailableException.class,
                () -> restaurant.book(USER, 7, at("19:00"), at("20:00")));
    }

    @Test
    void failsWhenEverySuitableTableIsTaken() {
        Restaurant restaurant = TestData.restaurant();
        restaurant.book(USER, 5, at("19:00"), at("21:00")); // the only table for 5+

        assertThrows(NoTableAvailableException.class,
                () -> restaurant.book(USER, 6, at("20:30"), at("21:30")));
    }

    @Test
    void backToBackBookingsCanShareATable() {
        Restaurant restaurant = TestData.restaurant();

        Reservation lunch = restaurant.book(USER, 2, at("13:00"), at("14:00"));
        Reservation next = restaurant.book(USER, 2, at("14:00"), at("15:00"));

        assertEquals("T2", lunch.table().id());
        assertEquals("T2", next.table().id());
    }

    @Test
    void cancellingFreesTheSlots() {
        Restaurant restaurant = TestData.restaurant();
        Reservation r = restaurant.book(USER, 2, at("13:00"), at("14:00"));

        restaurant.cancel(r.id());

        assertEquals(ReservationStatus.CANCELLED, r.status());
        Set<String> free = ids(restaurant.freeTablesAt(at("13:30")));
        assertEquals(Set.of("T1", "T2", "T3"), free);
        assertEquals("T2", restaurant.book(USER, 2, at("13:00"), at("14:00")).table().id());
    }

    @Test
    void cantCancelTwice() {
        Restaurant restaurant = TestData.restaurant();
        Reservation r = restaurant.book(USER, 2, at("13:00"), at("14:00"));
        restaurant.cancel(r.id());

        assertThrows(IllegalStateException.class, () -> restaurant.cancel(r.id()));
    }

    @Test
    void rejectsTimesOffTheHalfHour() {
        Restaurant restaurant = TestData.restaurant();

        assertThrows(IllegalArgumentException.class,
                () -> restaurant.book(USER, 2, at("13:15"), at("14:00")));
        assertThrows(IllegalArgumentException.class,
                () -> restaurant.book(USER, 2, at("13:00"), at("13:45")));
    }

    @Test
    void rejectsEmptyOrBackwardsRanges() {
        Restaurant restaurant = TestData.restaurant();

        assertThrows(IllegalArgumentException.class,
                () -> restaurant.book(USER, 2, at("14:00"), at("14:00")));
        assertThrows(IllegalArgumentException.class,
                () -> restaurant.book(USER, 2, at("14:00"), at("13:00")));
        assertThrows(IllegalArgumentException.class,
                () -> restaurant.book(USER, 0, at("13:00"), at("14:00")));
    }

    @Test
    void failedBookingDoesNotHoldAnything() {
        Restaurant restaurant = TestData.restaurant();
        assertThrows(NoTableAvailableException.class,
                () -> restaurant.book(USER, 9, at("13:00"), at("14:00")));

        assertTrue(ids(restaurant.freeTablesAt(at("13:00"))).containsAll(Set.of("T1", "T2", "T3")));
    }

    private static Set<String> ids(Set<Table> tables) {
        return Set.copyOf(tables.stream().map(Table::id).toList());
    }
}
