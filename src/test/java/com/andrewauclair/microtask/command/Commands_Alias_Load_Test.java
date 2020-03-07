// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.Utils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class Commands_Alias_Load_Test extends CommandsBaseTestCase {
	@Test
	void load_alias() throws IOException {
		Mockito.when(osInterface.fileExists("git-data/aliases.txt")).thenReturn(true);

		String contents = "t=\"times --today\"" + Utils.NL +
				"end=\"eod -h 8\"" + Utils.NL;

		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());

		Mockito.when(osInterface.createInputStream("git-data/aliases.txt")).thenReturn(inputStream);

		commands.loadAliases();

		assertThat(commands.getAliases()).containsOnly(
				entry("t", "times --today"),
				entry("end", "eod -h 8")
		);
	}

	@Test
	void loading_aliases_removes_existing_aliases() throws IOException {
		Mockito.when(osInterface.fileExists("git-data/aliases.txt")).thenReturn(true);

		String contents = "tt=\"times --today\"" + Utils.NL +
				"end=\"eod -h 8\"" + Utils.NL;

		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());

		Mockito.when(osInterface.createInputStream("git-data/aliases.txt")).thenReturn(inputStream);

		commands.addAlias("test", "start 8");

		commands.loadAliases();

		assertThat(commands.getAliases()).containsOnly(
				entry("tt", "times --today"),
				entry("end", "eod -h 8")
		);
	}

	@Test
	void nothing_happens_if_the_alias_file_does_not_exist() {
		Mockito.when(osInterface.fileExists("git-data/aliases.txt")).thenReturn(false);

		commands.addAlias("test", "start 8");

		commands.loadAliases();

		assertThat(commands.getAliases()).containsOnly(
				entry("test", "start 8")
		);
	}

	@Test
	void prints_exception_if_fails() throws IOException {
		Mockito.when(osInterface.fileExists("git-data/aliases.txt")).thenReturn(true);

		Mockito.when(osInterface.createInputStream("git-data/aliases.txt")).thenThrow(IOException.class);

		commands.addAlias("test", "start 8");

		commands.loadAliases();

		assertThat(commands.getAliases()).containsOnly(
				entry("test", "start 8")
		);

		assertOutput(
				"java.io.IOException"
		);
	}
}
