// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskState;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.jline.builtins.Completers.TreeCompleter.node;

@CommandLine.Command(name = "next")
public class NextCommand extends Command {
	@CommandLine.Option(names = {"-c", "--count"})
	private int count;

	private final Tasks tasks;
	
	NextCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		int max = count;

		List<Task> tasks = this.tasks.getAllTasks().stream()
				.sorted(Comparator.comparingLong(o -> o.id))
				.filter(task -> task.state != TaskState.Finished)
				.filter(task -> !task.isRecurring())
				.limit(max)
				.collect(Collectors.toList());

		System.out.println("Next " + tasks.size() + " Tasks To Complete");
		System.out.println();

		tasks.forEach(task -> System.out.println(task.description()));

		System.out.println();
	}
}
