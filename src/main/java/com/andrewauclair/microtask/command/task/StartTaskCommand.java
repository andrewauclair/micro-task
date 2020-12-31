// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.task;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.*;
import picocli.CommandLine;

import java.util.List;
import java.util.Optional;

@CommandLine.Command(name = "task")
public class StartTaskCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Parameters(index = "0", description = "The task to start.")
	private ExistingID id;

	@CommandLine.Option(names = {"-f", "--finish-active-task"}, description = "Finish the active task.")
	private boolean finish;

	public StartTaskCommand(Tasks tasks, OSInterface osInterface) {
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

		//		// exclude add and finish when finished
//		if (state == TaskState.Finished) {
//			return startStopTimes.subList(1, startStopTimes.size() - 1);
//		}
//		// exclude add
//		return startStopTimes.subList(1, startStopTimes.size());
		List<TaskTimes> times = task.startStopTimes;
		TaskTimes startTime = times.get(times.size() - 1);

		System.out.println(startTime.description(osInterface.getZoneId()));
		System.out.println();
	}
}
