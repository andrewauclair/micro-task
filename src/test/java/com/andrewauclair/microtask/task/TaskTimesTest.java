// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

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
	void create_active_times_with_project_and_feature() {
		TaskTimes times = new TaskTimes(1234, "Test/Project", "Feature");

		assertEquals(1234, times.start);
		assertEquals(Long.MIN_VALUE, times.stop);
		assertEquals("Test/Project", times.project);
		assertEquals("Feature", times.feature);
	}

	@Test
	void create_finished_times_with_project_and_feature() {
		TaskTimes times = new TaskTimes(1234, 4567, "Test/Project", "Feature");

		assertEquals(1234, times.start);
		assertEquals(4567, times.stop);
		assertEquals("Test/Project", times.project);
		assertEquals("Feature", times.feature);
	}

	@Test
	void task_times_equals() {
		EqualsVerifier.forClass(TaskTimes.class).verify();
	}

	@Test
	void times_description_prints_dates_and_times() {
		TaskTimes times = new TaskTimes(1561078202, 1561080022);

		Locale.setDefault(Locale.US);
		assertEquals("06/20/2019 08:50:02 PM - 06/20/2019 09:20:22 PM", times.description(ZoneId.of("America/New_York")));
	}

	@Test
	void times_description_prints_only_start_time_for_active_tasks() {
		TaskTimes times = new TaskTimes(1561078202);

		Locale.setDefault(Locale.US);
		assertEquals("06/20/2019 08:50:02 PM -", times.description(ZoneId.of("America/New_York")));
	}

	@Test
	void times_toString() {
		TaskTimes times = new TaskTimes(1561078202, 1561080022, "Project", "Feature");

		assertEquals("1561078202 - 1561080022, project='Project', feature='Feature'", times.toString());
	}

	@Test
	void start_time_can_not_be_before_stop_time() {
		TaskException taskException = assertThrows(TaskException.class, () -> new TaskTimes(2000, 1000));
		
		assertEquals("Stop time can not come before start time.", taskException.getMessage());
	}

	@Test
	void exception_is_only_thrown_if_stop_time_is_set() {
		assertDoesNotThrow(() -> new TaskTimes(1000, TaskTimes.TIME_NOT_SET));
	}
}
