// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.task;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.build.TaskBuilder;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@CommandLine.Command(name = "task")
public class SetTaskCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Parameters(description = "Task to set.")
	private ExistingID id;

	@CommandLine.Option(names = {"-r", "--recurring"}, description = "Set task to recurring.")
	private Boolean recurring;

	@CommandLine.Option(names = {"--not-recurring"}, description = "Set task to non-recurring.")
	private Boolean not_recurring;

	@CommandLine.Option(names = {"--inactive"}, description = "Set task state to inactive.")
	private boolean inactive;

	@CommandLine.Option(names = {"--due"}, description = "Due time of the task.")
	private String due;

	public SetTaskCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		if (recurring != null) {
			tasks.setRecurring(id, true);

			System.out.println("Set recurring for task " + tasks.getTask(id).description() + " to true");
		}
		else if (not_recurring != null) {
			tasks.setRecurring(id, false);

			System.out.println("Set recurring for task " + tasks.getTask(id).description() + " to false");
		}
		else if (due != null) {
			Instant instant = Instant.ofEpochSecond(osInterface.currentSeconds());

			ZoneId zoneId = osInterface.getZoneId();

			LocalDateTime baseDate = LocalDateTime.ofInstant(instant, zoneId);

			// I think it's dumb to need the "P" in front, but this parses what I want for now
			long dueTime = baseDate.plus(Period.parse("P" + due)).atZone(zoneId).toEpochSecond();

			tasks.replaceTask(new ExistingListName(tasks, tasks.getListForTask(id).getFullPath()),
					tasks.getTask(id), new TaskBuilder(tasks.getTask(id))
					.withDueTime(dueTime)
					.build());

			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
			String eodStr = Instant.ofEpochSecond(dueTime).atZone(zoneId).format(dateTimeFormatter);

			System.out.println("Set due date for task " + tasks.getTask(id).description() + " to " + eodStr);
		}
		else {
			Task task = tasks.getTask(id);

			if (task.state == TaskState.Finished) {
				task = tasks.setTaskState(id, TaskState.Inactive);

				System.out.println("Set state of task " + task.description() + " to Inactive");
			}
			else {
				System.out.println("Task " + task.description() + " must be finished first");
			}
		}
		System.out.println();
	}
}
