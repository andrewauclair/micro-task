// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.TaskList;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "move", description = "Move a task, list or group.")
final class MoveCommand implements Runnable {
	private final Tasks tasks;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.ArgGroup(multiplicity = "1")
	private Args args;

	private static class Args {
		@Option(names = {"-t", "--task"}, split = ",", description = "Tasks to move.")
		private ExistingID[] id;

		@Option(names = {"-l", "--list"}, completionCandidates = ListCompleter.class, description = "List to move.")
		private ExistingListName list;

		@Option(names = {"-g", "--group"}, completionCandidates = GroupCompleter.class, description = "Group to move.")
		private ExistingGroupName group;
	}

	@Option(names = {"--dest-group"}, completionCandidates = GroupCompleter.class, description = "Destination group for list or group.")
	private ExistingGroupName dest_group;

	@Option(names = {"--dest-list"}, completionCandidates = ListCompleter.class, description = "Destination list for task.")
	private ExistingListName dest_list;

	MoveCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		if (args.id != null) {
			if (dest_list == null) {
				throw new TaskException("move --task requires --dest-list");
			}

			for (ExistingID taskID : args.id) {
				moveTask(dest_list, taskID);
			}
			System.out.println();
		}
		else if (args.list != null) {
			if (dest_group == null) {
				throw new TaskException("move --list requires --dest-group");
			}

			tasks.moveList(args.list, dest_group);

			System.out.println("Moved list " + args.list + " to group '" + dest_group + "'");
			System.out.println();
		}
		else {
			if (dest_group == null) {
				throw new TaskException("move --group requires --dest-group");
			}

			tasks.moveGroup(args.group, dest_group);

			System.out.println("Moved group '" + args.group + "' to group '" + dest_group + "'");
			System.out.println();
		}
	}

	private void moveTask(ExistingListName list, ExistingID taskID) {
		TaskList taskList = tasks.getListForTask(taskID);
		taskList.moveTask(taskID, tasks.getListByName(list));

		System.out.println("Moved task " + taskID.get() + " to list '" + list + "'");
	}
}
