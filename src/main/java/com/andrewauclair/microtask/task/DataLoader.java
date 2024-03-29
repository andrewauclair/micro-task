// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.project.*;
import com.andrewauclair.microtask.schedule.Schedule;
import com.andrewauclair.microtask.task.build.TaskBuilder;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.group.name.NewTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

public class DataLoader {
	private final Tasks tasks;
	private final TaskReader reader;
	private final LocalSettings localSettings;
	private final Projects projects;
	private final Schedule schedule;
	private final OSInterface osInterface;

	public DataLoader(Tasks tasks, TaskReader reader, LocalSettings localSettings, Projects projects, Schedule schedule, OSInterface osInterface) {
		this.tasks = tasks;
		this.reader = reader;
		this.localSettings = localSettings;
		this.projects = projects;
		this.schedule = schedule;
		this.osInterface = osInterface;
	}

	public void load() throws IOException {
		loadTasks("git-data/tasks", true);

		// if the default list is missing, add it and the /projects/ group
		if (!new TaskListFinder(tasks).hasList(new TaskListName(tasks, "/default") {
		})) {
			tasks.addList(new NewTaskListName(tasks, "/default"), osInterface.canCreateFiles());
			tasks.addGroup(new NewTaskGroupName(tasks, "/projects/"));
			tasks.getActiveContext().setCurrentList(new ExistingListName(tasks, "/default"));
		}

		localSettings.load(tasks);
		schedule.loadSchedule(projects);
//		projects.load();
	}

	private void loadTasks(String folder, boolean inGroup) throws IOException {
		TaskList list = inGroup ? null : tasks.getList(tasks.getActiveContext().getCurrentList());

		for (OSInterface.TaskFileInfo fileInfo : osInterface.listFiles(folder)) {
			if (fileInfo.isDirectory()) {
				if (isGroupFolder(fileInfo.getPath())) {
					loadGroup(folder, fileInfo);
				}
				else {
					loadList(folder, fileInfo);
				}
			}
			else if (isArchiveFile(fileInfo)) {
				if (inGroup) {
					// currently we do not support archiving entire lists
					throw new TaskException("Unexpected file '" + fileInfo.getPath() + "'");
				}

				try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(osInterface.createInputStream(fileInfo.getPath())))) {
					String line = bufferedReader.readLine();

					do {
						int lastSlash = line.lastIndexOf('/');
						int lastDot = line.indexOf(".txt");

						long id = Long.parseLong(line.substring(lastSlash + 1, lastDot));

						// probably our bug where we keep writing the finished tasks that are in the archive, delete the file
						if (tasks.hasTaskWithID(id) && tasks.getTask(new ExistingID(tasks.idValidator(), id)).state == TaskState.Finished) {
//							osInterface.removeFile(fileInfo.getPath());
							osInterface.removeFile("git-data/tasks" + list.getFullPath() + "/" + id + ".txt");
						}
						else {
							tasks.idValidator().addExistingID(id);

							Task task = reader.readTask(new ExistingID(tasks.idValidator(), id), bufferedReader);
							tasks.addTask(new TaskBuilder(task), list, false);
						}
						line = bufferedReader.readLine();
					} while (line != null);
				}
			}
			else if (isTaskFile(fileInfo)) {
				if (inGroup) {
					throw new TaskException("Unexpected file '" + fileInfo.getPath() + "'");
				}

				String fileName = fileInfo.getFileName();
				long id = idFromFileName(fileInfo, fileName);

				tasks.idValidator().addExistingID(id);

				Task task = reader.readTask(new ExistingID(tasks.idValidator(), id), fileInfo.getPath());
				tasks.addTask(new TaskBuilder(task), list, false);
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
				!fileInfo.getFileName().equals("list.txt") &&
				!fileInfo.getFileName().equals("project.txt") &&
				!fileInfo.getFileName().equals("feature.txt") &&
				!fileInfo.getFileName().startsWith("milestone");
	}

	private boolean isArchiveFile(OSInterface.TaskFileInfo fileInfo) {
		return fileInfo.getFileName().equals("archive.txt");
	}

