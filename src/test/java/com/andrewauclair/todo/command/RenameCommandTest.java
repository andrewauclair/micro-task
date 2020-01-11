// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.jline.RenameCompleter;
import org.jline.builtins.Completers;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jline.builtins.Completers.TreeCompleter.node;

class RenameCommandTest extends CommandsBaseTestCase {
	@Test
	void verify_auto_complete_nodes() {
		RenameCommand command = new RenameCommand(tasks);
		
		List<Completers.TreeCompleter.Node> autoCompleteNodes = command.getAutoCompleteNodes();
		
		assertThat((Object) autoCompleteNodes).isEqualToComparingFieldByFieldRecursively(
				Collections.singletonList(
						node("rename",
								node("--task",
										node(new RenameCompleter(tasks))
								),
								node("--list",
										node(new ListCompleter(tasks, true)
										)
								)
						)
				)
		);
	}
}
