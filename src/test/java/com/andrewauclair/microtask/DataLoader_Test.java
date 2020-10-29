// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.project.*;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.andrewauclair.microtask.TestUtils.createInputStream;
import static com.andrewauclair.microtask.TestUtils.newTask;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

// responsible for testing all data loading now that we have a lot of different file types
public class DataLoader_Test {

	private DataFolder support = new DataFolder("support");
	private DataFolder releases = new DataFolder("releases");

	private static interface FileSystemEntity {
	}

	private static class DataFolder implements FileSystemEntity {
		final String name;
		List<FileSystemEntity> entities = new ArrayList<>();

		DataFolder(String name) {
			this.name = name;
		}
	}

	private static class DataFile implements FileSystemEntity {
		final String name;
		List<String> contents = new ArrayList<>();

		DataFile(String name, String... contents) {
			this.name = name;
			this.contents.addAll(Arrays.asList(contents));
		}
	}

	final TaskWriter writer = Mockito.mock(TaskWriter.class);
	protected final MockOSInterface osInterface = Mockito.spy(MockOSInterface.class);
	final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	protected Tasks tasks;

	private final TaskReader reader = new TaskReader(osInterface);
	private final LocalSettings localSettings = new LocalSettings(osInterface);
	private Projects projects;

	// TODO setup tasks, lists, groups, projects, features, and milestones to test loading

	// TODO make specific tests for each thing and use the setup data

	DataFolder root = new DataFolder("");

	@BeforeEach
	public void setup() throws IOException {

		Mockito.when(osInterface.createInputStream("settings.properties")).thenReturn(new DataInputStream(new ByteArrayInputStream(new byte[0])));
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(new DataOutputStream(new ByteArrayOutputStream()));

		Mockito.when(osInterface.fileExists("git-data")).thenReturn(true);

		PrintStream output = new PrintStream(outputStream);
		System.setOut(output);

		tasks = new Tasks(writer, output, osInterface);
		projects = new Projects(tasks, osInterface);

		// put these here, the Tasks constructor uses these for legit reasons and we don't want to make those fail
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenThrow(new RuntimeException("DataLoader should not write files"));
		Mockito.doThrow(new RuntimeException("DataLoader should not run git commands")).when(osInterface).gitCommit(Mockito.anyString());

		DataFolder doneGroup = new DataFolder("done-group");
		DataFolder doneList = new DataFolder("done-list");
		DataFolder first = new DataFolder("first");
		DataFolder projectsFolder = new DataFolder("projects");
		DataFolder microtaskProject = new DataFolder("micro-task");
		DataFolder featureOne = new DataFolder("one");
		DataFolder featureTwo = new DataFolder("two");
		DataFolder meetings = new DataFolder("meetings");

		support.entities.add(new DataFile("list.txt", "InProgress", ""));

		doneGroup.entities.add(new DataFile("group.txt", "Finished"));
		doneList.entities.add(new DataFile("list.txt", "Finished", ""));

		root.entities.add(support);
		root.entities.add(doneGroup);
		root.entities.add(releases);
		root.entities.add(projectsFolder);

		doneGroup.entities.add(doneList);

		releases.entities.add(first);

		projectsFolder.entities.add(microtaskProject);

		microtaskProject.entities.add(featureOne);
		microtaskProject.entities.add(featureTwo);
		microtaskProject.entities.add(meetings);

		first.entities.add(new DataFile("list.txt", "InProgress", ""));

		releases.entities.add(new DataFile("group.txt", "InProgress", ""));

		projectsFolder.entities.add(new DataFile("group.txt", "InProgress", ""));

		microtaskProject.entities.add(new DataFile("group.txt", "InProgress", ""));
		microtaskProject.entities.add(new DataFile("project.txt", "micro-task", ""));
		microtaskProject.entities.add(new DataFile("milestone.20.9.3.txt", "20.9.3", "feature one", "feature two", "task 1", "task 2"));

		featureOne.entities.add(new DataFile("feature.txt", "one", ""));
		featureOne.entities.add(new DataFile("list.txt", "InProgress", ""));
		featureTwo.entities.add(new DataFile("feature.txt", "two", ""));
		featureTwo.entities.add(new DataFile("list.txt", "InProgress", ""));

		featureTwo.entities.add(new DataFile("1.txt", "Test",
				"Active",
				"false",
				"",
				"",
				"add 500",
				"END"));

		featureOne.entities.add(new DataFile("2.txt", "Test",
				"Inactive",
				"false",
				"due 605800",
				"",
				"",
				"add 1000",
				"END"));
		featureOne.entities.add(new DataFile("archive.txt",
				"git-data/tasks/projects/micro-task/one/3.txt",
				"Test Finished",
				"Finished",
				"false",
				"due 605801",
				"",
				"",
				"add 1001",
				"END",
				"git-data/tasks/projects/micro-task/one/4.txt",
				"Test Finished 2",
				"Finished",
				"false",
				"due 605802",
				"",
				"",
				"add 1002",
				"END",
				"git-data/tasks/projects/micro-task/one/5.txt",
				"Test Finished 3",
				"Finished",
				"false",
				"due 605803",
				"",
				"",
				"add 1003",
				"END"));

		meetings.entities.add(new DataFile("list.txt", "InProgress"));

		// /support (not a project)
		// /support, task 1/2/3
		// /done-group/done-list, finished list and group
		// /releases/first (not a feature or project)
		// /releases/first task 4/5/6
		// /projects/micro-task (project)
		// /projects/micro-task/one (feature)
		// /projects/micro-task/one, task 7/8/9
		// /projects/micro-task/two (feature)
		// task 10/11/12
		// /projects/micro-task/meetings (not a feature
		// task 13
	}

