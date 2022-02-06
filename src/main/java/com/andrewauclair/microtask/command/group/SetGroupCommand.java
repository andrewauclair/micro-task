// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.group;

import com.andrewauclair.microtask.DueDate;
import com.andrewauclair.microtask.command.list.SetListCommand;
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

		@ArgGroup()
		DueArgs dueArgs;

		private static class DueArgs {
			@Option(names = {"--due"}, description = "Set the due time for all tasks in the group.")
			private DueDate due;

			@Option(names = {"--due-today"}, description = "Set the due time for all tasks in the group to today.")
			private Boolean due_today;
		}
	}

	@ArgGroup(exclusive = false, multiplicity = "1")
	Args args;

	public SetGroupCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		checkDueToday();

		if (args.in_progress) {
			TaskGroup group = tasks.getGroup(this.group.absoluteName());

			if (group.getState() == TaskContainerState.Finished) {
				tasks.setGroupState(this.group, TaskContainerState.InProgress, true);
			}
			else {
				System.out.println("Group '" + group.getFullPath() + "' must be finished first");
			}
		}
		else if (args.dueArgs.due != null) {
			ZoneId zoneId = osInterface.getZoneId();

			long dueTime = args.dueArgs.due.dueTime();

			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
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

	private void checkDueToday() {
		if (args.dueArgs != null && args.dueArgs.due_today != null) {
			args.dueArgs.due = new DueDate(osInterface, Period.parse("p0d"));
		}
	}
}
