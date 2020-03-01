// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskTimes;
import com.andrewauclair.todo.task.Tasks;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Command(name = "info")
final class InfoCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Parameters(index = "0")
	private long id;

	InfoCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		Task task = tasks.getTask(id);

		System.out.println("Info for " + task.description());
		System.out.println();

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");

		System.out.print(Instant.ofEpochSecond(task.getAddTime().start).atZone(osInterface.getZoneId()).format(dateTimeFormatter));
		System.out.println(" -- added");
		System.out.println();

		int count = 1;

		for (TaskTimes startStopTime : task.getStartStopTimes()) {
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

		if (task.getFinishTime().isPresent()) {
			System.out.print(Instant.ofEpochSecond(task.getFinishTime().get().start).atZone(osInterface.getZoneId()).format(dateTimeFormatter));
			System.out.println(" -- finished");
			System.out.println();
		}

		System.out.println("on list '" + tasks.findListForTask(task.id).getFullPath() + "'");
		System.out.println();

		System.out.println("Project '" + tasks.getProjectForTask(task.id) + "'");
		System.out.println("Feature '" + tasks.getFeatureForTask(task.id) + "'");
		System.out.println();
	}
}
