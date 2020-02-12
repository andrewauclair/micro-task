// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.os.GitLabReleases;
import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import static org.jline.builtins.Completers.TreeCompleter.node;

@CommandLine.Command(name = "update")
public class UpdateCommand extends Command {
	private static final int MAX_DISPLAYED_VERSIONS = 5;

	@CommandLine.Option(names = {"--tasks"})
	private boolean tasks;

	@CommandLine.Option(names = {"-r", "--releases"})
	private boolean releases;

	@CommandLine.Option(names = {"-l", "--latest"})
	private boolean latest;

	@CommandLine.Option(names = {"--release"})
	private String release;

	private final GitLabReleases gitLabReleases;
	private final Tasks tasksData;
	private final OSInterface osInterface;
	
	UpdateCommand(GitLabReleases gitLabReleases, Tasks tasks, OSInterface osInterface) {
		this.gitLabReleases = gitLabReleases;
		this.tasksData = tasks;
		this.osInterface = osInterface;
	}

	private void updateToVersion(PrintStream output, String version) {
		try {
			boolean updated = gitLabReleases.updateToRelease(version);
			
			if (updated) {
				output.println("Updated to version '" + version + "'");
			}
			else {
				output.println("Version '" + version + "' not found on GitLab");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			output.println("Failed to update to version '" + version + "'");
		}
	}

	@Override
	public void run() {
		List<String> versions;
		try {
			versions = gitLabReleases.getVersions();
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to get releases from GitLab");
			System.out.println();
			return;
		}

		if (releases) {
			System.out.println("Releases found on GitLab");
			System.out.println();

			int toDisplay = Math.min(versions.size(), MAX_DISPLAYED_VERSIONS);

			String currentVersion = "";

			try {
				currentVersion = osInterface.getVersion();
			}
			catch (IOException ignored) {
			}

			if (versions.size() > 5) {
				System.out.println((versions.size() - 5) + " older releases");
				System.out.println();

				if (!versions.subList(versions.size() - 5, versions.size()).contains(currentVersion) && !currentVersion.isEmpty()) {
					System.out.print(currentVersion);
					System.out.print(" ");
					System.out.print(ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN);
					System.out.print("-- current");
					System.out.println(ConsoleColors.ANSI_RESET);
				}
			}

			for (int i = versions.size() - toDisplay; i < versions.size(); i++) {
				if (i + 1 == versions.size()) {
					System.out.print(ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN);
				}
				System.out.print(versions.get(i));

				if (versions.get(i).equals(currentVersion)) {
					System.out.print(" ");
					System.out.print(ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN);
					System.out.print("-- current");

					if (i + 1 == versions.size()) {
						System.out.print(" & latest");
					}
					System.out.print(ConsoleColors.ANSI_RESET);
				}
				else if (i + 1 == versions.size()) {
					System.out.print(" -- latest");
					System.out.print(ConsoleColors.ANSI_RESET);
				}

				System.out.println();
			}
		}
		else if (latest) {
			updateToVersion(System.out, versions.get(versions.size() - 1));
		}
		else if (tasks) {
			List<Task> taskList = new ArrayList<>(tasksData.getAllTasks());

			taskList.sort(Comparator.comparingLong(o -> o.id));

			for (Task task : taskList) {
				String list = tasksData.findListForTask(task.id).getFullPath();
				tasksData.getWriter().writeTask(task, "git-data/tasks" + list + "/" + task.id + ".txt");
			}

			osInterface.runGitCommand("git add .", false);
			osInterface.runGitCommand("git commit -m \"Updating task files.\"", false);

			System.out.println("Updated all tasks.");
		}
		else if (release != null) {
			updateToVersion(System.out, release);
		}
		else {
			System.out.println("Invalid command.");
		}
		System.out.println();
	}
}
