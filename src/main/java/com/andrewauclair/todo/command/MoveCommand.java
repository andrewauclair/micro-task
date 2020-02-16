// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

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
			String list = this.dest_list;

			for (Integer taskID : id) {
				moveTask(list, taskID);
			}
			System.out.println();
		}
		else if (this.list != null) {
			String list = this.list;
			String group = this.dest_group;

			tasks.moveList(list, group);

			System.out.println("Moved list " + list + " to group '" + group + "'");
			System.out.println();
		}
		else if (this.group != null) {
			String srcGroup = this.group;
			String destGroup = this.dest_group;

			tasks.moveGroup(srcGroup, destGroup);

			System.out.println("Moved group '" + srcGroup + "' to group '" + destGroup + "'");
			System.out.println();
		}
	}

	private void moveTask(String list, long taskID) {
		TaskList taskList = tasks.getListForTask(taskID);
		taskList.moveTask(taskID, tasks.getListByName(list));

		System.out.println("Moved task " + taskID + " to list '" + list + "'");
	}
}
