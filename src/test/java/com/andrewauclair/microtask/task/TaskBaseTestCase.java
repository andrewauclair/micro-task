// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.MockOSInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;

@ExtendWith(MockitoExtension.class)
class TaskBaseTestCase {
	final TaskWriter writer = Mockito.mock(TaskWriter.class);
	final MockOSInterface osInterface = Mockito.spy(MockOSInterface.class);
	final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	Tasks tasks;

	@BeforeEach
	void setup() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(new DataOutputStream(new ByteArrayOutputStream()));
		Mockito.when(osInterface.runGitCommand(Mockito.any(), Mockito.anyBoolean())).thenReturn(true);

		Mockito.when(osInterface.fileExists("git-data")).thenReturn(true);

		Mockito.when(osInterface.createInputStream("settings.properties")).thenThrow(IOException.class);

		tasks = new Tasks(writer, new PrintStream(outputStream), osInterface);
		tasks.addList("default", true); // add the default list, in reality it gets created, but we don't want all that stuff to happen
	}
}
