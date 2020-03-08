// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.jline;

import com.andrewauclair.microtask.command.CommandsBaseTestCase;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ActiveTaskCompleter_Test extends CommandsBaseTestCase {
	private ActiveTaskCompleter completer;
	private final LineReader lineReader = Mockito.mock(LineReader.class);
	private final ParsedLine parsedLine = Mockito.mock(ParsedLine.class);

	@BeforeEach
	public void setup() throws IOException {
		super.setup();

		completer = new ActiveTaskCompleter(tasks);
	}

	@Test
	void active_id_is_in_candidates_list() {
		commands.execute(printStream, "add -n \"Test\"");
		commands.execute(printStream, "start 1");

		Mockito.when(parsedLine.word()).thenReturn("");

		List<Candidate> candidates = new ArrayList<>();

		completer.complete(lineReader, parsedLine, candidates);

		assertThat(candidates).hasSize(1);
		assertThat(new TestCandidate(candidates.get(0))).isEqualTo(new TestCandidate(new Candidate("1")));
	}

	@Test
	void candidates_list_is_empty_when_there_is_no_active_task() {
		Mockito.when(parsedLine.word()).thenReturn("");

		List<Candidate> candidates = new ArrayList<>();

		completer.complete(lineReader, parsedLine, candidates);

		assertThat(candidates).isEmpty();
	}
}
