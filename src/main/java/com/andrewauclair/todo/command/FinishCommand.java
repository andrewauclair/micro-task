// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Task;
import com.andrewauclair.todo.TaskDuration;
import com.andrewauclair.todo.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class FinishCommand extends Command {
	private final Tasks tasks;
	
	public FinishCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void print(PrintStream output, String command) {
		String[] s = command.split(" ");
		
		Task task;
		
		if (s.length == 2) {
			task = tasks.finishTask(Long.parseLong(s[1]));
		}
		else {
			task = tasks.finishTask();
		}
		
		output.println("Finished task " + task.description());
		output.println();
		output.print("Task finished in: ");
		output.println(new TaskDuration(task.getTimes()));
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("finish"));
	}
}
