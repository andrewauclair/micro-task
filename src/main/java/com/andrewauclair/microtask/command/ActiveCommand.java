// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.*;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

@Command(name = "active", description = "Display information about the active task, list and group.")
final class ActiveCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"--context"}, description = "Display the active context.")
	private boolean context;

	ActiveCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		if (context) {
			ActiveContext activeContext = tasks.getActiveContext();

			System.out.println("Active Context");
			System.out.println();

			if (activeContext.getActiveTaskID() == ActiveContext.NO_ACTIVE_TASK) {
				System.out.println("task:      none");
			}
			else {
				System.out.println("task:      " + activeContext.getActiveTaskID());
			}

			if (activeContext.getActiveList().isPresent()) {
				System.out.println("list:      " + activeContext.getActiveList().get().absoluteName());
			}
			else {
				System.out.println("list:      none");
			}

			if (activeContext.getActiveGroup().isPresent()) {
				System.out.println("group:     " + activeContext.getActiveGroup().get().absoluteName());
			}
			else {
				System.out.println("group:     none");
			}

			if (activeContext.getActiveProject().isPresent()) {
				System.out.println("project:   " + activeContext.getActiveProject().get().getName());
			}
			else {
				System.out.println("project:   none");
			}

			if (activeContext.getActiveFeature().isPresent()) {
				System.out.println("feature:   " + activeContext.getActiveFeature().get().getName());
			}
			else {
				System.out.println("feature:   none");
			}

			if (activeContext.getActiveMilestone().isPresent()) {
				System.out.println("milestone: " + activeContext.getActiveMilestone().get().getName());
			}
			else {
				System.out.println("milestone: none");
			}

			if (activeContext.getActiveTags().isEmpty()) {
				System.out.println("tags:      none");
			}
			else {
				System.out.println("tags:      " + String.join(", ", activeContext.getActiveTags()));
			}
			System.out.println();
			return;
		}
		System.out.println("Current group is '" + tasks.getCurrentGroup().getFullPath() + "'");
		System.out.println();
		System.out.println("Current list is '" + tasks.getCurrentList() + "'");
		System.out.println();

		try {
			Task task = tasks.getActiveTask();
			System.out.println("Active task is " + task.description());
			System.out.println();

			System.out.println("Active task is on the '" + tasks.getActiveTaskList() + "' list");
			System.out.println();

			List<TaskTimes> times = task.startStopTimes;
			TaskTimes activeTime = times.get(times.size() - 1);

			activeTime = new TaskTimes(activeTime.start, osInterface.currentSeconds());
			System.out.print("Current time elapsed: ");
			System.out.println(Utils.formatTime(activeTime.getDuration(osInterface), Utils.HighestTime.None));
			System.out.println();
		}
		catch (RuntimeException e) {
			System.out.println("No active task.");
			System.out.println();
		}
	}
}
