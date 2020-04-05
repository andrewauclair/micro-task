// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static com.andrewauclair.microtask.Utils.NL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_Finish_Test extends TaskBaseTestCase {
	@Test
	void finish_writes_file_on_correct_list() {
		tasks.addTask("Test");
		tasks.addList("one", true);
		
		tasks.startTask(1, false);
		
		tasks.setActiveList("one");
		
		tasks.addTask("Test 2");
		
		Mockito.reset(writer);
		
		Task task = tasks.finishTask();
		
		Mockito.verify(writer).writeTask(task, "git-data/tasks/default/1.txt");
	}
	
	@Test
	void finish_tells_git_control_to_add_correct_files() {
		tasks.addTask("Test");
		tasks.addList("one", true);
		
		tasks.startTask(1, false);
		
		tasks.setActiveList("one");
		
		tasks.addTask("Test 2");
		
		Mockito.reset(osInterface);
		
		tasks.finishTask();
		
		InOrder order = Mockito.inOrder(osInterface);
		
		order.verify(osInterface).runGitCommand("git add tasks/default/1.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Finished task 1 - 'Test'\"");
	}
	
	@Test
	void finish_task_when_on_different_list() {
		tasks.addTask("Test");
		
		tasks.startTask(1, false);
		
		tasks.addList("test", true);
		tasks.setActiveList("test");
		
		Task task = tasks.finishTask(1);
		
		assertEquals(
				new Task(1, "Test", TaskState.Finished, Arrays.asList(new TaskTimes(1000), new TaskTimes(2000, 3000), new TaskTimes(3000))),
				task
		);
	}

	@Test
	void recurring_tasks_cannot_be_finished() {
		tasks.addTask("Test");
		tasks.setRecurring(1, true);

		tasks.startTask(1, false);
		
		TaskException taskException = assertThrows(TaskException.class, tasks::finishTask);
		
		assertEquals("Recurring tasks cannot be finished.", taskException.getMessage());
	}
	
	@Test
	void finish_list_writes_file() throws IOException {
		tasks.addList("/test/one", true);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/one/list.txt")).thenReturn(new DataOutputStream(outputStream));
		
		tasks.finishList("/test/one");
		
		assertThat(outputStream.toString()).isEqualTo(
				"" + NL + NL + "Finished" + NL
		);
	}
	
	@Test
	void finish_list_commits_files_to_git() {
		tasks.addList("/test/one", true);
		
		tasks.finishList("/test/one");
		
		InOrder order = Mockito.inOrder(osInterface);
		
		order.verify(osInterface).runGitCommand("git add tasks/test/one/list.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Finished list '/test/one'\"");
	}
	
	@Test
	void finish_group_writes_file() throws IOException {
		tasks.addGroup("/test/");
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/group.txt")).thenReturn(new DataOutputStream(outputStream));
		
		tasks.finishGroup("/test/");
		
		assertThat(outputStream.toString()).isEqualTo(
				"" + NL + NL + "Finished" + NL
		);
	}
	
	@Test
	void finish_group_commits_files_to_git() {
		tasks.addGroup("/test/");
		
		tasks.finishGroup("/test/");
		
		InOrder order = Mockito.inOrder(osInterface);
		
		order.verify(osInterface).runGitCommand("git add tasks/test/group.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Finished group '/test/'\"");
	}
}
