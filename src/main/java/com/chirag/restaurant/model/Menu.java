package com.chirag.restaurant.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Menu {

    private final Map<String, MenuItem> items = new LinkedHashMap<>();

    public Menu(List<MenuItem> items) {
        for (MenuItem item : items) {
            this.items.put(item.id(), item);
        }
    }

    public MenuItem get(String itemId) {
        MenuItem item = items.get(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Not on the menu: " + itemId);
        }
        return item;
    }

    public List<MenuItem> items() {
        return List.copyOf(items.values());
    }
}
