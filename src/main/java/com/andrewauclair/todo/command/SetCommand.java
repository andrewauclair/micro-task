// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.LongCompleter;
import com.andrewauclair.todo.task.TaskGroup;
import com.andrewauclair.todo.task.TaskList;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.*;

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
	
	public SetCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		List<CommandArgument> args = parser.parse(command);
		Map<String, CommandArgument> argsMap = new HashMap<>();
		
		for (CommandArgument arg : args) {
			argsMap.put(arg.getName(), arg);
		}
		
		if (argsMap.containsKey("task")) {
			long taskID = Long.parseLong(argsMap.get("task").getValue());
			
			boolean recurring = Boolean.parseBoolean(argsMap.get("recurring").getValue());
			
			tasks.setRecurring(taskID, recurring);
		}
		else if (argsMap.containsKey("list")) {
			String list = argsMap.get("list").getValue();
			TaskList listByName = tasks.getListByName(list);
			
			if (argsMap.containsKey("project")) {
				String project = argsMap.get("project").getValue();
				tasks.setProject(listByName, project);
			}
			else if (argsMap.containsKey("feature")) {
				String feature = argsMap.get("feature").getValue();
				tasks.setFeature(listByName, feature);
			}
		}
		else if (argsMap.containsKey("group")) {
			String group = argsMap.get("group").getValue();
			TaskGroup groupByName = tasks.getGroup(group);
			
			if (argsMap.containsKey("project")) {
				String project = argsMap.get("project").getValue();
				tasks.setProject(groupByName, project);
			}
			else if (argsMap.containsKey("feature")) {
				String feature = argsMap.get("feature").getValue();
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
