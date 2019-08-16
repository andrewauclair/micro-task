// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.task.*;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class ListCommand extends Command {
	private static final int MAX_DISPLAYED_TASKS = 20;
	
	private final Tasks tasks;
	
	public ListCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		String[] s = command.split(" ");
		
		List<String> parameters = Arrays.asList(s);
		
		boolean all = parameters.contains("--all");
		boolean showTasks = parameters.contains("--tasks");
		boolean showLists = parameters.contains("--lists");

		String list = tasks.getActiveList();
		
		if (parameters.contains("--list")) {
			list = parameters.get(parameters.indexOf("--list") + 1);
		}

		if (!list.startsWith("/")) {
			list = "/" + list;
		}
		
		if (showLists) {
			tasks.getListNames().stream()
					.sorted()
					.forEach(str -> printList(output, str));
			output.println();
		}
		else if (showTasks) {
			if (!list.equals(tasks.getActiveList())) {
				output.println("Tasks on list '" + list + "'");
				output.println();
			}
			
			List<Task> tasksList = tasks.getTasksForList(list).stream()
					.filter(task -> task.state != TaskState.Finished)
					.collect(Collectors.toList());
			
			final int limit = all ? Integer.MAX_VALUE : MAX_DISPLAYED_TASKS;
			tasksList.stream()
					.limit(limit)
					.forEach(str -> printTask(output, str));
			
			if (tasksList.size() > limit) {
				output.println("(" + (tasksList.size() - MAX_DISPLAYED_TASKS) + " more tasks.)");
			}
			else if (tasksList.size() == 0) {
				output.println("No tasks.");
			}
			output.println();
		}
		else {
			TaskGroup activeGroup = tasks.getActiveGroup();

			List<TaskContainer> children = activeGroup.getChildren().stream()
					.sorted(Comparator.comparing(TaskContainer::getName))
					.collect(Collectors.toList());

			output.println("Current group is '" + activeGroup.getFullPath() + "'");
			output.println();

			for (TaskContainer child : children) {
				if (child instanceof TaskList) {
					printListRelative(output, (TaskList) child);
				}
				else {
					output.print("  ");
					output.println(child.getName() + "/");
				}
			}
			output.println();
		}
	}
	
	private void printList(PrintStream output, String list) {
		if (list.equals(tasks.getActiveList())) {
			output.print("* ");
			ConsoleColors.println(output, ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN, list);
		}
		else {
			output.print("  ");
			output.println(list);
		}
	}

	private void printListRelative(PrintStream output, TaskList list) {
		if (list.getFullPath().equals(tasks.getActiveList())) {
			output.print("* ");
			ConsoleColors.println(output, ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN, list.getName());
		}
		else {
			output.print("  ");
			output.println(list.getName());
		}
	}

	private void printTask(PrintStream output, Task task) {
		if (task.id == tasks.getActiveTaskID()) {
			output.print("* ");
			ConsoleColors.println(output, ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN, task.description());
		}
		else {
			output.print("  ");
			output.println(task.description());
		}
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Arrays.asList(
				node("list",
						node("--tasks",
								node("--list",
										node(new ListCompleter(tasks, true), node("--all"))
								),
								node("--all")
						)
				),
				node("list",
						node("--lists")
				)
		);
	}
}
