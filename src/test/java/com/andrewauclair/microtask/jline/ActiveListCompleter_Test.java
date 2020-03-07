// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.jline;

import com.andrewauclair.microtask.command.CommandsBaseTestCase;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ActiveListCompleter_Test extends CommandsBaseTestCase {
	private final ActiveListCompleter completer = new ActiveListCompleter(tasks);
	private final LineReader lineReader = Mockito.mock(LineReader.class);
	private final ParsedLine parsedLine = Mockito.mock(ParsedLine.class);

	@Test
	void active_id_is_in_candidates_list() {
		commands.execute(printStream, "mk -l test");
		commands.execute(printStream, "ch -l test");

		Mockito.when(parsedLine.word()).thenReturn("");

		List<Candidate> candidates = new ArrayList<>();

		completer.complete(lineReader, parsedLine, candidates);

		assertThat(candidates).hasSize(1);
		assertThat(new TestCandidate(candidates.get(0))).isEqualTo(new TestCandidate(new Candidate("/test")));
	}

	@Test
	void candidates_list_contains_default_list_by_default() {
		Mockito.when(parsedLine.word()).thenReturn("");

		List<Candidate> candidates = new ArrayList<>();

		completer.complete(lineReader, parsedLine, candidates);

		assertThat(candidates).hasSize(1);
		assertThat(new TestCandidate(candidates.get(0))).isEqualTo(new TestCandidate(new Candidate("/default")));
	}
}
