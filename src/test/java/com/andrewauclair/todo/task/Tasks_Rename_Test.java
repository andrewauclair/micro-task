// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_Rename_Test extends TaskBaseTestCase {
	@Test
	void rename_task_sets_new_name_and_keeps_id() {
		tasks.addTask("Testing rename typo hre");
		
		Task renameTask = tasks.renameTask(1, "Testing rename typo here, fixed");

		Task task = new Task(1, "Testing rename typo here, fixed", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)));
		
		assertEquals(task, renameTask);
		assertThat(tasks.getTasks()).containsOnly(task);
	}
	
	@Test
	void rename_task_saves_state_and_times() {
		tasks.addTask("Inactive task");
		tasks.addTask("Active task");
		tasks.startTask(2, false);
		
		Task renameTask = tasks.renameTask(2, "Renaming task");

		Task task = new Task(2, "Renaming task", TaskState.Active, Arrays.asList(new TaskTimes(2000), new TaskTimes(3000)));
		
		assertEquals(task, renameTask);
	}

	@Test
	void renaming_a_task_writes_the_new_file() {
		tasks.addTask("Testing the rename of a task");

		// addTask will call the writer, don't want to know about that execution
		Mockito.reset(writer);

		Task renameTask = tasks.renameTask(1, "Renaming task");

		Mockito.verify(writer).writeTask(renameTask, "git-data/tasks/default/1.txt");
	}

	@Test
	void renaming_a_task_tells_git_control_to_add_file_and_commit() {
		tasks.addTask("Testing the rename of a task");

		// addTask will call the writer, don't want to know about that execution
		Mockito.reset(writer);

		InOrder order = Mockito.inOrder(osInterface);

		tasks.renameTask(1, "Renaming task");

		order.verify(osInterface).runGitCommand("git add tasks/default/1.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Renamed task 1 - 'Renaming task'\"");
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

		Task task = new Task(1, "Renamed this task", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)));
		
		assertEquals(task, renameTask);
		
		assertThat(tasks.getTasksForList("default"))
				.containsOnly(task);
	}

	@Test
	void renaming_list_deletes_the_current_task_files_in_the_folder() {
		tasks.addList("one");

		tasks.setCurrentList("one");

		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		Mockito.reset(osInterface); // reset the os interface after it does all the git adds and commits above

		tasks.renameList("one", "test");

		Mockito.verify(osInterface).removeFile("git-data/tasks/one/1.txt");
		Mockito.verify(osInterface).removeFile("git-data/tasks/one/2.txt");
		Mockito.verify(osInterface).removeFile("git-data/tasks/one");
	}

	@Test
	void renaming_list_writes_new_task_files_into_new_folder() {
		tasks.addList("one");

		tasks.setCurrentList("one");

		Task task1 = tasks.addTask("Test 1");
		Task task2 = tasks.addTask("Test 2");

		Mockito.reset(writer); // reset the writer after it does all the add writes above

		tasks.renameList("one", "test");

		Mockito.verify(writer).writeTask(task1, "git-data/tasks/test/1.txt");
		Mockito.verify(writer).writeTask(task2, "git-data/tasks/test/2.txt");
	}

	@Test
	void renaming_list_tells_git_control_to_add_new_task_files_and_commit() {
		tasks.addList("one");

		tasks.setCurrentList("one");

		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		Mockito.reset(osInterface); // reset the os interface after it does all the git adds and commits above

		tasks.renameList("one", "test");

		InOrder order = Mockito.inOrder(osInterface);

//		order.verify(osInterface).runGitCommand("git add tasks/one/1.txt");
//		order.verify(osInterface).runGitCommand("git add tasks/test/1.txt");
//		order.verify(osInterface).runGitCommand("git add tasks/one/2.txt");
//		order.verify(osInterface).runGitCommand("git add tasks/test/2.txt");
		order.verify(osInterface).runGitCommand("git add .");
		order.verify(osInterface).runGitCommand("git commit -m \"Renamed list '/one' to '/test'\"");
	}

	@Test
	void renaming_current_list_changes_the_name_of_current_list() {
		tasks.addList("one");

		tasks.setCurrentList("one");

		tasks.renameList("one", "two");

		assertEquals("/two", tasks.getCurrentList());
	}

	@Test
	void renaming_active_list_changes_the_name_of_active_list() {
		tasks.addList("one");

		tasks.setCurrentList("one");

		tasks.addTask("Test");

		tasks.startTask(1, false);

		tasks.renameList("one", "two");

		assertEquals("/two", tasks.getActiveTaskList());
	}

	@Test
	void list_rename_throws_exception_if_old_list_is_not_found() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> tasks.renameList("old", "new"));

		assertEquals("List '/old' not found.", runtimeException.getMessage());
	}
	
	@Test
	void list_rename_throws_exception_if_old_group_does_not_exist() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> tasks.renameList("/one/two", "/one/three"));
		
		assertEquals("Group '/one' not found.", runtimeException.getMessage());
	}
}
