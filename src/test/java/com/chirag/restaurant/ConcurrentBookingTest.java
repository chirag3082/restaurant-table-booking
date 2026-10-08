package com.chirag.restaurant;

import com.chirag.restaurant.exception.NoTableAvailableException;
import com.chirag.restaurant.model.Reservation;
import com.chirag.restaurant.model.Table;
import org.junit.jupiter.api.RepeatedTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.chirag.restaurant.TestData.USER;
import static com.chirag.restaurant.TestData.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrentBookingTest {

    private static final int REQUESTS = 50;

    /** 50 people try to book the same hour at the same moment. Only 3 tables exist, so only 3 can win. */
    @RepeatedTest(20)
    void sameSlotIsNeverGivenToTwoPeople() throws InterruptedException {
        Restaurant restaurant = TestData.restaurant(
                List.of(new Table("T1", 2), new Table("T2", 2), new Table("T3", 2)));
        ConcurrentLinkedQueue<Reservation> booked = new ConcurrentLinkedQueue<>();
        AtomicInteger turnedAway = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService pool = Executors.newFixedThreadPool(16);
        for (int i = 0; i < REQUESTS; i++) {
            pool.submit(() -> {
                start.await();
                try {
                    booked.add(restaurant.book(USER, 2, at("20:00"), at("21:00")));
                } catch (NoTableAvailableException e) {
                    turnedAway.incrementAndGet();
                }
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(3, booked.size());
        assertEquals(REQUESTS - 3, turnedAway.get());
        Set<String> tables = new HashSet<>();
        for (Reservation r : booked) {
            assertTrue(tables.add(r.table().id()), "table given out twice: " + r.table().id());
        }
    }
}
