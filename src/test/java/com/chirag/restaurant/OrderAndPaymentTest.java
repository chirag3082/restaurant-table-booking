package com.chirag.restaurant;

import com.chirag.restaurant.model.Bill;
import com.chirag.restaurant.model.BillStatus;
import com.chirag.restaurant.model.Order;
import com.chirag.restaurant.model.Payment;
import com.chirag.restaurant.model.Reservation;
import com.chirag.restaurant.model.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.chirag.restaurant.TestData.USER;
import static com.chirag.restaurant.TestData.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderAndPaymentTest {

    private Restaurant restaurant;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        restaurant = TestData.restaurant();
        reservation = restaurant.book(USER, 2, at("13:00"), at("14:00"));
    }

    @Test
    void orderTotalsQuantityTimesPrice() {
        Order order = restaurant.placeOrder(reservation.id(), Map.of("paneer-tikka", 1, "butter-naan", 4));

        assertEquals(320 + 4 * 60, order.total());
    }

    @Test
    void laterOrdersKeepTheSameWaiter() {
        Order first = restaurant.placeOrder(reservation.id(), Map.of("paneer-tikka", 1));
        Order second = restaurant.placeOrder(reservation.id(), Map.of("gulab-jamun", 2));

        assertEquals(first.waiter(), second.waiter());
    }

    @Test
    void rejectsItemsNotOnTheMenu() {
        assertThrows(IllegalArgumentException.class,
                () -> restaurant.placeOrder(reservation.id(), Map.of("pizza", 1)));
    }

    @Test
    void rejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> restaurant.placeOrder(reservation.id(), Map.of("paneer-tikka", 0)));
    }

    @Test
    void cantOrderOnACancelledReservation() {
        restaurant.cancel(reservation.id());

        assertThrows(IllegalStateException.class,
                () -> restaurant.placeOrder(reservation.id(), Map.of("paneer-tikka", 1)));
    }

    @Test
    void billCoversEveryOrderOnTheReservation() {
        restaurant.placeOrder(reservation.id(), Map.of("paneer-tikka", 1, "butter-naan", 4));
        restaurant.placeOrder(reservation.id(), Map.of("gulab-jamun", 2));

        Bill bill = restaurant.requestBill(reservation.id());

        assertEquals(2, bill.orders().size());
        assertEquals(560 + 240, bill.total());
        assertEquals(ReservationStatus.BILLED, reservation.status());
    }

    @Test
    void askingForTheBillTwiceGivesTheSameBill() {
        restaurant.placeOrder(reservation.id(), Map.of("paneer-tikka", 1));

        assertSame(restaurant.requestBill(reservation.id()), restaurant.requestBill(reservation.id()));
    }

    @Test
    void cantBillWithNothingOrdered() {
        assertThrows(IllegalStateException.class, () -> restaurant.requestBill(reservation.id()));
    }

    @Test
    void cantOrderAfterTheBillIsOut() {
        restaurant.placeOrder(reservation.id(), Map.of("paneer-tikka", 1));
        restaurant.requestBill(reservation.id());

        assertThrows(IllegalStateException.class,
                () -> restaurant.placeOrder(reservation.id(), Map.of("gulab-jamun", 1)));
    }

    @Test
    void payingTheExactAmountClosesEverything() {
        restaurant.placeOrder(reservation.id(), Map.of("paneer-tikka", 1));
        Bill bill = restaurant.requestBill(reservation.id());

        Payment payment = restaurant.pay(bill.id(), 320);

        assertEquals(320, payment.amount());
        assertEquals(BillStatus.PAID, bill.status());
        assertEquals(ReservationStatus.COMPLETED, reservation.status());
    }

    @Test
    void rejectsTheWrongAmount() {
        restaurant.placeOrder(reservation.id(), Map.of("paneer-tikka", 1));
        Bill bill = restaurant.requestBill(reservation.id());

        assertThrows(IllegalArgumentException.class, () -> restaurant.pay(bill.id(), 300));
        assertEquals(BillStatus.OPEN, bill.status());
    }

    @Test
    void cantPayTwice() {
        restaurant.placeOrder(reservation.id(), Map.of("paneer-tikka", 1));
        Bill bill = restaurant.requestBill(reservation.id());
        restaurant.pay(bill.id(), 320);

        assertThrows(IllegalStateException.class, () -> restaurant.pay(bill.id(), 320));
    }
}