	private void createMocks() throws IOException {
		createMocksForFolder("", root);
	}

	private void createMocksForFolder(String parentFolder, DataFolder folder) throws IOException {
		List<OSInterface.TaskFileInfo> fileInfo = new ArrayList<>();

		String path = parentFolder.endsWith("/") ? parentFolder + folder.name : parentFolder + "/" + folder.name;

		if (path.equals("/")) {
			path = "";
		}
		for (final FileSystemEntity entity : folder.entities) {
			if (entity instanceof DataFolder childFolder) {
				createMocksForFolder(path, childFolder);
				fileInfo.add(new OSInterface.TaskFileInfo(childFolder.name, "git-data/tasks" + path + "/" + childFolder.name, true));
			}
			else if (entity instanceof DataFile file) {
				fileInfo.add(new OSInterface.TaskFileInfo(file.name, "git-data/tasks" + path + "/" + file.name, false));

				Mockito.when(osInterface.createInputStream("git-data/tasks" + path + "/" + file.name)).thenReturn(
						createInputStream(file.contents.toArray(new String[0]))
				);

				Mockito.when(osInterface.fileExists("git-data/tasks" + path + "/" + file.name)).thenReturn(true);
			}
		}

		Mockito.when(osInterface.listFiles("git-data/tasks" + path)).thenReturn(fileInfo);
	}

	@Test
	void loads_support_list() throws IOException {
		createMocks();

		DataLoader dataLoader = new DataLoader(tasks, reader, localSettings, projects, osInterface);

		dataLoader.load();

		List<Task> listTasks = this.tasks.getTasksForList(new ExistingListName(this.tasks, "/support"));
	}

	@Test
	void load_micro_task_project() throws IOException {
		createMocks();

		DataLoader dataLoader = new DataLoader(tasks, reader, localSettings, projects, osInterface);

		dataLoader.load();

		assertTrue(projects.hasProject("micro-task"));

		Project project = projects.getProject(new ExistingProject(projects, "micro-task"));

		assertTrue(project.hasFeature("one"));
		assertTrue(project.hasFeature("two"));

		assertTrue(project.hasMilestone("20.9.3"));

		assertThat(project.getMilestone(new ExistingMilestone(project, "20.9.3")).getFeatures()).containsOnly(
				new ExistingFeature(project, "one"),
				new ExistingFeature(project, "two")
		);

		//new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000))),
		//			new Task(2, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)))
		assertThat(project.getMilestone(new ExistingMilestone(project, "20.9.3")).getTasks()).containsOnly(
				new ExistingID(tasks, 1L),
				new ExistingID(tasks, 2L),
				new ExistingID(tasks, 3L),
				new ExistingID(tasks, 4L),
				new ExistingID(tasks, 5L)
		);
	}

