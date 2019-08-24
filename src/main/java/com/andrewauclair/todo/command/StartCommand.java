// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskTimes;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class StartCommand extends Command {
	private final Tasks tasks;
	private final OSInterface osInterface;
	
	StartCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		String[] s = command.split(" ");

		List<String> parameters = Arrays.asList(s);

		int taskID = Integer.parseInt(s[1]);

		Optional<Task> activeTask = Optional.empty();
		if (tasks.hasActiveTask()) {
			activeTask = Optional.of(tasks.getActiveTask());
		}

		Task task = tasks.startTask(taskID, parameters.contains("--finish") || parameters.contains("-f"));

		if (activeTask.isPresent()) {
			if (parameters.contains("--finish") || parameters.contains("-f")) {
				output.println("Finished task " + activeTask.get().description());
			}
			else {
				output.println("Stopped task " + activeTask.get().description());
			}

			output.println();
		}

		output.println("Started task " + task.description());
		output.println();

		List<TaskTimes> times = task.getStartStopTimes();
		TaskTimes startTime = times.get(times.size() - 1);

		output.println(startTime.description(osInterface.getZoneId()));
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("start"));
	}
}
