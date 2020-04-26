package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.MockOSInterface;
import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.TaskWriter;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;
import com.andrewauclair.microtask.task.group.name.NewTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingTaskListName;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;

@ExtendWith(MockitoExtension.class)
class ProjectBaseTestCase {
	final TaskWriter writer = Mockito.mock(TaskWriter.class);
	final MockOSInterface osInterface = Mockito.spy(MockOSInterface.class);
	final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	Tasks tasks;
	Projects projects;

	Project project;

	@BeforeEach
	void setup() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(new DataOutputStream(new ByteArrayOutputStream()));
		Mockito.when(osInterface.runGitCommand(Mockito.any())).thenReturn(true);

		Mockito.when(osInterface.fileExists("git-data")).thenReturn(true);

		tasks = new Tasks(writer, new PrintStream(outputStream), osInterface);
//		tasks.addList("default", true); // add the default list, in reality it gets created, but we don't want all that stuff to happen

		projects = new Projects(tasks);
		project = projects.createProject("Test");
	}

	public ExistingTaskListName existingList(String name) {
		return new ExistingTaskListName(tasks, name);
	}

	public NewTaskListName newList(String name) {
		return new NewTaskListName(tasks, name);
	}

	public ExistingTaskGroupName existingGroup(String name) {
		return new ExistingTaskGroupName(tasks, name);
	}

	public NewTaskGroupName newGroup(String name) {
		return new NewTaskGroupName(tasks, name);
	}

	public ExistingID existingID(long ID) {
		return new ExistingID(tasks, ID);
	}
}
