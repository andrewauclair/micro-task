// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Task;
import com.andrewauclair.todo.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class AddCommand extends Command {
	private final Tasks tasks;
	
	public AddCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		String taskTitle = command.substring(5, command.lastIndexOf('"'));
		
		Task task = tasks.addTask(taskTitle);
		
		output.println("Added task " + task.description());
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("add"));
	}
}
