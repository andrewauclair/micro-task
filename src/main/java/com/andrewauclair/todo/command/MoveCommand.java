// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.TaskException;
import com.andrewauclair.todo.jline.GroupCompleter;
import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.task.TaskList;
import com.andrewauclair.todo.task.Tasks;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "move")
final class MoveCommand implements Runnable {
	private final Tasks tasks;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.ArgGroup(multiplicity = "1")
	private Args args;

	private static class Args {
		@Option(names = {"-t", "--task"}, split = ",")
		private Integer[] id;

		@Option(names = {"-l", "--list"}, completionCandidates = ListCompleter.class)
		private String list;

		@Option(names = {"-g", "--group"}, completionCandidates = GroupCompleter.class)
		private String group;
	}

	@Option(names = {"--dest-group"}, completionCandidates = GroupCompleter.class)
	private String dest_group;

	@Option(names = {"--dest-list"}, completionCandidates = ListCompleter.class)
	private String dest_list;

	MoveCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		if (args.id != null) {
			if (dest_list == null) {
				throw new TaskException("move --task requires --dest-list");
			}

			for (Integer taskID : args.id) {
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

	private void moveTask(String list, long taskID) {
		TaskList taskList = tasks.getListForTask(taskID);
		taskList.moveTask(taskID, tasks.getListByName(list));

		System.out.println("Moved task " + taskID + " to list '" + list + "'");
	}
}
