// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static com.andrewauclair.todo.Utils.NL;
import static org.assertj.core.api.Assertions.assertThat;

class Commands_Alias_Test extends CommandsBaseTestCase {
	@ParameterizedTest
	@ValueSource(strings = {"-c", "--command"})
	void add_new_alias(String arg) {
		commands.execute(printStream, "alias -n ttt " + arg + " \"times --tasks --today\"");
		
		assertOutput(
				"Created alias 'ttt' for command 'times --tasks --today'",
				""
		);
	}
	
	@Test
	void writes_alias_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		Mockito.when(osInterface.createOutputStream("git-data/aliases.txt")).thenReturn(new DataOutputStream(outputStream));
		
		commands.execute(printStream, "alias --name ttt --command \"times --tasks --today\"");
		
		assertThat(outputStream.toString()).isEqualTo(
				"ttt=\"times --tasks --today\"" + NL
		);
	}
	
	@Test
	void write_multiple_aliases_to_a_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		Mockito.when(osInterface.createOutputStream("git-data/aliases.txt")).thenReturn(new DataOutputStream(outputStream));
		
		commands.execute(printStream, "alias --name ttt --command \"times --tasks --today\"");
		
		outputStream.reset();
		
		commands.execute(printStream, "alias --name lt --command \"list --tasks\"");
		
		assertThat(outputStream.toString()).isEqualTo(
				"ttt=\"times --tasks --today\"" + NL +
						"lt=\"list --tasks\"" + NL
		);
	}
	
	@Test
	void running_alias_runs_command() {
		commands.execute(printStream, "alias -n ttt -c \"times --tasks --today\"");
		
		commands.execute(printStream, "ttt");
		
		assertOutput(
				"Created alias 'ttt' for command 'times --tasks --today'",
				"",
				"Times for day 12/31/1969",
				"",
				"",
				"Total time: 00s",
				""
		);
	}
	
	@Test
	void adding_an_alias_commits_the_file_to_git() {
		commands.execute(printStream, "alias -n ttt -c \"times --tasks --today\"");
		
		InOrder order = Mockito.inOrder(osInterface);
		
		order.verify(osInterface).runGitCommand("git add aliases.txt", false);
		order.verify(osInterface).runGitCommand("git commit -m \"Added alias 'ttt' for command 'times --tasks --today'\"", false);
	}
	
	@Test
	void remove_an_alias() {
		commands.addAlias("ttt", "times --tasks --today");
		
		assertThat(commands.getAliases()).containsEntry("ttt", "times --tasks --today");
		
		commands.execute(printStream, "alias -r -n ttt");
		
		assertThat(commands.getAliases()).isEmpty();
		
		assertOutput(
				"Removed alias 'ttt' for command 'times --tasks --today'",
				""
		);
	}
	
	@Test
	void removing_alias_writes_new_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		Mockito.when(osInterface.createOutputStream("git-data/aliases.txt")).thenReturn(new DataOutputStream(outputStream));
		
		commands.execute(printStream, "alias --name ttt --command \"times --tasks --today\"");
		
		outputStream.reset();
		
		commands.execute(printStream, "alias -n ltg -c \"list --tasks --group\"");
		
		assertThat(outputStream.toString()).isEqualTo(
				"ttt=\"times --tasks --today\"" + NL +
						"ltg=\"list --tasks --group\"" + NL
		);
		
		outputStream.reset();
		
		commands.execute(printStream, "alias -r -n ttt");
		
		assertThat(outputStream.toString()).isEqualTo(
				"ltg=\"list --tasks --group\"" + NL
		);
	}
	
	@Test
	void removing_alias_commits_new_file() {
		commands.addAlias("ttt", "times --tasks --today");
		
		assertThat(commands.getAliases()).containsEntry("ttt", "times --tasks --today");
		
		commands.execute(printStream, "alias --remove -n ttt");
		
		InOrder order = Mockito.inOrder(osInterface);
		
		order.verify(osInterface).runGitCommand("git add aliases.txt", false);
		order.verify(osInterface).runGitCommand("git commit -m \"Removed alias 'ttt' for command 'times --tasks --today'\"", false);
	}
	
	@Test
	void attempting_to_remove_alias_that_does_not_exist_prints_error_message() {
		commands.execute(printStream, "alias -r -n ttt");
		
		assertOutput(
				"Alias 'ttt' not found.",
				""
		);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"--list", "-l"})
	void list_aliases_command(String parameter) {
		commands.addAlias("ttt", "times --tasks --today");
		commands.addAlias("ltg", "list --tasks --group");
		
		commands.execute(printStream, "alias " + parameter);
		
		assertOutput(
				"'ttt' = 'times --tasks --today'",
				"'ltg' = 'list --tasks --group'",
				""
		);
	}
	
	@Test
	void invalid_command() {
		commands.execute(printStream, "alias");
		
		assertOutput(
				"Invalid command.",
				""
		);
	}
}
