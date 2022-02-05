// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.list;

import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Command(name = "list")
public class SetListCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Parameters(completionCandidates = ListCompleter.class, description = "The list to set.")
	private ExistingListName list;

	@ArgGroup(exclusive = false, multiplicity = "1")
	Args args;

	private static class Args {
		@Option(names = {"--in-progress"}, description = "Set the list state to in progress.")
		private boolean in_progress;

		@Option(names = {"--due"}, description = "Set the due time for all tasks in the list.")
		private String due;

		@Option(names = {"--due-today"}, description = "Set the due time for all tasks in the list to today.")
		private Boolean dueToday;
	}

	public SetListCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		if (args.dueToday != null) {
			args.due = "0d";
		}

		if (args.in_progress) {
			TaskList list = tasks.getListByName(this.list);

			if (list.getState() == TaskContainerState.Finished) {
				tasks.setListState(this.list, TaskContainerState.InProgress, true);
			}
			else {
				System.out.println("List '" + list.getFullPath() + "' must be finished first");
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

			TaskList list = tasks.getList(new ExistingListName(tasks, this.list.absoluteName()));

			for (final Task task : list.getTasks()) {
				tasks.setDueDate(new ExistingID(tasks, task.id), dueTime);

				System.out.println("Set due date for task " + task.description() + " to " + eodStr);
			}

			osInterface.gitCommit("Set due date for multiple tasks to " + eodStr);
		}

		System.out.println();
	}
}
