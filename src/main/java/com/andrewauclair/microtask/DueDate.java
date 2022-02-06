// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import com.andrewauclair.microtask.os.OSInterface;

import java.time.*;

public class DueDate {
	private final long dueTime;

	public DueDate(OSInterface osInterface, Period period) {
		Instant instant = Instant.ofEpochSecond(osInterface.currentSeconds());

		ZoneId zoneId = osInterface.getZoneId();

		LocalDateTime baseDate = LocalDateTime.ofInstant(instant, zoneId);
		baseDate = baseDate.plus(period);
		baseDate.atZone(zoneId);
		baseDate = baseDate.toLocalDate().atStartOfDay();

		dueTime = baseDate.atZone(zoneId).toEpochSecond();
	}

	public DueDate(OSInterface osInterface, MonthDay parse) {
		Instant instant = Instant.ofEpochSecond(osInterface.currentSeconds());

		ZoneId zoneId = osInterface.getZoneId();

		LocalDateTime baseDate = LocalDateTime.ofInstant(instant, zoneId);

		dueTime = parse.atYear(baseDate.getYear()).atStartOfDay().atZone(zoneId).toEpochSecond();
	}

	public DueDate(OSInterface osInterface, LocalDate parse) {
		ZoneId zoneId = osInterface.getZoneId();

		LocalDateTime baseDate = parse.atStartOfDay();

		dueTime = baseDate.atZone(zoneId).toEpochSecond();
	}

	public long dueTime() {
		return dueTime;
	}
}
