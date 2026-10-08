package com.chirag.restaurant.service;

import com.chirag.restaurant.model.Slot;
import com.chirag.restaurant.model.Table;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Keeps a map from each 30 minute slot to the tables still free in it.
 * A slot nobody has booked yet isn't in the map, which means every table is free.
 *
 * Every method is synchronized, so "check the slots" and "take the table" happen as one step.
 * Without that, two requests could both see T2 as free and both book it.
 */
public final class TableAvailability {

    private final List<Table> tablesSmallestFirst;
    private final Map<Slot, Set<Table>> freeTables = new HashMap<>();

    public TableAvailability(List<Table> tables) {
        this.tablesSmallestFirst = tables.stream()
                .sorted(Comparator.comparingInt(Table::capacity).thenComparing(Table::id))
                .toList();
    }

    /**
     * Finds the smallest table that seats the party and is free in every slot, and takes it.
     * Walking the tables smallest first means the first match is the best fit,
     * so a couple doesn't end up on a table for six.
     */
    public synchronized Optional<Table> claimSmallestFit(List<Slot> slots, int partySize) {
        for (Table table : tablesSmallestFirst) {
            if (table.capacity() >= partySize && isFreeInAll(table, slots)) {
                for (Slot slot : slots) {
                    free(slot).remove(table);
                }
                return Optional.of(table);
            }
        }
        return Optional.empty();
    }

    public synchronized void release(Table table, List<Slot> slots) {
        for (Slot slot : slots) {
            free(slot).add(table);
        }
    }

    public synchronized Set<Table> freeAt(Slot slot) {
        return Set.copyOf(free(slot));
    }

    private boolean isFreeInAll(Table table, List<Slot> slots) {
        for (Slot slot : slots) {
            if (!free(slot).contains(table)) {
                return false;
            }
        }
        return true;
    }

    private Set<Table> free(Slot slot) {
        return freeTables.computeIfAbsent(slot, s -> new HashSet<>(tablesSmallestFirst));
    }
}
