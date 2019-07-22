// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Tasks_IssueTracking_Test extends TaskBaseTestCase {
	@Test
	void set_issue_in_task() {
		tasks.addTask("Test 1");

		tasks.setIssue(1, 12345);

		Optional<Task> task = tasks.getTask(1);

		assertThat(task).isPresent();

		assertEquals(12345, task.get().getIssue());
		
		assertThat(tasks.getTasks()).containsOnly(task.get());
	}
}
