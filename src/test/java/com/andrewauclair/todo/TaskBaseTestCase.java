// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

@ExtendWith(MockitoExtension.class)
class TaskBaseTestCase {
	final TaskWriter writer = Mockito.mock(TaskWriter.class);
	final OSInterface osInterface = Mockito.mock(OSInterface.class);
	final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	final Tasks tasks = new Tasks(1, writer, new PrintStream(outputStream), osInterface);

	@BeforeEach
	void setup() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(new ByteArrayOutputStream());
		Mockito.when(osInterface.runGitCommand(Mockito.any())).thenReturn(true);
	}
}
