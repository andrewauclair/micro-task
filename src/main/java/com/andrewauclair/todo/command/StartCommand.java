// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Task;
import com.andrewauclair.todo.TaskTimes;
import com.andrewauclair.todo.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class StartCommand extends Command {
	private final Tasks tasks;
	
	public StartCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void print(PrintStream output, String command) {
		String[] s = command.split(" ");
		int taskID = Integer.parseInt(s[1]);
		
		if (tasks.hasActiveTask()) {
			Task activeTask = tasks.getActiveTask();
			
			output.println("Stopped task " + activeTask.description());
			output.println();
		}
		
		Task task = tasks.startTask(taskID);
		
		output.println("Started task " + task.description());
		output.println();
		
		List<TaskTimes> times = task.getTimes();
		TaskTimes startTime = times.get(times.size() - 1);
		
		output.println(startTime.description(tasks.osInterface.getZoneId()));
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("start"));
	}
}
