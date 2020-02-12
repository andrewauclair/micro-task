// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskDuration;
import com.andrewauclair.todo.task.TaskTimes;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.jline.builtins.Completers.TreeCompleter.node;

@CommandLine.Command(name = "start")
public class StartCommand extends Command {
	@Parameters(index = "0")
	private long id;

	@Option(names = {"-f", "--finish"})
	private boolean finish;

	private final Tasks tasks;
	private final OSInterface osInterface;
	
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
