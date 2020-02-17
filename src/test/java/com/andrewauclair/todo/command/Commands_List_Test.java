// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

import static com.andrewauclair.todo.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.todo.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Commands_List_Test extends CommandsBaseTestCase {
	@Test
	void list_command_lists_groups_and_lists_in_the_current_group() {
		tasks.addList("charlie", true);
		tasks.addList("bravo", true);
		tasks.addList("alpha", true);
		tasks.addList("/test/one/two", true);

		commands.execute(printStream, "list");

		assertOutput(
				"Current group is '/'",
				"",
				"  alpha",
				"  bravo",
				"  charlie",
				"* " + ANSI_FG_GREEN + "default" + ANSI_RESET,
				"  test/",
				""
		);
	}

	@Test
	void list_groups_and_lists_for_nested_group() {
		tasks.addList("none", true);

		tasks.createGroup("/one/two/");
		tasks.createGroup("/one/three/");
		tasks.switchGroup("/one/two/");
		tasks.addList("charlie", true);
		tasks.addList("bravo", true);
		tasks.addList("alpha", true);

		commands.execute(printStream, "list");

		assertOutput(
				"Current group is '/one/two/'",
				"",
				"  alpha",
				"  bravo",
				"  charlie",
				""
		);
	}

	@Test
	void list_command_hides_finished_lists() {
		tasks.addList("/test/one", true);
		tasks.addList("/test/two", true);

		tasks.finishList("/test/two");

		tasks.switchGroup("/test/");

		commands.execute(printStream, "list");

		assertOutput(
				"Current group is '/test/'",
				"",
				"  one",
				""
		);
	}

	@Test
	void list_command_hides_finished_groups() {
		tasks.addGroup("/test/one/");
		tasks.addGroup("/test/two/");

		tasks.switchGroup("/test/");

		tasks.finishGroup("/test/two/");

		commands.execute(printStream, "list");

		assertOutput(
				"Current group is '/test/'",
				"",
				"  one/",
				""
		);
	}

	@Test
	void list_command_with_finished_parameter_displays_finished_lists_and_groups() {
		tasks.addGroup("/test/one/");
		tasks.addGroup("/test/two/");
		tasks.addList("/test/three", true);
		tasks.addList("/test/four", true);

		tasks.switchGroup("/test/");

		tasks.finishGroup("/test/two/");
		tasks.finishList("/test/four");

		commands.execute(printStream, "list --finished");

		assertOutput(
				"Current group is '/test/'",
				"",
				"  four",
				"  two/",
				""
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"ch", "add", "finish", "list", "rename", "move", "set-list", "times"})
	void command_list_option_has_list_completer(String command) {
		CommandLine cmd = commands.buildCommandLine();

		CommandLine.Model.CommandSpec spec = cmd.getSubcommands().get(command).getCommandSpec();

		assertNotNull(spec.optionsMap().get("--list").completionCandidates());
		assertEquals("/default", spec.optionsMap().get("--list").completionCandidates().iterator().next());
	}
	
	@Test
	void move_command_dest_list_option_has_list_completer() {
		CommandLine cmd = commands.buildCommandLine();
		
		CommandLine.Model.CommandSpec spec = cmd.getSubcommands().get("move").getCommandSpec();
		
		assertNotNull(spec.optionsMap().get("--dest-list").completionCandidates());
		assertEquals("/default", spec.optionsMap().get("--dest-list").completionCandidates().iterator().next());
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void list_command_help(String parameter) {
		commands.execute(printStream, "list " + parameter);

		assertOutput(
				"Usage:  list [-h] [--all] [--finished] [--group] [--recursive] [--tasks]",
						"             [--list=<list>]",
						"      --all",
						"      --finished",
						"      --group",
						"  -h, --help          Show this help message.",
						"      --list=<list>",
						"      --recursive",
						"      --tasks"
		);
	}
}
