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

class GroupCompleterTest extends CommandsBaseTestCase {
	@Test
	void candidates_list_contains_all_groups() {
		tasks.addGroup(newGroup("/test/one/two/"));
		tasks.addGroup(newGroup("/last/"));
		tasks.addGroup(newGroup("/three/five/"));

		final GroupCompleter completer = new GroupCompleter(tasks);

		assertThat(completer).containsOnly(
				"/test/",
				"/test/one/",
				"/test/one/two/",
				"/last/",
				"/three/",
				"/three/five/"
		);
	}

	@Test
	void candidates_list_contains_only_in_progress_groups() {
		tasks.addGroup(newGroup("/test/one/"));
		tasks.addGroup(newGroup("/test/two/"));
		tasks.addGroup(newGroup("/test/three/"));

		tasks.finishGroup(existingGroup("/test/two/"));

		final GroupCompleter completer = new GroupCompleter(tasks);

		assertThat(completer).containsOnly(
				"/test/",
				"/test/one/",
				"/test/three/"
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"ch", "finish", "group", "search"})
	void command_group_option_has_group_completer(String group) {
		tasks.addGroup(newGroup("/test/"));

		CommandLine cmd = commands.buildCommandLineWithAllCommands();

		CommandLine.Model.CommandSpec spec = cmd.getSubcommands().get(group).getCommandSpec();

		assertNotNull(spec.optionsMap().get("--group").completionCandidates());
		assertEquals("/test/", spec.optionsMap().get("--group").completionCandidates().iterator().next());
	}

	@Test
	void move_group_parameter_has_group_completer() {
		tasks.addGroup(newGroup("/test/"));

		CommandLine.Model.CommandSpec spec = commands.buildCommandLine("move").getSubcommands().get("move").getSubcommands().get("group").getCommandSpec();

		CommandLine.Model.PositionalParamSpec positionalParamSpec = spec.positionalParameters().get(0);

		assertEquals("/test/", positionalParamSpec.completionCandidates().iterator().next());
	}

	@Test
	void rename_group_command_group_option_has_group_completer() {
		tasks.addGroup(newGroup("/test/"));

		CommandLine cmd = commands.buildCommandLine("rename").getSubcommands().get("rename").getSubcommands().get("group");

		CommandLine.Model.PositionalParamSpec positionalParamSpec = cmd.getCommandSpec().positionalParameters().get(0);

		assertNotNull(positionalParamSpec.completionCandidates());
		assertEquals("/test/", positionalParamSpec.completionCandidates().iterator().next());
	}

	@Test
	void set_group_group_option_has_group_completer() {
		tasks.addGroup(newGroup("/test/"));

		CommandLine cmd = commands.buildCommandLine("set");

		CommandLine.Model.CommandSpec spec = cmd.getSubcommands().get("set").getSubcommands().get("group").getCommandSpec();

		assertNotNull(spec.positionalParameters().get(0).completionCandidates());
		assertEquals("/test/", spec.positionalParameters().get(0).completionCandidates().iterator().next());
	}

	@Test
	void move_command_dest_group_option_has_group_completer() {
		tasks.addGroup(newGroup("/test/"));

		CommandLine cmd = commands.buildCommandLineWithAllCommands();

		CommandLine.Model.CommandSpec spec = cmd.getSubcommands().get("move").getSubcommands().get("group").getCommandSpec();

		assertNotNull(spec.optionsMap().get("--dest-group").completionCandidates());
		assertEquals("/test/", spec.optionsMap().get("--dest-group").completionCandidates().iterator().next());
	}
}
