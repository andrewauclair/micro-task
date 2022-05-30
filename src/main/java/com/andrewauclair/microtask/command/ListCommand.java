// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.task.TaskList;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "list", description = "List tasks or the content of a group.")
final class ListCommand implements Runnable {
	private final Tasks tasksData;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"-l", "--list"}, completionCandidates = ListCompleter.class, description = "List tasks on list.")
	private ExistingListName list;

	ListCommand(Tasks tasks) {
		this.tasksData = tasks;
	}

	@Override
	public void run() {
		ExistingListName listName = list != null ? list : tasksData.getCurrentList();

		if (list == null) {
			System.out.println("Current list is '" + tasksData.getCurrentList() + "'");
		}
		else {
			System.out.println("List '" + listName + "'");
		}
		System.out.println();

		System.out.print("Progress: ");

		TaskList currentList = tasksData.getList(listName);

		long finishedCount = currentList.getTasks().stream()
				.filter(task -> task.state == TaskState.Finished)
				.count();

		long totalCount = currentList.getTasks().size();

		int percent = (int) ((finishedCount / (double) totalCount) * 100);

		System.out.printf("%d / %d %s ", finishedCount, totalCount, progressBar(finishedCount, totalCount));
		System.out.printf("%d %%", percent);
		System.out.println();
		System.out.println();
	}

	private String progressBar(long finished, long total) {
		int percent = (int) ((finished / (double) total) * 100);

		return "[" +
				"=".repeat(Math.max(0, percent / 10)) +
				" ".repeat(Math.max(0, 10 - (percent / 10))) +
				"]";
	}
}
