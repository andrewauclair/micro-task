// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskTimes;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

@Command(name = "stop", description = "Stop the active task.")
final class StopCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	StopCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		Task task = tasks.stopTask();

		System.out.println("Stopped task " + task.description());
		System.out.println();

		List<TaskTimes> times = task.getAllTimes();
		TaskTimes stopTime = times.get(times.size() - 1);

		System.out.println(stopTime.description(osInterface.getZoneId()));
		System.out.println();
		System.out.print("Task was active for: ");
		System.out.println(Utils.formatTime(stopTime.getDuration(osInterface), Utils.HighestTime.None));
		System.out.println();
	}
}
