// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import java.util.List;

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
}
