package com.andrewauclair.todo.command;

import com.andrewauclair.todo.CommandsBaseTestCase;
import org.jline.builtins.Completers;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jline.builtins.Completers.TreeCompleter.node;

class VersionCommandTest extends CommandsBaseTestCase {
	@Test
	void verify_auto_complete_nodes() {
		VersionCommand command = new VersionCommand(tasks);

		List<Completers.TreeCompleter.Node> autoCompleteNodes = command.getAutoCompleteNodes();

		assertThat((Object) autoCompleteNodes).isEqualToComparingFieldByFieldRecursively(
				Collections.singletonList(node("version"))
		);
	}
}
