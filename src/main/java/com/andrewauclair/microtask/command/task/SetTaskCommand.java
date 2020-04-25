// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.task;

import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine;

@CommandLine.Command(name = "task")
public class SetTaskCommand implements Runnable {
	private final Tasks tasks;

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

	public SetTaskCommand(Tasks tasks) {
		this.tasks = tasks;
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
