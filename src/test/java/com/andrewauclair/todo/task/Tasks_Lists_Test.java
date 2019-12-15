// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.TaskException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
		tasks.addList("test");
		assertTrue(tasks.hasListWithName("/test"));
	}

	@Test
	void returned_list_should_be_unmodifiable() {
		List<Task> tasks = this.tasks.getTasks();

		assertThrows(UnsupportedOperationException.class, () -> tasks.add(new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(0)))));
	}

	@Test
	void returns_a_set_of_the_list_names() {
		tasks.addList("test");
		assertThat(tasks.getListNames()).containsOnly("/default", "/test");
	}

	@Test
	void returns_a_list_of_tasks_for_the_specified_list() {
		tasks.addTask("default List Task 1");
		tasks.addTask("default List Task 2");

		tasks.addList("test");
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
	void adding_new_list_creates_empty_folder() {
		tasks.addList("one");

		Mockito.verify(osInterface, Mockito.times(1)).createFolder("git-data/tasks/one");
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
		TaskList list = new TaskList("one", osInterface, writer, "", "");
		TaskException taskException = assertThrows(TaskException.class, () -> list.finishTask(3));
		
		assertEquals("Task 3 does not exist.", taskException.getMessage());
	}
	
	@Test
	void findListForTask_throws_exception_if_list_is_not_found() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.findListForTask(1));
		
		assertEquals("List for task 1 was not found.", taskException.getMessage());
	}
}
