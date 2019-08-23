// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.os.LongCompleter;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class MoveCommand extends Command {
	private final Tasks tasks;
	
	MoveCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		String[] s = command.split(" ");
		
		Task task = tasks.moveTask(Long.parseLong(s[1]), s[2]);
		
		output.println("Moved task " + task.id + " to list '" + s[2] + "'");
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(
				node("move",
						node(new LongCompleter(),
								node(new ListCompleter(tasks, false)
								)
						)
				)
		);
	}
}
