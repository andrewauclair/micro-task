// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.ConsoleColors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;

import static com.andrewauclair.todo.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN;

class Commands_Update_Test extends CommandsBaseTestCase {
	@Test
	void execute_update_command() throws IOException {
		Mockito.when(gitLabReleases.updateToRelease("version-1")).thenReturn(true);
		
		commands.execute(printStream, "update --release version-1");
		
		assertOutput(
				"Updated to version 'version-1'",
				""
		);
	}
	
	@Test
	void update_prints_not_found_when_version_is_unknown() throws IOException {
		Mockito.when(gitLabReleases.updateToRelease("version-1")).thenReturn(false);
		
		commands.execute(printStream, "update --release version-1");
		
		assertOutput(
				"Version 'version-1' not found on GitLab",
				""
		);
	}
	
	@Test
	void update_prints_failure_if_updateToRelease_throws_exception() throws IOException {
		Mockito.when(gitLabReleases.updateToRelease("version-1")).thenThrow(IOException.class);
		
		commands.execute(printStream, "update --release version-1");
		
		assertOutput(
				"Failed to update to version 'version-1'",
				""
		);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"-r", "--releases"})
	void print_out_releases(String command) throws IOException {
		Mockito.when(gitLabReleases.getVersions()).thenReturn(Arrays.asList("version-1", "version-2", "version-3"));
		
		commands.execute(printStream, "update " + command);
		
		assertOutput(
				"Releases found on GitLab",
				"",
				"version-1",
				"version-2",
				ANSI_FG_GREEN + "version-3 -- latest" + ConsoleColors.ANSI_RESET,
				""
		);
	}
	
	@Test
	void lists_only_the_newest_5_versions_and_highlights_newest() throws IOException {
		Mockito.when(gitLabReleases.getVersions()).thenReturn(Arrays.asList(
				"19.1.1",
				"19.1.2",
				"19.1.3",
				"19.1.4",
				"19.1.5",
				"19.1.6",
				"19.1.7"
		));
		Mockito.when(osInterface.getVersion()).thenReturn("19.1.5");
		
		commands.execute(printStream, "update -r");
		
		assertOutput(
				"Releases found on GitLab",
				"",
				"2 older releases",
				"",
				"19.1.3",
				"19.1.4",
				"19.1.5 " + ANSI_FG_GREEN + "-- current" + ConsoleColors.ANSI_RESET,
				"19.1.6",
				ANSI_FG_GREEN + "19.1.7 -- latest" + ConsoleColors.ANSI_RESET,
				""
		);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"-r", "--releases"})
	void failure_to_retrieve_releases_from_gitlab(String command) throws IOException {
		Mockito.when(gitLabReleases.getVersions()).thenThrow(IOException.class);
		
		commands.execute(printStream, "update " + command);
		
		assertOutput(
				"Failed to get releases from GitLab",
				""
		);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"-l", "--latest"})
	void update_to_latest_release(String command) throws IOException {
		Mockito.when(gitLabReleases.getVersions()).thenReturn(Arrays.asList("version-1", "version-2", "version-3"));
		Mockito.when(gitLabReleases.updateToRelease("version-3")).thenReturn(true);
		
		commands.execute(printStream, "update " + command);
		
		assertOutput(
				"Updated to version 'version-3'",
				""
		);
	}
	
	@Test
	void highlights_current_release() throws IOException {
		Mockito.when(gitLabReleases.getVersions()).thenReturn(Arrays.asList(
				"19.1.1",
				"19.1.2",
				"19.1.3",
				"19.1.4",
				"19.1.5",
				"19.1.6",
				"19.1.7"
		));
		Mockito.when(osInterface.getVersion()).thenReturn("19.1.5");
		
		commands.execute(printStream, "update -r");
		
		assertOutput(
				"Releases found on GitLab",
				"",
				"2 older releases",
				"",
				"19.1.3",
				"19.1.4",
				"19.1.5 " + ANSI_FG_GREEN + "-- current" + ConsoleColors.ANSI_RESET,
				"19.1.6",
				ANSI_FG_GREEN + "19.1.7 -- latest" + ConsoleColors.ANSI_RESET,
				""
		);
	}
	
	@Test
	void current_and_latest_are_same_release() throws IOException {
		Mockito.when(gitLabReleases.getVersions()).thenReturn(Arrays.asList(
				"19.1.1",
				"19.1.2",
				"19.1.3",
				"19.1.4",
				"19.1.5",
				"19.1.6",
				"19.1.7"
		));
		Mockito.when(osInterface.getVersion()).thenReturn("19.1.7");
		
		commands.execute(printStream, "update -r");
		
		assertOutput(
				"Releases found on GitLab",
				"",
				"2 older releases",
				"",
				"19.1.3",
				"19.1.4",
				"19.1.5",
				"19.1.6",
				ANSI_FG_GREEN + "19.1.7 " + ANSI_FG_GREEN + "-- current & latest" + ConsoleColors.ANSI_RESET,
				""
		);
	}
	
	@Test
	void highlights_current_release_if_current_release_is_not_one_of_newest_5_releases() throws IOException {
		Mockito.when(gitLabReleases.getVersions()).thenReturn(Arrays.asList(
				"19.1.1",
				"19.1.2",
				"19.1.3",
				"19.1.4",
				"19.1.5",
				"19.1.6",
				"19.1.7"
		));
		Mockito.when(osInterface.getVersion()).thenReturn("19.1.1");
		
		commands.execute(printStream, "update -r");
		
		assertOutput(
				"Releases found on GitLab",
				"",
				"2 older releases",
				"",
				"19.1.1 " + ANSI_FG_GREEN + "-- current" + ConsoleColors.ANSI_RESET,
				"19.1.3",
				"19.1.4",
				"19.1.5",
				"19.1.6",
				ANSI_FG_GREEN + "19.1.7 -- latest" + ConsoleColors.ANSI_RESET,
				""
		);
	}
	
	@Test
	void ignores_exception_for_getting_version() throws IOException {
		Mockito.when(osInterface.getVersion()).thenThrow(IOException.class);
		
		Mockito.when(gitLabReleases.getVersions()).thenReturn(Arrays.asList(
				"19.1.1",
				"19.1.2",
				"19.1.3",
				"19.1.4",
				"19.1.5",
				"19.1.6",
				"19.1.7"
		));
		
		commands.execute(printStream, "update -r");
		
		assertOutput(
				"Releases found on GitLab",
				"",
				"2 older releases",
				"",
				"19.1.3",
				"19.1.4",
				"19.1.5",
				"19.1.6",
				ANSI_FG_GREEN + "19.1.7 -- latest" + ConsoleColors.ANSI_RESET,
				""
		);
	}
	
	@Test
	void invalid_command() {
		commands.execute(printStream, "update");
		
		assertOutput(
				"Invalid command.",
				""
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void update_command_help(String parameter) {
		commands.execute(printStream, "update " + parameter);

		assertOutput(
				"Usage:  update [-hlr] [--tasks] [--release=<release>]",
				"  -h, --help                Show this help message.",
				"  -l, --latest",
				"  -r, --releases",
				"      --release=<release>",
				"      --tasks"
		);
	}
}
