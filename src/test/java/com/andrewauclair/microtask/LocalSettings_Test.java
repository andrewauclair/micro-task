// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;
import com.andrewauclair.microtask.task.group.name.NewTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingTaskListName;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static com.andrewauclair.microtask.TestUtils.assertOutput;
import static com.andrewauclair.microtask.TestUtils.createInputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;

class LocalSettings_Test {
	private LocalSettings localSettings;
	
	final TaskWriter writer = Mockito.mock(TaskWriter.class);
	protected final MockOSInterface osInterface = Mockito.spy(MockOSInterface.class);
	final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	protected Tasks tasks;
	
//	private final OSInterface osInterface = Mockito.mock(OSInterface.class);
//	private final Tasks tasks = Mockito.mock(Tasks.class);

	@BeforeEach
	public void setup() throws IOException {
		localSettings = new LocalSettings(osInterface);
		
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(new DataOutputStream(new ByteArrayOutputStream()));
		Mockito.when(osInterface.runGitCommand(Mockito.any())).thenReturn(true);
		
		Mockito.when(osInterface.fileExists("git-data")).thenReturn(true);
		
//		Mockito.when(osInterface.createInputStream("settings.properties")).thenThrow(IOException.class);
		
		PrintStream output = new PrintStream(outputStream);
		System.setOut(output);
		
		tasks = new Tasks(writer, output, osInterface);
//		tasks.addList(newList("default", true); // add the default list, in reality it gets created, but we don't want all that stuff to happen
	}

	@Test
	void default_active_list_is_default() {
		assertEquals("/default", localSettings.getActiveList());
	}

	@Test
	void default_active_group_is_root() {
		assertEquals("/", localSettings.getActiveGroup());
	}

	@Test
	void default_debug_flag_is_false() {
		assertFalse(localSettings.isDebugEnabled());
	}

	@Test
	void default_length_of_day_is_8_hours() {
		assertEquals(8, localSettings.hoursInDay());
	}

	@Test
	void default_estimated_time_per_task_is_30_minutes() {
		assertEquals(30000, localSettings.getEstimatedTimePerTask());
	}

	@Test
	void load_active_list_from_file() throws IOException {
		tasks.addGroup(new NewTaskGroupName(tasks, "/test/"));
		tasks.addList(new NewTaskListName(tasks, "/test/one"), true);

		Mockito.when(osInterface.createInputStream("settings.properties")).thenReturn(
				createInputStream("active_list=/test/one")
		);

		localSettings.load(tasks);

		assertEquals("/test/one", localSettings.getActiveList());
	}

	@Test
	void load_active_group_from_file() throws IOException {
		tasks.addGroup(new NewTaskGroupName(tasks, "/test/"));
		
		Mockito.when(osInterface.createInputStream("settings.properties")).thenReturn(
				createInputStream("active_group=/test/")
		);

		localSettings.load(tasks);

		assertEquals("/test/", localSettings.getActiveGroup());
	}

	@Test
	void load_debug_flag_from_file() throws IOException {
		Mockito.when(osInterface.createInputStream("settings.properties")).thenReturn(
				createInputStream("debug=true")
		);

		localSettings.load(tasks);

		assertTrue(localSettings.isDebugEnabled());
	}

	@Test
	void load_hours_in_day_from_file() throws IOException {
		Mockito.when(osInterface.createInputStream("settings.properties")).thenReturn(
				createInputStream("hours_in_day=6")
		);

		localSettings.load(tasks);

		assertEquals(6, localSettings.hoursInDay());
	}

	@Test
	void load_estimated_time_per_Task_from_file() throws IOException {
		Mockito.when(osInterface.createInputStream("settings.properties")).thenReturn(
			createInputStream("estimated_time_per_task=25000")
		);

		localSettings.load(tasks);

		assertEquals(25000, localSettings.getEstimatedTimePerTask());
	}

