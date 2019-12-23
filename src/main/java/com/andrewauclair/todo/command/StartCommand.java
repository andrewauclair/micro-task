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
	private final List<CommandOption> options = Arrays.asList(
			new CommandOption("task", CommandOption.NO_SHORTNAME, false),
			new CommandOption("finish", 'f', true)
	);
	private final CommandParser parser = new CommandParser(options);
	private final Tasks tasks;
	private final OSInterface osInterface;
	
	StartCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		CommandParser.CommandParseResult result = parser.parse(command);
		
		int taskID = result.getIntArgument("task");

		Optional<Task> activeTask = Optional.empty();
		if (tasks.hasActiveTask()) {
			activeTask = Optional.of(tasks.getActiveTask());
		}
		
		Task task = tasks.startTask(taskID, result.hasArgument("finish"));

		if (activeTask.isPresent()) {
			if (result.hasArgument("finish")) {
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
