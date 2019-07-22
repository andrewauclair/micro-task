// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Task;
import com.andrewauclair.todo.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Arrays;
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
		String[] s = command.split(" ");
		
		List<String> parameters = Arrays.asList(s);
		
		int titleEnd = command.lastIndexOf('"');
		String taskTitle = command.substring(command.substring(0, titleEnd - 1).lastIndexOf('"') + 1, titleEnd);
		
		Task task = tasks.addTask(taskTitle);
		
		if (parameters.contains("--issue")) {
			task = tasks.setIssue(task.id, Long.parseLong(parameters.get(parameters.indexOf("--issue") + 1)));
		}
		
		if (parameters.contains("--charge")) {
			int chargeStart = command.indexOf('"');
			String charge = command.substring(chargeStart + 1);
			charge = charge.substring(0, charge.indexOf('"'));
			
			task = tasks.setCharge(task.id, charge);
		}
		
		output.println("Added task " + task.description());
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("add"));
	}
}
