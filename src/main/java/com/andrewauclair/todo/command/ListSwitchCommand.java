// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

@CommandLine.Command(name = "chlist")
public class ListSwitchCommand extends Command {
	@CommandLine.Parameters(index = "0")
	private String list;

	private final Tasks tasks;
	
	ListSwitchCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		String list = this.list.toLowerCase();

		tasks.setActiveList(list);

		String actualList = tasks.getAbsoluteListName(list);

		String group = tasks.getGroupForList(actualList).getFullPath();

		tasks.switchGroup(group);

		System.out.println("Switched to list '" + actualList + "'");
		System.out.println();
	}
}
