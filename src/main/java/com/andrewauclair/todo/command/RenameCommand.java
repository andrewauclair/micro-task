// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.TaskException;
import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.jline.RenameCompleter;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class RenameCommand extends Command {
	private final List<CommandOption> options = Arrays.asList(
			new CommandOption("list", 'l', Collections.singletonList("List")),
			new CommandOption("task", 't', Collections.singletonList("Task")),
			new CommandOption("name", 'n', Collections.singletonList("Name"))
	);
	private final CommandParser parser = new CommandParser(options);
	private final Tasks tasks;
	
	RenameCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	// TODO Although it isn't really possible, we shouldn't allow list and task at the same time. it'll just run like a list rename, as it always has, but it should throw an error, this should be part of the CommandParser options
	@Override
	public void execute(PrintStream output, String command) {
		CommandParser.CommandParseResult result = parser.parse(command);
		
		if (result.hasArgument("list")) {
			String newName = result.getStrArgument("name");
			String list = result.getStrArgument("list");
			
			if (newName.contains("/")) {
				throw new TaskException("Lists must be renamed with name, not paths.");
			}
			
			if (list.contains("/")) {
				throw new TaskException("Lists must be renamed with name, not paths.");
			}
			
			tasks.renameList(list, newName);
			
			output.println("Renamed list '" + tasks.getAbsoluteListName(list) + "' to '" + tasks.getAbsoluteListName(newName) + "'");
			output.println();
		}
		else if (result.hasArgument("task")) {
			if (!result.hasArgument("name")) {
				output.println("Missing name parameter.");
				output.println();
				return;
			}
			
			String newName = result.getStrArgument("name");
			long taskID = result.getLongArgument("task");

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
