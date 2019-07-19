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

public class ActiveCommand extends Command {
	private final Tasks tasks;
	
	public ActiveCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void print(PrintStream output, String command) {
		Task task = tasks.getActiveTask();
		
		output.println("Active task is " + task.description());
		output.println();
		output.println("Active task is on the '" + tasks.getActiveTaskList() + "' list");
		output.println();
		
		List<TaskTimes> times = task.getTimes();
		TaskTimes activeTime = times.get(times.size() - 1);
		
		activeTime = new TaskTimes(activeTime.start, tasks.osInterface.currentSeconds());
		output.print("Current time elapsed: ");
		output.println(new TaskDuration(activeTime));
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("active"));
	}
}
