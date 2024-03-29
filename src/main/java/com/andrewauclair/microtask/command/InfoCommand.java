// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.*;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Command(name = "info", description = "Display info for a task.")
final class InfoCommand implements Runnable {
	private final Tasks tasks;
	private final Projects projects;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Parameters(index = "0", description = "The task to display information for.")
	private ExistingID id;

	@Option(names = {"--copy-name"}, description = "Copy the name of the task to the clipboard.")
	private boolean copy_name;

	InfoCommand(Tasks tasks, Projects projects, OSInterface osInterface) {
		this.tasks = tasks;
		this.projects = projects;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		Task task = tasks.getTask(id);

		if (copy_name) {
			osInterface.copyToClipboard(task.task);

			System.out.println("Copied name of task " + task.ID() + " to the clipboard.");
			System.out.println();

			return;
		}

		System.out.println("Info for " + task.description());
		System.out.println();

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");

		System.out.print(Instant.ofEpochSecond(task.addTime).atZone(osInterface.getZoneId()).format(dateTimeFormatter));
		System.out.println(" -- added");
		System.out.println();

		int count = 1;

		for (TaskTimes startStopTime : task.startStopTimes) {
			System.out.print(Instant.ofEpochSecond(startStopTime.start).atZone(osInterface.getZoneId()).format(dateTimeFormatter));
			System.out.print(" - ");

			if (startStopTime.stop != TaskTimes.TIME_NOT_SET) {
				System.out.print(Instant.ofEpochSecond(startStopTime.stop).atZone(osInterface.getZoneId()).format(dateTimeFormatter));
				System.out.println(" -- " + count++);
			}
			else {
				System.out.println();
			}
		}

		System.out.println();

		if (task.finishTime != TaskTimes.TIME_NOT_SET) {
			System.out.print(Instant.ofEpochSecond(task.finishTime).atZone(osInterface.getZoneId()).format(dateTimeFormatter));
			System.out.println(" -- finished");
			System.out.println();
		}

		System.out.println("on list '" + tasks.findListForTask(task.ID()).getFullPath() + "'");
		System.out.println();


		System.out.println("Project '" + projects.getProjectForList(tasks.getListForTask(task.ID())) + "'");
		System.out.println("Feature '" + projects.getFeatureForList(tasks.getListForTask(task.ID())) + "'");
		System.out.println();

		if (task.state != TaskState.Finished) {
			DateTimeFormatter dueDateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
			System.out.println("Due: " + Instant.ofEpochSecond(task.dueTime).atZone(osInterface.getZoneId()).format(dueDateTimeFormatter));
			System.out.println();
		}
	}
}
