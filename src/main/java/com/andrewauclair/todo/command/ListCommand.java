// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.GroupCompleter;
import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.*;
import picocli.CommandLine;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.andrewauclair.todo.os.ConsoleColors.ANSI_BOLD;
import static com.andrewauclair.todo.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.todo.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN;

@CommandLine.Command(name = "list")
public class ListCommand extends Command {
	private static final int MAX_DISPLAYED_TASKS = 20;

	@CommandLine.Option(names = {"--tasks"})
	private boolean tasks;

	@CommandLine.Option(names = {"--list"}, completionCandidates = ListCompleter.class)
	private String list;

	@CommandLine.Option(names = {"--group"}, completionCandidates = GroupCompleter.class)
	private boolean group;

	@CommandLine.Option(names = {"--recursive"})
	private boolean recursive;

	@CommandLine.Option(names = {"--finished"})
	private boolean finished;

	@CommandLine.Option(names = {"--all"})
	private boolean all;

	private final Tasks tasksData;
	private final OSInterface osInterface;

	ListCommand(Tasks tasks, OSInterface osInterface) {
		this.tasksData = tasks;
		this.osInterface = osInterface;
	}

	private void printTasks(List<Task> tasksList, int limit) {
		Optional<Task> max = tasksList.stream()
				.limit(limit)
				.max(Comparator.comparingInt(o -> String.valueOf(o.id).length()));

		tasksList.stream()
				.limit(limit)
				.sorted(Comparator.comparingLong(o -> o.id))
				.forEach(str -> printTask(str, String.valueOf(max.get().id).length()));
	}

	private void printListRelative(TaskList list, boolean finished) {
		if (list.getFullPath().equals(tasksData.getActiveList())) {
			System.out.print("* ");
			ConsoleColors.println(System.out, ANSI_FG_GREEN, list.getName());
		}
		else if (finished == (list.getState() == TaskContainerState.Finished)) {
			System.out.print("  ");
			System.out.println(list.getName());
		}
	}

	private void printTask(Task task, int maxLength) {
		String line;

		boolean active = task.id == tasksData.getActiveTaskID();

		if (active) {
			line = "* ";
		}
		else if (task.isRecurring()) {
			line = "R ";
		}
		else {
			line = "  ";
		}

		line += String.join("", Collections.nCopies(maxLength - String.valueOf(task.id).length(), " "));

		if (active) {
			line += ANSI_FG_GREEN;
		}

		line += task.description();

		int length = line.length();

		if (active) {
			length -= ANSI_FG_GREEN.toString().length();
		}

		if (length > osInterface.getTerminalWidth()) {
			line = line.substring(0, osInterface.getTerminalWidth() - 4 + (line.length() - length));
			line += "...'";
		}

		if (active) {
			line += ANSI_RESET;
		}

		System.out.println(line);
	}

	private int printTasks(TaskGroup group, int totalTasks, boolean finished, boolean recursive) {
		for (TaskContainer child : group.getChildren()) {
			if (child instanceof TaskList) {
				TaskList listChild = (TaskList) child;

				List<Task> tasksList = listChild.getTasks().stream()
						.filter(task -> finished == (task.state == TaskState.Finished))
						.collect(Collectors.toList());

				totalTasks += tasksList.size();

				if (tasksList.size() > 0) {
					System.out.println(ANSI_BOLD + listChild.getFullPath() + ANSI_RESET);
					printTasks(tasksList, Integer.MAX_VALUE);
					System.out.println();
				}
			}
			else if (recursive) {
				totalTasks = printTasks((TaskGroup) child, totalTasks, finished, recursive);
			}
		}
		return totalTasks;
	}

	@Override
	public void run() {
		boolean all = this.all;
		boolean showTasks = this.tasks;
//		boolean showLists = this.lists;
		boolean useGroup = this.group;
		boolean recursive = this.recursive;
		boolean finished = this.finished;

		String list = tasksData.getActiveList();

		if (this.list != null) {
			list = this.list;
		}

		if (!list.startsWith("/")) {
			list = "/" + list;
		}

		if (showTasks) {
			if (!useGroup) {
				if (finished) {
					System.out.println("Finished tasks on list '" + list + "'");
				}
				else {
					System.out.println("Tasks on list '" + list + "'");
				}
				System.out.println();
			}

			final int limit = all ? Integer.MAX_VALUE : MAX_DISPLAYED_TASKS;

			int totalTasks = 0;

			if (useGroup) {
				totalTasks = printTasks(tasksData.getActiveGroup(), totalTasks, finished, recursive);
			}
			else {
				List<Task> tasksList = tasksData.getTasksForList(list).stream()
						.filter(task -> finished == (task.state == TaskState.Finished))
						.collect(Collectors.toList());

				totalTasks += tasksList.size();

				printTasks(tasksList, limit);
			}

			if (totalTasks > limit) {
				System.out.println("(" + (totalTasks - MAX_DISPLAYED_TASKS) + " more tasks.)");
			}
			else if (totalTasks == 0) {
				System.out.println("No tasks.");
			}

			if (totalTasks > 0) {
				System.out.println();
				System.out.print(ANSI_BOLD);
				if (finished) {
					System.out.print("Total Finished Tasks: " + totalTasks);
				}
				else {
					System.out.print("Total Tasks: " + totalTasks);
				}
				System.out.print(ANSI_RESET);
				System.out.println();
			}
			System.out.println();
		}
		else {
			TaskGroup activeGroup = tasksData.getActiveGroup();

			List<TaskContainer> children = activeGroup.getChildren().stream()
					.sorted(Comparator.comparing(TaskContainer::getName))
					.collect(Collectors.toList());

			System.out.println("Current group is '" + activeGroup.getFullPath() + "'");
			System.out.println();

			for (TaskContainer child : children) {
				if (child instanceof TaskList) {
					printListRelative((TaskList) child, finished);
				}
				else if (finished == (child.getState() == TaskContainerState.Finished)) {
					System.out.print("  ");
					System.out.println(child.getName() + "/");
				}
			}
			System.out.println();
		}
	}
}
