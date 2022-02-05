// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.tags;

import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "tags")
public class StartTagsCommand implements Runnable {
	private final Tasks tasks;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Parameters(description = "The tag(s) to start.", split = ",")
	private List<String> tags;

	public StartTagsCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		tasks.getActiveContext().setActiveTags(tags);

		System.out.println("Starting tag(s): " + String.join(", ", tags));
		System.out.println();
	}
}
