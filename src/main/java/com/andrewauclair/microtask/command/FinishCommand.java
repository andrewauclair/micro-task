// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "finish", description = "Finish a task, list or group.")
final class FinishCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@ArgGroup(multiplicity = "1")
	private FinishOptions options;

	private static final class FinishOptions {
		@Option(required = true, names = {"-t", "--task"}, split = ",", description = "Task(s) to finish.")
		private ExistingID[] id;

		@Option(required = true, names = {"-l", "--list"}, completionCandidates = ListCompleter.class, description = "List to finish.")
		private ExistingListName list;

		@Option(required = true, names = {"-g", "--group"}, completionCandidates = GroupCompleter.class, description = "Group to finish.")
		private ExistingGroupName group;

		@Option(required = true, names = {"--active-task"}, description = "Finish the active task.")
		private boolean active;
	}

	FinishCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		if (options.list != null) {
			finishList();
		}
		else if (this.options.group != null) {
			finishGroup();
		}
		else if (options.active) {
			finishTask(tasks.finishTask());
		}
		else {
			for (final ExistingID id : options.id) {
				finishTask(tasks.finishTask(id));
			}
		}
	}

	private void finishTask(Task task) {
		System.out.println("Finished task " + task.description());
		System.out.println();
		System.out.print("Task finished in: ");
		System.out.println(new TaskDuration(task, osInterface));
		System.out.println();
	}

	private void finishGroup() {
		if (tasks.getGroup(this.options.group.absoluteName()).getFullPath().equals(tasks.getCurrentGroup().getFullPath())) {
			System.out.println("Group to finish must not be active.");
		}
		else if (tasks.getGroup(this.options.group.absoluteName()).getTasks().stream()
				.anyMatch(task -> task.state != TaskState.Finished)) {
			System.out.println("Group to finish still has tasks to complete.");
		}
		else {
			if (tasks.getGroup(this.options.group.absoluteName()).getState() == TaskContainerState.Finished) {
				System.out.println("Group has already been finished.");
			}
			else {
				TaskGroup taskGroup = tasks.finishGroup(this.options.group);

				System.out.println("Finished group '" + taskGroup.getFullPath() + "'");
			}
		}

		System.out.println();
	}

	private void finishList() {
		if (this.options.list.equals(tasks.getCurrentList())) {
			System.out.println("List to finish must not be active.");
		}
		else if (tasks.getListByName(this.options.list).getTasks().stream()
				.anyMatch(task -> task.state != TaskState.Finished)) {
			System.out.println("List to finish still has tasks to complete.");
		}
		else {
			if (tasks.getListByName(this.options.list).getState() == TaskContainerState.Finished) {
				System.out.println("List has already been finished.");
			}
			else {
				TaskList taskList = tasks.finishList(this.options.list);

				System.out.println("Finished list '" + taskList.getFullPath() + "'");
			}
		}
		System.out.println();
	}
}
