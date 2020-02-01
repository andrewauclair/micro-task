// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.TaskException;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_Start_Test extends TaskBaseTestCase {
	@Test
	void starting_task_assigns_it_as_the_active_task() {
		tasks.addTask("Empty task");
		Task task = tasks.addTask("Testing task start command");

		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);

		Task newActiveTask = tasks.startTask(task.id, false);

		Task oldTask = new Task(2, "Testing task start command", TaskState.Inactive, Collections.singletonList(new TaskTimes(0)));
		Task activeTask = new Task(2, "Testing task start command", TaskState.Active, Arrays.asList(new TaskTimes(2000), new TaskTimes(1234)));

		assertEquals(activeTask, tasks.getActiveTask());
		assertEquals(tasks.getActiveTask(), newActiveTask);
		assertThat(tasks.getTasks()).doesNotContain(oldTask);
		assertThat(tasks.getTasks()).contains(activeTask);
	}

	@Test
	void starting_non_existent_id_throws_exception_with_message() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.startTask(5, false));
		
		assertEquals("Task 5 does not exist.", taskException.getMessage());
	}

	@Test
	void starting_task_tells_task_writer_to_write_file() {
		tasks.addTask("Testing task add command 1");
		tasks.addTask("Testing task add command 2");

		Mockito.reset(writer);

		Task task2 = tasks.startTask(1, false);

		Mockito.verify(writer).writeTask(task2, "git-data/tasks/default/1.txt");
	}

	@Test
	void starting_task_tells_git_control_to_add_file_and_commit() {
		tasks.addTask("Testing task add command 1");
		tasks.addTask("Testing task add command 2");

		Mockito.reset(osInterface);

		tasks.startTask(2, false);

		InOrder order = Mockito.inOrder(osInterface);
		
		order.verify(osInterface).runGitCommand("git add tasks/default/2.txt", false);
		order.verify(osInterface).runGitCommand("git commit -m \"Started task 2 - 'Testing task add command 2'\"", false);
	}

	@Test
	void starting_a_task_tracks_the_time_second_it_was_started() {
		tasks.addTask("Testing Task");

		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);

		Task task = tasks.startTask(1, false);
		
		assertThat(task.getAllTimes()).containsOnly(
				new TaskTimes(1000),
				new TaskTimes(1234, Long.MIN_VALUE)
		);

		assertThat(task.getStartStopTimes()).containsOnly(
				new TaskTimes(1234, Long.MIN_VALUE)
		);
	}

	@Test
	void starting_a_task_on_a_different_list_automatically_switches_to_that_list() {
		tasks.addTask("Test 1");

		tasks.addList("test");
		tasks.setActiveList("test");

		assertEquals("/test", tasks.getActiveList());

		tasks.addTask("Test 2");

		tasks.startTask(1, false);

		assertEquals("/default", tasks.getActiveList());
	}

	@Test
	void when_auto_switching_to_new_list_the_new_active_list_changes() {
		tasks.addTask("Test 1");

		tasks.addList("test");
		tasks.setActiveList("test");

		assertEquals("/test", tasks.getActiveList());

		tasks.addTask("Test 2");

		tasks.startTask(1, false);

		Task task = tasks.stopTask();

		assertThat(tasks.getTasks()).containsOnly(task);
	}

	@Test
	void attempting_to_start_task_twice_throws_exception() {
		tasks.addTask("Test 1");

		tasks.startTask(1, false);
		
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.startTask(1, false));
		
		assertEquals("Task is already active.", taskException.getMessage());
	}

	@Test
	void starting_second_task_stops_active_task() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		tasks.startTask(1, false);

		Mockito.when(osInterface.currentSeconds()).thenReturn(1561078202L);

		tasks.startTask(2, false);

		assertThat(tasks.getTasks().stream()
				.filter(task -> task.state == TaskState.Active)).hasSize(1);
	}

	@Test
	void starting_second_task_finishes_active_task() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		tasks.startTask(1, false);

		Mockito.when(osInterface.currentSeconds()).thenReturn(1561078202L);

		tasks.startTask(2, true);

		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Test 1", TaskState.Finished, Arrays.asList(new TaskTimes(1000), new TaskTimes(3000, 1561078202L), new TaskTimes(1561078202L))),
				new Task(2, "Test 2", TaskState.Active, Arrays.asList(new TaskTimes(2000), new TaskTimes(1561078202L)))
		);
	}

	@Test
	void starting_task_from_different_group() {
		tasks.addList("/one/two/three/test");
		
		tasks.switchGroup("/one/two/three/");

		tasks.addTask("Test");
		
		tasks.switchGroup("/one/two/");

		Task task = tasks.startTask(1, false);

		assertEquals(new Task(1, "Test", TaskState.Active, Arrays.asList(new TaskTimes(1000), new TaskTimes(2000))), task);
	}

	@Test
	void starting_time_adds_times_with_project_and_feature_of_task() {
		tasks.addTask("Test");
		tasks.setProject(tasks.findListForTask(1), "Project");
		tasks.setFeature(tasks.findListForTask(1), "Feature");

		tasks.startTask(1, false);

		tasks.stopTask();
		
		tasks.setProject(tasks.findListForTask(1), "Project 2");
		tasks.setFeature(tasks.findListForTask(1), "Feature 2");

		tasks.startTask(1, false);

		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Test", TaskState.Active,
						Arrays.asList(
								new TaskTimes(1000),
								new TaskTimes(2000, 3000, "Project", "Feature"),
								new TaskTimes(4000, "Project 2", "Feature 2")
						),
						false
				)
		);
	}
}
