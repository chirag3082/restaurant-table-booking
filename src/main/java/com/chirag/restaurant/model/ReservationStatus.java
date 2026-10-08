package com.chirag.restaurant.model;

/**
 * CONFIRMED -> BILLED -> COMPLETED for a normal visit.
 * CONFIRMED -> CANCELLED if the user cancels before ordering.
 */
public enum ReservationStatus {
    CONFIRMED,
    BILLED,
    COMPLETED,
    CANCELLED
}
