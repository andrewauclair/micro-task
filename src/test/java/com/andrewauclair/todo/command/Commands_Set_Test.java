// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskState;
import com.andrewauclair.todo.task.TaskTimes;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class Commands_Set_Test extends CommandsBaseTestCase {
	@Test
	void execute_set_recurring_true_command() {
		tasks.addTask("Test 1");
		
		commands.execute(printStream, "set --task 1 --recurring true");
		
		Task task = tasks.getTask(1);
		
		assertTrue(task.isRecurring());
	}

	@Test
	void execute_set_recurring_false_command() {
		tasks.addTask("Test 1");
		
		commands.execute(printStream, "set --task 1 --recurring false");
		
		Task task = tasks.getTask(1);
		
		assertFalse(task.isRecurring());
	}

	@Test
	void execute_set_project_command_for_list() {
		tasks.addList("/test", true);
		tasks.setActiveList("/test");
		tasks.addTask("Test 1");
		
		commands.execute(printStream, "set --list /test --project \"Issues\"");
		
		assertEquals("Issues", tasks.getProjectForTask(1));
	}
	
	@Test
	void execute_set_project_command_for_group() {
		tasks.addList("/test/one", true);
		tasks.setActiveList("/test/one");
		tasks.addTask("Test 1");
		
		commands.execute(printStream, "set --group /test/ --project \"Issues\"");
		
		assertEquals("Issues", tasks.getProjectForTask(1));
	}
	
	@Test
	void execute_set_feature_command_for_list() {
		tasks.addList("/test", true);
		tasks.setActiveList("/test");
		tasks.addTask("Test 1");
		
		commands.execute(printStream, "set --list /test --feature \"Feature\"");
		
		assertEquals("Feature", tasks.getFeatureForTask(1));
	}
	
	@Test
	void execute_set_feature_command_for_group() {
		tasks.addList("/test/one", true);
		tasks.setActiveList("/test/one");
		tasks.addTask("Test 1");
		
		commands.execute(printStream, "set --group /test/ --feature \"Feature\"");
		
		assertEquals("Feature", tasks.getFeatureForTask(1));
	}

	@Test
	void execute_set_task_to_inactive() {
		tasks.addTask("Test");
		tasks.finishTask(1);

		assertEquals(TaskState.Finished, tasks.getTask(1).state);

		commands.execute(printStream, "set --task 1 --inactive");

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

		commands.execute(printStream, "set --task 1 --inactive");
		
		Mockito.verify(writer).writeTask(new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000))), "git-data/tasks/default/1.txt");
	}

	@Test
	void write_git_commit_when_setting_inactive() {
		tasks.addTask("Test");
		tasks.finishTask(1);

		Mockito.reset(osInterface);

		commands.execute(printStream, "set --task 1 --inactive");

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).runGitCommand("git add tasks/default/1.txt", false);
		order.verify(osInterface).runGitCommand("git commit -m \"Set state for task 1 to Inactive\"", false);
	}
}
