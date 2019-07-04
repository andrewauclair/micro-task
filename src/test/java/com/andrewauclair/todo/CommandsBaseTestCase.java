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
import java.time.ZoneId;

@ExtendWith(MockitoExtension.class)
class CommandsBaseTestCase {
	final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	final OSInterface osInterface = Mockito.mock(OSInterface.class);
	private final TaskWriter writer = Mockito.mock(TaskWriter.class);
	private final PrintStream printStream = new PrintStream(outputStream);
	final Tasks tasks = new Tasks(1, writer, printStream, osInterface);

	final Commands commands = new Commands(tasks, printStream);

	@BeforeEach
	void setup() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(new ByteArrayOutputStream());
		Mockito.when(osInterface.getZoneId()).thenReturn(ZoneId.of("America/Chicago"));
	}

	void setTime(long time) {
		Mockito.when(osInterface.currentSeconds()).thenReturn(time);
	}
}
