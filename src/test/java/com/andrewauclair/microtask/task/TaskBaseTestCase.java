// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.MockOSInterface;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.group.name.NewTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;

@ExtendWith(MockitoExtension.class)
public
class TaskBaseTestCase {
	final TaskWriter writer = Mockito.mock(TaskWriter.class);
	protected final MockOSInterface osInterface = Mockito.spy(MockOSInterface.class);
	final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	protected IDValidator idValidator;
	protected Tasks tasks;
	protected Projects projects;

	@BeforeEach
	protected void setup() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(new DataOutputStream(new ByteArrayOutputStream()));
//		Mockito.when(osInterface.runGitCommand(Mockito.any())).thenReturn(true);

		Mockito.when(osInterface.fileExists("git-data")).thenReturn(true);

		Mockito.when(osInterface.createInputStream("settings.properties")).thenThrow(IOException.class);

		PrintStream output = new PrintStream(outputStream);
		System.setOut(output);

		idValidator = new TaskIDValidator(output, osInterface);
		tasks = new Tasks(idValidator, writer, output, osInterface);

		tasks.addList(newList("/default"), true);
		tasks.setCurrentList(existingList("/default"));

		projects = new Projects(tasks, osInterface);
		tasks.setProjects(projects);
//		tasks.addList(newList("default", true); // add the default list, in reality it gets created, but we don't want all that stuff to happen
	}

	public ExistingListName existingList(String name) {
		return new ExistingListName(tasks, name);
	}

	public NewTaskListName newList(String name) {
		return new NewTaskListName(tasks, name);
	}

	public ExistingGroupName existingGroup(String name) {
		return new ExistingGroupName(tasks, name);
	}

	public NewTaskGroupName newGroup(String name) {
		return new NewTaskGroupName(tasks, name);
	}

	public ExistingID existingID(long id) {
		return new ExistingID(tasks, id);
	}

	public NewID newID(long id) {
		return new NewID(idValidator, id);
	}
}
