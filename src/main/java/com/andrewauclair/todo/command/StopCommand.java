// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskDuration;
import com.andrewauclair.todo.task.TaskTimes;
import com.andrewauclair.todo.task.Tasks;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

@Command(name = "stop")
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
		System.out.println(new TaskDuration(stopTime, osInterface));
		System.out.println();
	}
}
