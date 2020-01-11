// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.LongCompleter;
import org.jline.builtins.Completers;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jline.builtins.Completers.TreeCompleter.node;

class SetCommandTest extends CommandsBaseTestCase {
	@Test
	void verify_auto_complete_nodes() {
		SetCommand command = new SetCommand(tasks);

		List<Completers.TreeCompleter.Node> autoCompleteNodes = command.getAutoCompleteNodes();

		assertThat((Object) autoCompleteNodes).isEqualToComparingFieldByFieldRecursively(
				Collections.singletonList(
						node("set",
								node("--task",
										node(new LongCompleter(),
												node("--recurring"),
												node("--project"),
												node("--feature")
										)
								)
						)
				)
		);
	}
}
