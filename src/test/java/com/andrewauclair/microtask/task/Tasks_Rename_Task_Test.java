// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static com.andrewauclair.microtask.TestUtils.newTask;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_Rename_Task_Test extends TaskBaseTestCase {
	@Test
	void rename_task_sets_new_name_and_keeps_id() {
		tasks.addTask("Testing rename typo hre");
		
		Task renameTask = tasks.renameTask(existingID(1), "Testing rename typo here, fixed");

		Task task = newTask(existingID(1), "Testing rename typo here, fixed", TaskState.Inactive, 1000);
		
		assertEquals(task, renameTask);
		assertThat(tasks.getTasks()).containsOnly(task);
	}
	
	@Test
	void rename_task_saves_state_and_times() {
		tasks.addTask("Inactive task");
		tasks.addTask("Active task");
		tasks.startTask(existingID(2), false);
		
		Task renameTask = tasks.renameTask(existingID(2), "Renaming task");

		Task task = newTask(existingID(2), "Renaming task", TaskState.Active, 2000, Collections.singletonList(new TaskTimes(3000)));
		
		assertEquals(task, renameTask);
	}

	@Test
	void renaming_a_task_writes_the_new_file() {
		tasks.addTask("Testing the rename of a task");

		// addTask will call the writer, don't want to know about that execution
		Mockito.reset(writer);

		Task renameTask = tasks.renameTask(existingID(1), "Renaming task");

		Mockito.verify(writer).writeTask(renameTask, "git-data/tasks/default/1.txt");
	}

	@Test
	void renaming_a_task_tells_git_control_to_add_file_and_commit() {
		tasks.addTask("Testing the rename of a task");

		// addTask will call the writer, don't want to know about that execution
		Mockito.reset(writer);

		InOrder order = Mockito.inOrder(osInterface);

		tasks.renameTask(existingID(1), "Renaming task");
		
		order.verify(osInterface).gitCommit("Renamed task 1 - 'Renaming task'");
	}

	@Test
	void throws_exception_if_task_was_not_found() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.renameTask(existingID(5), "Throws exception"));
		assertEquals("Task 5 does not exist.", taskException.getMessage());
	}
	
	@Test
	void getTask_throws_exception_if_task_was_not_found() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.getTask(existingID(5)));
		assertEquals("Task 5 does not exist.", taskException.getMessage());
	}

	@Test
	void getTaskWithRelativeID_throws_exception_if_task_was_not_found() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.getTaskWithRelativeID(new RelativeTaskID(55)));
		assertEquals("Task with relative ID 55 does not exist.", taskException.getMessage());
	}

	@Test
	void can_rename_tasks_on_a_different_list() {
		tasks.addTask("Task to rename");
		tasks.addList(newList("testing-rename"), true);
		tasks.setCurrentList(existingList("testing-rename"));
		
		Task renameTask = tasks.renameTask(existingID(1), "Renamed this task");

		Task task = newTask(existingID(1), "Renamed this task", TaskState.Inactive, 1000);
		
		assertEquals(task, renameTask);
		
		assertThat(tasks.getTasksForList(existingList("default")))
				.containsOnly(task);
	}

	@Test
	void finished_task_can_not_be_renamed() {
		tasks.addTask("Test");
		tasks.finishTask(existingID(1));

		Mockito.reset(writer, osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.renameTask(existingID(1), "new title"));

		assertEquals("Task 1 cannot be renamed because it has been finished.", taskException.getMessage());

		Mockito.verifyNoInteractions(writer, osInterface);
	}
}
