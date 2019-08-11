// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

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
		
		if (setType.equals("--issue")) {
			tasks.setIssue(taskID, Long.parseLong(setArg));
		}
		else {
			tasks.setCharge(taskID, command.substring(command.indexOf('"') + 1, command.lastIndexOf('"')));
		}
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("set"));
	}
}
