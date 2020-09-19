// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class Tasks_Move_Test extends TaskBaseTestCase {
	@Test
	void moving_task_moves_it_to_the_new_list() {
		Task task = tasks.addTask("Task to move");
		tasks.addList(newList("one"), true);

		tasks.moveTask(existingID(1), new ExistingListName(tasks, "one"));

		assertThat(tasks.getTasksForList(existingList("default")))
				.isEmpty();

		assertThat(tasks.getTasksForList(existingList("one")))
				.containsOnly(task);
	}

	@Test
	void moving_task_deletes_the_current_task_files_in_the_folder() {
		tasks.addTask("Test 1");
		tasks.addList(newList("one"), true);

		tasks.moveTask(existingID(1), new ExistingListName(tasks, "one"));

		Mockito.verify(osInterface).removeFile("git-data/tasks/default/1.txt");

	}

	@Test
	void moving_task_writes_new_task_files_into_new_folder() {
		tasks.addTask("Test 1");
		tasks.addList(newList("one"), true);

		Task task = tasks.moveTask(existingID(1), new ExistingListName(tasks, "one"));

		Mockito.verify(writer).writeTask(task, "git-data/tasks/one/1.txt");
	}

	@Test
	void moving_task_tells_git_control_to_add_new_task_file_and_commit() {
		tasks.addTask("Test 1");
		tasks.addList(newList("one"), true);

		InOrder order = Mockito.inOrder(osInterface);

		tasks.moveTask(existingID(1), new ExistingListName(tasks, "one"));

		order.verify(osInterface).gitCommit("Moved task 1 - 'Test 1' to list '/one'");
	}

	@Test
	void can_move_task_on_different_list() {
		Task task = tasks.addTask("Task to move");
		tasks.addList(newList("one"), true);
		tasks.setCurrentList(existingList("one"));

		tasks.moveTask(existingID(1), new ExistingListName(tasks, "one"));

		assertThat(tasks.getTasksForList(existingList("default")))
				.isEmpty();

		assertThat(tasks.getTasksForList(existingList("one")))
				.containsOnly(task);
	}

	@Test
	void moving_the_active_task_changes_active_list() {
		tasks.addTask("Test 1");
		tasks.addList(newList("one"), true);

		tasks.startTask(existingID(1), false);

		tasks.moveTask(existingID(1), new ExistingListName(tasks, "one"));

		assertEquals("/one", tasks.getActiveTaskList().absoluteName());
	}

	@Test
	void moving_inactive_task_does_not_change_active_task_list() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addList(newList("one"), true);

		tasks.startTask(existingID(2), false);

		tasks.moveTask(existingID(1), new ExistingListName(tasks, "one"));

		assertEquals("/default", tasks.getActiveTaskList().absoluteName());
	}

	@Test
	void moving_list_moves_folder_of_files() throws IOException {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);

		InOrder order = Mockito.inOrder(osInterface);

		tasks.moveList(existingList("/test/one"), existingGroup("/"));

		order.verify(osInterface).moveFolder("/test/one", "/one");
		order.verify(osInterface).gitCommit("Moved list '/test/one' to group '/'");
	}

	@Test
	void moving_group_moves_folder_of_files() throws IOException {
		tasks.createGroup(newGroup("/one/"));
		tasks.createGroup(newGroup("/two/"));

		Mockito.reset(osInterface);

		InOrder order = Mockito.inOrder(osInterface);

		tasks.moveGroup(existingGroup("/one/"), existingGroup("/two/"));

		order.verify(osInterface).moveFolder("/one/", "/two/one/");
		order.verify(osInterface).gitCommit("Moved group '/one/' to group '/two/'");
	}

	@Test
	void moving_active_list_changes_active_list_name() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);
		tasks.setCurrentList(existingList("/test/one"));

		tasks.moveList(existingList("/test/one"), existingGroup("/"));

		assertEquals(existingList("/one"), tasks.getCurrentList());
	}

	@Test
	void moving_active_group_changes_active_group_name() {
		tasks.createGroup(newGroup("/one/"));
		tasks.createGroup(newGroup("/two/"));

		tasks.setCurrentGroup(existingGroup("/one/"));

		tasks.moveGroup(existingGroup("/one/"), existingGroup("/two/"));

		assertEquals("/two/one/", tasks.getCurrentGroup().getFullPath());
	}

	@Test
	void throws_exception_trying_to_move_list_that_does_not_exist() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.moveList(existingList("/one"), existingGroup("/")));

		assertEquals("List '/one' does not exist.", taskException.getMessage());
	}

	@Test
	void throws_exception_when_destination_group_does_not_exist() {
		tasks.addList(newList("one"), true);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.moveList(existingList("/one"), existingGroup("/test/")));

		assertEquals("Group '/test/' does not exist.", taskException.getMessage());
	}

	@Test
	void throws_exception_trying_to_move_group_that_does_not_exist() {
		tasks.createGroup(newGroup("/two/"));

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.moveGroup(existingGroup("/one/"), existingGroup("/two/")));

		assertEquals("Group '/one/' does not exist.", taskException.getMessage());
	}

	@Test
	void throws_exception_trying_to_move_to_group_that_does_not_exist() {
		tasks.createGroup(newGroup("/one/"));

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.moveGroup(existingGroup("/one/"), existingGroup("/two/")));

		assertEquals("Group '/two/' does not exist.", taskException.getMessage());
	}

	@Test
	void catch_moveFolder_io_exception_in_moveList() throws IOException {
		Mockito.doThrow(IOException.class).when(osInterface).moveFolder(Mockito.anyString(), Mockito.anyString());

		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);
		tasks.setCurrentList(existingList("/test/one"));

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.moveList(existingList("/test/one"), existingGroup("/")));

		assertEquals("Failed to move list folder.", taskException.getMessage());

		Assertions.assertEquals("java.io.IOException" + Utils.NL, this.outputStream.toString());
	}

	@Test
	void catch_moveFolder_io_exception_in_moveGroup() throws IOException {
		Mockito.doThrow(IOException.class).when(osInterface).moveFolder(Mockito.anyString(), Mockito.anyString());

		tasks.createGroup(newGroup("/test/one/"));
		tasks.setCurrentGroup(existingGroup("/test/one/"));

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.moveGroup(existingGroup("/test/one/"), existingGroup("/")));

		assertEquals("Failed to move group folder.", taskException.getMessage());

		Assertions.assertEquals("java.io.IOException" + Utils.NL, this.outputStream.toString());
	}

	@Test
	void throws_exception_if_task_was_not_found() {
		tasks.addList(newList("one"), true);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.moveTask(existingID(5), new ExistingListName(tasks, "one")));
		assertEquals("Task 5 does not exist.", taskException.getMessage());
	}

	@Test
	void moving_task_throws_exception_if_move_to_list_is_not_found() {
		tasks.addTask("Test 1");

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.moveTask(existingID(1), new ExistingListName(tasks, "one")));
		assertEquals("List '/one' does not exist.", taskException.getMessage());
	}

	@Test
	void task_already_on_list() {
		tasks.addTask("Test");

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.moveTask(existingID(1), new ExistingListName(tasks, "/default")));

		assertEquals("Task 1 is already on list '/default'.", taskException.getMessage());
	}

	@Test
	void throws_exception_if_attempting_to_move_task_from_finished_list() {
		tasks.addTask("Test");
		tasks.addList(newList("/one"), true);
		tasks.finishList(existingList("/default"));

		Mockito.reset(writer, osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.moveTask(existingID(1), new ExistingListName(tasks, "/one")));

		assertEquals("Task 1 cannot be moved because list '/default' has been finished.", taskException.getMessage());

		assertTrue(tasks.getListByName(existingList("/default")).containsTask(1));

		Mockito.verifyNoInteractions(writer, osInterface);
	}

	@Test
	void throws_exception_if_attempting_to_move_task_to_finished_list() {
		tasks.addTask("Test");
		tasks.addList(newList("/one"), true);
		tasks.finishList(existingList("/one"));

		Mockito.reset(writer, osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.moveTask(existingID(1), new ExistingListName(tasks, "/one")));

		assertEquals("Task 1 cannot be moved because list '/one' has been finished.", taskException.getMessage());

		assertTrue(tasks.getListByName(existingList("/default")).containsTask(1));

		Mockito.verifyNoInteractions(writer, osInterface);
	}

	@Test
	void throws_exception_if_attempting_to_move_task_that_has_been_finished() {
		tasks.addTask("Test");
		tasks.finishTask(existingID(1));
		tasks.addList(newList("/one"), true);

		Mockito.reset(writer, osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.moveTask(existingID(1), new ExistingListName(tasks, "/one")));

		assertEquals("Task 1 cannot be moved because it has been finished.", taskException.getMessage());

		assertTrue(tasks.getListByName(existingList("/default")).containsTask(1));

		Mockito.verifyNoInteractions(writer, osInterface);
	}
}
