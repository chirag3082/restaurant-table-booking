package com.chirag.restaurant.exception;

import java.time.LocalDateTime;

public class NoTableAvailableException extends RuntimeException {

    public NoTableAvailableException(int partySize, LocalDateTime from, LocalDateTime to) {
        super("No table for " + partySize + " is free from " + from + " to " + to);
    }
}
