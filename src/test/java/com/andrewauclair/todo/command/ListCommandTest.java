// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.ListCompleter;
import org.jline.builtins.Completers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jline.builtins.Completers.TreeCompleter.node;

class ListCommandTest extends CommandsBaseTestCase {
	@Test
	void verify_auto_complete_nodes() {
		ListCommand command = new ListCommand(tasks);
		
		List<Completers.TreeCompleter.Node> autoCompleteNodes = command.getAutoCompleteNodes();
		
		assertThat((Object) autoCompleteNodes).isEqualToComparingFieldByFieldRecursively(
				Arrays.asList(
						node("list",
								node("--tasks",
										node("--list",
												node(new ListCompleter(tasks, true), node("--all"))
										),
										node("--all"),
										node("--group",
												node("--all"),
												node("--recursive")
										)
								)
						),
						node("list",
								node("--lists")
						)
				)
		);
	}
}
