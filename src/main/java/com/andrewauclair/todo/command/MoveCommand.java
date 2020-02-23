// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.TaskException;
import com.andrewauclair.todo.jline.GroupCompleter;
import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.task.TaskList;
import com.andrewauclair.todo.task.Tasks;
import picocli.CommandLine;

@CommandLine.Command(name = "move")
public class MoveCommand extends Command {
	@CommandLine.Option(names = {"-t", "--task"}, split = ",")
	private Integer[] id;

	@CommandLine.Option(names = {"-l", "--list"}, completionCandidates = ListCompleter.class)
	private String list;

	@CommandLine.Option(names = {"-g", "--group"}, completionCandidates = GroupCompleter.class)
	private String group;

	@CommandLine.Option(names = {"--dest-group"}, completionCandidates = GroupCompleter.class)
	private String dest_group;

	@CommandLine.Option(names = {"--dest-list"}, completionCandidates = ListCompleter.class)
	private String dest_list;

	private final Tasks tasks;

	MoveCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		if (id != null) {
			if (dest_list == null) {
				throw new TaskException("move --task requires --dest-list");
			}
			
			for (Integer taskID : id) {
				moveTask(dest_list, taskID);
			}
			System.out.println();
		}
		else if (this.list != null) {
			if (dest_group == null) {
				throw new TaskException("move --list requires --dest-group");
			}

			tasks.moveList(list, dest_group);

			System.out.println("Moved list " + list + " to group '" + dest_group + "'");
			System.out.println();
		}
		else if (this.group != null) {
			if (dest_group == null) {
				throw new TaskException("move --group requires --dest-group");
			}
			
			tasks.moveGroup(group, dest_group);

			System.out.println("Moved group '" + group + "' to group '" + dest_group + "'");
			System.out.println();
		}
	}

	private void moveTask(String list, long taskID) {
		TaskList taskList = tasks.getListForTask(taskID);
		taskList.moveTask(taskID, tasks.getListByName(list));

		System.out.println("Moved task " + taskID + " to list '" + list + "'");
	}
}
