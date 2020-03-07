// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.MockOSInterface;
import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.os.GitLabReleases;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskWriter;
import com.andrewauclair.microtask.task.Tasks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public
class CommandsBaseTestCase {
	protected final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	protected final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
	protected final MockOSInterface osInterface = Mockito.spy(MockOSInterface.class);
	protected final TaskWriter writer = Mockito.mock(TaskWriter.class);
	final GitLabReleases gitLabReleases = Mockito.mock(GitLabReleases.class);
	protected final PrintStream printStream = new PrintStream(outputStream);
	protected final PrintStream errPrintStream = new PrintStream(errorStream);
	protected final Tasks tasks = new Tasks(writer, printStream, osInterface);

	protected Commands commands;

	@BeforeEach
	void setup() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(new DataOutputStream(new ByteArrayOutputStream()));
		Mockito.when(osInterface.getZoneId()).thenReturn(ZoneId.of("America/Chicago"));

		System.setOut(printStream);
		System.setErr(errPrintStream);

		commands = new Commands(tasks, gitLabReleases, osInterface);
	}

	void setTime(long time) {
		osInterface.setTime(time);
	}

	Task addTaskWithTimes(String name, long start, long stop) {
		Task task = tasks.addTask(name);
		setTime(start);
		tasks.startTask(task.id, false);
		setTime(stop);
		return tasks.stopTask();
	}

	void addTaskTimes(long id, long start, long stop) {
		setTime(start);
		tasks.startTask(id, false);
		setTime(stop);
		tasks.stopTask();
	}
	
	protected void assertOutput(String... lines) {
		assertOutput(outputStream, lines);
	}

	protected void assertErrOutput(String... lines) {
		assertOutput(errorStream, lines);
	}

	public static void assertOutput(OutputStream outputStream, String... lines) {
		StringBuilder output = new StringBuilder();

		for (String line : lines) {
			output.append(line);
			output.append(Utils.NL);
		}

		assertThat(outputStream.toString()).isEqualTo(output.toString());
	}

//	protected void assertExceptionOutput(Class<?> e, String message) {
//		assertThat(outputStream.toString()).startsWith(e.getName() + ": " + message);
//	}
}
