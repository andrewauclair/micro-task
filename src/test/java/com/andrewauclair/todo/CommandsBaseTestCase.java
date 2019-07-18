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

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CommandsBaseTestCase {
	final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	final OSInterface osInterface = Mockito.mock(OSInterface.class);
	private final TaskWriter writer = Mockito.mock(TaskWriter.class);
	public final PrintStream printStream = new PrintStream(outputStream);
	public final Tasks tasks = new Tasks(1, writer, printStream, osInterface);
	
	public final Commands commands = new Commands(tasks);

	@BeforeEach
	void setup() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(new ByteArrayOutputStream());
		Mockito.when(osInterface.getZoneId()).thenReturn(ZoneId.of("America/Chicago"));
	}

	void setTime(long time) {
		Mockito.when(osInterface.currentSeconds()).thenReturn(time);
	}

	// TODO I'm really not liking how much we have to set the mock return for doing times
	void addTaskWithTimes(String name, long start, long stop) {
		Task task = tasks.addTask(name);
		setTime(start);
		tasks.startTask(task.id);
		setTime(stop);
		tasks.stopTask();
	}

	void addTaskTimes(long id, long start, long stop) {
		setTime(start);
		tasks.startTask(id);
		setTime(stop);
		tasks.stopTask();
	}

	void assertOutput(String... lines) {
		StringBuilder output = new StringBuilder();

		for (String line : lines) {
			output.append(line);
			output.append(Utils.NL);
		}

		assertThat(outputStream.toString()).isEqualTo(output.toString());
	}
}
