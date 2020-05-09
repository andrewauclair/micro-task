// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.ConsoleColors;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static com.andrewauclair.microtask.Utils.NL;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class Commands_Alias_Test extends CommandsBaseTestCase {
	@Test
	void add_new_alias() {
		commands.execute(printStream, "alias -n ttt --command \"times --today\"");

		assertOutput(
				"Created alias 'ttt' for command 'times --today'",
				""
		);
	}

	@Test
	void writes_alias_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/aliases.txt")).thenReturn(new DataOutputStream(outputStream));

		commands.execute(printStream, "alias --name ttt --command \"times --today\"");

		assertThat(outputStream.toString()).isEqualTo(
				"ttt=\"times --today\"" + NL
		);
	}

	@Test
	void write_failure_prints_exception() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenThrow(IOException.class);

		commands.execute(printStream, "alias --name ttt --command \"times --today\"");

		assertOutput(
				"Created alias 'ttt' for command 'times --today'",
				"",
				"java.io.IOException"
		);
	}

	@Test
	void write_multiple_aliases_to_a_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/aliases.txt")).thenReturn(new DataOutputStream(outputStream));

		commands.execute(printStream, "alias --name ttt --command \"times --today\"");

		outputStream.reset();

		commands.execute(printStream, "alias --name lt --command \"tasks --all\"");

		assertThat(outputStream.toString()).isEqualTo(
				"ttt=\"times --today\"" + NL +
						"lt=\"tasks --all\"" + NL
		);
	}

	@Test
	void running_alias_runs_command() {
		commands.execute(printStream, "alias -n tt -c \"times --today\"");

		commands.execute(printStream, "tt");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Created alias 'tt' for command 'times --today'",
				"",
				ConsoleColors.ANSI_BOLD + "times --today" + ConsoleColors.ANSI_RESET,
				"Times for day 12/31/1969",
				"",
				u + "Time" + r + "  " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				"",
				" 0s    Total",
				""
		);
	}

	@Test
	void adding_an_alias_commits_the_file_to_git() {
		commands.execute(printStream, "alias -n tt -c \"times --today\"");

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).runGitCommand("git add .");
		order.verify(osInterface).runGitCommand("git commit -m \"Added alias 'tt' for command 'times --today'\"");
	}

	@Test
	void remove_an_alias() {
		commands.addAlias("ttt", "times --today");

		assertThat(commands.getAliases()).containsEntry("ttt", "times --today");

		commands.execute(printStream, "alias -r -n ttt");

		assertThat(commands.getAliases()).isEmpty();

		assertOutput(
				"Removed alias 'ttt' for command 'times --today'",
				""
		);
	}

	@Test
	void removing_alias_writes_new_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/aliases.txt")).thenReturn(new DataOutputStream(outputStream));

		commands.execute(printStream, "alias --name ttt --command \"times --today\"");

		outputStream.reset();

		commands.execute(printStream, "alias -n ltg -c \"tasks --all\"");

		assertThat(outputStream.toString()).isEqualTo(
				"ttt=\"times --today\"" + NL +
						"ltg=\"tasks --all\"" + NL
		);

		outputStream.reset();

		commands.execute(printStream, "alias -r -n ttt");

		assertThat(outputStream.toString()).isEqualTo(
				"ltg=\"tasks --all\"" + NL
		);
	}

	@Test
	void removing_alias_commits_new_file() {
		commands.addAlias("ttt", "times --today");

		assertThat(commands.getAliases()).containsEntry("ttt", "times --today");

		commands.execute(printStream, "alias --remove -n ttt");

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).runGitCommand("git add .");
		order.verify(osInterface).runGitCommand("git commit -m \"Removed alias 'ttt' for command 'times --today'\"");
	}

	@Test
	void attempting_to_remove_alias_that_does_not_exist_prints_error_message() {
		commands.execute(printStream, "alias -r -n ttt");

		assertOutput(
				"Alias 'ttt' not found.",
				""
		);
	}

	@Test
	void list_aliases_command() {
		commands.addAlias("ttt", "times --today");
		commands.addAlias("ltg", "list --tasks --group");

		commands.execute(printStream, "alias --list");

		assertOutput(
				"'ttt' = 'times --today'",
				"'ltg' = 'list --tasks --group'",
				""
		);
	}

	@Test
	void alias_already_exists() {
		commands.addAlias("ttt", "times --today");

		commands.execute(printStream, "alias -n ttt -c \"times --today\"");

		assertOutput(
				"Alias 'ttt' already exists.",
				""
		);
	}

	@Test
	void update_alias_command() {
		commands.addAlias("eod", "day --end -h 8");

		commands.execute(printStream, "alias -n eod --update \"day --end -h 9\"");
		commands.execute(printStream, "alias --list");

		assertOutput(
				"Updated alias 'eod' to command 'day --end -h 9'",
				"",
				"'eod' = 'day --end -h 9'",
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

		commands.execute(printStream, "alias -n eod -c \"day --end -h 8\"");

		assertThat(outputStream.toString()).isEqualTo(
				"eod=\"day --end -h 8\"" + NL
		);

		outputStream.reset();

		commands.execute(printStream, "alias -n eod --update \"day --end -h 9\"");

		assertThat(outputStream.toString()).isEqualTo(
				"eod=\"day --end -h 9\"" + NL
		);
	}

	@Test
	void updating_alias_commits_new_file() {
		commands.addAlias("eod", "day --end -h 8");

		assertThat(commands.getAliases()).containsEntry("eod", "day --end -h 8");

		commands.execute(printStream, "alias --name eod --update \"day --end -h 9\"");

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).runGitCommand("git add .");
		order.verify(osInterface).runGitCommand("git commit -m \"Updated alias 'eod' to command 'day --end -h 9'\"");
	}

	@Test
	void alias_command_checks_if_command_is_valid_when_creating_new_alias() {
		Mockito.reset(osInterface);

		commands.execute(printStream, "alias -n tt -c \"times --unknown-option\"");

		Mockito.verifyNoInteractions(osInterface);

		assertThat(commands.getAliases()).isEmpty();

		assertOutput(
				"Unknown option: '--unknown-option'",
				"",
				"Command 'times --unknown-option' is invalid.",
				""
		);
	}

	@Test
	void alias_command_checks_if_command_is_valid_when_updating_alias() {
		commands.addAlias("tt", "times --today");

		Mockito.reset(osInterface);

		commands.execute(printStream, "alias -n tt -u \"times --unknown-option\"");

		Mockito.verifyNoInteractions(osInterface);

		assertThat(commands.getAliases()).containsOnly(entry("tt", "times --today"));

		assertOutput(
				"Unknown option: '--unknown-option'",
				"",
				"Command 'times --unknown-option' is invalid.",
				""
		);
	}

	@Test
	void alias_command_cannot_have_same_name_as_real_command() {
		Mockito.reset(osInterface);

		commands.execute(printStream, "alias -n start -c \"tasks\"");

		Mockito.verifyNoInteractions(osInterface);

		assertOutput(
				"'start' is a command and cannot be used for an alias name.",
				""
		);
	}

	@Test
	void invalid_command() {
		commands.execute(printStream, "alias");

		assertOutput(
				"Error: Missing required argument (specify one of these): (-c=<command> | -u=<update> | -r | -l)",
				""
		);
	}

	@Test
	void creating_alias_requires_a_name() {
		commands.execute(printStream, "alias -c \"eod -h 9\"");

		assertOutput(
				"Option 'command' requires option 'name'",
				""
		);
	}

	@Test
	void updating_alias_requires_a_name() {
		commands.execute(printStream, "alias -u \"eod -h 9\"");

		assertOutput(
				"Option 'update' requires option 'name'",
				""
		);
	}

	@Test
	void removing_alias_requires_a_name() {
		commands.execute(printStream, "alias --remove");

		assertOutput(
				"Option 'remove' requires option 'name'",
				""
		);
	}

	@Test
	void alias_command_help() {
		commands.execute(printStream, "alias --help");

		assertOutput(
				"Usage:  alias (-c=<command> | -u=<update> | -r | -l) [-h] [-n=<name>]",
						"Add, list, update or delete aliases.",
						"  -c, --command=<command>   The alias command.",
						"  -h, --help                Show this help message.",
						"  -l, --list                List alias commands.",
						"  -n, --name=<name>         The name of the alias.",
						"  -r, --remove              Remove alias command.",
						"  -u, --update=<update>     Update alias command."
		);
	}
}
