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
	private final Tasks tasks;
	
	MoveCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		String[] s = command.split(" ");

		String type = s[1];

		if (type.equals("--task")) {
			Task task = tasks.moveTask(Long.parseLong(s[2]), s[3]);

			output.println("Moved task " + task.id + " to list '" + s[3] + "'");
			output.println();
		}
		else if (type.equals("--list")) {
			String list = s[2];
			String group = s[3];

			tasks.moveList(list, group);

			output.println("Moved list " + list + " to group '" + group + "'");
			output.println();
		}
		else {
			String srcGroup = s[2];
			String destGroup = s[3];

			tasks.moveGroup(srcGroup, destGroup);

			output.println("Moved group '" + srcGroup + "' to group '" + destGroup + "'");
			output.println();
		}
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Arrays.asList(
				node("move",
						node(new LongCompleter(),
								node(new ListCompleter(tasks, false)
								)
						)
				),
				node("move",
						node(new ListCompleter(tasks, true),
								node(new GroupCompleter(tasks, false)
								)
						)
				),
				node("move",
						node(new GroupCompleter(tasks, true),
								node(new GroupCompleter(tasks, false)
								)
						)
				)
		);
	}
}
