// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.LongCompleter;
import com.andrewauclair.todo.task.TaskGroup;
import com.andrewauclair.todo.task.TaskList;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class SetCommand extends Command {
	private final List<CommandOption> options = Arrays.asList(
			new CommandOption("task", CommandOption.NO_SHORTNAME, Collections.singletonList("ID")),
			new CommandOption("list", CommandOption.NO_SHORTNAME, Collections.singletonList("List")),
			new CommandOption("group", CommandOption.NO_SHORTNAME, Collections.singletonList("Group")),
			new CommandOption("recurring", CommandOption.NO_SHORTNAME, Collections.singletonList("Recurring")),
			new CommandOption("project", CommandOption.NO_SHORTNAME, Collections.singletonList("Project")),
			new CommandOption("feature", CommandOption.NO_SHORTNAME, Collections.singletonList("Feature"))
	);
	private final CommandParser parser = new CommandParser(options);
	
	private final Tasks tasks;
	
	SetCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		CommandParser.CommandParseResult result = parser.parse(command);
		
		if (result.hasArgument("task")) {
			long taskID = result.getLongArgument("task");
			
			boolean recurring = result.getBoolArgument("recurring");
			
			tasks.setRecurring(taskID, recurring);
		}
		else if (result.hasArgument("list")) {
			String list = result.getStrArgument("list");
			TaskList listByName = tasks.getListByName(list);
			
			if (result.hasArgument("project")) {
				String project = result.getStrArgument("project");
				tasks.setProject(listByName, project);
			}
			else if (result.hasArgument("feature")) {
				String feature = result.getStrArgument("feature");
				tasks.setFeature(listByName, feature);
			}
		}
		else if (result.hasArgument("group")) {
			String group = result.getStrArgument("group");
			TaskGroup groupByName = tasks.getGroup(group);
			
			if (result.hasArgument("project")) {
				String project = result.getStrArgument("project");
				tasks.setProject(groupByName, project);
			}
			else if (result.hasArgument("feature")) {
				String feature = result.getStrArgument("feature");
				tasks.setFeature(groupByName, feature);
			}
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
