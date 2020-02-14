// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.jline;

import com.andrewauclair.todo.command.CommandsBaseTestCase;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
