// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.task;

import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.TaskList;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine;

@CommandLine.Command(name = "task")
public class MoveTaskCommand implements Runnable {
	private final Tasks tasks;
	private final OSInterface osInterface;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Parameters(index = "0", split = ",", description = "Tasks to move.")
	private ExistingID[] id;

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
		for (ExistingID taskID : id) {
			boolean move = true;

			if (interactive) {
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
