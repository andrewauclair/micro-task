// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.GroupCompleter;
import com.andrewauclair.todo.task.TaskGroup;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

@CommandLine.Command(name = "chgrp")
public class GroupSwitchCommand extends Command {
	@CommandLine.Parameters(index = "0")
	private String group;

	private final Tasks tasks;

	GroupSwitchCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		String group = this.group;

		if (group.equals("..")) {
			if (tasks.getActiveGroup().getFullPath().equals("/")) {
				return;
			}
			group = tasks.getActiveGroup().getParent();
		}
		TaskGroup group1 = tasks.switchGroup(group);

		System.out.println("Switched to group '" + group1.getFullPath() + "'");
		System.out.println();
	}
}
