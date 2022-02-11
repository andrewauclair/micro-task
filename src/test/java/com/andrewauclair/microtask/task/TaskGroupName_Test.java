// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskGroupName_Test extends TaskBaseTestCase {
	@Test
	void throws_exception_if_name_is_group_name() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskGroupName(tasks, "test"){});

		assertEquals("Group name must end in /", runtimeException.getMessage());
	}

	@Test
	void backslashes_are_not_allowed() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskGroupName(tasks, "te\\st/"){});

		assertEquals("Illegal character in group name: '\\'", runtimeException.getMessage());
	}

	@Test
	void less_thans_are_not_allowed() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskGroupName(tasks, "te<st/"){});

		assertEquals("Illegal character in group name: '<'", runtimeException.getMessage());
	}

	@Test
	void greater_thans_are_not_allowed() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskGroupName(tasks, "te>st/"){});

		assertEquals("Illegal character in group name: '>'", runtimeException.getMessage());
	}

	@Test
	void colons_are_not_allowed() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskGroupName(tasks, "te:st/"){});

		assertEquals("Illegal character in group name: ':'", runtimeException.getMessage());
	}

	@Test
	void double_quotes_are_not_allowed() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskGroupName(tasks, "te\"st/"){});

		assertEquals("Illegal character in group name: '\"'", runtimeException.getMessage());
	}

	@Test
	void vertical_bar_pipes_are_not_allowed() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskGroupName(tasks, "te|st/"){});

		assertEquals("Illegal character in group name: '|'", runtimeException.getMessage());
	}

	@Test
	void question_marks_are_not_allowed() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskGroupName(tasks, "te?st/"){});

		assertEquals("Illegal character in group name: '?'", runtimeException.getMessage());
	}

	@Test
	void asterisks_are_not_allowed() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskGroupName(tasks, "te*st/"){});

		assertEquals("Illegal character in group name: '*'", runtimeException.getMessage());
	}
}