	@Test
	void load_micro_task_feature_one_tasks() throws IOException {
		createMocks();

		DataLoader dataLoader = new DataLoader(tasks, reader, localSettings, projects, osInterface);

		dataLoader.load();

		assertThat(tasks.getTasksForList(new ExistingListName(tasks, "/projects/micro-task/one"))).containsOnly(
				newTask(2, "Test", TaskState.Inactive, 1000),
				newTask(3, "Test Finished", TaskState.Finished, 1001),
				newTask(4, "Test Finished 2", TaskState.Finished, 1002),
				newTask(5, "Test Finished 3", TaskState.Finished, 1003)
		);
	}

	@Test
	void list_file_format() throws IOException {
		createMocks();

		DataLoader dataLoader = new DataLoader(tasks, reader, localSettings, projects, osInterface);

		dataLoader.load();

		assertEquals(TaskContainerState.InProgress, tasks.getList(new ExistingListName(tasks, "support")).getState());
		assertEquals(TaskContainerState.Finished, tasks.getList(new ExistingListName(tasks, "done-group/done-list")).getState());
	}

	// TODO Remove this after we release the new version
	@Test
	void legacy_list_file_format() throws IOException {
		DataFolder folder = new DataFolder("legacy");
		root.entities.add(folder);

		folder.entities.add(new DataFile("list.txt", "", "", "InProgress"));

		createMocks();

		DataLoader dataLoader = new DataLoader(tasks, reader, localSettings, projects, osInterface);

		dataLoader.load();

		assertEquals(TaskContainerState.InProgress, tasks.getList(new ExistingListName(tasks, "legacy")).getState());
	}

	@Test
	void group_file_format() throws IOException {
		createMocks();

		DataLoader dataLoader = new DataLoader(tasks, reader, localSettings, projects, osInterface);

		dataLoader.load();

		assertEquals(TaskContainerState.InProgress, tasks.getGroup("/projects/").getState());
		assertEquals(TaskContainerState.Finished, tasks.getGroup("/done-group/").getState());
	}

	// TODO Remove this after we release the new version
	@Test
	void legacy_group_file_format() throws IOException {
		DataFolder folder = new DataFolder("legacy");
		root.entities.add(folder);

		folder.entities.add(new DataFile("group.txt", "", "", "InProgress"));

		createMocks();

		DataLoader dataLoader = new DataLoader(tasks, reader, localSettings, projects, osInterface);

		dataLoader.load();

		assertEquals(TaskContainerState.InProgress, tasks.getGroup("/legacy/").getState());
	}

	@Test
	void throws_exception_for_unknown_file_in_list_folder() throws IOException {
		support.entities.add(new DataFile("junk.txt"));

		createMocks();

		DataLoader dataLoader = new DataLoader(tasks, reader, localSettings, projects, osInterface);

		TaskException taskException = assertThrows(TaskException.class, dataLoader::load);

		assertEquals("Unexpected file 'git-data/tasks/support/junk.txt'", taskException.getMessage());
	}

	@Test
	void throws_exception_for_unknown_file_in_group_folder() throws IOException {
		releases.entities.add(new DataFile("1.txt"));

		createMocks();

		DataLoader dataLoader = new DataLoader(tasks, reader, localSettings, projects, osInterface);

		TaskException taskException = assertThrows(TaskException.class, dataLoader::load);

		assertEquals("Unexpected file 'git-data/tasks/releases/1.txt'", taskException.getMessage());
	}
}
