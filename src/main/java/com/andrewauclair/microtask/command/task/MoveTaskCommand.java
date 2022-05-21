// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.task;

import com.andrewauclair.microtask.command.group.SetGroupCommand;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine;

import java.util.Comparator;
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
			List<Task> tasks = this.tasks.getList(args.src_list).getTasks().stream()
					.filter(task -> task.state != TaskState.Finished)
					.sorted(Comparator.comparingLong(o -> o.ID()))
					.collect(Collectors.toList());

			args.id = new ExistingID[tasks.size()];

			for (int i = 0; i < tasks.size(); i++) {
				args.id[i] = new ExistingID(this.tasks, tasks.get(i).ID());
			}
		}

		for (ExistingID taskID : args.id) {
			boolean move = true;

			if (interactive) {
				System.out.println(tasks.getTask(taskID).description());
				move = osInterface.promptChoice("move task " + taskID.get());
			}

			if (move) {
				moveTask(dest_list, taskID);
			}
		}
		System.out.println();
	}

	private void moveTask(ExistingListName list, ExistingID taskID) {
		TaskList taskList = tasks.getListForTask(taskID);
		taskList.moveTask(taskID, tasks.getListByName(list));

		System.out.println("Moved task " + taskID.get() + " to list '" + list + "'");
	}
}
