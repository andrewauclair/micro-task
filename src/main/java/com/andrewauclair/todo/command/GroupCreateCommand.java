// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.TaskGroup;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

@CommandLine.Command(name = "mkgrp")
public class GroupCreateCommand extends Command {
	@CommandLine.Parameters(index = "0")
	private String group;

	private final Tasks tasks;

	GroupCreateCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		String group = this.group.toLowerCase();

		TaskGroup group1 = tasks.createGroup(group);

		System.out.println("Created group '" + group1.getFullPath() + "'");
		System.out.println();
	}
}
