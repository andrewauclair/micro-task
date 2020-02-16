// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.os.ConsoleColors;
import org.junit.jupiter.api.Assertions;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

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
	void write_failure_prints_exception() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenThrow(IOException.class);
		
		commands.execute(printStream, "alias --name ttt --command \"times --tasks --today\"");
		
		assertOutput(
				"Created alias 'ttt' for command 'times --tasks --today'",
				"",
				"java.io.IOException"
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
				ConsoleColors.ANSI_BOLD + "times --tasks --today" + ConsoleColors.ANSI_RESET,
				"Times for day 12/31/1969",
				"",
				"",
				"Total time: 00s",
				""
		);
	}
	
	@Test
	void adding_an_alias_commits_the_file_to_git() throws IOException, InterruptedException {
		commands.execute(printStream, "alias -n ttt -c \"times --tasks --today\"");
		
		InOrder order = Mockito.inOrder(osInterface);
		
		order.verify(osInterface).runGitCommand("git add .", false);
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
		
		order.verify(osInterface).runGitCommand("git add .", false);
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
	void alias_already_exists() {
		commands.addAlias("ttt", "times --tasks --today");
		
		commands.execute(printStream, "alias -n ttt -c \"times --tasks --today\"");
		
		assertOutput(
				"Alias 'ttt' already exists.",
				""
		);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"--update", "-u"})
	void update_alias_command(String parameter) {
		commands.addAlias("end", "eod -h 8");
		
		commands.execute(printStream, "alias -n end " + parameter + " \"eod -h 9\"");
		commands.execute(printStream, "alias --list");
		
		assertOutput(
				"Updated alias 'end' to command 'eod -h 9'",
				"",
				"'end' = 'eod -h 9'",
				""
		);
	}
	
	@Test
	void cannot_update_alias_that_does_not_exist() {
		commands.execute(printStream, "alias -n end -u \"eod -h 9\"");
		
		assertOutput(
				"Alias 'end' does not exist.",
				""
		);
	}
	
	@Test
	void updating_alias_writes_new_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		Mockito.when(osInterface.createOutputStream("git-data/aliases.txt")).thenReturn(new DataOutputStream(outputStream));
		
		commands.execute(printStream, "alias -n end -c \"eod -h 8\"");
		
		assertThat(outputStream.toString()).isEqualTo(
				"end=\"eod -h 8\"" + NL
		);
		
		outputStream.reset();
		
		commands.execute(printStream, "alias -n end --update \"eod -h 9\"");
		
		assertThat(outputStream.toString()).isEqualTo(
				"end=\"eod -h 9\"" + NL
		);
	}
	
	@Test
	void updating_alias_commits_new_file() {
		commands.addAlias("end", "eod -h 8");
		
		assertThat(commands.getAliases()).containsEntry("end", "eod -h 8");
		
		commands.execute(printStream, "alias --name end --update \"eod -h 9\"");
		
		InOrder order = Mockito.inOrder(osInterface);
		
		order.verify(osInterface).runGitCommand("git add .", false);
		order.verify(osInterface).runGitCommand("git commit -m \"Updated alias 'end' to command 'eod -h 9'\"", false);
	}
	
	@Test
	void block_alias_when_behind_remote() {
		Mockito.when(osInterface.isBehindOrigin()).thenReturn(true);
		
		commands.execute(printStream, "alias -n ttt -c \"times --tasks --today\"");
		
		assertNull(commands.getAliases().get("ttt"));
		
		assertOutput(
				"Behind origin/master. Please run 'update --from-remote'",
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

	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void alias_command_help(String parameter) {
		commands.execute(printStream, "active " + parameter);

		assertOutput(
				"Usage:  active [-h]",
				"  -h, --help   Show this help message."
		);
	}
}
