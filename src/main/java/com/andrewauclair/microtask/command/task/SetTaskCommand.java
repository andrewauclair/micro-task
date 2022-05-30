// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.task;

import com.andrewauclair.microtask.DueDate;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine;

import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
	private Boolean inactive;

	@CommandLine.Option(names = {"--due"}, description = "Due time of the task.")
	private DueDate due;

	@CommandLine.Option(names = {"--due-today"}, description = "Set due date of task as today.")
	private Boolean dueToday;

	@CommandLine.Option(names = {"--add-tags"}, split = ",", description = "Tags to add to task.")
	private List<String> addTags;

	@CommandLine.Option(names = {"--remove-tags"}, split = ",", description = "Tags to remove from task.")
	private List<String> removeTags;

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

		if (dueToday != null) {
			// set the due date to 0d, which is "today" in the Period.parse method
			due = new DueDate(osInterface, Period.parse("p0d"));
		}

		if (due != null) {
			ZoneId zoneId = osInterface.getZoneId();

			long dueTime = due.dueTime();

			tasks.setDueDate(id, dueTime);

			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
			String eodStr = Instant.ofEpochSecond(dueTime).atZone(zoneId).format(dateTimeFormatter);

			// TODO This isn't covered by a test
			osInterface.gitCommit("Set due date for task " + tasks.getTask(id).description() + " to " + eodStr);

			System.out.println("Set due date for task " + tasks.getTask(id).description() + " to " + eodStr);
		}

		Task task = tasks.getTask(id);

		if (addTags != null) {
			List<String> tags = new ArrayList<>(task.tags);
			tags.addAll(addTags);

			tasks.setTags(id, tags);

			System.out.println("Task " + id.get() + ", add tag(s): " + String.join(", ", addTags));
		}

		if (removeTags != null) {
			List<String> tags = new ArrayList<>(task.tags);
			tags.removeAll(removeTags);

			tasks.setTags(id, tags);

			System.out.println("Task " + id.get() + ", remove tag(s): " + String.join(", ", removeTags));
		}

		if (inactive != null) {

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
