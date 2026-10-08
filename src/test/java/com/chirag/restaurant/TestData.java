package com.chirag.restaurant;

import com.chirag.restaurant.model.Menu;
import com.chirag.restaurant.model.MenuItem;
import com.chirag.restaurant.model.Table;
import com.chirag.restaurant.model.User;
import com.chirag.restaurant.model.Waiter;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

final class TestData {

    static final LocalDate DAY = LocalDate.of(2026, 3, 1);
    static final User USER = new User(1, "Chirag", "98100 00001");

    private TestData() {
    }

    /** T1 seats 4, T2 seats 2, T3 seats 6. Same tables as the whiteboard example. */
    static Restaurant restaurant() {
        return restaurant(List.of(new Table("T1", 4), new Table("T2", 2), new Table("T3", 6)));
    }

    static Restaurant restaurant(List<Table> tables) {
        Menu menu = new Menu(List.of(
                new MenuItem("paneer-tikka", "Paneer Tikka", 320),
                new MenuItem("butter-naan", "Butter Naan", 60),
                new MenuItem("gulab-jamun", "Gulab Jamun", 120)));
        Clock clock = Clock.fixed(Instant.parse("2026-03-01T09:00:00Z"), ZoneOffset.UTC);
        return new Restaurant("Test", tables, menu,
                List.of(new Waiter(1, "Ravi"), new Waiter(2, "Meena")), clock);
    }

    static LocalDateTime at(String time) {
        return LocalDateTime.of(DAY, LocalTime.parse(time));
    }
}
