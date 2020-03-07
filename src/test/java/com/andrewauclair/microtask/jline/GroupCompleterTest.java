// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.jline;

import com.andrewauclair.microtask.command.CommandsBaseTestCase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GroupCompleterTest extends CommandsBaseTestCase {
	@Test
	void candidates_list_contains_all_groups() {
		commands.execute(printStream, "mk -g /test/one/two");
		commands.execute(printStream, "mk -g /last");
		commands.execute(printStream, "mk -g /three/five");

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
}
