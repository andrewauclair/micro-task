// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.LongCompleter;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class SetCommand extends Command {
	private final Tasks tasks;
	
	public SetCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		String[] s = command.split(" ");
		
		long taskID = Long.parseLong(s[1]);
		String setType = s[2];
		String setArg = s[3];

		switch (setType) {
			case "--recurring":
				tasks.setRecurring(taskID, Boolean.parseBoolean(setArg));
				break;
			case "--project":
				tasks.setProject(taskID, command.substring(command.indexOf('"') + 1, command.lastIndexOf('"')));
				break;
			case "--feature":
				tasks.setFeature(taskID, command.substring(command.indexOf('"') + 1, command.lastIndexOf('"')));
				break;
		}
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(
				node("set",
						node("--task",
								node(new LongCompleter(),
										node("--recurring"),
										node("--project"),
										node("--feature")
								)
						)
				)
		);
	}
}
