// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.TaskException;
import com.andrewauclair.todo.jline.GroupCompleter;
import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.jline.RenameCompleter;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

@CommandLine.Command(name = "rename")
public class RenameCommand extends Command {
	@CommandLine.Option(names = {"-l", "--list"}, completionCandidates = ListCompleter.class)
	private String list;

	@CommandLine.Option(names = {"-g", "--group"}, completionCandidates = GroupCompleter.class)
	private String group;

	@CommandLine.Option(names = {"-t", "--task"})
	private Integer id;

	@CommandLine.Option(names = {"-n", "--name"})
	private String name;

	private final Tasks tasks;
	
	RenameCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	// TODO Although it isn't really possible, we shouldn't allow list and task at the same time. it'll just run like a list rename, as it always has, but it should throw an error, this should be part of the CommandParser options

	@Override
	public void run() {
		if (list != null) {
			String newName = this.name;
			String list = this.list;

			if (newName.contains("/")) {
				throw new TaskException("Lists must be renamed with name, not paths.");
			}

			if (list.contains("/")) {
				throw new TaskException("Lists must be renamed with name, not paths.");
			}

			tasks.renameList(list, newName);

			System.out.println("Renamed list '" + tasks.getAbsoluteListName(list) + "' to '" + tasks.getAbsoluteListName(newName) + "'");
			System.out.println();
		}
		else if (group != null) {
			String newName = this.name;
			String group = this.group;

			if (!group.endsWith("/")) {
				System.out.println("Old group name should end with /");
				System.out.println();
			}
			else if (!newName.endsWith("/")) {
				System.out.println("New group name should end with /");
				System.out.println();
			}
			else {
				String oldGroupPath = tasks.getGroup(group).getFullPath();

				tasks.renameGroup(group, newName);

				System.out.println("Renamed group '" + oldGroupPath + "' to '" + tasks.getGroup(newName).getFullPath() + "'");
				System.out.println();
			}
		}
		else if (id != null) {
			String newName = this.name;
			long taskID = id;

			Task task = tasks.renameTask(taskID, newName);

			System.out.println("Renamed task " + task.description());
			System.out.println();
		}
		else {
			System.out.println("Invalid command.");
			System.out.println();
		}
	}
}
