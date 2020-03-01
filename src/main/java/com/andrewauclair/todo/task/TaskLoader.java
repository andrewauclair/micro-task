// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.TaskException;
import com.andrewauclair.todo.os.OSInterface;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

@SuppressWarnings("CanBeFinal")
public class TaskLoader {
	private final Tasks tasks;
	private final TaskReader reader;
	private final OSInterface osInterface;

	public TaskLoader(Tasks tasks, TaskReader reader, OSInterface osInterface) {
		this.tasks = tasks;
		this.reader = reader;
		this.osInterface = osInterface;
	}


	public void load() throws IOException {
		loadTasks("git-data/tasks", true);
	}

	private void loadTasks(String folder, boolean inGroup) throws IOException {
		for (OSInterface.TaskFileInfo fileInfo : osInterface.listFiles(folder)) {
			if (fileInfo.isDirectory()) {
				String name = fileInfo.getFileName();

				boolean isGroup = isGroupFolder(fileInfo.getPath());
				if (isGroup) {
					TaskGroup group = tasks.addGroup(name + "/");
					tasks.switchGroup(name + "/");

					try (InputStream inputStream = osInterface.createInputStream(folder + "/" + name + "/group.txt")) {
						Scanner scanner = new Scanner(inputStream);

						if (scanner.hasNextLine()) {
							tasks.setProject(tasks.getGroup(group.getFullPath()), scanner.nextLine(), false);
							tasks.setFeature(tasks.getGroup(group.getFullPath()), scanner.nextLine(), false);

							if (scanner.hasNextLine()) {
								tasks.setGroupState(tasks.getGroup(group.getFullPath()), TaskContainerState.valueOf(scanner.nextLine()), false);
							}
						}
					}
					catch (IOException ignored) {
					}

					inGroup = true;
				}
				else {
					tasks.addList(name, false);
					tasks.setActiveList(name);

					try (InputStream inputStream = osInterface.createInputStream(folder + "/" + name + "/list.txt")) {
						Scanner scanner = new Scanner(inputStream);

						tasks.setProject(tasks.getListByName(tasks.getActiveList()), scanner.nextLine(), false);
						tasks.setFeature(tasks.getListByName(tasks.getActiveList()), scanner.nextLine(), false);

						if (scanner.hasNextLine()) {
							tasks.setListState(tasks.getListByName(tasks.getActiveList()), TaskContainerState.valueOf(scanner.nextLine()), false);
						}
					}
					catch (IOException ignored) {
					}

					inGroup = false;
				}
				loadTasks(fileInfo.getPath(), inGroup);

				if (isGroup) {
					tasks.switchGroup(tasks.getActiveGroup().getParent());
				}
			}
			else if (!fileInfo.getFileName().equals("group.txt") &&
					!fileInfo.getFileName().equals("list.txt")) {
				if (inGroup) {
					throw new TaskException("Unexpected file '" + fileInfo.getPath() + "'");
				}

				String fileName = fileInfo.getFileName();
				long id;

				try {
					id = Long.parseLong(fileName.substring(fileName.lastIndexOf('/') + 1, fileName.indexOf(".txt")));
				}
				catch (NumberFormatException e) {
					throw new TaskException("Unexpected file '" + fileInfo.getPath() + "'");
				}

				Task task = reader.readTask(id, fileInfo.getPath());
				tasks.addTask(task);
			}
		}
	}

	private boolean isGroupFolder(String folder) {
		return osInterface.listFiles(folder).stream()
				.anyMatch(fileInfo -> fileInfo.getFileName().equals("group.txt"));
	}
}
