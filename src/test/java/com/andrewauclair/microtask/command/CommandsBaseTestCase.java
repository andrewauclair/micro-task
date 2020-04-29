// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.MockOSInterface;
import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.os.GitLabReleases;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskWriter;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;
import com.andrewauclair.microtask.task.group.name.NewTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingTaskListName;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.linesOf;

@ExtendWith(MockitoExtension.class)
public class CommandsBaseTestCase {
	protected final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	protected final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
	protected final MockOSInterface osInterface = Mockito.spy(MockOSInterface.class);
	protected final TaskWriter writer = Mockito.mock(TaskWriter.class);
	final GitLabReleases gitLabReleases = Mockito.mock(GitLabReleases.class);
	final LocalSettings localSettings = Mockito.mock(LocalSettings.class);
	protected final PrintStream printStream = new PrintStream(outputStream);
	protected final PrintStream errPrintStream = new PrintStream(errorStream);
	protected Tasks tasks;
	protected Projects projects;

	protected Commands commands;

	private PrintStream originalSystemOut = System.out;
	private PrintStream originalSystemErr = System.err;

	@BeforeEach
	public void setup() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(new DataOutputStream(new ByteArrayOutputStream()));
		Mockito.when(osInterface.getZoneId()).thenReturn(ZoneId.of("America/Chicago"));

		Mockito.when(osInterface.fileExists("git-data")).thenReturn(true);

		Mockito.when(localSettings.getActiveList()).thenReturn("/default");
		Mockito.when(localSettings.getActiveGroup()).thenReturn("/");

		System.setOut(printStream);
		System.setErr(errPrintStream);

		tasks = new Tasks(writer, printStream, osInterface);
		projects = new Projects(tasks, osInterface);
//		tasks.addList(newList("default", true); // add the default list, in reality it gets created, but we don't want all that stuff to happen

		commands = new Commands(tasks, projects, gitLabReleases, localSettings, osInterface);
	}

	@AfterEach
	void tearDown() {
		System.setOut(originalSystemOut);
		System.setErr(originalSystemErr);

		System.out.print(outputStream.toString());
		System.err.print(errorStream.toString());
	}

	void setTime(long time) {
		osInterface.setTime(time);
	}

	Task addTaskWithTimes(String name, long start, long stop) {
		Task task = tasks.addTask(name);
		setTime(start);
		tasks.startTask(existingID(task.id), false);
		setTime(stop);
		return tasks.stopTask();
	}

	void addTaskTimes(long id, long start, long stop) {
		setTime(start);
		tasks.startTask(existingID(id), false);
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

	public ExistingTaskListName existingList(String name) {
		return new ExistingTaskListName(tasks, name);
	}

	public NewTaskListName newList(String name) {
		return new NewTaskListName(tasks, name);
	}

	public ExistingTaskGroupName existingGroup(String name) {
		return new ExistingTaskGroupName(tasks, name);
	}

	public NewTaskGroupName newGroup(String name) {
		return new NewTaskGroupName(tasks, name);
	}

	public ExistingID existingID(long ID) {
		return new ExistingID(tasks, ID);
	}
}
