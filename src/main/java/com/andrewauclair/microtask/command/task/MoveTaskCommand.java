// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.task;

import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine;

import java.util.List;
import java.util.stream.Collectors;

@CommandLine.Command(name = "task")
public class MoveTaskCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	private static class Args {
		@CommandLine.Parameters(index = "0", split = ",", description = "Tasks to move.")
		private ExistingID[] id;

		@CommandLine.Option(names = {"--src-list"}, completionCandidates = ListCompleter.class, description = "Source list for moving all tasks on a list.")
		private ExistingListName src_list;
	}

	@CommandLine.ArgGroup(multiplicity = "1")
	Args args;

	@CommandLine.Option(names = {"--dest-list"}, required = true, completionCandidates = ListCompleter.class, description = "Destination list for task.")
	private ExistingListName dest_list;

	@CommandLine.Option(names = {"--interactive"}, description = "Prompt y/n per task.")
	private boolean interactive;

	public MoveTaskCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		if (args.id == null) {
			// create list of IDs for all non-finished tasks on the given src list
			List<Task> tasks = this.tasks.getList(args.src_list).getTasks().stream()
					.filter(task -> task.state != TaskState.Finished)
					.sorted((o1, o2) -> ExistingID.compare(o1.ID(), o2.ID()))
					.collect(Collectors.toList());

			args.id = new ExistingID[tasks.size()];

			for (int i = 0; i < tasks.size(); i++) {
				args.id[i] = tasks.get(i).ID();
			}
		}

		TaskList list = tasks.getListByName(dest_list);

		for (ExistingID taskID : args.id) {
			if (interactive) {
				System.out.println(tasks.getTask(taskID).description());

				if (!osInterface.promptChoice("move task " + taskID.get())) {
					continue;
				}
			}

			moveTask(list, taskID);
		}
		System.out.println();
	}

	private void moveTask(TaskList list, ExistingID taskID) {
		TaskList taskList = tasks.getListForTask(taskID);
		taskList.moveTask(taskID, list);

		System.out.println("Moved task " + taskID.get() + " to list '" + list.getFullPath() + "'");
	}
}
