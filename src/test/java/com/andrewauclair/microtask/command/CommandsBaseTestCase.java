// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.MockOSInterface;
import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.os.GitLabReleases;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.schedule.Schedule;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.group.name.NewTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CommandsBaseTestCase {
	protected final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	protected final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
	@InjectMocks
	protected final MockOSInterface osInterface = Mockito.spy(MockOSInterface.class);
	protected final TaskWriter writer = Mockito.mock(TaskWriter.class);
	final GitLabReleases gitLabReleases = Mockito.mock(GitLabReleases.class);
	final LocalSettings localSettings = Mockito.mock(LocalSettings.class);
	protected final PrintStream printStream = new PrintStream(outputStream);
	protected final PrintStream errPrintStream = new PrintStream(errorStream);
	protected IDValidator idValidator;
	protected Tasks tasks;
	protected Projects projects;
	protected Schedule schedule;
	protected Commands commands;

	private final PrintStream originalSystemOut = System.out;
	private final PrintStream originalSystemErr = System.err;

	@BeforeEach
	public void setup() throws IOException {
		MockitoAnnotations.openMocks(this);

		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(new DataOutputStream(new ByteArrayOutputStream()));
		Mockito.when(osInterface.getZoneId()).thenReturn(ZoneId.of("America/Chicago"));

		Mockito.when(osInterface.fileExists("git-data")).thenReturn(true);

		Mockito.when(osInterface.getTerminalHeight()).thenReturn(30);
		Mockito.when(osInterface.getTerminalWidth()).thenReturn(80);

		Mockito.when(localSettings.getActiveList()).thenReturn("/default");
		Mockito.when(localSettings.getActiveGroup()).thenReturn("/");

		System.setOut(printStream);
		System.setErr(errPrintStream);

		idValidator = new TaskIDValidator(printStream, osInterface);
		tasks = new Tasks(idValidator, writer, printStream, osInterface);

		schedule = new Schedule(tasks, osInterface);

		tasks.addList(newList("/default"), true);
		tasks.setCurrentList(existingList("/default"));

		projects = new Projects(tasks, osInterface);
		tasks.setProjects(projects);

		commands = new Commands(tasks, projects, schedule, gitLabReleases, localSettings, osInterface);
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

	Task addTask(String name) {
		return tasks.addTask(name, existingList("/default"));
	}

	Task addTaskWithTimes(String name, long start, long stop) {
		Task task = tasks.addTask(name);
		setTime(start);
		tasks.startTask(existingID(task.ID()), false);
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
		return new ExistingID(idValidator, ID);
	}

	public NewID newID(long ID) {
		return new NewID(idValidator, ID);
	}
}
