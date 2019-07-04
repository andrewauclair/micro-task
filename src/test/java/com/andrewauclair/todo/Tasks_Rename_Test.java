// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_Rename_Test extends TaskBaseTestCase {
	@Test
	void rename_task_sets_new_name_and_keeps_id() {
		tasks.addTask("Testing rename typo hre");
		
		Task renameTask = tasks.renameTask(1, "Testing rename typo here, fixed");
		
		Task task = new Task(1, "Testing rename typo here, fixed");
		
		assertEquals(task, renameTask);
		assertThat(tasks.getTasks()).containsOnly(task);
	}
	
	@Test
	void rename_task_saves_state_and_times() {
		tasks.addTask("Inactive task");
		tasks.addTask("Active task");
		tasks.startTask(2);
		
		Task renameTask = tasks.renameTask(2, "Renaming task");
		
		Task task = new Task(2, "Renaming task", Task.TaskState.Active, Collections.singletonList(new TaskTimes(0)));
		
		assertEquals(task, renameTask);
	}
	
	@Test
	void throws_exception_if_task_was_not_found() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> tasks.renameTask(5, "Throws exception"));
		assertEquals("Task 5 was not found.", runtimeException.getMessage());
	}
	
	@Test
	void can_rename_tasks_on_a_different_list() {
		tasks.addTask("Task to rename");
		tasks.addList("testing-rename");
		tasks.setCurrentList("testing-rename");
		
		Task renameTask = tasks.renameTask(1, "Renamed this task");
		
		Task task = new Task(1, "Renamed this task");
		
		assertEquals(task, renameTask);
		
		assertThat(tasks.getTasksForList("default"))
				.containsOnly(task);
	}
	// TODO Test that you can rename a task on any list, currently it'll be stuck to the current list
}
