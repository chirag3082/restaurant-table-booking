package com.chirag.restaurant.model;

/** Price is in whole rupees to keep the example readable. Real money would be paise in a long. */
public record MenuItem(String id, String name, int price) {

    public MenuItem {
        if (price < 0) {
            throw new IllegalArgumentException("Price can't be negative: " + price);
        }
    }
}
