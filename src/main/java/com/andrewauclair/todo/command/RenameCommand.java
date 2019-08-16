// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.jline.RenameCompleter;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.*;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class RenameCommand extends Command {
	private final List<CommandOption> options = Arrays.asList(
			new CommandOption("list", 'l', Collections.singletonList("List")),
			new CommandOption("task", 't', Collections.singletonList("Task")),
			new CommandOption("name", 'n', Collections.singletonList("Name"))
	);
	private final CommandParser parser = new CommandParser(options);
	private final Tasks tasks;
	
	public RenameCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	// TODO Although it isn't really possible, we shouldn't allow list and task at the same time. it'll just run like a list rename, as it always has, but it should throw an error, this should be part of the CommandParser options
	@Override
	public void execute(PrintStream output, String command) {
		List<CommandArgument> args = parser.parse(command);
		Map<String, CommandArgument> argsMap = new HashMap<>();

		for (CommandArgument arg : args) {
			argsMap.put(arg.getName(), arg);
		}

		if (argsMap.containsKey("list")) {
			String newName = argsMap.get("name").getValue();
			String list = argsMap.get("list").getValue();
			
			if (newName.contains("/")) {
				throw new RuntimeException("Lists must be renamed with name, not paths.");
			}
			
			if (list.contains("/")) {
				throw new RuntimeException("Lists must be renamed with name, not paths.");
			}
			
			tasks.renameList(list, newName);
			
			output.println("Renamed list '" + tasks.getAbsoluteListName(list) + "' to '" + tasks.getAbsoluteListName(newName) + "'");
			output.println();
		}
		else if (argsMap.containsKey("task")) {
			if (!argsMap.containsKey("name")) {
				output.println("Missing name parameter.");
				output.println();
				return;
			}

			String newName = argsMap.get("name").getValue();
			long taskID = Long.parseLong(argsMap.get("task").getValue());

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