	private void loadList(String folder, OSInterface.TaskFileInfo fileInfo) throws IOException {
		String name = fileInfo.getFileName();

		tasks.addList(new NewTaskListName(tasks, name), false);
		tasks.setCurrentList(new ExistingListName(tasks, name));

		try (InputStream inputStream = osInterface.createInputStream(folder + "/" + name + "/list.txt")) {
			Scanner scanner = new Scanner(inputStream);

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();

				if (line.startsWith("state")) {
					TaskContainerState state = TaskContainerState.valueOf(line.substring(6));
					tasks.setListState(tasks.getCurrentList(), state, false);
				}
				else if (line.startsWith("time")) {
					String timeCategory = line.substring(5);
					tasks.setListTimeCategory(tasks.getCurrentList(), timeCategory, false);
				}
				else {
					// legacy v1
					TaskContainerState state;

					try {
						// legacy, v2
						state = TaskContainerState.valueOf(line);
					}
					catch (IllegalArgumentException e) {
						// legacy, v1
						scanner.nextLine();
						state = TaskContainerState.valueOf(scanner.nextLine());
					}
					tasks.setListState(tasks.getCurrentList(), state, false);
				}
			}
		}
		catch (NullPointerException e) {
			System.err.println(folder + "/" + name + "/list.txt");
			throw e;
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}

		if (osInterface.fileExists(folder + "/" + name + "/feature.txt")) {
			String projectForList = projects.getProjectForList(tasks.getList(tasks.getCurrentList()));

			Project project = projects.getProject(new ExistingProject(projects, projectForList));

			if (!project.hasFeature(name)) {
				project.addFeature(new NewFeature(project, name), false);
			}
		}

		loadTasks(fileInfo.getPath(), false);
	}

	private void loadGroup(String folder, OSInterface.TaskFileInfo fileInfo) throws IOException {
		String name = fileInfo.getFileName();

		TaskGroup group = tasks.addGroup(new NewTaskGroupName(tasks, name + "/"));
		tasks.setCurrentGroup(new ExistingGroupName(tasks, name + "/"));

		try (InputStream inputStream = osInterface.createInputStream(folder + "/" + name + "/group.txt")) {
			Scanner scanner = new Scanner(inputStream);

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();

				if (line.startsWith("state")) {
					TaskContainerState state = TaskContainerState.valueOf(line.substring(6));

					tasks.setGroupState(new ExistingGroupName(tasks, group.getFullPath()), state, false);
				}
			}
		}
		catch (IOException e) {
			// TODO I don't want to ignore any exceptions, especially ones from creating an input stream, test this
			e.printStackTrace(System.out);
		}

		if (osInterface.fileExists(folder + "/" + name + "/project.txt")) {
			if (!projects.hasProject(name)) {
				projects.createProject(new NewProject(projects, name), false);
			}
		}

		loadTasks(folder + "/" + name, true);
		tasks.setCurrentGroup(new ExistingGroupName(tasks, tasks.getCurrentGroup().getParent()));

		// load milestones after we load everything in the project
		if (osInterface.fileExists(folder + "/" + name + "/project.txt")) {
			// load milestones
			for (final OSInterface.TaskFileInfo file : osInterface.listFiles(folder + "/" + name)) {
				if (file.getFileName().startsWith("milestone")) {
					try (InputStream inputStream = osInterface.createInputStream(file.getPath())) {
						Scanner scanner = new Scanner(inputStream);

						String milestoneName = scanner.nextLine();

						Project project = projects.getProject(new ExistingProject(projects, name));
						project.addMilestone(new NewMilestone(project, milestoneName), false);

						Milestone ms = project.getMilestone(new ExistingMilestone(project, milestoneName));

						while (scanner.hasNextLine()) {
							String line = scanner.nextLine();

							if (line.startsWith("feature")) {
								String feature = line.split(" ")[1];

								ms.addFeature(ExistingFeature.tryCreate(project, feature));
							}
							else {
								long taskID = Long.parseLong(line.split(" ")[1]);

								ms.addTask(new ExistingID(tasks.idValidator(), taskID));
							}
						}
					}
					catch (IOException e) {
						e.printStackTrace(System.out);
					}
				}
			}
		}
	}

	private boolean isGroupFolder(String folder) {
		return osInterface.listFiles(folder).stream()
				.anyMatch(fileInfo -> fileInfo.getFileName().equals("group.txt"));
	}
}
