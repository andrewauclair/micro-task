// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.group;

import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.build.TaskBuilder;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@CommandLine.Command(name = "group")
public class SetGroupCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Parameters(completionCandidates = GroupCompleter.class, description = "The group to set.")
	private ExistingGroupName group;

	private static class Args {
		@Option(names = {"--in-progress"}, description = "Set the list state to in progress.")
		private boolean in_progress;

		@Option(names = {"--due"}, description = "Set the due time for all tasks in the group.")
		private String due;

		@Option(names = {"--due-today"}, description = "Set the due time for all tasks in the group to today.")
		private Boolean dueToday;
	}

	@ArgGroup(exclusive = false, multiplicity = "1")
	Args args;

	public SetGroupCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		if (args.dueToday != null) {
			args.due = "0d";
		}

		if (args.in_progress) {
			TaskGroup group = tasks.getGroup(this.group.absoluteName());

			if (group.getState() == TaskContainerState.Finished) {
				tasks.setGroupState(this.group, TaskContainerState.InProgress, true);
			}
			else {
				System.out.println("Group '" + group.getFullPath() + "' must be finished first");
			}
		}
		else if (args.due != null) {
			Instant instant = Instant.ofEpochSecond(osInterface.currentSeconds());

			ZoneId zoneId = osInterface.getZoneId();

			LocalDateTime baseDate = LocalDateTime.ofInstant(instant, zoneId);

			// I think it's dumb to need the "P" in front, but this parses what I want for now
			long dueTime = baseDate.plus(Period.parse("P" + args.due)).atZone(zoneId).toEpochSecond();

			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
			String eodStr = Instant.ofEpochSecond(dueTime).atZone(zoneId).format(dateTimeFormatter);

			TaskGroup group = tasks.getGroup(this.group.absoluteName());

			for (final Task task : group.getTasks()) {
				tasks.setDueDate(new ExistingID(tasks, task.id), dueTime);

				System.out.println("Set due date for task " + task.description() + " to " + eodStr);
			}

			osInterface.gitCommit("Set due date for multiple tasks to " + eodStr);
		}

		System.out.println();
	}
}
