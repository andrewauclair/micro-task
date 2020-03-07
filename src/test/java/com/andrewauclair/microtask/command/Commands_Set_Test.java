// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.TaskTimes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class Commands_Set_Test extends CommandsBaseTestCase {
	@Test
	void execute_set_recurring_true_command() {
		tasks.addTask("Test 1");

		commands.execute(printStream, "set-task --task 1 --recurring");

		Task task = tasks.getTask(1);

		assertTrue(task.isRecurring());

		assertOutput(
				"Set recurring for task 1 - 'Test 1' to true",
				""
		);
	}

	@Test
	void execute_set_recurring_false_command() {
		tasks.addTask("Test 1");

		commands.execute(printStream, "set-task --task 1 --not-recurring");

		Task task = tasks.getTask(1);

		assertFalse(task.isRecurring());

		assertOutput(
				"Set recurring for task 1 - 'Test 1' to false",
				""
		);
	}

	@Test
	void execute_set_project_command_for_list() {
		tasks.addList("/test", true);
		tasks.setActiveList("/test");
		tasks.addTask("Test 1");

		commands.execute(printStream, "set-list --list /test --project \"Issues\"");

		assertEquals("Issues", tasks.getProjectForTask(1));

		assertOutput(
				"Set project for list '/test' to 'Issues'",
				""
		);
	}

	@Test
	void execute_set_feature_command_for_list() {
		tasks.addList("/test", true);
		tasks.setActiveList("/test");
		tasks.addTask("Test 1");

		commands.execute(printStream, "set-list --list /test --feature \"Feature\"");

		assertEquals("Feature", tasks.getFeatureForTask(1));

		assertOutput(
				"Set feature for list '/test' to 'Feature'",
				""
		);
	}

	@Test
	void set_project_and_feature_for_list_at_same_time() {
		tasks.addList("/test", true);
		tasks.setActiveList("/test");
		tasks.addTask("Test 1");

		commands.execute(printStream, "set-list --list /test --project \"Issues\" --feature \"Feature\"");

		assertEquals("Issues", tasks.getProjectForTask(1));
		assertEquals("Feature", tasks.getFeatureForTask(1));

		assertOutput(
				"Set project for list '/test' to 'Issues'",
				"Set feature for list '/test' to 'Feature'",
				""
		);
	}

	@Test
	void set_list_requires_one_of_project_or_feature() {
		commands.execute(printStream, "set-list --list /test");

		assertOutput(
				"Error: Missing required argument(s): ([-p=<project>] [-f=<feature>])",
				""
		);
	}

	@Test
	void execute_set_project_command_for_group() {
		tasks.addList("/test/one", true);
		tasks.setActiveList("/test/one");
		tasks.addTask("Test 1");

		commands.execute(printStream, "set-group --group /test/ --project \"Issues\"");

		assertEquals("Issues", tasks.getProjectForTask(1));

		assertOutput(
				"Set project for group '/test/' to 'Issues'",
				""
		);
	}

	@Test
	void execute_set_feature_command_for_group() {
		tasks.addList("/test/one", true);
		tasks.setActiveList("/test/one");
		tasks.addTask("Test 1");

		commands.execute(printStream, "set-group --group /test/ --feature \"Feature\"");

		assertEquals("Feature", tasks.getFeatureForTask(1));

		assertOutput(
				"Set feature for group '/test/' to 'Feature'",
				""
		);
	}

	@Test
	void set_project_and_feature_for_group_at_same_time() {
		tasks.addList("/test/one", true);
		tasks.setActiveList("/test/one");
		tasks.addTask("Test 1");

		commands.execute(printStream, "set-group --group /test/ --project \"Issues\" --feature \"Feature\"");

		assertEquals("Issues", tasks.getProjectForTask(1));
		assertEquals("Feature", tasks.getFeatureForTask(1));

		assertOutput(
				"Set project for group '/test/' to 'Issues'",
				"Set feature for group '/test/' to 'Feature'",
				""
		);
	}

	@Test
	void set_group_requires_one_of_project_or_feature() {
		commands.execute(printStream, "set-group --group /test/");

		assertOutput(
				"Error: Missing required argument(s): ([-p=<project>] [-f=<feature>])",
				""
		);
	}

	@Test
	void execute_set_task_to_inactive() {
		tasks.addTask("Test");
		tasks.finishTask(1);

		assertEquals(TaskState.Finished, tasks.getTask(1).state);

		commands.execute(printStream, "set-task --task 1 --inactive");

		assertEquals(TaskState.Inactive, tasks.getTask(1).state);

		assertOutput(
				"Set state of task 1 - 'Test' to Inactive",
				""
		);
	}

	@Test
	void write_task_when_setting_inactive() {
		tasks.addTask("Test");
		tasks.finishTask(1);

		Mockito.reset(writer);

		commands.execute(printStream, "set-task --task 1 --inactive");

		Mockito.verify(writer).writeTask(new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000))), "git-data/tasks/default/1.txt");
	}

	@Test
	void write_git_commit_when_setting_inactive() {
		tasks.addTask("Test");
		tasks.finishTask(1);

		Mockito.reset(osInterface);

		commands.execute(printStream, "set-task --task 1 --inactive");

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).runGitCommand("git add tasks/default/1.txt", false);
		order.verify(osInterface).runGitCommand("git commit -m \"Set state for task 1 to Inactive\"", false);
	}

	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void set_task_command_help(String parameter) {
		commands.execute(printStream, "set-task " + parameter);

		assertOutput(
				"Usage:  set-task [-hr] [--inactive] [--not-recurring] --task=<id>",
				"  -h, --help            Show this help message.",
				"      --inactive",
				"      --not-recurring",
				"  -r, --recurring",
				"      --task=<id>"
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void set_list_command_help(String parameter) {
		commands.execute(printStream, "set-list " + parameter);

		assertOutput(
				"Usage:  set-list ([-p=<project>] [-f=<feature>]) [-h] -l=<list>",
				"  -f, --feature=<feature>",
				"  -h, --help                Show this help message.",
				"  -l, --list=<list>",
				"  -p, --project=<project>"
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void set_group_command_help(String parameter) {
		commands.execute(printStream, "set-group " + parameter);

		assertOutput(
				"Usage:  set-group ([-p=<project>] [-f=<feature>]) [-h] -g=<group>",
				"  -f, --feature=<feature>",
				"  -g, --group=<group>",
				"  -h, --help                Show this help message.",
				"  -p, --project=<project>"
		);
	}
}
