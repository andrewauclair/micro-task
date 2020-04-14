// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_Rename_List_Test extends TaskBaseTestCase {
	@Test
	void renaming_list_moves_folder_to_new_folder_name() throws IOException {
		tasks.addList("one", true);

		tasks.setActiveList(existingList("one"));

		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		Mockito.reset(osInterface, writer);

		tasks.renameList("one", "test");

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).moveFolder("/one", "/test");
		order.verify(osInterface).runGitCommand("git add .");
		order.verify(osInterface).runGitCommand("git commit -m \"Renamed list '/one' to '/test'\"");

		Mockito.verifyNoMoreInteractions(osInterface);
		Mockito.verifyNoInteractions(writer);
	}

	@Test
	void catch_IOException_from_moveFolder_for_renameList() throws IOException {
		tasks.addList("one", true);

		tasks.setActiveList(existingList("one"));

		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		Mockito.reset(osInterface, writer);

		Mockito.doThrow(IOException.class).when(osInterface).moveFolder(Mockito.anyString(), Mockito.anyString());

		tasks.renameList("one", "test");

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).moveFolder("/one", "/test");

		Mockito.verifyNoMoreInteractions(osInterface);
		Mockito.verifyNoInteractions(writer);

		Assertions.assertEquals("java.io.IOException" + Utils.NL, this.outputStream.toString());
	}

	@Test
	void renaming_list_tells_git_control_to_add_new_task_files_and_commit() {
		tasks.addList("one", true);

		tasks.setActiveList(existingList("one"));

		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		Mockito.reset(osInterface); // reset the os interface after it does all the git adds and commits above

		tasks.renameList("one", "test");

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).runGitCommand("git add .");
		order.verify(osInterface).runGitCommand("git commit -m \"Renamed list '/one' to '/test'\"");
	}

	@Test
	void renaming_current_list_changes_the_name_of_current_list() {
		tasks.addList("one", true);

		tasks.setActiveList(existingList("one"));

		tasks.renameList("one", "two");

		assertEquals("/two", tasks.getActiveList());
	}

	@Test
	void renaming_active_list_changes_the_name_of_active_list() {
		tasks.addList("one", true);

		tasks.setActiveList(existingList("one"));

		tasks.addTask("Test");

		tasks.startTask(1, false);

		tasks.renameList("one", "two");

		assertEquals("/two", tasks.getActiveTaskList().absoluteName());
	}

	@Test
	void finished_list_cannot_be_renamed() {
		tasks.addList("/test", true);
		tasks.finishList("/test");

		Mockito.reset(writer, osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.renameList("test", "new"));

		assertEquals("List '/test' has been finished and cannot be renamed.", taskException.getMessage());

		Mockito.verifyNoInteractions(writer, osInterface);
	}

	@Test
	void does_not_change_parent_group_when_attempting_to_rename_finished_list() {
		tasks.addList("/test", true);
		tasks.finishList("/test");

		TaskGroup originalGroup = tasks.getGroupForList(existingList("/test"));

		assertThrows(TaskException.class, () -> tasks.renameList("test", "new"));

		assertEquals(originalGroup, tasks.getGroupForList(existingList("/test")));
	}

	@Test
	void list_rename_throws_exception_if_old_list_is_not_found() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.renameList("old", "new"));

		assertEquals("List '/old' does not exist.", taskException.getMessage());
	}

	@Test
	void list_rename_throws_exception_if_old_group_does_not_exist() {
		tasks.addGroup("/one/");
		
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.renameList("/one/two", "/one/three"));

		assertEquals("List '/one/two' does not exist.", taskException.getMessage());
	}
}
