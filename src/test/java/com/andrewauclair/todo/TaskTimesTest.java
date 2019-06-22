// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTimesTest {
	@Test
	void creates_times() {
		TaskTimes times = new TaskTimes(1234, 4567);

		assertEquals(1234, times.start);
		assertEquals(4567, times.stop);
	}

	@Test
	void times_with_only_start_time_sets_stop_time_to_min_long() {
		TaskTimes times = new TaskTimes(1234);

		assertEquals(1234, times.start);
		assertEquals(Long.MIN_VALUE, times.stop);
	}

	@Test
	void task_times_equals() {
		EqualsVerifier.forClass(TaskTimes.class).verify();
	}

	@Test
	@Disabled("This is the better version, being lazy for now")
	void times_toString_prints_dates_and_times() {
		// 6/20/2019 8:50:02 - 6/20/2019 9:20:22
		TaskTimes times = new TaskTimes(1561078202, 1561080022);

		assertEquals("6/20/2019 8:50:02 PM - 6/20/2019 9:20:22 PM", times.toString());
	}

	@Test
	void times_toString() {
		TaskTimes times = new TaskTimes(1561078202, 1561080022);

		assertEquals("1561078202 - 1561080022", times.toString());
	}
}
