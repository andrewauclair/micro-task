// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.task.*;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.andrewauclair.todo.os.ConsoleColors.ANSI_BOLD;
import static com.andrewauclair.todo.os.ConsoleColors.ANSI_RESET;
import static org.jline.builtins.Completers.TreeCompleter.node;

public class ListCommand extends Command {
	private static final int MAX_DISPLAYED_TASKS = 20;

	private final Tasks tasks;

	ListCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void execute(PrintStream output, String command) {
		String[] s = command.split(" ");

		List<String> parameters = Arrays.asList(s);

		boolean all = parameters.contains("--all");
		boolean showTasks = parameters.contains("--tasks");
		boolean showLists = parameters.contains("--lists");
		boolean useGroup = parameters.contains("--group");

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

			final int limit = all ? Integer.MAX_VALUE : MAX_DISPLAYED_TASKS;

			int totalTasks = 0;

			if (useGroup) {
				for (TaskContainer child : tasks.getActiveGroup().getChildren()) {
					if (child instanceof TaskList) {
						TaskList listChild = (TaskList) child;

						List<Task> tasksList = listChild.getTasks().stream()
								.filter(task -> task.state != TaskState.Finished)
								.collect(Collectors.toList());

						totalTasks += tasksList.size();

						if (tasksList.size() > 0) {
							output.println(ANSI_BOLD + listChild.getFullPath() + ANSI_RESET);
							printTasks(output, tasksList, Integer.MAX_VALUE);
							output.println();
						}
					}
				}
			}
			else {
				List<Task> tasksList = tasks.getTasksForList(list).stream()
						.filter(task -> task.state != TaskState.Finished)
						.collect(Collectors.toList());

				totalTasks += tasksList.size();

				printTasks(output, tasksList, limit);
			}

			if (totalTasks > limit) {
				output.println("(" + (totalTasks - MAX_DISPLAYED_TASKS) + " more tasks.)");
			}
			else if (totalTasks == 0) {
				output.println("No tasks.");
			}

			if (totalTasks > 0) {
				output.println();
				output.print(ANSI_BOLD);
				output.print("Total Tasks: " + totalTasks);
				output.print(ANSI_RESET);
				output.println();
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

	private void printTasks(PrintStream output, List<Task> tasksList, int limit) {
		Optional<Task> max = tasksList.stream()
				.limit(limit)
				.max(Comparator.comparingInt(o -> String.valueOf(o.id).length()));

		tasksList.stream()
				.limit(limit)
				.sorted(Comparator.comparingLong(o -> o.id))
				.forEach(str -> printTask(output, str, String.valueOf(max.get().id).length()));
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

	private void printTask(PrintStream output, Task task, int maxLength) {
		String printID = String.join("", Collections.nCopies(maxLength - String.valueOf(task.id).length(), " "));
		
		if (task.id == tasks.getActiveTaskID()) {
			output.print("* ");
			output.print(printID);
			ConsoleColors.println(output, ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN, task.description());
		}
		else if (task.isRecurring()) {
			output.print("R ");
			output.print(printID);
			output.println(task.description());
		}
		else {
			output.print("  ");
			output.print(printID);
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
								node("--all"),
								node("--group",
										node("--all")
								)
						)
				),
				node("list",
						node("--lists")
				)
		);
	}
}
