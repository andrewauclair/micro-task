// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class Tasks_Lists_Test extends TaskBaseTestCase {
	@Test
	void starting_list_is_default() {
		assertEquals("default", tasks.getCurrentList());
	}
	
	@Test
	void does_not_contain_test_list_at_start() {
		assertFalse(tasks.hasListWithName("test"));
	}
	
	@Test
	void contains_list_after_creating_it() {
		tasks.addList("test");
		assertTrue(tasks.hasListWithName("test"));
	}
	
	@Test
	void returned_list_should_be_unmodifiable() {
		List<Task> tasks = this.tasks.getTasks();
		
		assertThrows(UnsupportedOperationException.class, () -> tasks.add(new Task(1, "Test")));
	}
	
	@Test
	void returns_a_set_of_the_list_names() {
		tasks.addList("test");
		assertThat(tasks.getListNames()).containsOnly("default", "test");
	}
	
	@Test
	void returns_a_list_of_tasks_for_the_specified_list() {
		tasks.addTask("default List Task 1");
		tasks.addTask("default List Task 2");
		
		tasks.addList("test");
		tasks.setCurrentList("test");
		
		tasks.addTask("test List Task 1");
		tasks.addTask("test List Task 2");
		
		tasks.setCurrentList("default");
		
		assertThat(tasks.getTasksForList("default")).containsOnly(
				new Task(1, "default List Task 1"),
				new Task(2, "default List Task 2")
		);
		
		assertThat(tasks.getTasksForList("test")).containsOnly(
				new Task(3, "test List Task 1"),
				new Task(4, "test List Task 2")
		);
	}
}
