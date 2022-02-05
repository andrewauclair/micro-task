// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class Tasks_Set_Test extends TaskBaseTestCase {
	@Test
	void set_recurring_writes_file() {
		tasks.addTask("Test 1");
		Mockito.reset(osInterface);

		Task task = tasks.setRecurring(existingID(1), true);

		Mockito.verify(writer).writeTask(task, "git-data/tasks/default/1.txt");

		assertTrue(task.recurring);
	}
	
	@Test
	void set_recurring_adds_and_commits_file_to_git() {
		InOrder order = Mockito.inOrder(osInterface);

		tasks.addTask("Test 1");
		Mockito.reset(osInterface);

		Task task = tasks.setRecurring(existingID(1), false);
		
		order.verify(osInterface).gitCommit("Set recurring for task 1 to false");

		assertFalse(task.recurring);
	}

	@Test
	void set_tags_adds_and_commits_file_to_git() {
		InOrder order = Mockito.inOrder(osInterface);

		tasks.addTask("Test 1");
		Mockito.reset(osInterface);

		Task task = tasks.setTags(existingID(1), Arrays.asList("one", "two"));

		order.verify(osInterface).gitCommit("Set tag(s) for task 1 to one, two");

		assertThat(task.tags).containsOnly("one", "two");
	}
	@Test
	void exception_is_thrown_when_trying_to_set_recurring_on_finished_task() {
		Task task = tasks.addTask("Test 1");
		tasks.finishTask(existingID(1));

		Mockito.reset(writer, osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.setRecurring(existingID(1), true));

		assertEquals("Cannot set task 1 recurring state. The task has been finished.", taskException.getMessage());

		assertFalse(task.recurring);

		Mockito.verifyNoInteractions(writer, osInterface);
	}
}
