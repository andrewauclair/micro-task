// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.jline;

import com.andrewauclair.microtask.command.CommandsBaseTestCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ListCompleterTest extends CommandsBaseTestCase {
	@Test
	void candidates_list_contains_all_lists() {
		tasks.addList(newList("alpha"), false);
		tasks.addList(newList("bravo"), false);
		tasks.addList(newList("charlie"), false);

		final ListCompleter completer = new ListCompleter(tasks);

		assertThat(completer).containsOnly(
				"/default",
				"/alpha",
				"/bravo",
				"/charlie"
		);
	}

	@Test
	void candidates_list_contains_only_in_progress_lists() {
		tasks.addList(newList("/alpha"), true);
		tasks.addList(newList("/bravo"), true);
		tasks.addList(newList("/charlie"), true);

		tasks.finishList(existingList("/bravo"));

		final ListCompleter completer = new ListCompleter(tasks);

		assertThat(completer).containsOnly(
				"/alpha",
				"/charlie",
				"/default"
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"ch", "finish", "list", "move", "times"})
	void command_list_option_has_list_completer(String command) {
		CommandLine cmd = commands.buildCommandLineWithAllCommands();

		CommandLine.Model.CommandSpec spec = cmd.getSubcommands().get(command).getCommandSpec();

		assertNotNull(spec.optionsMap().get("--list").completionCandidates());
		assertEquals("/default", spec.optionsMap().get("--list").completionCandidates().iterator().next());
	}

	@Test
	void add_list_parameter_has_list_completer() {
		CommandLine cmd = commands.buildCommandLine("add").getSubcommands().get("add").getSubcommands().get("list");

		CommandLine.Model.PositionalParamSpec positionalParamSpec = cmd.getCommandSpec().positionalParameters().get(0);

		assertNotNull(positionalParamSpec.completionCandidates());
		assertEquals("/default", positionalParamSpec.completionCandidates().iterator().next());
	}

	@Test
	void rename_list_parameter_has_list_completer() {
		CommandLine cmd = commands.buildCommandLine("rename").getSubcommands().get("rename").getSubcommands().get("list");

		CommandLine.Model.PositionalParamSpec positionalParamSpec = cmd.getCommandSpec().positionalParameters().get(0);

		assertNotNull(positionalParamSpec.completionCandidates());
		assertEquals("/default", positionalParamSpec.completionCandidates().iterator().next());
	}

	@Test
	void set_list_command_list_option_has_list_completer() {
		CommandLine cmd = commands.buildCommandLine("set");

		CommandLine.Model.CommandSpec spec = cmd.getSubcommands().get("set").getSubcommands().get("list").getCommandSpec();

		assertNotNull(spec.positionalParameters().get(0).completionCandidates());
		assertEquals("/default", spec.positionalParameters().get(0).completionCandidates().iterator().next());
	}

	@Test
	void move_command_dest_list_option_has_list_completer() {
		CommandLine cmd = commands.buildCommandLineWithAllCommands();

		CommandLine.Model.CommandSpec spec = cmd.getSubcommands().get("move").getCommandSpec();

		assertNotNull(spec.optionsMap().get("--dest-list").completionCandidates());
		assertEquals("/default", spec.optionsMap().get("--dest-list").completionCandidates().iterator().next());
	}
}
