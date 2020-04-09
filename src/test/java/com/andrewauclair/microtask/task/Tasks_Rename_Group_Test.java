// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class Tasks_Rename_Group_Test extends TaskBaseTestCase {
	@Test
	void catch_IOException_from_moveFolder_for_renameGroup() throws IOException {
		tasks.addGroup("/one/");

		Mockito.reset(osInterface, writer);

		Mockito.doThrow(IOException.class).when(osInterface).moveFolder(Mockito.anyString(), Mockito.anyString());

		tasks.renameGroup("/one/", "/two/");

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).moveFolder("/one/", "/two/");

		Mockito.verifyNoMoreInteractions(osInterface);
		Mockito.verifyNoInteractions(writer);

		Assertions.assertEquals("java.io.IOException" + Utils.NL, this.outputStream.toString());
	}

	@Test
	void finished_group_cannot_be_renamed() {
		tasks.addGroup("/test/");
		tasks.finishGroup("/test/");

		Mockito.reset(writer, osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.renameGroup("test/", "new/"));

		assertEquals("Group '/test/' has been finished and cannot be renamed.", taskException.getMessage());

		Mockito.verifyNoInteractions(writer, osInterface);
	}

	@Test
	void does_not_change_parent_group_when_attempting_to_rename_finished_group() {
		tasks.addGroup("/test/");
		tasks.finishGroup("/test/");

		assertThrows(TaskException.class, () -> tasks.renameGroup("test/", "new/"));

		assertDoesNotThrow(() -> tasks.getGroup("/test/"));
	}
}
