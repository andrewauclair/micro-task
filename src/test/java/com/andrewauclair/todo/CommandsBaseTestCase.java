// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.GitLabReleases;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CommandsBaseTestCase {
	final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	protected final MockOSInterface osInterface = Mockito.spy(MockOSInterface.class);
	final TaskWriter writer = Mockito.mock(TaskWriter.class);
	final GitLabReleases gitLabReleases = Mockito.mock(GitLabReleases.class);
	protected final PrintStream printStream = new PrintStream(outputStream);
	protected final Tasks tasks = new Tasks(1, writer, printStream, osInterface);

	protected final Commands commands = new Commands(tasks, gitLabReleases, osInterface);

	@BeforeEach
	void setup() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(new DataOutputStream(new ByteArrayOutputStream()));
		Mockito.when(osInterface.getZoneId()).thenReturn(ZoneId.of("America/Chicago"));
	}

	void setTime(long time) {
		osInterface.setTime(time);
	}

	void addTaskWithTimes(String name, long start, long stop) {
		Task task = tasks.addTask(name);
		setTime(start);
		tasks.startTask(task.id, false);
		setTime(stop);
		tasks.stopTask();
	}

	void addTaskTimes(long id, long start, long stop) {
		setTime(start);
		tasks.startTask(id, false);
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
