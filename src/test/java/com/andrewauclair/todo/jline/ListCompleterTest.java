// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.jline;

import com.andrewauclair.todo.CommandsBaseTestCase;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ListCompleterTest extends CommandsBaseTestCase {

	private final LineReader lineReader = Mockito.mock(LineReader.class);
	private final ParsedLine parsedLine = Mockito.mock(ParsedLine.class);
	
	@Test
	void candidates_list_contains_all_lists() {
		final ListCompleter completer = new ListCompleter(tasks, true);

		commands.execute(printStream, "create-list alpha");
		commands.execute(printStream, "create-list bravo");
		commands.execute(printStream, "create-list charlie");
		
		List<Candidate> candidates = new ArrayList<>();
		
		completer.complete(lineReader, parsedLine, candidates);
		
		List<TestCandidate> actual = candidates.stream()
				.map(TestCandidate::new)
				.collect(Collectors.toList());
		
		assertThat(actual).containsOnly(
				new TestCandidate(new Candidate("default")),
				new TestCandidate(new Candidate("alpha")),
				new TestCandidate(new Candidate("bravo")),
				new TestCandidate(new Candidate("charlie"))
		);
	}

	@Test
	void list_completer_supports_mode_that_excludes_the_current_list() {
		final ListCompleter completer = new ListCompleter(tasks, false);

		commands.execute(printStream, "create-list alpha");
		commands.execute(printStream, "create-list bravo");
		commands.execute(printStream, "create-list charlie");

		List<Candidate> candidates = new ArrayList<>();

		completer.complete(lineReader, parsedLine, candidates);

		List<TestCandidate> actual = candidates.stream()
				.map(TestCandidate::new)
				.collect(Collectors.toList());

		assertThat(actual).containsOnly(
				new TestCandidate(new Candidate("alpha")),
				new TestCandidate(new Candidate("bravo")),
				new TestCandidate(new Candidate("charlie"))
		);
	}
}
