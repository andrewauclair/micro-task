// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.jline;

import com.andrewauclair.microtask.command.CommandsBaseTestCase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ListCompleterTest extends CommandsBaseTestCase {
	@Test
	void candidates_list_contains_all_lists() {
		commands.execute(printStream, "mk -l alpha");
		commands.execute(printStream, "mk -l bravo");
		commands.execute(printStream, "mk -l /charlie");

		final ListCompleter completer = new ListCompleter(tasks);

		assertThat(completer).containsOnly(
				"/default",
				"/alpha",
				"/bravo",
				"/charlie"
		);
	}

	@Test
	void candidates_list_contains_only_in_progress_lists() {
		tasks.addList("/alpha", true);
		tasks.addList("/bravo", true);
		tasks.addList("/charlie", true);

		tasks.finishList("/bravo");

		final ListCompleter completer = new ListCompleter(tasks);

		assertThat(completer).containsOnly(
				"/alpha",
				"/charlie",
				"/default"
		);
	}
}
