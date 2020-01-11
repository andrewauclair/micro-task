// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.ActiveListCompleter;
import com.andrewauclair.todo.jline.ActiveTaskCompleter;
import org.jline.builtins.Completers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jline.builtins.Completers.TreeCompleter.node;

class TimesCommandTest extends CommandsBaseTestCase {
	@Test
	void verify_auto_complete_nodes() {
		TimesCommand command = new TimesCommand(tasks, osInterface);
		
		List<Completers.TreeCompleter.Node> autoCompleteNodes = command.getAutoCompleteNodes();
		
		assertThat((Object) autoCompleteNodes).isEqualToComparingFieldByFieldRecursively(
				Arrays.asList(
						node("times",
								node("--list",
										node(new ActiveListCompleter(tasks)),
										node("--today")
								)
						),
						node("times",
								node("--tasks",
										node(new ActiveTaskCompleter(tasks)),
										node("--today")
								)
						),
						node("times",
								node("--task"
								)
						)
				)
		);
	}
}
