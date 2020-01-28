// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.ActiveListCompleter;
import org.jline.builtins.Completers;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jline.builtins.Completers.TreeCompleter.node;

class AddCommandTest extends CommandsBaseTestCase {
	@Test
	void verify_auto_complete_nodes() {
		AddCommand command = new AddCommand(tasks, commands);

		List<Completers.TreeCompleter.Node> autoCompleteNodes = command.getAutoCompleteNodes();

		assertThat((Object) autoCompleteNodes).isEqualToComparingFieldByFieldRecursively(
				Collections.singletonList(
						node("add",
								node("--list",
										node(new ActiveListCompleter(tasks))
								),
								node("-l",
										node(new ActiveListCompleter(tasks))
								),
								node("--name"),
								node("-n")
						)//,
//				node("add",
//				)
				)
		);
	}
}
