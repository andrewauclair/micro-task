// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.TaskState;
import org.junit.jupiter.api.Test;

import static com.andrewauclair.microtask.TestUtils.newTaskBuilder;
import static org.assertj.core.api.Assertions.assertThat;

public class Commands_Set_Task_Tags_Test extends CommandsBaseTestCase {
	@Test
	void add_tags_to_task() {
		tasks.addTask("Test");

		commands.execute(printStream, "set task 1 --add-tags one,two");

		assertThat(tasks.getTask(existingID(1)).tags).containsOnly("one", "two");

		assertOutput(
				"Task 1, add tag(s): one, two",
				""
		);
	}

	@Test
	void remove_tags_from_task() {
		tasks.addTask(newTaskBuilder(1, "Test", TaskState.Inactive, 1234)
				.withTag("one")
				.withTag("two")
				.withTag("three")
				.build());

		commands.execute(printStream, "set task 1 --remove-tags one,two");

		assertThat(tasks.getTask(existingID(1)).tags).containsOnly("three");

		assertOutput(
				"Task 1, remove tag(s): one, two",
				""
		);
	}
}
