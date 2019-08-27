// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
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

public class ActiveCommand extends Command {
	private final Tasks tasks;
	private final OSInterface osInterface;

	public ActiveCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		output.println("Active group is '" + tasks.getActiveGroup().getFullPath() + "'");
		output.println();
		output.println("Active list is '" + tasks.getActiveList() + "'");
		output.println();
		
		Task task = tasks.getActiveTask();
		output.println("Active task is " + task.description());
		output.println();
		
		output.println("Active task is on the '" + tasks.getActiveTaskList() + "' list");
		output.println();
		
		List<TaskTimes> times = task.getTimes();
		TaskTimes activeTime = times.get(times.size() - 1);

		activeTime = new TaskTimes(activeTime.start, osInterface.currentSeconds());
		output.print("Current time elapsed: ");
		output.println(new TaskDuration(activeTime, osInterface));
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("active"));
	}
}
