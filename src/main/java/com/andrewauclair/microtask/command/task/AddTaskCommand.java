// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.task;

import com.andrewauclair.microtask.command.Commands;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskList;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.build.TaskBuilder;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;

import static java.time.Duration.parse;

@CommandLine.Command(name = "task")
public class AddTaskCommand implements Runnable {
	private final Tasks tasks;
	private final Commands commands;
	private final OSInterface osInterface;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Parameters(index = "0", description = "The name of the new task.")
	private String name;

	@CommandLine.Option(names = {"-l", "--list"}, completionCandidates = ListCompleter.class, description = "The list to add the new task to.")
	private ExistingListName list;

	@CommandLine.Option(names = {"-r", "--recurring"}, description = "Set the task to recurring.")
	private boolean recurring;

	@CommandLine.Option(names = {"--due"}, description = "Due time of the task. Defaults to 1 week.")
	private String due;

	@CommandLine.Option(names = {"--due-today"}, description = "Task is due today.")
	private Boolean dueToday;

	@CommandLine.Option(names = {"-s", "--start"}, description = "Start the task immediately.")
	private boolean start;

	@CommandLine.Option(names = {"-t", "--tags"}, split = ",", description = "Tags to set on the task.")
	private List<String> tags;

	public AddTaskCommand(Tasks tasks, Commands commands, OSInterface osInterface) {
		this.tasks = tasks;
		this.commands = commands;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		ExistingListName list = tasks.getCurrentList();

		if (this.list != null) {
			list = this.list;
		}

		if (dueToday != null) {
			// set the due date to 0d, which is "today" in the Period.parse method
			due = "0w";
		}

		TaskList taskList = tasks.getList(list);

		if (taskList.canAddTask()) {
			long newID = tasks.incrementID();

			long addTime = osInterface.currentSeconds();

			TaskBuilder builder = new TaskBuilder(newID)
					.withTask(name)
					.withState(TaskState.Inactive)
					.withAddTime(addTime)
					.withDueTime(addTime + Tasks.DEFAULT_DUE_TIME);

			builder.withRecurring(recurring);

			if (due != null) {
				Instant instant = Instant.ofEpochSecond(addTime);

				ZoneId zoneId = osInterface.getZoneId();

				LocalDateTime baseDate = LocalDateTime.ofInstant(instant, zoneId);

				// I think it's dumb to need the "P" in front, but this parses what I want for now
				long dueTime = baseDate.plus(Period.parse("P" + due)).atZone(zoneId).toEpochSecond();

				builder.withDueTime(dueTime);
			}

			if (tags != null) {
				tags.forEach(builder::withTag);
			}

			Task task = builder.build();

			tasks.addTask(task, taskList, true);

			System.out.println("Added task " + task.description());

			if (this.tags != null) {
				System.out.println("with tag(s): " + String.join(", ", tags));
			}

			if (this.list != null) {
				System.out.println("to list '" + this.list + "'");
			}

			System.out.println();

			if (start) {
				commands.execute(System.out, "start task " + task.id);
			}
		}
		else {
			System.out.println("Task '" + name + "' cannot be created because list '" + taskList.getFullPath() + "' has been finished.");
		}
	}
}
