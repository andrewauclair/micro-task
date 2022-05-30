// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.TestUtils;
import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.project.NewFeature;
import com.andrewauclair.microtask.project.NewProject;
import com.andrewauclair.microtask.project.Project;
import com.andrewauclair.microtask.task.build.TaskBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Collections;

import static com.andrewauclair.microtask.TestUtils.newTask;
import static com.andrewauclair.microtask.TestUtils.newTaskBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_Start_Test extends TaskBaseTestCase {
	@Test
	void starting_task_assigns_it_as_the_active_task() {
		tasks.addTask("Empty task");
		Task task = tasks.addTask("Testing task start command");

		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);

		Task newActiveTask = tasks.startTask(task.ID(), false);

		Task oldTask = TestUtils.existingTask(existingID(2), "Testing task start command", TaskState.Inactive, 0).build();
		Task activeTask = TestUtils.existingTask(existingID(2), "Testing task start command", TaskState.Active, 2000, Collections.singletonList(new TaskTimes(1234)));

		assertEquals(activeTask, tasks.getActiveTask());
		assertEquals(tasks.getActiveTask(), newActiveTask);
		assertThat(tasks.getTasks()).doesNotContain(oldTask);
		assertThat(tasks.getTasks()).contains(activeTask);
	}

	@Test
	void starting_non_existent_id_throws_exception_with_message() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.startTask(existingID(5), false));
		
		assertEquals("Task 5 does not exist.", taskException.getMessage());
	}

	@Test
	void starting_task_tells_task_writer_to_write_file() {
		tasks.addTask("Testing task add command 1");
		tasks.addTask("Testing task add command 2");

		Mockito.reset(writer);

		Task task2 = tasks.startTask(existingID(1), false);

		Mockito.verify(writer).writeTask(task2, "git-data/tasks/default/1.txt");
	}

	@Test
	void starting_task_tells_git_control_to_add_file_and_commit() {
		tasks.addTask("Testing task add command 1");
		tasks.addTask("Testing task add command 2");

		Mockito.reset(osInterface);

		tasks.startTask(existingID(2), false);

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).gitCommit("Started task 2 - 'Testing task add command 2'");
	}

	@Test
	void starting_a_task_tracks_the_time_second_it_was_started() {
		tasks.addTask("Testing Task");

		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);

		Task task = tasks.startTask(existingID(1), false);

		assertEquals(1000, task.addTime);
		assertThat(task.startStopTimes).containsOnly(
				new TaskTimes(1234, Long.MIN_VALUE)
		);

		//		// exclude add and finish when finished
//		if (state == TaskState.Finished) {
//			return startStopTimes.subList(1, startStopTimes.size() - 1);
//		}
//		// exclude add
//		return startStopTimes.subList(1, startStopTimes.size());
		assertThat(task.startStopTimes).containsOnly(
				new TaskTimes(1234, Long.MIN_VALUE)
		);
	}

	@Test
	void starting_a_task_on_a_different_list_automatically_switches_to_that_list() {
		tasks.addTask("Test 1");
		
		tasks.addList(newList("test"), true);
		tasks.setCurrentList(existingList("test"));

		assertEquals(existingList("/test"), tasks.getCurrentList());

		tasks.addTask("Test 2");

		tasks.startTask(existingID(1), false);

		assertEquals(existingList("/default"), tasks.getCurrentList());
	}

	@Test
	void when_auto_switching_to_new_list_the_new_active_list_changes() {
		tasks.addTask("Test 1");
		
		tasks.addList(newList("test"), true);
		tasks.setCurrentList(existingList("test"));

		assertEquals(existingList("/test"), tasks.getCurrentList());

		tasks.addTask("Test 2");

		tasks.startTask(existingID(1), false);

		Task task = tasks.stopTask();

		assertThat(tasks.getTasks()).containsOnly(task);
	}

	@Test
	void attempting_to_start_task_twice_throws_exception() {
		tasks.addTask("Test 1");

		tasks.startTask(existingID(1), false);
		
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.startTask(existingID(1), false));
		
		assertEquals("Task is already active.", taskException.getMessage());
	}

	@Test
	void starting_second_task_stops_active_task() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		tasks.startTask(existingID(1), false);

		Mockito.when(osInterface.currentSeconds()).thenReturn(1561078202L);

		tasks.startTask(existingID(2), false);

		assertThat(tasks.getTasks().stream()
				.filter(task -> task.state == TaskState.Active)).hasSize(1);
	}

	@Test
	void starting_second_task_finishes_active_task() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		tasks.startTask(existingID(1), false);

		Mockito.when(osInterface.currentSeconds()).thenReturn(1561078202L);

		tasks.startTask(existingID(2), true);

		assertThat(tasks.getTasks()).containsOnly(
				TestUtils.existingTask(existingID(1), "Test 1", TaskState.Finished, 1000, 1561078202L, Collections.singletonList(new TaskTimes(3000, 1561078202L))).build(),
				TestUtils.existingTaskBuilder(existingID(2), "Test 2", TaskState.Active, 2000, Collections.singletonList(new TaskTimes(1561078202L))).withDueTime(2000 + Tasks.DEFAULT_DUE_TIME).build()
		);
	}

	@Test
	void starting_task_from_different_group() {
		tasks.addGroup(newGroup("/one/two/three/"));
		tasks.addList(newList("/one/two/three/test"), true);
		
		tasks.setCurrentGroup(existingGroup("/one/two/three/"));

		tasks.addTask("Test");
		
		tasks.setCurrentGroup(existingGroup("/one/two/"));

		Task task = tasks.startTask(existingID(1), false);

		assertEquals(TestUtils.existingTask(existingID(1), "Test", TaskState.Active, 1000, Collections.singletonList(new TaskTimes(2000))), task);
	}

	@Test
	void starting_time_adds_times_with_project_and_feature_of_task() {
		tasks.createGroup(newGroup("/projects/project-1/"));
		tasks.createGroup(newGroup("/projects/project-2/"));
		tasks.addList(newList("/projects/project-1/one"), true);
		tasks.addList(newList("/projects/project-2/two"), true);

		projects.createProject(new NewProject(projects, "project-1"), true);
		projects.createProject(new NewProject(projects, "project-2"), true);

		Project project1 = projects.getProject(new ExistingProject(projects, "project-1"));
		Project project2 = projects.getProject(new ExistingProject(projects, "project-2"));

		project1.addFeature(new NewFeature(project1, "one"), true);
		project2.addFeature(new NewFeature(project2, "two"), true);

		tasks.setCurrentList(existingList("/projects/project-1/one"));
		tasks.addTask("Test");

		tasks.startTask(existingID(3), false);

		tasks.stopTask();

		tasks.moveTask(existingID(3), existingList("/projects/project-2/two"));

		tasks.startTask(existingID(3), false);

		Task expectedTask = new TaskBuilder(existingID(3))
				.withTask("Test")
				.withState(TaskState.Active)
				.withAddTime(3000)
				.withDueTime(607800)
				.withStartStopTime(new TaskTimes(4000, 5000, "project-1", "one"))
				.withStartStopTime(new TaskTimes(6000, "project-2", "two"))
				.build();

		assertThat(tasks.getTasks()).containsOnly(
				expectedTask
		);
	}
	
	@Test
	void finished_tasks_cannot_be_started() {
		tasks.addTask(newTask(newID(1), idValidator, "Test", TaskState.Finished, 1234, Collections.singletonList(new TaskTimes(2345))));
		
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.startTask(existingID(1), false));
		
		assertEquals("Task has already been finished.", taskException.getMessage());
	}
}