	@Test
	void changing_active_list_saves_file() throws IOException {
		tasks.addGroup(new NewTaskGroupName(tasks, "/test/"));
		tasks.addList(new NewTaskListName(tasks, "/test/one"), true);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("settings.properties")).thenReturn(new DataOutputStream(outputStream));

		localSettings.setActiveList(new ExistingTaskListName(tasks, "/test/one"));

		assertThat(outputStream.toString()).contains("active_list=/test/one");
	}

	@Test
	void changing_active_group_saves_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("settings.properties")).thenReturn(new DataOutputStream(outputStream));

		tasks.addGroup(new NewTaskGroupName(tasks, "/test/"));
		localSettings.setActiveGroup(new ExistingTaskGroupName(tasks, "/test/"));

		assertThat(outputStream.toString()).contains("active_group=/test/");
	}

	@Test
	void changing_debug_flag_saves_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("settings.properties")).thenReturn(new DataOutputStream(outputStream));

		localSettings.setDebugEnabled(true);

		assertThat(outputStream.toString()).contains("debug=true");
	}

	@Test
	void changing_hours_in_day_saves_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("settings.properties")).thenReturn(new DataOutputStream(outputStream));

		localSettings.setHoursInDay(6);

		assertThat(outputStream.toString()).contains("hours_in_day=6");
	}

	@Test
	void changing_estimated_time_per_task_saves_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("settings.properties")).thenReturn(new DataOutputStream(outputStream));

		localSettings.setEstimatedTimePerTask(40000);

		assertThat(outputStream.toString()).contains("estimated_time_per_task=40000");
	}

	@Test
	void prints_failure_to_load_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(outputStream);

		Mockito.when(osInterface.createInputStream("settings.properties")).thenThrow(IOException.class);

		System.setOut(printStream);

		localSettings.load(tasks);

		assertOutput(
				outputStream,

				"java.io.IOException",
				""
		);
	}

	@Test
	void prints_failure_to_save_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(outputStream);

		Mockito.when(osInterface.createOutputStream("settings.properties")).thenThrow(IOException.class);

		System.setOut(printStream);

		tasks.addGroup(new NewTaskGroupName(tasks, "/test/"));
		localSettings.setActiveGroup(new ExistingTaskGroupName(tasks, "/test/"));

		assertOutput(
				outputStream,

				"java.io.IOException",
				""
		);
	}

	@Test
	void failure_to_read_file_sets_default_active_list() throws IOException {
		Mockito.when(osInterface.createInputStream("settings.properties")).thenThrow(IOException.class);

		localSettings.load(tasks);

		assertEquals("/default", localSettings.getActiveList());
	}

	@Test
	void failure_to_read_file_sets_default_active_group() throws IOException {
		Mockito.when(osInterface.createInputStream("settings.properties")).thenThrow(IOException.class);

		localSettings.load(tasks);

		assertEquals("/", localSettings.getActiveGroup());
	}

	@Test
	void failure_to_read_file_sets_default_debug_flag() throws IOException {
		Mockito.when(osInterface.createInputStream("settings.properties")).thenThrow(IOException.class);

		localSettings.load(tasks);

		assertFalse(localSettings.isDebugEnabled());
	}

	@Test
	void failure_to_read_file_sets_default_hours_in_day() throws IOException {
		Mockito.when(osInterface.createInputStream("settings.properties")).thenThrow(IOException.class);

		localSettings.load(tasks);

		assertEquals(8, localSettings.hoursInDay());
	}

	@Test
	void local_settings_sets_list_and_group_in_tasks() throws IOException {
		tasks.addGroup(new NewTaskGroupName(tasks, "/test/"));
		tasks.addList(new NewTaskListName(tasks, "/test/one"), true);

		Mockito.when(osInterface.createInputStream("settings.properties")).thenReturn(
				createInputStream("active_list=/test/one", "active_group=/test/")
		);

		localSettings.load(tasks);

		assertEquals(new ExistingTaskListName(tasks, "/test/one"), tasks.getActiveList());
		assertEquals("/test/", tasks.getActiveGroup().getFullPath());
	}
}
