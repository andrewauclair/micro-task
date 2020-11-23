// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.ConsoleTable;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.andrewauclair.microtask.ConsoleTable.Alignment.LEFT;
import static com.andrewauclair.microtask.ConsoleTable.Alignment.RIGHT;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GREEN;

@Command(name = "next", description = "Display the next tasks to be completed.")
final class NextCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"-c", "--count"}, description = "Number of tasks to display.")
	private int count;

	@Option(names = {"--due"}, description = "Show tasks that are due.")
	private boolean due;

	@Option(names = {"-v", "--verbose"}, description = "Display the full description of a task.")
	private boolean verbose;

	@Option(names = {"--list"}, description = "List to include in the next search.")
	private List<ExistingListName> list;

	@Option(names = {"--group"}, description = "Group to include in the next search.")
	private List<ExistingGroupName> group;

	@Option(names = {"--exclude-list"}, description = "List to exclude from the next search.")
	private List<ExistingListName> excludeList;

	@Option(names = {"--exclude-group"}, description = "Group to exclude from the next search.")
	private List<ExistingGroupName> excludeGroup;

	NextCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		int max = count;

		if (list != null && excludeList != null) {
			System.out.println("--list and --exclude-list cannot be used together.");
			System.out.println();
			return;
		}

		List<Task> tasks = this.tasks.getAllTasks().stream()
				.sorted(Comparator.comparingLong(o -> o.id))
				.filter(task -> task.state != TaskState.Finished)
				.filter(task -> !task.recurring)
				.filter(this::includeTask)
//				.limit(max)
				.collect(Collectors.toList());

		if (due) {
			tasks = tasks.stream()
					.skip(tasks.size() - max)
					.collect(Collectors.toList());

			tasks.sort(new Comparator<Task>() {
				@Override
				public int compare(Task o1, Task o2) {
					return Long.compare(o1.dueTime, o2.dueTime);
				}
			});
		}
		else {
			tasks = tasks.stream()
					.limit(max)
					.collect(Collectors.toList());
		}

		System.out.print("Next " + tasks.size() + (due ? " Due" : "") + " Tasks to Complete");

		if (list != null || group != null || excludeGroup != null || excludeList != null) {
			String includeList = "";
			String includeGroup = "";
			String excludeGroup = "";
			String excludeList = "";

			if (list != null) {
				includeList = list.stream().map(TaskListName::absoluteName).collect(Collectors.joining(", "));
			}
			if (group != null) {
				includeGroup = group.stream().map(TaskGroupName::absoluteName).collect(Collectors.joining(", "));
			}
			if (this.excludeList != null) {
				excludeList = this.excludeList.stream().map(TaskListName::absoluteName).collect(Collectors.joining(", "));
			}
			if (this.excludeGroup != null) {
				excludeGroup = this.excludeGroup.stream().map(TaskGroupName::absoluteName).collect(Collectors.joining(", "));
			}

			boolean include = !includeList.isEmpty() || !includeGroup.isEmpty();
			boolean exclude = !excludeList.isEmpty() || !excludeGroup.isEmpty();

			if (include || exclude) {
				System.out.print(" (");
			}

			if (include) {
				System.out.print("include: ");
				if (!includeList.isEmpty()) {
					System.out.print(includeList);
				}
				if (!includeGroup.isEmpty()) {
					if (!includeList.isEmpty()) {
						System.out.print(", ");
					}
					System.out.print(includeGroup);
				}
			}

			if (exclude) {
				if (include) {
					System.out.print("; ");
				}
				System.out.print("exclude: ");
				if (!excludeList.isEmpty()) {
					System.out.print(excludeList);
				}
				if (!excludeGroup.isEmpty()) {
					if (!excludeList.isEmpty()) {
						System.out.print(", ");
					}
					System.out.print(excludeGroup);
				}
			}
			if (include || exclude) {
				System.out.print(")");
			}
		}

		System.out.println();
		System.out.println();

		ConsoleTable table = new ConsoleTable(osInterface);
		table.enableAlternatingColors();

		if (due) {
			table.showFirstRows();
			table.setHeaders("List", "ID", "Due", "Description");
			table.setColumnAlignment(LEFT, RIGHT, LEFT, LEFT);
		}
		else {
			table.setHeaders("List", "ID", "Description");
			table.setColumnAlignment(LEFT, RIGHT, LEFT);
		}

		if (verbose) {
			table.enableWordWrap();
		}

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		ZoneId zoneId = osInterface.getZoneId();

		for (final Task task : tasks) {
			if (task.state == TaskState.Active) {
				if (due) {
					String dueStr = Instant.ofEpochSecond(task.dueTime).atZone(zoneId).format(dateTimeFormatter);
					table.addRow(ANSI_BG_GREEN, this.tasks.findListForTask(new ExistingID(this.tasks, task.id)).getFullPath(), String.valueOf(task.id), dueStr, task.task);
				}
				else {
					table.addRow(ANSI_BG_GREEN, this.tasks.findListForTask(new ExistingID(this.tasks, task.id)).getFullPath(), String.valueOf(task.id), task.task);
				}
			}
			else {
				if (due) {
					String dueStr = Instant.ofEpochSecond(task.dueTime).atZone(zoneId).format(dateTimeFormatter);
					table.addRow(this.tasks.findListForTask(new ExistingID(this.tasks, task.id)).getFullPath(), String.valueOf(task.id), dueStr, task.task);

				}
				else {
					table.addRow(this.tasks.findListForTask(new ExistingID(this.tasks, task.id)).getFullPath(), String.valueOf(task.id), task.task);
				}
			}
		}

		table.print();
		System.out.println();
	}

	private boolean includeTask(Task task) {
		if (list == null && group == null && excludeList == null && excludeGroup == null) {
			return true;
		}

		if (excludeList != null) {
			for (final ExistingListName listName : excludeList) {
				boolean match = tasks.getList(listName).getTasks().stream()
						.anyMatch(t -> t.id == task.id);

				if (match) {
					return false;
				}
			}
		}

		if (excludeGroup != null) {
			for (final ExistingGroupName groupName : excludeGroup) {
				boolean match = tasks.getGroup(groupName).getTasks().stream()
						.anyMatch(t -> t.id == task.id);

				if (match) {
					return false;
				}
			}
		}

		// automatically include the task if the user didn't use --list or --group
		boolean include = list == null && group == null;

		if (list != null) {
			for (final ExistingListName listName : list) {
				boolean match = tasks.getList(listName).getTasks().stream()
						.anyMatch(t -> t.id == task.id);

				if (match) {
					include = true;
				}
			}
		}

		if (group != null) {
			for (final ExistingGroupName groupName : group) {
				boolean match = tasks.getGroup(groupName).getTasks().stream()
						.anyMatch(t -> t.id == task.id);

				if (match) {
					include = true;
				}
			}
		}

		return include;
	}
}
