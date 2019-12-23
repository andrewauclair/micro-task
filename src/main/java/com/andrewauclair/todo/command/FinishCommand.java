// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskDuration;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class FinishCommand extends Command {
	private final List<CommandOption> options = Collections.singletonList(
			new CommandOption("task", CommandOption.NO_SHORTNAME, false)
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
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("finish"));
	}
}
