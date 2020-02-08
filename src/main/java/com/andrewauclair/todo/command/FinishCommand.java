// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.*;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class FinishCommand extends Command {
	private final List<CommandOption> options = Arrays.asList(
			new CommandOption("task", CommandOption.NO_SHORTNAME, false),
			new CommandOption("list", 'l', Collections.singletonList("list")),
			new CommandOption("group", 'g', Collections.singletonList("group"))
	);
	private final CommandParser parser = new CommandParser(options);
	private final Tasks tasks;
	private final OSInterface osInterface;
	
	FinishCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		CommandParser.CommandParseResult result = parser.parse(command);
		
		if (result.hasArgument("list")) {
			String list = result.getStrArgument("list");
			
			TaskList taskList = tasks.finishList(list);
			
			output.println("Finished list '" + taskList.getFullPath() + "'");
			output.println();
		}
		else if (result.hasArgument("group")) {
			String group = result.getStrArgument("group");
			
			TaskGroup taskGroup = tasks.finishGroup(group);
			
			output.println("Finished group '" + taskGroup.getFullPath() + "'");
			output.println();
		}
		else {
			Task task;
			
			if (result.hasArgument("task")) {
				task = tasks.finishTask(result.getLongArgument("task"));
			}
			else {
				task = tasks.finishTask();
			}
			
			output.println("Finished task " + task.description());
			output.println();
			output.print("Task finished in: ");
			output.println(new TaskDuration(task, osInterface));
			output.println();
		}
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("finish"));
	}
}
