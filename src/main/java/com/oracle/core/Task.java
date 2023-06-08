package com.oracle.core;

import java.time.LocalDate;

public record Task(long id, String description, LocalDate date, boolean completed) {
}
