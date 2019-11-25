// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class Commands_Alias_Test extends CommandsBaseTestCase {
	@ParameterizedTest
	@ValueSource(strings = {"-c", "--command"})
	void add_new_alias(String arg) {
		commands.execute(printStream, "alias -n ttt " + arg + " \"times --tasks --today\"");
		
		assertOutput(
				"Created alias 'ttt' for 'times --tasks --today'",
				""
		);
	}
	
	@Test
	void writes_alias_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		Mockito.when(osInterface.createOutputStream("git-data/aliases.txt")).thenReturn(new DataOutputStream(outputStream));
		
		commands.execute(printStream, "alias --name ttt --command \"times --tasks --today\"");
		
		assertThat(outputStream.toString()).isEqualTo(
				"ttt=\"times --tasks --today\""
		);
	}
	
	@Test
	void running_alias_runs_command() {
		commands.execute(printStream, "alias -n ttt -c \"times --tasks --today\"");
		
		commands.execute(printStream, "ttt");
		
		assertOutput(
				"Created alias 'ttt' for 'times --tasks --today'",
				"",
				"Times for day 12/31/1969",
				"",
				"",
				"Total time: 00s",
				""
		);
	}
}
