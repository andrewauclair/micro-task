// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.jline;

import com.andrewauclair.todo.command.CommandsBaseTestCase;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RenameCompleter_Rename_Test extends CommandsBaseTestCase {
	private final RenameCompleter completer = new RenameCompleter(tasks);
	private final LineReader lineReader = Mockito.mock(LineReader.class);
	private final ParsedLine parsedLine = Mockito.mock(ParsedLine.class);

	@Test
	void commands_completer_basic_rename_completion() {
		commands.execute(printStream, "add -n \"Test\"");

		Mockito.when(parsedLine.word()).thenReturn("1");

		List<Candidate> candidates = new ArrayList<>();

		completer.complete(lineReader, parsedLine, candidates);

		assertThat(candidates).hasSize(1);
		assertThat(new TestCandidate(candidates.get(0))).isEqualTo(new TestCandidate(new Candidate("1 -n \"Test\"")));
	}

	@Test
	void commands_completer_rename_no_candidates_when_id_is_not_complete_with_whitespace_after() {
		commands.execute(printStream, "add \"Test\"");

		Mockito.when(parsedLine.word()).thenReturn("1 ");

		List<Candidate> candidates = new ArrayList<>();

		completer.complete(lineReader, parsedLine, candidates);

		assertThat(candidates).isEmpty();
	}

	@Test
	void commands_completer_rename_with_no_id_returns_empty_list() {
		commands.execute(printStream, "add \"Test\"");

		Mockito.when(parsedLine.word()).thenReturn("");

		List<Candidate> candidates = new ArrayList<>();

		completer.complete(lineReader, parsedLine, candidates);

		assertThat(candidates).isEmpty();
	}

	@Test
	void commands_completer_rename_with_invalid_id_returns_empty_list() {
		commands.execute(printStream, "add \"Test\"");

		Mockito.when(parsedLine.word()).thenReturn("rename --task 123");

		List<Candidate> candidates = new ArrayList<>();

		completer.complete(lineReader, parsedLine, candidates);

		assertThat(candidates).isEmpty();
	}

	@Test
	void commands_completer_rename_with_id_that_is_not_a_number_returns_empty_list() {
		commands.execute(printStream, "add \"Test\"");

		Mockito.when(parsedLine.word()).thenReturn("rename --task 12a82");

		List<Candidate> candidates = new ArrayList<>();

		completer.complete(lineReader, parsedLine, candidates);

		assertThat(candidates).isEmpty();
	}

	@Test
	void commands_completer_rename_no_candidates_after_task_name_has_been_added() {
		commands.execute(printStream, "add \"Test\"");

		Mockito.when(parsedLine.word()).thenReturn("rename --task 123 -n \"New Name Here\"");

		List<Candidate> candidates = new ArrayList<>();

		completer.complete(lineReader, parsedLine, candidates);

		assertThat(candidates).isEmpty();
	}
}
