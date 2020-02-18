// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.GroupCompleter;
import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.*;
import picocli.CommandLine;

@CommandLine.Command(name = "finish")
public class FinishCommand extends Command {

	@CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
	private FinishOptions options;

	private static class FinishOptions {
		@CommandLine.Option(required = true, names = {"-t", "--task"})
		private Integer id;

		@CommandLine.Option(required = true, names = {"-l", "--list"}, completionCandidates = ListCompleter.class)
		private String list;

		@CommandLine.Option(required = true, names = {"-g", "--group"}, completionCandidates = GroupCompleter.class)
		private String group;

		@CommandLine.Option(required = true, names = {"-a", "--active"})
		private boolean active;
	}

	private final Tasks tasks;
	private final OSInterface osInterface;

	FinishCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		if (options.list != null) {
			String list = this.options.list;

			TaskList taskList = tasks.finishList(list);

			System.out.println("Finished list '" + taskList.getFullPath() + "'");
			System.out.println();
		}
		else if (this.options.group != null) {
			String group = this.options.group;

			TaskGroup taskGroup = tasks.finishGroup(group);

			System.out.println("Finished group '" + taskGroup.getFullPath() + "'");
			System.out.println();
		}
		else if (options.active) {
			Task task = tasks.finishTask();

			System.out.println("Finished task " + task.description());
			System.out.println();
			System.out.print("Task finished in: ");
			System.out.println(new TaskDuration(task, osInterface));
			System.out.println();
		}
		else {
			Task task;

			task = tasks.finishTask(options.id);

			System.out.println("Finished task " + task.description());
			System.out.println();
			System.out.print("Task finished in: ");
			System.out.println(new TaskDuration(task, osInterface));
			System.out.println();
		}
	}
}
