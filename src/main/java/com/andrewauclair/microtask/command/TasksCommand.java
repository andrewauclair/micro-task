// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.ConsoleTable;
import com.andrewauclair.microtask.DueDate;
import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.project.*;
import com.andrewauclair.microtask.schedule.Schedule;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.andrewauclair.microtask.ConsoleTable.Alignment.LEFT;
import static com.andrewauclair.microtask.ConsoleTable.Alignment.RIGHT;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_BOLD;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.*;

public class TasksCommand implements Runnable {
	@CommandLine.Option(names = {"-l", "--list"}, completionCandidates = ListCompleter.class, description = "List tasks on list.")
	private ExistingListName list;

	@CommandLine.Option(names = {"--current-group"}, description = "List tasks in the current group.")
	private boolean current_group;

	@CommandLine.Option(names = {"-g", "--group"}, completionCandidates = GroupCompleter.class, description = "List tasks in this group.")
	private ExistingGroupName group;

	@CommandLine.Option(names = {"--schedule"}, description = "Display tasks on the schedule.")
	private boolean display_schedule;

	@CommandLine.Option(names = {"--recursive"}, description = "List tasks recursively in all sub-groups.")
	private boolean recursive;

	@CommandLine.Option(names = {"--finished"}, description = "List finished tasks.")
	private boolean finished;

	@CommandLine.Option(names = {"--all"}, description = "List all tasks.")
	private boolean all;

	@CommandLine.ArgGroup
	private DueArgs due_args;

	private static class DueArgs {
		@CommandLine.Option(names = {"--due-before"}, description = "Show tasks due before this date.")
		private LocalDate due_before;

		@CommandLine.Option(names = {"--due-within"}, description = "Show tasks due within a period.")
		private Period due_within;
	}

	@CommandLine.Option(names = {"-v", "--verbose"}, description = "Display verbose information.")
	private boolean verbose;

	private final Tasks tasksData;
	private final Projects projects;
	private final Schedule schedule;
	private final OSInterface osInterface;

