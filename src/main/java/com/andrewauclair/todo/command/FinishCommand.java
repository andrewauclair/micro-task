// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.*;
import org.jline.builtins.Completers;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

@CommandLine.Command(name = "finish")
public class FinishCommand extends Command {
	@CommandLine.Option(names = {"-t", "--task"})
	private Integer id;

	@CommandLine.Option(names = {"-l", "--list"})
	private String list;

	@CommandLine.Option(names = {"-g", "--group"})
	private String group;

	@CommandLine.Option(names = {"-a", "--active"})
	private boolean active;

	private final Tasks tasks;
	private final OSInterface osInterface;
	
	FinishCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		if (list != null) {
			String list = this.list;

			TaskList taskList = tasks.finishList(list);

			System.out.println("Finished list '" + taskList.getFullPath() + "'");
			System.out.println();
		}
		else if (this.group != null) {
			String group = this.group;

			TaskGroup taskGroup = tasks.finishGroup(group);

			System.out.println("Finished group '" + taskGroup.getFullPath() + "'");
			System.out.println();
		}
		else if (active) {
			Task task = tasks.finishTask();

			System.out.println("Finished task " + task.description());
			System.out.println();
			System.out.print("Task finished in: ");
			System.out.println(new TaskDuration(task, osInterface));
			System.out.println();
		}
		else {
			Task task;

//			if (this.id != null) {
				task = tasks.finishTask(id);
//			}
//			else {
//				task = tasks.finishTask();
//			}

			System.out.println("Finished task " + task.description());
			System.out.println();
			System.out.print("Task finished in: ");
			System.out.println(new TaskDuration(task, osInterface));
			System.out.println();
		}
	}
}
