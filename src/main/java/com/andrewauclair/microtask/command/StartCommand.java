// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskDuration;
import com.andrewauclair.microtask.task.TaskTimes;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.Optional;

@Command(name = "start")
final class StartCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Parameters(index = "0")
	private long id;

	@Option(names = {"-f", "--finish"})
	private boolean finish;

	StartCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		Optional<Task> activeTask = Optional.empty();
		if (tasks.hasActiveTask()) {
			activeTask = Optional.of(tasks.getActiveTask());
		}

		Task task = tasks.startTask(id, finish);

		if (activeTask.isPresent()) {
			if (finish) {
				System.out.println("Finished task " + activeTask.get().description());
				System.out.println();
				System.out.print("Task finished in: ");
				System.out.println(new TaskDuration(activeTask.get(), osInterface));
			}
			else {
				System.out.println("Stopped task " + activeTask.get().description());
			}

			System.out.println();
		}

		System.out.println("Started task " + task.description());
		System.out.println();

		List<TaskTimes> times = task.getStartStopTimes();
		TaskTimes startTime = times.get(times.size() - 1);

		System.out.println(startTime.description(osInterface.getZoneId()));
		System.out.println();
	}
}
