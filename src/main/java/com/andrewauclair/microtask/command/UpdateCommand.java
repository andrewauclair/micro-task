// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.project.Project;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.group.TaskGroupFileWriter;
import com.andrewauclair.microtask.task.list.TaskListFileWriter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "update", description = "Update the application or remote repo.")
public final class UpdateCommand implements Runnable {
	private final Tasks tasksData;
	private final Commands commands;
	private final LocalSettings localSettings;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Spec
	private CommandLine.Model.CommandSpec spec;

	UpdateCommand(Tasks tasks, Commands commands, LocalSettings localSettings, OSInterface osInterface) {
		this.tasksData = tasks;
		this.commands = commands;
		this.localSettings = localSettings;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		spec.commandLine().usage(System.out);
	}

	public static void updateFiles(Tasks tasks, OSInterface osInterface, LocalSettings localSettings, Projects projects, Commands commands) {
		updateGroupFiles(tasks, tasks.getRootGroup(), osInterface);

		String currentVersion = Utils.writeCurrentVersion(osInterface);

		osInterface.gitCommit("Updating files to version '" + currentVersion + "'");

		tasks.load(new DataLoader(tasks, new TaskReader(osInterface), localSettings, projects, osInterface), commands);

		System.out.println("Updated all files.");
		System.out.println();
	}

	private static void updateGroupFiles(Tasks tasks, TaskGroup group, OSInterface osInterface) {
		for (TaskContainer child : group.getChildren()) {
			if (child instanceof TaskGroup childGroup) {
				TaskGroupFileWriter writer = new TaskGroupFileWriter(childGroup, osInterface);
				writer.write();

				updateGroupFiles(tasks, childGroup, osInterface);
			}
			else {
				TaskList list = (TaskList) child;

				TaskListFileWriter writer = new TaskListFileWriter(list, osInterface);
				writer.write();

				for (final Task task : list.getTasks()) {
					if (task.state == TaskState.Finished) {
						osInterface.removeFile("git-data/tasks" + list.getFullPath() + "/" + task.id + ".txt");
					}
					else {
						tasks.getWriter().writeTask(task, "git-data/tasks" + list.getFullPath() + "/" + task.id + ".txt");
					}
				}

				list.writeArchive();
			}
		}
	}
}
