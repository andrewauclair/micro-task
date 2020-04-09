// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.TestUtils;
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
		assertEquals("/default", tasks.getActiveList());
	}

	@Test
	void does_not_contain_test_list_at_start() {
		assertFalse(tasks.hasListWithName("test"));
	}

	@Test
	void contains_list_after_creating_it() {
		tasks.addList("test", true);
		assertTrue(tasks.hasListWithName("/test"));
	}

	@Test
	void returned_list_should_be_unmodifiable() {
		List<Task> tasks = this.tasks.getTasks();

		assertThrows(UnsupportedOperationException.class, () -> tasks.add(new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(0)))));
	}

	@Test
	void returns_a_set_of_the_list_names() {
		tasks.addList("test", true);
		assertThat(tasks.getInProgressListNames()).containsOnly("/default", "/test");
	}

	@Test
	void returns_a_list_of_tasks_for_the_specified_list() {
		tasks.addTask("default List Task 1");
		tasks.addTask("default List Task 2");
		
		tasks.addList("test", true);
		tasks.setActiveList("test");

		tasks.addTask("test List Task 1");
		tasks.addTask("test List Task 2");

		tasks.setActiveList("default");

		assertThat(tasks.getTasksForList("default")).containsOnly(
				new Task(1, "default List Task 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000))),
				new Task(2, "default List Task 2", TaskState.Inactive, Collections.singletonList(new TaskTimes(2000)))
		);

		assertThat(tasks.getTasksForList("test")).containsOnly(
				new Task(3, "test List Task 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(3000))),
				new Task(4, "test List Task 2", TaskState.Inactive, Collections.singletonList(new TaskTimes(4000)))
		);
	}

	@Test
	void adding_list_creates_list_txt() throws IOException {
		OutputStream listStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/list.txt")).thenReturn(new DataOutputStream(listStream));

		tasks.addList("test", true);

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
		tasks.addList("/test/one", true);

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).runGitCommand("git add .");
		order.verify(osInterface).runGitCommand("git commit -m \"Created list '/test/one'\"");
	}

	@Test
	void adding_new_list_creates_empty_folder() {
		tasks.addList("one", true);

		Mockito.verify(osInterface, Mockito.times(1)).createFolder("git-data/tasks/one");
	}

	@Test
	void addList_throws_exception_if_parent_group_is_finished() {
		tasks.addGroup("/test/");
		tasks.finishGroup("/test/");

		Mockito.reset(writer, osInterface);


		TaskException taskException = assertThrows(TaskException.class, () -> tasks.addList("/test/one", true));

		assertEquals("List '/test/one' cannot be created because group '/test/' has been finished.", taskException.getMessage());

		assertFalse(tasks.getGroup("/test/").containsListAbsolute("/test/one"));

		Mockito.verifyNoInteractions(writer, osInterface);
	}

	@Test
	void setCurrentList_throws_exception_when_list_does_not_exist() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.setActiveList("/one"));
		
		assertEquals("List '/one' does not exist.", taskException.getMessage());
	}
	
	@Test
	void setCurrentList_throws_exception_when_group_does_not_exist() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.setActiveList("/one/two"));
		
		assertEquals("Group '/one/' does not exist.", taskException.getMessage());
	}
	
	@Test
	void if_task_does_not_exist_then_an_exception_is_thrown() {
		TaskList list = new TaskList("one", new TaskGroup("/"), osInterface, writer, "", "", TaskContainerState.InProgress);
		TaskException taskException = assertThrows(TaskException.class, () -> list.finishTask(3));
		
		assertEquals("Task 3 does not exist.", taskException.getMessage());
	}
	
	@Test
	void findListForTask_throws_exception_if_list_is_not_found() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.findListForTask(1));
		
		assertEquals("List for task 1 was not found.", taskException.getMessage());
	}
}
