// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

@CommandLine.Command(name = "mklist")
public class ListCreateCommand extends Command {
	@CommandLine.Parameters(index = "0")
	private String list;

	private final Tasks tasks;
	
	ListCreateCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		String list = this.list.toLowerCase();

		boolean added = tasks.addList(list, true);

		String actualList = tasks.getAbsoluteListName(list);

		if (added) {
			System.out.println("Created new list '" + actualList + "'");
		}
		else {
			System.out.println("List '" + actualList + "' already exists.");
		}
		System.out.println();
	}
}
