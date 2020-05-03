// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.ConsoleTable;
import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.andrewauclair.microtask.ConsoleTable.Alignment.LEFT;
import static com.andrewauclair.microtask.ConsoleTable.Alignment.RIGHT;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_BOLD;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_BLACK;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GREEN;

public class TasksCommand implements Runnable {
	@CommandLine.Option(names = {"-l", "--list"}, completionCandidates = ListCompleter.class, description = "List tasks on list.")
	private ExistingListName list;

	@CommandLine.Option(names = {"--current-group"}, description = "List tasks in the current group.")
	private boolean current_group;

	@CommandLine.Option(names = {"-g", "--group"}, completionCandidates = GroupCompleter.class, description = "List tasks in this group.")
	private ExistingGroupName group;

	@CommandLine.Option(names = {"--recursive"}, description = "List tasks recursively in all sub-groups.")
	private boolean recursive;

	@CommandLine.Option(names = {"--finished"}, description = "List finished tasks.")
	private boolean finished;

	@CommandLine.Option(names = {"--all"}, description = "List all tasks.")
	private boolean all;

	@CommandLine.Option(names = {"-v", "--verbose"}, description = "Display verbose information.")
	private boolean verbose;

	private final Tasks tasksData;
	private final OSInterface osInterface;

	public TasksCommand(Tasks tasks, OSInterface osInterface) {
		tasksData = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		boolean all = this.all;
//		boolean showTasks = this.tasks;
		boolean useGroup = this.current_group;
		boolean recursive = this.recursive;
		boolean finished = this.finished;

		ExistingListName list = tasksData.getActiveList();

		if (this.list != null) {
			list = this.list;
		}

		ExistingGroupName group = new ExistingGroupName(tasksData, tasksData.getActiveGroup().getFullPath());

		if (this.group != null) {
			group = this.group;
			useGroup = true;
		}

//		if (showTasks) {
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
				tasks = getTasks(tasksData.getActiveGroup(), finished, recursive);
			}
			else {
				List<Task> tasksList = tasksData.getTasksForList(list).stream()
						.filter(task -> finished == (task.state == TaskState.Finished))
						.collect(Collectors.toList());

				tasks.addAll(tasksList);
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
//		}
		System.out.println();
	}

	private void addTaskToTable(ConsoleTable table, Task task, String listName, boolean printListName) {
		boolean active = task.id == tasksData.getActiveTaskID();

		String type = "";
		if (active) {
			type = "*";
		}
		else {
			type = " ";
		}

		if (task.isRecurring()) {
			type += "R";
		}
		else {
			type += " ";
		}

		if (task.state == TaskState.Finished) {
			type += "F";
		}
		else {
			type += " ";
		}

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
}
