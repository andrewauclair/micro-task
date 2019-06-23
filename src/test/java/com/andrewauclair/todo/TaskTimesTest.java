// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;

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
	void times_description_prints_dates_and_times() {
		TaskTimes times = new TaskTimes(1561078202, 1561080022);
		
		assertEquals("06/20/2019 08:50:02 PM - 06/20/2019 09:20:22 PM", times.description(ZoneId.of("America/New_York")));
	}
	
	@Test
	void times_description_prints_only_start_time_for_active_tasks() {
		TaskTimes times = new TaskTimes(1561078202);
		
		assertEquals("06/20/2019 08:50:02 PM -", times.description(ZoneId.of("America/New_York")));
	}
	
	@Test
	void times_toString() {
		TaskTimes times = new TaskTimes(1561078202, 1561080022);
		
		assertEquals("1561078202 - 1561080022", times.toString());
	}
}
