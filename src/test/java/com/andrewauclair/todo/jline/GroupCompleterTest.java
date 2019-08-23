// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
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
	private final LineReader lineReader = Mockito.mock(LineReader.class);
	private final ParsedLine parsedLine = Mockito.mock(ParsedLine.class);

	@Test
	void candidates_list_contains_all_groups() {
		final GroupCompleter completer = new GroupCompleter(tasks, true);

		commands.execute(printStream, "mkgrp /test/one/two");
		commands.execute(printStream, "mkgrp /last");
		commands.execute(printStream, "mkgrp /three/five");

		List<Candidate> candidates = new ArrayList<>();

		completer.complete(lineReader, parsedLine, candidates);

		List<TestCandidate> actual = candidates.stream()
				.map(TestCandidate::new)
				.collect(Collectors.toList());

		assertThat(actual).containsOnly(
				new TestCandidate(new Candidate("/test")),
				new TestCandidate(new Candidate("/test/one")),
				new TestCandidate(new Candidate("/test/one/two")),
				new TestCandidate(new Candidate("/last")),
				new TestCandidate(new Candidate("/three")),
				new TestCandidate(new Candidate("/three/five"))
		);
	}

	@Test
	void group_completer_supports_mode_that_excludes_the_current_group() {
		final GroupCompleter completer = new GroupCompleter(tasks, false);

		commands.execute(printStream, "mkgrp /test/one/two");
		commands.execute(printStream, "mkgrp /last");
		commands.execute(printStream, "mkgrp /three/five");

		commands.execute(printStream, "chgrp /test/one");

		List<Candidate> candidates = new ArrayList<>();

		completer.complete(lineReader, parsedLine, candidates);

		List<TestCandidate> actual = candidates.stream()
				.map(TestCandidate::new)
				.collect(Collectors.toList());

		assertThat(actual).containsOnly(
				new TestCandidate(new Candidate("/test")),
				new TestCandidate(new Candidate("/test/one/two")),
				new TestCandidate(new Candidate("/last")),
				new TestCandidate(new Candidate("/three")),
				new TestCandidate(new Candidate("/three/five"))
		);
	}
}
