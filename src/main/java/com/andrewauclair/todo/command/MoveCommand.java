// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.GroupCompleter;
import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.os.LongCompleter;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class MoveCommand extends Command {
	private final List<CommandOption> options = Arrays.asList(
			new CommandOption("task", 't', Arrays.asList("Task ID", "List")),
			new CommandOption("list", 'l', Arrays.asList("List", "Group")),
			new CommandOption("group", 'g', Arrays.asList("Source Group", "Destination Group"))
	);
	private final CommandParser parser = new CommandParser(options);
	private final Tasks tasks;

	MoveCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void execute(PrintStream output, String command) {
		CommandParser.CommandParseResult result = parser.parse(command);
		
		if (result.hasArgument("task")) {
			String list = result.getStrArgument("task", 1);
			Task task = tasks.moveTask(result.getLongArgument("task", 0), list);
			
			output.println("Moved task " + task.id + " to list '" + list + "'");
			output.println();
		}
		else if (result.hasArgument("list")) {
			String list = result.getStrArgument("list", 0);
			String group = result.getStrArgument("list", 1);

			tasks.moveList(list, group);

			output.println("Moved list " + list + " to group '" + group + "'");
			output.println();
		}
		else if (result.hasArgument("group")) {
			String srcGroup = result.getStrArgument("group", 0);
			String destGroup = result.getStrArgument("group", 1);

			tasks.moveGroup(srcGroup, destGroup);

			output.println("Moved group '" + srcGroup + "' to group '" + destGroup + "'");
			output.println();
		}
	}

	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Arrays.asList(
				node("move",
						node("--task",
								node(new LongCompleter(),
										node(new ListCompleter(tasks, false)
										)
								)
						)
				),
				node("move",
						node("--list",
								node(new ListCompleter(tasks, true),
										node(new GroupCompleter(tasks, false)
										)
								)
						)
				),
				node("move",
						node("--group",
								node(new GroupCompleter(tasks, true),
										node(new GroupCompleter(tasks, false)
										)
								)
						)
				)
		);
	}
}
