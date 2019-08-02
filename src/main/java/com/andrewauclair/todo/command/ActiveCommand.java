// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Task;
import com.andrewauclair.todo.TaskDuration;
import com.andrewauclair.todo.TaskTimes;
import com.andrewauclair.todo.Tasks;
import com.andrewauclair.todo.os.OSInterface;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class ActiveCommand extends Command {
	private final Tasks tasks;
	private final OSInterface osInterface;

	public ActiveCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		Task task = tasks.getActiveTask();
		output.println("Active task is " + task.description());
		output.println();
		
		if (task.getIssue() != -1) {
			output.println("Issue: " + task.getIssue());
			output.println();
		}
		
		output.println("Active task is on the '" + tasks.getActiveTaskList() + "' list");
		output.println();
		
		List<TaskTimes> times = task.getTimes();
		TaskTimes activeTime = times.get(times.size() - 1);

		activeTime = new TaskTimes(activeTime.start, osInterface.currentSeconds());
		output.print("Current time elapsed: ");
		output.println(new TaskDuration(activeTime));
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("active"));
	}
}
