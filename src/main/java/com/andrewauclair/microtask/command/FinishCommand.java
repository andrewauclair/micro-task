// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.*;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "finish")
final class FinishCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@ArgGroup(multiplicity = "1")
	private FinishOptions options;

	private static final class FinishOptions {
		@Option(required = true, names = {"-t", "--task"}, split = ",")
		private Integer[] id;

		@Option(required = true, names = {"-l", "--list"}, completionCandidates = ListCompleter.class)
		private String list;

		@Option(required = true, names = {"-g", "--group"}, completionCandidates = GroupCompleter.class)
		private String group;

		@Option(required = true, names = {"-a", "--active"})
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
			for (final Integer id : options.id) {
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
		if (tasks.getGroup(this.options.group).getFullPath().equals(tasks.getActiveGroup().getFullPath())) {
			System.out.println("Group to finish must not be active.");
		}
		else if (tasks.getGroup(this.options.group).getTasks().stream()
				.anyMatch(task -> task.state != TaskState.Finished)) {
			System.out.println("Group to finish still has tasks to complete.");
		}
		else {
			TaskGroup taskGroup = tasks.finishGroup(this.options.group);

			System.out.println("Finished group '" + taskGroup.getFullPath() + "'");
		}

		System.out.println();
	}

	private void finishList() {
		if (tasks.getListByName(this.options.list).getFullPath().equals(tasks.getActiveList())) {
			System.out.println("List to finish must not be active.");
		}
		else if (tasks.getListByName(this.options.list).getTasks().stream()
				.anyMatch(task -> task.state != TaskState.Finished)) {
			System.out.println("List to finish still has tasks to complete.");
		}
		else {
			TaskList taskList = tasks.finishList(this.options.list);

			System.out.println("Finished list '" + taskList.getFullPath() + "'");
		}
		System.out.println();
	}
}
