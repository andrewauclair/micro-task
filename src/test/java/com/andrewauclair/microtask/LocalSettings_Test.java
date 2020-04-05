// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.Tasks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

import static com.andrewauclair.microtask.TestUtils.assertOutput;
import static com.andrewauclair.microtask.UtilsTest.byteInStream;
import static com.andrewauclair.microtask.UtilsTest.createFile;
import static org.junit.jupiter.api.Assertions.*;

class LocalSettings_Test {
	private LocalSettings localSettings;

	private final OSInterface osInterface = Mockito.mock(OSInterface.class);
	private final Tasks tasks = Mockito.mock(Tasks.class);

	@BeforeEach
	void setup() {
		localSettings = new LocalSettings(osInterface);
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
	void load_settings_from_file() throws IOException {
		Mockito.when(osInterface.createInputStream("settings.properties")).thenReturn(
				byteInStream(createFile("active_list=/test/one", "active_group=/test/", "debug=true"))
		);

		localSettings.load(tasks);

		assertEquals("/test/one", localSettings.getActiveList());
		assertEquals("/test/", localSettings.getActiveGroup());
		assertTrue(localSettings.isDebugEnabled());
	}

	@Test
	void changing_active_list_saves_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("settings.properties")).thenReturn(new DataOutputStream(outputStream));

		localSettings.setActiveList("/test");

		assertOutput(
				outputStream,

				"#",
				"#" + new Date().toString(),
				"active_group=/",
				"debug=false",
				"active_list=/test",
				""
		);
	}

	@Test
	void changing_active_group_saves_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("settings.properties")).thenReturn(new DataOutputStream(outputStream));

		localSettings.setActiveGroup("/test/");

		assertOutput(
				outputStream,

				"#",
				"#" + new Date().toString(),
				"active_group=/test/",
				"debug=false",
				"active_list=/default",
				""
		);
	}

	@Test
	void changing_debug_flag_saves_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("settings.properties")).thenReturn(new DataOutputStream(outputStream));

		localSettings.setDebugEnabled(true);

		assertOutput(
				outputStream,

				"#",
				"#" + new Date().toString(),
				"active_group=/",
				"debug=true",
				"active_list=/default",
				""
		);
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

		localSettings.setActiveGroup("/test/");

		assertOutput(
				outputStream,

				"java.io.IOException",
				""
		);
	}

	@Test
	void failing_to_read_from_the_file_results_in_default_values() throws IOException {
		Mockito.when(osInterface.createInputStream("settings.properties")).thenThrow(IOException.class);

		localSettings.load(tasks);

		assertEquals("/default", localSettings.getActiveList());
		assertEquals("/", localSettings.getActiveGroup());
		assertFalse(localSettings.isDebugEnabled());
	}

	@Test
	void local_settings_sets_list_and_group_in_tasks() throws IOException {
		Mockito.when(osInterface.createInputStream("settings.properties")).thenReturn(
				byteInStream(createFile("active_list=/test/one", "active_group=/test/"))
		);

		localSettings.load(tasks);

		Mockito.verify(tasks).setActiveList("/test/one");
		Mockito.verify(tasks).switchGroup("/test/");
	}
}
