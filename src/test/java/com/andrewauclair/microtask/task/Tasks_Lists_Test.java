// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.TestUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class Tasks_Lists_Test extends TaskBaseTestCase {
	@Test
	void starting_list_is_default() {
		assertEquals(existingList("/default"), tasks.getActiveList());
	}

	@Test
	void does_not_contain_test_list_at_start() {
		TaskListFinder finder = new TaskListFinder(tasks);
		
		TaskListName listName = new TaskListName(tasks, "test") {};
		
		assertFalse(finder.hasList(listName));
	}

	@Test
	void contains_list_after_creating_it() {
		tasks.addList(newList("test"), true);
		
		TaskListFinder finder = new TaskListFinder(tasks);
		
		TaskListName listName = new TaskListName(tasks, "test") {};
		
		assertTrue(finder.hasList(listName));
	}

	@Test
	void returned_list_should_be_unmodifiable() {
		List<Task> tasks = this.tasks.getTasks();

		assertThrows(UnsupportedOperationException.class, () -> tasks.add(new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(0)))));
	}

	@Test
	void returns_a_set_of_the_list_names() {
		tasks.addList(newList("test"), true);
		assertThat(tasks.getInProgressListNames()).containsOnly("/default", "/test");
	}

	@Test
	void returns_a_list_of_tasks_for_the_specified_list() {
		tasks.addTask("default List Task 1");
		tasks.addTask("default List Task 2");
		
		tasks.addList(newList("test"), true);
		tasks.setActiveList(existingList("test"));

		tasks.addTask("test List Task 1");
		tasks.addTask("test List Task 2");

		tasks.setActiveList(existingList("default"));

		assertThat(tasks.getTasksForList(existingList("default"))).containsOnly(
				new Task(1, "default List Task 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000))),
				new Task(2, "default List Task 2", TaskState.Inactive, Collections.singletonList(new TaskTimes(2000)))
		);

		assertThat(tasks.getTasksForList(existingList("test"))).containsOnly(
				new Task(3, "test List Task 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(3000))),
				new Task(4, "test List Task 2", TaskState.Inactive, Collections.singletonList(new TaskTimes(4000)))
		);
	}

	@Test
	void adding_list_creates_list_txt() throws IOException {
		OutputStream listStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/list.txt")).thenReturn(new DataOutputStream(listStream));

		tasks.addList(newList("test"), true);

		Mockito.verify(osInterface).createOutputStream("git-data/tasks/test/list.txt");

		TestUtils.assertOutput(listStream,
				"",
				"",
				"InProgress",
				""
		);
	}

	@Test
	void adding_list_commits_the_list_txt_file() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).gitCommit("Created list '/test/one'");
	}

	@Test
	void adding_new_list_creates_empty_folder() {
		tasks.addList(newList("one"), true);

		Mockito.verify(osInterface, Mockito.times(1)).createFolder("git-data/tasks/one");
	}

	@Test
	void addList_throws_exception_if_parent_group_is_finished() {
		tasks.addGroup(newGroup("/test/"));
		tasks.finishGroup(existingGroup("/test/"));

		Mockito.reset(writer, osInterface);


		TaskException taskException = assertThrows(TaskException.class, () -> tasks.addList(newList("/test/one"), true));

		assertEquals("List '/test/one' cannot be created because group '/test/' has been finished.", taskException.getMessage());

		assertFalse(tasks.getGroup("/test/").containsListAbsolute("/test/one"));

		Mockito.verifyNoInteractions(writer, osInterface);
	}

	@Test
	void does_not_throw_exception_for_finished_list_when_not_creating_files() {
		tasks.addGroup(newGroup("/test/"));
		tasks.finishGroup(existingGroup("/test/"));

		assertDoesNotThrow(() -> tasks.addList(newList("/test/one"), false));

		assertTrue(tasks.getGroup("/test/").containsListAbsolute("/test/one"));
	}

	@Test
	void setCurrentList_throws_exception_when_list_does_not_exist() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.setActiveList(existingList("/one")));
		
		assertEquals("List '/one' does not exist.", taskException.getMessage());
	}
	
	@Test
	@Disabled("This now throws a list does not exist exception, I don't think we need to check this once we make addTask take an ExistingTaskListName instance")
	void setCurrentList_throws_exception_when_group_does_not_exist() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.setActiveList(existingList("/one/two")));
		
		assertEquals("Group '/one/' does not exist.", taskException.getMessage());
	}
	
	@Test
	void if_task_does_not_exist_then_an_exception_is_thrown() {
		TaskList list = new TaskList("one", new TaskGroup("/"), osInterface, writer, "", "", TaskContainerState.InProgress);
		TaskException taskException = assertThrows(TaskException.class, () -> list.finishTask(existingID(3)));
		
		assertEquals("Task 3 does not exist.", taskException.getMessage());
	}
	
	@Test
	@Disabled("Using the ExistingID class means this will never work again as is")
	void findListForTask_throws_exception_if_list_is_not_found() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.findListForTask(existingID(1)));
		
		assertEquals("List for task 1 was not found.", taskException.getMessage());
	}
}
