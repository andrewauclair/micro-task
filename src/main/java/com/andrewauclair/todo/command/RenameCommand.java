// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Task;
import com.andrewauclair.todo.Tasks;
import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.jline.RenameCompleter;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class RenameCommand extends Command {
	private final Tasks tasks;
	
	public RenameCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void print(PrintStream output, String command) {
		String[] s = command.split(" ");
		
		List<String> parameters = Arrays.asList(s);
		
		if (parameters.contains("--list")) {
			String newName = command.substring(command.indexOf("\"") + 1, command.lastIndexOf("\""));
			tasks.renameList(s[2], newName);
			
			output.println("Renamed list '" + s[2] + "' to '" + newName + "'");
			output.println();
		}
		else if (parameters.contains("--task")) {
			String newName = command.substring(command.indexOf("\"") + 1, command.lastIndexOf("\""));
			long taskID = Long.parseLong(s[2]);
			
			Task task = tasks.renameTask(taskID, newName);
			
			output.println("Renamed task " + task.description());
			output.println();
		}
		else {
			output.println("Invalid command.");
			output.println();
		}
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(
				node("rename",
						node("--task",
								node(new RenameCompleter(tasks))
						),
						node("--list",
								node(new ListCompleter(tasks, true)
								)
						)
				)
		);
	}
}
