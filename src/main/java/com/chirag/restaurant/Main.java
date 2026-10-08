package com.chirag.restaurant;

import com.chirag.restaurant.model.Bill;
import com.chirag.restaurant.model.Menu;
import com.chirag.restaurant.model.MenuItem;
import com.chirag.restaurant.model.Order;
import com.chirag.restaurant.model.Payment;
import com.chirag.restaurant.model.Reservation;
import com.chirag.restaurant.model.Table;
import com.chirag.restaurant.model.User;
import com.chirag.restaurant.model.Waiter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Replays the example from the whiteboard: book, order, pay. */
public class Main {

    private static final LocalDate DAY = LocalDate.of(2026, 3, 1);

    public static void main(String[] args) {
        Restaurant restaurant = new Restaurant(
                "Our Restaurant",
                List.of(new Table("T1", 4), new Table("T2", 2), new Table("T3", 6)),
                new Menu(List.of(
                        new MenuItem("paneer-tikka", "Paneer Tikka", 320),
                        new MenuItem("dal-makhani", "Dal Makhani", 280),
                        new MenuItem("butter-naan", "Butter Naan", 60),
                        new MenuItem("gulab-jamun", "Gulab Jamun", 120))),
                List.of(new Waiter(1, "Ravi"), new Waiter(2, "Meena")));

        // Two earlier bookings, so the slots look like the whiteboard
        restaurant.book(new User(1, "Asha", "98100 00001"), 5, at("13:00"), at("13:30"));  // gets T3
        restaurant.book(new User(2, "Kabir", "98100 00002"), 3, at("14:00"), at("14:30")); // gets T1

        System.out.println("Free tables per slot:");
        for (String time : List.of("13:00", "13:30", "14:00")) {
            System.out.println("  " + time + "  " + freeTableIds(restaurant, time));
        }

        User user = new User(3, "Chirag", "98100 00003");
        Reservation reservation = restaurant.book(user, 2, at("13:00"), at("14:00"));
        System.out.println();
        System.out.println("Booked: " + reservation);

        Map<String, Integer> mains = new LinkedHashMap<>();
        mains.put("paneer-tikka", 1);
        mains.put("butter-naan", 4);
        Order first = restaurant.placeOrder(reservation.id(), mains);
        Order second = restaurant.placeOrder(reservation.id(), Map.of("gulab-jamun", 2));
        System.out.println("Order " + first.id() + " (" + first.waiter().name() + "): " + first.total());
        System.out.println("Order " + second.id() + " (" + second.waiter().name() + "): " + second.total());

        Bill bill = restaurant.requestBill(reservation.id());
        System.out.println("Bill " + bill.id() + " total: " + bill.total());

        Payment payment = restaurant.pay(bill.id(), bill.total());
        System.out.println("Paid " + payment.amount() + ", bill is " + bill.status());
        System.out.println("Reservation is now " + reservation.status());
    }

    private static LocalDateTime at(String time) {
        return LocalDateTime.of(DAY, LocalTime.parse(time));
    }

    private static List<String> freeTableIds(Restaurant restaurant, String time) {
        return restaurant.freeTablesAt(at(time)).stream()
                .sorted(Comparator.comparing(Table::id))
                .map(Table::id)
                .toList();
    }
}
