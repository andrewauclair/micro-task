// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;

class Commands_Update_Test extends CommandsBaseTestCase {
	@Test
	void execute_update_command() throws IOException {
		Mockito.when(gitLabReleases.updateToRelease("version-1")).thenReturn(true);
		
		commands.execute(printStream, "update version-1");
		
		assertOutput(
				"Updated to version 'version-1'",
				""
		);
	}
	
	@Test
	void update_prints_not_found_when_version_is_unknown() throws IOException {
		Mockito.when(gitLabReleases.updateToRelease("version-1")).thenReturn(false);
		
		commands.execute(printStream, "update version-1");
		
		assertOutput(
				"Version 'version-1' not found on GitLab",
				""
		);
	}
	
	@Test
	void update_prints_failure_if_updateToRelease_throws_exception() throws IOException {
		Mockito.when(gitLabReleases.updateToRelease("version-1")).thenThrow(IOException.class);
		
		commands.execute(printStream, "update version-1");
		
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
				"version-3",
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
}
