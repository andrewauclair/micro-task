// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingTaskListName;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_BOLD;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN;

@Command(name = "list", description = "List tasks or the content of a group.")
final class ListCommand implements Runnable {
	private static final int MAX_DISPLAYED_TASKS = 20;
	private final Tasks tasksData;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"--tasks"}, description = "List tasks on list or in group.")
	private boolean tasks;

	@Option(names = {"-l", "--list"}, completionCandidates = ListCompleter.class, description = "List tasks on list.")
	private ExistingTaskListName list;

	@Option(names = {"--current-group"}, description = "List tasks in the current group.")
	private boolean current_group;

	@Option(names = {"-g", "--group"}, completionCandidates = GroupCompleter.class, description = "List tasks in this group.")
	private ExistingTaskGroupName group;

	@Option(names = {"--recursive"}, description = "List tasks recursively in all sub-groups.")
	private boolean recursive;

	@Option(names = {"--finished"}, description = "List finished tasks.")
	private boolean finished;

	@Option(names = {"--all"}, description = "List all tasks.")
	private boolean all;

	ListCommand(Tasks tasks, OSInterface osInterface) {
		this.tasksData = tasks;
		this.osInterface = osInterface;
	}

	private void printTasks(List<Task> tasksList, int limit) {
		Optional<Task> max = tasksList.stream()
				.limit(limit)
				.max(Comparator.comparingInt(o -> String.valueOf(o.id).length()));

		max.ifPresent(task -> tasksList.stream()
				.limit(limit)
				.sorted(Comparator.comparingLong(o -> o.id))
				.forEach(str -> printTask(str, String.valueOf(task.id).length())));
	}

	private void printListRelative(TaskList list, boolean finished) {
		if (list.getFullPath().equals(tasksData.getActiveList().absoluteName())) {
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
			if (child instanceof TaskList list) {
				List<Task> tasksList = list.getTasks().stream()
						.filter(task -> finished == (task.state == TaskState.Finished))
						.collect(Collectors.toList());

				totalTasks += tasksList.size();

				if (tasksList.size() > 0) {
					System.out.println(ANSI_BOLD + list.getFullPath() + ANSI_RESET);
					printTasks(tasksList, Integer.MAX_VALUE);
					System.out.println();
				}
			}
			else if (recursive) {
				totalTasks = printTasks((TaskGroup) child, totalTasks, finished, true);
			}
		}
		return totalTasks;
	}

	@Override
	public void run() {
		boolean all = this.all;
		boolean showTasks = this.tasks;
		boolean useGroup = this.current_group;
		boolean recursive = this.recursive;
		boolean finished = this.finished;

		ExistingTaskListName list = tasksData.getActiveList();

		if (this.list != null) {
			list = this.list;
		}

		ExistingTaskGroupName group = new ExistingTaskGroupName(tasksData, tasksData.getActiveGroup().getFullPath());

		if (this.group != null) {
			group = this.group;
			useGroup = true;
		}

		if (showTasks) {
			if (!useGroup) {
				if (finished) {
					System.out.println("Finished tasks on list '" + list + "'");
				}
				else {
					System.out.println("Tasks on list '" + list + "'");
				}
			}
			else {
				System.out.println("Tasks in group '" + group + "'");
			}
			System.out.println();

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
		}
		System.out.println();
	}
}
