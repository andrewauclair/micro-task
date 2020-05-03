// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.TaskLoader;
import com.andrewauclair.microtask.task.TaskReader;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine;
import picocli.CommandLine.Option;

@CommandLine.Command(name = "repo", description = "Push/pull changes to/from remote repo.")
public class UpdateRepoCommand implements Runnable {
	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.ArgGroup()
	private Args args;

	private static class Args {
		@Option(names = {"--to-remote"}, description = "Push local changes to the remote repo.")
		private boolean to_remote;

		@Option(names = {"--from-remote"}, description = "Pull changes from the remote repo.")
		private boolean from_remote;
	}

	private final Tasks tasks;
	private final OSInterface osInterface;
	private final LocalSettings localSettings;
	private final Projects projects;
	private final Commands commands;

	public UpdateRepoCommand(Tasks tasks, OSInterface osInterface, LocalSettings localSettings, Projects projects, Commands commands) {
		this.tasks = tasks;
		this.osInterface = osInterface;
		this.localSettings = localSettings;
		this.projects = projects;
		this.commands = commands;
	}

	@Override
	public void run() {
		if (args.to_remote) {
			osInterface.runGitCommand("git push");

			System.out.println("Pushed changes to remote");
		}
		else if (args.from_remote) {
			osInterface.runGitCommand("git pull");

			tasks.load(new TaskLoader(tasks, new TaskReader(osInterface), localSettings, projects, osInterface), commands);

			System.out.println("Pulled changes from remote");
		}
		System.out.println();
	}
}