	public TasksCommand(Tasks tasks, Projects projects, Schedule schedule, OSInterface osInterface) {
		tasksData = tasks;
		this.projects = projects;
		this.schedule = schedule;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		boolean all = this.all || display_schedule;
//		boolean showTasks = this.tasks;
		boolean useGroup = this.current_group;
		boolean recursive = this.recursive;
		boolean finished = this.finished || display_schedule; // always display finished tasks for schedule

		ExistingListName list = tasksData.getCurrentList();

		if (this.list != null) {
			list = this.list;
		}

		ExistingGroupName group = new ExistingGroupName(tasksData, tasksData.getCurrentGroup().getFullPath());

		if (tasksData.getActiveContext().getActiveGroup().isPresent()) {
			group = tasksData.getActiveContext().getActiveGroup().get();
			useGroup = true;
		}

		if (tasksData.getActiveContext().getActiveProject().isPresent()) {
			String projectName = tasksData.getActiveContext().getActiveProject().get().getName();
			Project project = projects.getProject(new ExistingProject(projects, projectName));

			group = new ExistingGroupName(tasksData, project.getGroup().getFullPath());
			useGroup = true;
		}

		Optional<Feature> feature = Optional.empty();
		Optional<Project> project = Optional.empty();

		if (tasksData.getActiveContext().getActiveFeature().isPresent()) {
			ExistingFeature existingFeature = tasksData.getActiveContext().getActiveFeature().get();
			project = Optional.of(projects.getProjectFromFeature(existingFeature));

			feature = Optional.of(project.get().getFeature(existingFeature));
		}

		Optional<Milestone> milestone = Optional.empty();

		if (tasksData.getActiveContext().getActiveMilestone().isPresent()) {
			ExistingMilestone existingMilestone = tasksData.getActiveContext().getActiveMilestone().get();
			project = Optional.of(projects.getProjectFromMilestone(existingMilestone));
			milestone = Optional.of(project.get().getMilestone(existingMilestone));
		}

		// override the active group if the --group option is present
		if (this.group != null) {
			group = this.group;
			useGroup = true;
		}

		boolean useTags = !tasksData.getActiveContext().getActiveTags().isEmpty();
		List<String> tags = tasksData.getActiveContext().getActiveTags();

		if (due_args != null && due_args.due_before != null) {
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
			System.out.println("Tasks due before " + dateTimeFormatter.format(due_args.due_before.atStartOfDay()));
		}
		else if (due_args != null && due_args.due_within != null) {
			System.out.print("Tasks due within ");

			int days = due_args.due_within.getDays();

			int weeks = days / 7;
			days -= weeks * 7;

			if (due_args.due_within.getMonths() > 0) {
				System.out.print(due_args.due_within.getMonths() + " month(s) ");
			}
			if (weeks > 0) {
				System.out.print(weeks + " week(s) ");
			}
			if (days > 0) {
				System.out.print(days + " day(s) ");
			}
			System.out.println();
		}
		else if (feature.isPresent()) {
			group = new ExistingGroupName(tasksData, project.get().getGroup().getFullPath());
			useGroup = true;

			System.out.print("Tasks for active feature '" + feature.get().getName() + "' of project '" + project.get().getName() + "'");

			if (!tasksData.getActiveContext().getActiveTags().isEmpty()) {
				System.out.print(" with tag(s): " + String.join(", ", tags));
			}
			System.out.println();
		}
		else if (milestone.isPresent()) {
			group = new ExistingGroupName(tasksData, project.get().getGroup().getFullPath());
			useGroup = true;

			System.out.print("Tasks for active milestone '" + milestone.get().getName() + "' for project '" + project.get().getName() + "'");

			if (!tasksData.getActiveContext().getActiveTags().isEmpty()) {
				System.out.print(" with tag(s): " + String.join(", ", tags));
			}
			System.out.println();
		}
		else if (display_schedule) {
			System.out.println("Tasks on Schedule");
		}
		else if (!useGroup) {
			if (finished) {
				System.out.println("Finished tasks on list '" + list + "'");
			}
			else {
				if (tasksData.getActiveContext().getActiveList().isPresent()) {
					list = tasksData.getActiveContext().getActiveList().get();

					System.out.print("Tasks on active list '" + list + "'");

					if (!tasksData.getActiveContext().getActiveTags().isEmpty()) {
						System.out.print(" with tag(s): " + String.join(", ", tags));
					}
					System.out.println();
				}
				else {
					System.out.print("Tasks on list '" + list + "'");

					if (!tasksData.getActiveContext().getActiveTags().isEmpty()) {
						System.out.print(" with tag(s): " + String.join(", ", tags));
					}
					System.out.println();
				}
			}
		}
		else {
			if (tasksData.getActiveContext().getActiveGroup().isPresent()) {
				System.out.print("Tasks in active group '" + group + "'");

				if (!tasksData.getActiveContext().getActiveTags().isEmpty()) {
					System.out.print(" with tag(s): " + String.join(", ", tags));
				}
				System.out.println();
			}
			else if (tasksData.getActiveContext().getActiveProject().isPresent()) {
				System.out.print("Tasks in active project '" + tasksData.getActiveContext().getActiveProject().get().getName() + "'");

				if (!tasksData.getActiveContext().getActiveTags().isEmpty()) {
					System.out.print(" with tag(s): " + String.join(", ", tags));
				}
				System.out.println();
			}
			else {
				System.out.println("Tasks in group '" + group + "'");
			}
		}
		System.out.println();

		int limit = all ? Integer.MAX_VALUE : osInterface.getTerminalHeight() - 8;

		List<Task> tasks = new ArrayList<>();

		ConsoleTable table = new ConsoleTable(osInterface);
		table.enableAlternatingColors();

		if (useGroup) {
			table.setHeaders("List", "Type", "ID", "", "Description");
			table.setColumnAlignment(LEFT, LEFT, RIGHT, RIGHT, LEFT);
		}
		else if (display_schedule) {
			table.setHeaders("Project", "Type", "ID", "", "Description");
			table.setColumnAlignment(LEFT, LEFT, RIGHT, RIGHT, LEFT);
		}
		else {
			table.setHeaders("Type", "ID", "", "Description");
			table.setColumnAlignment(LEFT, RIGHT, RIGHT, LEFT);
		}

		if (verbose) {
			table.enableWordWrap();
		}

		if (feature.isPresent()) {
			tasks = feature.get().getTasks().stream()
					.filter(task -> !useTags || new HashSet<>(task.tags).containsAll(tags))
					.collect(Collectors.toList());
		}
		else if (milestone.isPresent()) {
			tasks = milestone.get().getTasks().stream()
					.map(tasksData::getTask)
					.filter(task -> !useTags || new HashSet<>(task.tags).containsAll(tags))
					.collect(Collectors.toList());
		}
		else if (useGroup) {
			tasks = getTasks(tasksData.getGroup(group), finished, recursive).stream()
					.filter(task -> !useTags || new HashSet<>(task.tags).containsAll(tags))
					.collect(Collectors.toList());
		}
		else if (display_schedule) {
			tasks.addAll(schedule.tasks());
		}
		else {
			List<Task> tasksList = tasksData.getTasksForList(list).stream()
					.filter(task -> finished == (task.state == TaskState.Finished))
					.filter(task -> !useTags || new HashSet<>(task.tags).containsAll(tags))
					.collect(Collectors.toList());

			tasks.addAll(tasksList);
		}

		if (tasks.size() > limit) {
			limit--;
		}

		table.setRowLimit(limit, true);

		List<Task> dueTasks = getDueTasks();

		if (due_args != null) {
			tasks.clear();
		}

		dueTasks.sort((o1, o2) -> ExistingID.compare(o2.ID(), o1.ID()));
		tasks.sort((o1, o2) -> ExistingID.compare(o2.ID(), o1.ID()));

		for (final Task task : tasks) {
			TaskList listForTask = tasksData.findListForTask(task.ID());
			String name = listForTask.getFullPath().replace(group.absoluteName(), "");

			String project_name = projects.getProjectForList(listForTask);

			addTaskToTable(table, task, name, project_name, false, useGroup, display_schedule);
		}

		if (!display_schedule) {
			if (dueTasks.size() > 0 && due_args == null) {
				table.addRow(true, "Due Tasks");
			}

			for (Task dueTask : dueTasks) {
				TaskList listForTask = tasksData.findListForTask(dueTask.ID());
				String name = listForTask.getFullPath().replace(group.absoluteName(), "");

				String project_name = projects.getProjectForList(listForTask);

				addTaskToTable(table, dueTask, name, project_name, true, useGroup, display_schedule);
			}
		}

		if (tasksData.hasActiveTask() && !display_schedule) {
			table.addRow(true, "Active Task");

			TaskList listForTask = tasksData.findListForTask(tasksData.getActiveTask().ID());
			String name = listForTask.getFullPath().replace(group.absoluteName(), "");

			String project_name = projects.getProjectForList(listForTask);

			addTaskToTable(table, tasksData.getActiveTask(), name, project_name, false, useGroup, display_schedule);
		}

		int totalTasks = tasks.size() + dueTasks.size();

		if (tasksData.hasActiveTask() && !tasks.contains(tasksData.getActiveTask())) {
			totalTasks++;
		}

		if (totalTasks > 0) {
			table.print();
		}

		long totalRecurring = tasks.stream()
				.filter(task -> task.recurring)
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
			if (finished && !display_schedule) {
				System.out.print("Total Finished Tasks: " + tasks.size());
			}
			else if (due_args != null) {// && due_args.due_before != null) {
				System.out.print("Total Tasks: " + dueTasks.size());
			}
			else {
				System.out.print("Total Tasks: " + tasks.size());

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

	private List<Task> getDueTasks() {
		long epochSecond = osInterface.currentSeconds();

		ZoneId zoneId = osInterface.getZoneId();

		if (due_args != null && due_args.due_before != null) {
			epochSecond = new DueDate(osInterface, due_args.due_before).dueTime();
		}
		else if (due_args != null && due_args.due_within != null) {
			epochSecond = new DueDate(osInterface, due_args.due_within).dueTime();
		}
		else {
			Instant instant = Instant.ofEpochSecond(epochSecond);

			LocalDate today = LocalDate.ofInstant(instant, zoneId);
			LocalDateTime midnight = LocalDateTime.of(today, LocalTime.MIDNIGHT);
			LocalDateTime nextMidnight = midnight.plusDays(1);
			epochSecond = nextMidnight.atZone(zoneId).toEpochSecond();
		}

		List<Task> dueTasks = new ArrayList<>();

		for (Task task : tasksData.getAllTasks()) {
			if (task.state != TaskState.Finished && !task.recurring && task.dueTime < epochSecond) {
				dueTasks.add(task);
			}
		}

		return dueTasks;
	}

	private void addTaskToTable(ConsoleTable table, Task task, String listName, String projectName, boolean due, boolean printListName, boolean printProjectName) {
		boolean active = task.ID().get().ID() == tasksData.getActiveTaskID();

		String type;
		if (active) {
			type = "*";
		}
		else {
			type = " ";
		}

		if (task.recurring) {
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

		ConsoleColors.ConsoleBackgroundColor bgColor = ANSI_BG_BLACK;

		if (active) {
			bgColor = ANSI_BG_GREEN;
		}
		else if (due) {
			bgColor = ANSI_BG_RED;
		}

		String fullID = String.valueOf(task.fullID().ID());
		String shortID = task.shortID() == RelativeTaskID.NO_SHORT_ID ? "" : "(" + task.shortID().ID() + ")";

		if (printListName) {
			table.addRow(bgColor, false, listName, type, fullID, shortID, task.task);
		}
		else if (printProjectName) {
			table.addRow(bgColor, false, projectName, type, fullID, shortID, task.task);
		}
		else {
			table.addRow(bgColor, false, type, fullID, shortID, task.task);
		}
	}

	private List<Task> getTasks(TaskGroup group, boolean finished, boolean recursive) {
		List<Task> tasks = new ArrayList<>();

		for (final TaskContainer child : group.getChildren()) {
			if (child instanceof TaskList) {
				// TODO Pattern matching instanceof (JDK 16+), had to remove --enable-preview for now
				TaskList list = (TaskList) child;

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
