// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskDuration;
import com.andrewauclair.todo.task.TaskTimes;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

@CommandLine.Command(name = "stop")
public class StopCommand extends Command {
	private final Tasks tasks;
	private final OSInterface osInterface;

	public StopCommand(Tasks tasks, OSInterface osInterface) {
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
