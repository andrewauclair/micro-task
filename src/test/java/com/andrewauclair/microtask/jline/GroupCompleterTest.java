// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.jline;

import com.andrewauclair.microtask.command.CommandsBaseTestCase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GroupCompleterTest extends CommandsBaseTestCase {
	@Test
	void candidates_list_contains_all_groups() {
		tasks.addGroup("/test/one/two/");
		tasks.addGroup("/last/");
		tasks.addGroup("/three/five/");

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

	@Test
	void candidates_list_contains_only_in_progress_groups() {
		tasks.addGroup("/test/one/");
		tasks.addGroup("/test/two/");
		tasks.addGroup("/test/three/");

		tasks.finishGroup("/test/two/");

		final GroupCompleter completer = new GroupCompleter(tasks);

		assertThat(completer).containsOnly(
				"/test/",
				"/test/one/",
				"/test/three/"
		);
	}
}
