// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskDuration;
import com.andrewauclair.todo.task.TaskTimes;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class StopCommand extends Command {
	private final Tasks tasks;
	private final OSInterface osInterface;

	public StopCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		Task task = tasks.stopTask();
		
		output.println("Stopped task " + task.description());
		output.println();
		
		List<TaskTimes> times = task.getTimes();
		TaskTimes stopTime = times.get(times.size() - 1);

		output.println(stopTime.description(osInterface.getZoneId()));
		output.println();
		output.print("Task was active for: ");
		output.println(new TaskDuration(stopTime, osInterface));
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("stop"));
	}
}
