// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTimesTest {
	@Test
	void creates_times() {
		TaskTimes.Times times = new TaskTimes.Times(1234, 4567);
		
		assertEquals(1234, times.start);
		assertEquals(4567, times.stop);
	}
	
	@Test
	void creates_task_times_with_a_single_start_and_stop() {
		TaskTimes times = new TaskTimes(1234, 4567);
		
		assertThat(times.asList()).containsOnly(new TaskTimes.Times(1234, 4567));
	}
	
	@Test
	void creates_task_times_with_multiple_starts_and_stops() {
		List<TaskTimes.Times> timesList = Arrays.asList(
				new TaskTimes.Times(10, 20),
				new TaskTimes.Times(30, 40),
				new TaskTimes.Times(50, 60),
				new TaskTimes.Times(70, 80),
				new TaskTimes.Times(90, 100)
		);
		
		TaskTimes times = new TaskTimes(timesList);
		
		assertThat(times.asList()).containsAll(timesList);
	}
	
	@Test
	void task_times_provides_empty_for_new_tasks() {
		TaskTimes taskTimes = new TaskTimes();
		
		assertThat(taskTimes.asList()).isEmpty();
	}
	
	@Test
	void task_times_with_only_start_time_sets_stop_time_to_min_long() {
		TaskTimes taskTimes = new TaskTimes(1234);
		
		assertThat(taskTimes.asList()).containsOnly(new TaskTimes.Times(1234, Long.MIN_VALUE));
	}
	
	@Test
	void times_with_only_start_time_sets_stop_time_to_min_long() {
		TaskTimes.Times times = new TaskTimes.Times(1234);
		
		assertEquals(1234, times.start);
		assertEquals(Long.MIN_VALUE, times.stop);
	}
	
	@Test
	void times_equals() {
		EqualsVerifier.forClass(TaskTimes.Times.class).verify();
	}
	
	@Test
	@Disabled("This is the better version, being lazy for now")
	void times_toString_prints_dates_and_times() {
		// 6/20/2019 8:50:02 - 6/20/2019 9:20:22
		TaskTimes.Times times = new TaskTimes.Times(1561078202, 1561080022);
		
		assertEquals("6/20/2019 8:50:02 PM - 6/20/2019 9:20:22 PM", times.toString());
	}
	
	@Test
	void times_toString() {
		TaskTimes.Times times = new TaskTimes.Times(1561078202, 1561080022);
		
		assertEquals("1561078202 - 1561080022", times.toString());
	}
}
