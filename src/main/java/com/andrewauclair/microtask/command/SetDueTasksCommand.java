// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@CommandLine.Command(name = "due-tasks")
public class SetDueTasksCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Option(names = {"--due"}, description = "Set the due time for all due tasks.")
	private String due;

	@CommandLine.Option(names = {"--interactive"}, description = "Choose which tasks the new due time is assigned to. If there is no --due argument, then a custom time can be picked per task.")
	private boolean interactive;

	public SetDueTasksCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		Instant instant = Instant.ofEpochSecond(osInterface.currentSeconds());

		ZoneId zoneId = osInterface.getZoneId();

		List<Task> dueTasks = getDueTasks(instant);
		dueTasks.sort(Comparator.comparingLong(o -> o.id));

		long dueTime = 0;
		String dueTimeStr = "";

		if (due != null) {
			dueTime = getDueTime(instant, zoneId, due);
			dueTimeStr = getDueTimeString(dueTime, zoneId);
		}

		for (final Task dueTask : dueTasks) {
			ExistingID id = new ExistingID(tasks, dueTask.id);

			boolean set = true;

			if (interactive) {
				System.out.println(tasks.getTask(id).description());

				if (due != null) {
					set = osInterface.promptChoice("set task " + id.get() + " due in " + due);
				}
				else {
					set = osInterface.promptChoice("change due time for task " + id.get());

					if (set) {
						String due = osInterface.promptForString("task " + id.get() + ", due in: ");

						dueTime = getDueTime(instant, zoneId, due);
						dueTimeStr = getDueTimeString(dueTime, zoneId);
					}
				}
			}

			if (set) {
				tasks.setDueDate(id, dueTime);

				osInterface.gitCommit("Set due date for task " + tasks.getTask(id).description() + " to " + dueTimeStr);

				System.out.println("Set due date for task " + tasks.getTask(id).description() + " to " + dueTimeStr);
			}
		}
		System.out.println();
	}

	private String getDueTimeString(long dueTime, ZoneId zoneId) {
		String eodStr;
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		eodStr = Instant.ofEpochSecond(dueTime).atZone(zoneId).format(dateTimeFormatter);
		return eodStr;
	}

	private long getDueTime(Instant instant, ZoneId zoneId, String due) {
		long dueTime;
		LocalDateTime baseDate = LocalDateTime.ofInstant(instant, zoneId);

		// I think it's dumb to need the "P" in front, but this parses what I want for now
		dueTime = baseDate.plus(Period.parse("P" + due)).atZone(zoneId).toEpochSecond();
		return dueTime;
	}

	// TODO I borrowed this from TasksCommand, should put it somewhere common
	private List<Task> getDueTasks(Instant instant) {
		ZoneId zoneId = osInterface.getZoneId();

		LocalDate today = LocalDate.ofInstant(instant, zoneId);
		LocalDateTime midnight = LocalDateTime.of(today, LocalTime.MIDNIGHT);
		LocalDateTime nextMidnight = midnight.plusDays(1);
		long midnightStop = nextMidnight.atZone(zoneId).toEpochSecond();

		List<Task> dueTasks = new ArrayList<>();

		for (Task task : tasks.getAllTasks()) {
			if (task.state != TaskState.Finished && !task.recurring && task.dueTime < midnightStop) {
				dueTasks.add(task);
			}
		}

		return dueTasks;
	}
}
