package com.oracle.api;

import java.time.LocalDate;

public record TaskDto(long id, String description, LocalDate date, boolean completed) {
}
