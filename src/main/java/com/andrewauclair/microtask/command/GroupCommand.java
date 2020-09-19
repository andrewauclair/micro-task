// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import picocli.CommandLine;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN;

public class GroupCommand implements Runnable {
	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Option(names = {"-g", "--group"}, completionCandidates = GroupCompleter.class, description = "List tasks in this group.")
	private ExistingGroupName group;

	@CommandLine.Option(names = {"--finished"}, description = "List finished tasks.")
	private boolean finished;

	private final Tasks tasks;

	public GroupCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		TaskGroup taskGroup = tasks.getCurrentGroup();

		if (group != null) {
			taskGroup = tasks.getGroup(group);
		}

		List<TaskContainer> children = taskGroup.getChildren().stream()
				.sorted(Comparator.comparing(TaskContainer::getName))
				.collect(Collectors.toList());

		if (group == null) {
			System.out.println("Current group is '" + taskGroup.getFullPath() + "'");
		}
		else {
			System.out.println("Group '" + group + "'");
		}
		System.out.println();

		for (TaskContainer child : children) {
			if (child instanceof TaskList) {
				printListRelative((TaskList) child, finished);
			}
			else if (finished == (child.getState() == TaskContainerState.Finished)) {
				System.out.print("  ");
				System.out.println(child.getName() + "/");
			}
		}

		System.out.println();
	}

	private void printListRelative(TaskList list, boolean finished) {
		if (list.getFullPath().equals(tasks.getCurrentList().absoluteName())) {
			System.out.print("* ");
			ConsoleColors.println(System.out, ANSI_FG_GREEN, list.getName());
		}
		else if (finished == (list.getState() == TaskContainerState.Finished)) {
			System.out.print("  ");
			System.out.println(list.getName());
		}
	}
}
