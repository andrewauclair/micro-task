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

@Command(name = "active")
final class ActiveCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	ActiveCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		System.out.println("Active group is '" + tasks.getActiveGroup().getFullPath() + "'");
		System.out.println();
		System.out.println("Active list is '" + tasks.getActiveList() + "'");
		System.out.println();

		try {
			Task task = tasks.getActiveTask();
			System.out.println("Active task is " + task.description());
			System.out.println();

			System.out.println("Active task is on the '" + tasks.getActiveTaskList() + "' list");
			System.out.println();

			List<TaskTimes> times = task.getAllTimes();
			TaskTimes activeTime = times.get(times.size() - 1);

			activeTime = new TaskTimes(activeTime.start, osInterface.currentSeconds());
			System.out.print("Current time elapsed: ");
			System.out.println(new TaskDuration(activeTime, osInterface));
			System.out.println();
		}
		catch (RuntimeException e) {
			System.out.println("No active task.");
			System.out.println();
		}
	}
}
