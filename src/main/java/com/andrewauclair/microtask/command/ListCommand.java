// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.ConsoleTable;
import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingTaskListName;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.*;
import java.util.stream.Collectors;

import static com.andrewauclair.microtask.ConsoleTable.Alignment.LEFT;
import static com.andrewauclair.microtask.ConsoleTable.Alignment.RIGHT;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_BOLD;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_BLACK;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GREEN;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN;

@Command(name = "list", description = "List tasks or the content of a group.")
final class ListCommand implements Runnable {
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

	@Option(names = {"-v", "--verbose"}, description = "Display verbose information.")
	private boolean verbose;

	ListCommand(Tasks tasks, OSInterface osInterface) {
		this.tasksData = tasks;
		this.osInterface = osInterface;
	}

	private void printTasks(ConsoleTable table, List<Task> tasksList, int limit, String listName, boolean printListName) {
		Optional<Task> max = tasksList.stream()
				.limit(limit)
				.max(Comparator.comparingInt(o -> String.valueOf(o.id).length()));

		max.ifPresent(task -> tasksList.stream()
				.limit(limit)
				.sorted(((Comparator<Task>) (o1, o2) -> {
					return Long.compare(o1.id, o2.id);
				}).reversed())
//				.sorted(Comparator.comparingLong(o -> o.id))
				.forEach(str -> printTask(table, str, String.valueOf(task.id).length(), listName, printListName)));
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

	private void addTaskToTable(ConsoleTable table, Task task, String listName, boolean printListName) {
		boolean active = task.id == tasksData.getActiveTaskID();

		// TODO This should probably have an F for finished
		String type;
		if (active) {
			type = "*  ";
		}
		else if (task.isRecurring()) {
			type = " R ";
		}
		else if (task.state == TaskState.Finished) {
			type = "  F";
		}
		else {
			type = "   ";
		}

		if (printListName) {
			table.addRow(active ? ANSI_BG_GREEN : ANSI_BG_BLACK, listName, type, String.valueOf(task.id), task.task);
		}
		else {
			table.addRow(active ? ANSI_BG_GREEN : ANSI_BG_BLACK, type, String.valueOf(task.id), task.task);
		}
	}

	private void printTask(ConsoleTable table, Task task, int maxLength, String listName, boolean printListName) {
		String line = "";

		boolean active = task.id == tasksData.getActiveTaskID();

		if (printListName) {
			line += listName;
			line += " ";
		}

		// TODO This should probably have an F for finished
		String type;
		if (active) {
			line += "* ";
			type = "*  ";
		}
		else if (task.isRecurring()) {
			line += "R ";
			type = " R ";
		}
		else if (task.state == TaskState.Finished) {
			type = "  F";
		}
		else {
			line += "  ";
			type = "   ";
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

//		System.out.println(line);

		if (printListName) {
			table.addRow(active ? ANSI_BG_GREEN : ANSI_BG_BLACK, listName, type, String.valueOf(task.id), task.task);
		}
		else {
			table.addRow(active ? ANSI_BG_GREEN : ANSI_BG_BLACK, type, String.valueOf(task.id), task.task);
		}
	}

	private List<Task> getTasks(TaskGroup group, boolean finished, boolean recursive) {
		List<Task> tasks = new ArrayList<>();

		for (final TaskContainer child : group.getChildren()) {
			if (child instanceof TaskList list) {
				tasks.addAll(list.getTasks().stream()
						.filter(task -> finished == (task.state == TaskState.Finished))
						.collect(Collectors.toList()));


			}
			else if (recursive) {
				tasks.addAll(getTasks((TaskGroup) child, finished, true));
			}
		}
		return tasks;
	}

	private List<Task> printTasks(ConsoleTable table, TaskGroup mainGroup, TaskGroup group, boolean finished, boolean recursive) {
		List<Task> tasks = new ArrayList<>();

		for (TaskContainer child : group.getChildren()) {
			if (child instanceof TaskList list) {
				List<Task> tasksList = list.getTasks().stream()
						.filter(task -> finished == (task.state == TaskState.Finished))
						.collect(Collectors.toList());

				tasks.addAll(tasksList);

				if (tasksList.size() > 0) {
//					System.out.println(ANSI_BOLD + list.getFullPath() + ANSI_RESET);

					String name = list.getFullPath().replace(mainGroup.getFullPath(), "");
					printTasks(table, tasksList, Integer.MAX_VALUE, name, true);
//					System.out.println();
				}
			}
			else if (recursive) {
				tasks.addAll(printTasks(table, mainGroup, (TaskGroup) child, finished, true));
			}
		}
		return tasks;
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

			int limit = all ? Integer.MAX_VALUE : osInterface.getTerminalHeight() - 8;

			List<Task> tasks = new ArrayList<>();

			ConsoleTable table = new ConsoleTable(osInterface);
			table.enableAlternatingColors();

			if (useGroup) {
				table.setHeaders("List", "Type", "ID", "Description");
				table.setColumnAlignment(LEFT, LEFT, RIGHT, LEFT);
			}
			else {
				table.setHeaders("Type", "ID", "Description");
				table.setColumnAlignment(LEFT, RIGHT, LEFT);
			}

			if (verbose) {
				table.enableWordWrap();
			}

			if (useGroup) {
//				tasks = printTasks(table, tasksData.getActiveGroup(), tasksData.getActiveGroup(), finished, recursive);
				tasks = getTasks(tasksData.getActiveGroup(), finished, recursive);
			}
			else {
				List<Task> tasksList = tasksData.getTasksForList(list).stream()
						.filter(task -> finished == (task.state == TaskState.Finished))
						.collect(Collectors.toList());

				tasks.addAll(tasksList);

//				printTasks(table, tasksList, limit, "", false);
			}

			if (tasks.size() > limit) {
				limit--;
			}

			table.setRowLimit(limit, true);

			tasks.sort((o1, o2) -> Long.compare(o2.id, o1.id));

			Optional<Task> activeTask = Optional.empty();

			for (final Task task : tasks) {
				if (task.state == TaskState.Active) {
					activeTask = Optional.of(task);
				}
				else {
					TaskList listForTask = tasksData.findListForTask(new ExistingID(tasksData, task.id));
					String name = listForTask.getFullPath().replace(tasksData.getActiveGroup().getFullPath(), "");

					addTaskToTable(table, task, name, useGroup);
				}
			}

			if (activeTask.isPresent()) {
				TaskList listForTask = tasksData.findListForTask(new ExistingID(tasksData, activeTask.get().id));
				String name = listForTask.getFullPath().replace(tasksData.getActiveGroup().getFullPath(), "");

				addTaskToTable(table, activeTask.get(), name, useGroup);
			}

			int totalTasks = tasks.size();

			if (totalTasks > 0) {
				table.print();
			}

			long totalRecurring = tasks.stream()
					.filter(Task::isRecurring)
					.count();

			if (totalTasks > limit) {
				System.out.println();
				System.out.println("(" + (totalTasks - limit) + " more tasks.)");
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

					if (totalRecurring > 0) {
						System.out.print(" (" + totalRecurring + " Recurring)");
					}
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
