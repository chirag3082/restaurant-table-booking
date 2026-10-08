package com.chirag.restaurant.model;

public record Table(String id, int capacity) {

    public Table {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Table capacity must be positive: " + capacity);
        }
    }
}
