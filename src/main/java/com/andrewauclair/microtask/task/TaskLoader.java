// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;
import com.andrewauclair.microtask.task.group.name.NewTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingTaskListName;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

@SuppressWarnings("CanBeFinal")
public class TaskLoader {
	private final Tasks tasks;
	private final TaskReader reader;
	private final LocalSettings localSettings;
	private final OSInterface osInterface;

	public TaskLoader(Tasks tasks, TaskReader reader, LocalSettings localSettings, OSInterface osInterface) {
		this.tasks = tasks;
		this.reader = reader;
		this.localSettings = localSettings;
		this.osInterface = osInterface;
	}

	public void load() throws IOException {
		loadTasks("git-data/tasks", true);

		localSettings.load(tasks);
	}

	private void loadTasks(String folder, boolean inGroup) throws IOException {
		for (OSInterface.TaskFileInfo fileInfo : osInterface.listFiles(folder)) {
			if (fileInfo.isDirectory()) {
				if (isGroupFolder(fileInfo.getPath())) {
					loadGroup(folder, fileInfo);
				}
				else {
					loadList(folder, fileInfo);
				}
			}
			else if (isTaskFile(fileInfo)) {
				if (inGroup) {
					throw new TaskException("Unexpected file '" + fileInfo.getPath() + "'");
				}

				String fileName = fileInfo.getFileName();
				long id = idFromFileName(fileInfo, fileName);

				Task task = reader.readTask(id, fileInfo.getPath());
				tasks.addTask(task);
			}
		}
	}

	private long idFromFileName(OSInterface.TaskFileInfo fileInfo, String fileName) {
		long id;

		try {
			id = Long.parseLong(fileName.substring(fileName.lastIndexOf('/') + 1, fileName.indexOf(".txt")));
		}
		catch (NumberFormatException e) {
			throw new TaskException("Unexpected file '" + fileInfo.getPath() + "'");
		}
		return id;
	}

	private boolean isTaskFile(OSInterface.TaskFileInfo fileInfo) {
		return !fileInfo.getFileName().equals("group.txt") &&
				!fileInfo.getFileName().equals("list.txt");
	}

	private void loadList(String folder, OSInterface.TaskFileInfo fileInfo) throws IOException {
		String name = fileInfo.getFileName();

		tasks.addList(new NewTaskListName(tasks, name), false);
		tasks.setActiveList(new ExistingTaskListName(tasks, name));

		try (InputStream inputStream = osInterface.createInputStream(folder + "/" + name + "/list.txt")) {
			Scanner scanner = new Scanner(inputStream);

			tasks.setProject(tasks.getActiveList(), scanner.nextLine(), false);
			tasks.setFeature(tasks.getActiveList(), scanner.nextLine(), false);
			tasks.setListState(tasks.getActiveList(), TaskContainerState.valueOf(scanner.nextLine()), false);
		}
		catch (IOException e) {
			// TODO I don't want to ignore any exceptions, especially ones from creating an input stream, test this
			e.printStackTrace(System.out);
		}

		loadTasks(fileInfo.getPath(), false);
	}

	private void loadGroup(String folder, OSInterface.TaskFileInfo fileInfo) throws IOException {
		String name = fileInfo.getFileName();

		TaskGroup group = tasks.addGroup(new NewTaskGroupName(tasks, name + "/"));
		tasks.setActiveGroup(new ExistingTaskGroupName(tasks, name + "/"));

		try (InputStream inputStream = osInterface.createInputStream(folder + "/" + name + "/group.txt")) {
			Scanner scanner = new Scanner(inputStream);

			tasks.setProject(new ExistingTaskGroupName(tasks, group.getFullPath()), scanner.nextLine(), false);
			tasks.setFeature(new ExistingTaskGroupName(tasks, group.getFullPath()), scanner.nextLine(), false);
			tasks.setGroupState(new ExistingTaskGroupName(tasks, group.getFullPath()), TaskContainerState.valueOf(scanner.nextLine()), false);
		}
		catch (IOException e) {
			// TODO I don't want to ignore any exceptions, especially ones from creating an input stream, test this
			e.printStackTrace(System.out);
		}

		loadTasks(fileInfo.getPath(), true);
		tasks.setActiveGroup(new ExistingTaskGroupName(tasks, tasks.getActiveGroup().getParent()));
	}

	private boolean isGroupFolder(String folder) {
		return osInterface.listFiles(folder).stream()
				.anyMatch(fileInfo -> fileInfo.getFileName().equals("group.txt"));
	}
}
