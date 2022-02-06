// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Period;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DueDateTest {
	MockOSInterface osInterface = Mockito.mock(MockOSInterface.class);

	@BeforeEach
	void setup() {
		Mockito.when(osInterface.getZoneId()).thenReturn(ZoneId.of("America/Chicago"));
	}

	@Test
	void create_from_period() {
		DueDate due = new DueDate(osInterface, Period.parse("p1w"));

		assertEquals(604800, due.dueTime());
	}

	@Test
	void create_from_month_day() {
		Mockito.when(osInterface.currentSeconds()).thenReturn(50000L);

		DueDate due = new DueDate(osInterface, MonthDay.parse("--02-15"));

		assertEquals(3909600, due.dueTime());
	}

	@Test
	void create_from_local_date() {
		DueDate due = new DueDate(osInterface, LocalDate.parse("1971-02-15"));

		assertEquals(35445600, due.dueTime());
	}
}
