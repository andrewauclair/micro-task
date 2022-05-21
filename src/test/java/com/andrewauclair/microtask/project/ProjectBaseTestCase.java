// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.MockOSInterface;
import com.andrewauclair.microtask.task.*;
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
class ProjectBaseTestCase {
	final TaskWriter writer = Mockito.mock(TaskWriter.class);
	protected final MockOSInterface osInterface = Mockito.spy(MockOSInterface.class);
	final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	protected IDValidator idValidator;
	protected Tasks tasks;
	protected Projects projects;

	Project project;

	@BeforeEach
	void setup() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(new DataOutputStream(new ByteArrayOutputStream()));

		Mockito.when(osInterface.fileExists("git-data")).thenReturn(true);

		idValidator = new TaskIDValidator(new PrintStream(outputStream), osInterface);
		tasks = new Tasks(idValidator, writer, new PrintStream(outputStream), osInterface);

		tasks.addList(newList("/default"), true);
		tasks.setCurrentList(existingList("/default"));

		projects = new Projects(tasks, osInterface);
		project = projects.createProject(new NewProject(projects, "micro-task"), true);
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

	public ExistingID existingID(long ID) {
		return new ExistingID(tasks, ID);
	}
}
