// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.list;

import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.DueDate;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.time.*;
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
		private Boolean in_progress;

		@Option(names = {"--time-category"}, description = "Set the Time Category of the list.")
		private String time_category;

		@ArgGroup()
		DueArgs dueArgs;

		private static class DueArgs {
			@Option(names = {"--due"}, description = "Set the due time for all tasks in the list.")
			private DueDate due;

			@Option(names = {"--due-today"}, description = "Set the due time for all tasks in the list to today.")
			private Boolean due_today;
		}
	}

	public SetListCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		checkDueToday();

		if (args.in_progress != null) {
			TaskList list = tasks.getListByName(this.list);

			if (list.getState() == TaskContainerState.Finished) {
				tasks.setListState(this.list, TaskContainerState.InProgress, true);
			}
			else {
				System.out.println("List '" + list.getFullPath() + "' must be finished first");
			}
		}
		else if (args.time_category != null) {
			tasks.setListTimeCategory(this.list, args.time_category, true);
		}
		else if (args.dueArgs.due != null) {
			ZoneId zoneId = osInterface.getZoneId();

			long dueTime = args.dueArgs.due.dueTime();

			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
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

	private void checkDueToday() {
		if (args.dueArgs != null && args.dueArgs.due_today != null) {
			args.dueArgs.due = new DueDate(osInterface, Period.parse("p0d"));
		}
	}
}
