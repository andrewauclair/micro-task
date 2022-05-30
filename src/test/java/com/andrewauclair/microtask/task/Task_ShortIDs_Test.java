// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// tasks have short IDs, assigned in order of active tasks. Tasks are renumbered when a task is finished
class Task_ShortIDs_Test extends TaskBaseTestCase {
	@Test
	void short_id_matches_full_id_with_all_active_tasks() {
		for (int i = 0; i < 10; i++) {
			tasks.addTask("Test");
		}

		for (int i = 1; i <= 10; i++) {
			assertEquals(i, tasks.getTask(existingID(i)).fullID().ID());
			assertEquals(i, tasks.getTask(existingID(i)).shortID().ID());
		}
	}

	@Test
	void short_id_is_renumbered_when_task_is_finished() {
		List<Task> activeTasks = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			activeTasks.add(tasks.addTask("Test"));
		}

		tasks.finishTask(existingID(3));

		for (int i = 1; i < 10; i++) {
			assertEquals(i + 1, activeTasks.get(i).fullID().ID());
		}

		assertEquals(1, tasks.getTask(existingID(1)).shortID().ID());
		assertEquals(2, tasks.getTask(existingID(2)).shortID().ID());
		assertEquals(-1, tasks.getTask(existingID(3)).shortID().ID()); // finished
		assertEquals(3, tasks.getTask(existingID(4)).shortID().ID());
		assertEquals(4, tasks.getTask(existingID(5)).shortID().ID());
		assertEquals(5, tasks.getTask(existingID(6)).shortID().ID());
		assertEquals(6, tasks.getTask(existingID(7)).shortID().ID());
		assertEquals(7, tasks.getTask(existingID(8)).shortID().ID());
		assertEquals(8, tasks.getTask(existingID(9)).shortID().ID());
		assertEquals(9, tasks.getTask(existingID(10)).shortID().ID());
	}

	@Test
	void starting_task_keeps_short_id() {
		for (int i = 0; i < 10; i++) {
			tasks.addTask("Test");
		}

		tasks.startTask(existingID(4), false);

		for (int i = 1; i <= 10; i++) {
			assertEquals(i, tasks.getTask(existingID(i)).fullID().ID());
			assertEquals(i, tasks.getTask(existingID(i)).shortID().ID());
		}
	}
}
