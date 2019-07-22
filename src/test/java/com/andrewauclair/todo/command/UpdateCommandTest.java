// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.CommandsBaseTestCase;
import com.andrewauclair.todo.os.GitLabReleases;
import org.jline.builtins.Completers;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jline.builtins.Completers.TreeCompleter.node;

class UpdateCommandTest extends CommandsBaseTestCase {
	@Test
	void verify_auto_complete_nodes() {
		UpdateCommand command = new UpdateCommand(new GitLabReleases());
		
		List<Completers.TreeCompleter.Node> autoCompleteNodes = command.getAutoCompleteNodes();
		
		assertThat((Object) autoCompleteNodes).isEqualToComparingFieldByFieldRecursively(
				Collections.singletonList(
						node("update",
								node("-r", "--releases"),
								node("-l", "--latest")
						)
				)
		);
	}
}
