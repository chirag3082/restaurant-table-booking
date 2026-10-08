package com.chirag.restaurant.model;

import java.time.Instant;

public record Payment(long id, Bill bill, int amount, Instant paidAt) {
}
