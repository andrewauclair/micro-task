// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Task;
import com.andrewauclair.todo.TaskDuration;
import com.andrewauclair.todo.TaskTimes;
import com.andrewauclair.todo.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class StopCommand extends Command {
	private final Tasks tasks;
	
	public StopCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		Task task = tasks.stopTask();
		
		output.println("Stopped task " + task.description());
		output.println();
		
		List<TaskTimes> times = task.getTimes();
		TaskTimes stopTime = times.get(times.size() - 1);
		
		output.println(stopTime.description(tasks.osInterface.getZoneId()));
		output.println();
		output.print("Task was active for: ");
		output.println(new TaskDuration(stopTime));
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("stop"));
	}
}
