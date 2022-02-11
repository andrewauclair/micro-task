// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskListName_Test extends TaskBaseTestCase {
	@Test
	void throws_exception_if_name_is_group_name() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskListName(tasks, "test/") {});

		assertEquals("List name must not end in /", runtimeException.getMessage());
	}

	@Test
	void backslashes_are_not_allowed() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskListName(tasks, "te\\st"){});

		assertEquals("Illegal character in list name: '\\'", runtimeException.getMessage());
	}

	@Test
	void less_thans_are_not_allowed() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskListName(tasks, "te<st"){});

		assertEquals("Illegal character in list name: '<'", runtimeException.getMessage());
	}

	@Test
	void greater_thans_are_not_allowed() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskListName(tasks, "te>st"){});

		assertEquals("Illegal character in list name: '>'", runtimeException.getMessage());
	}

	@Test
	void colons_are_not_allowed() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskListName(tasks, "te:st"){});

		assertEquals("Illegal character in list name: ':'", runtimeException.getMessage());
	}

	@Test
	void double_quotes_are_not_allowed() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskListName(tasks, "te\"st"){});

		assertEquals("Illegal character in list name: '\"'", runtimeException.getMessage());
	}

	@Test
	void vertical_bar_pipes_are_not_allowed() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskListName(tasks, "te|st"){});

		assertEquals("Illegal character in list name: '|'", runtimeException.getMessage());
	}

	@Test
	void question_marks_are_not_allowed() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskListName(tasks, "te?st"){});

		assertEquals("Illegal character in list name: '?'", runtimeException.getMessage());
	}

	@Test
	void asterisks_are_not_allowed() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskListName(tasks, "te*st"){});

		assertEquals("Illegal character in list name: '*'", runtimeException.getMessage());
	}
}
