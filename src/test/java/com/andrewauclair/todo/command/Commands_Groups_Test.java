// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Groups_Test extends CommandsBaseTestCase {
	@Test
	void create_group_command() throws IOException {
		commands.execute(printStream, "mkgrp /test/one/two/three/");
		
		assertTrue(tasks.hasGroupPath("/test/one/two/three/"));

		Mockito.verify(osInterface).createOutputStream("git-data/tasks/test/group.txt");
		Mockito.verify(osInterface).createOutputStream("git-data/tasks/test/one/group.txt");
		Mockito.verify(osInterface).createOutputStream("git-data/tasks/test/one/two/group.txt");
		Mockito.verify(osInterface).createOutputStream("git-data/tasks/test/one/two/three/group.txt");
		Mockito.verify(osInterface).runGitCommand("git add .", false);
		Mockito.verify(osInterface).runGitCommand("git commit -m \"Created group '/test/one/two/three/'\"", false);

		assertOutput(
				"Created group '/test/one/two/three/'",
				""
		);
	}

	@Test
	void switch_group_command() {
		commands.execute(printStream, "mkgrp /test/one/two/three/");
		commands.execute(printStream, "chgrp /test/one/two/three/");
		
		assertEquals("/test/one/two/three/", tasks.getActiveGroup().getFullPath());
	}

	@Test
	void create_relative_group() {
		commands.execute(printStream, "mkgrp one/");
		commands.execute(printStream, "chgrp one/");

		outputStream.reset();
		
		commands.execute(printStream, "mkgrp two/three/");
		
		assertTrue(tasks.hasGroupPath("/one/two/three/"));

		assertOutput(
				"Created group '/one/two/three/'",
				""
		);
	}

	@Test
	void switch_to_relative_group() {
		commands.execute(printStream, "mkgrp /one/two/three/");
		commands.execute(printStream, "chgrp /one/two/");

		outputStream.reset();
		
		commands.execute(printStream, "chgrp three/");

		assertOutput(
				"Switched to group '/one/two/three/'",
				""
		);
	}

	@Test
	void move_back_one_group_with_dot_dot_parameter() {
		commands.execute(printStream, "mkgrp /one/two/");
		commands.execute(printStream, "chgrp /one/two/");

		outputStream.reset();

		commands.execute(printStream, "chgrp ..");

		assertOutput(
				"Switched to group '/one/'",
				""
		);
		
		assertEquals("/one/", tasks.getActiveGroup().getFullPath());
	}

	@Test
	void dot_dot_parameter_does_nothing_in_root_group() {
		commands.execute(printStream, "chgrp ..");

		assertOutput();

		assertEquals("/", tasks.getActiveGroup().getFullPath());
	}
}
