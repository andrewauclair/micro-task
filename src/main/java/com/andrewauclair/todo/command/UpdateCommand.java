// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.os.GitLabReleases;
import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskLoader;
import com.andrewauclair.todo.task.TaskReader;
import com.andrewauclair.todo.task.Tasks;
import picocli.CommandLine;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

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

	@CommandLine.Option(names = {"--to-remote"})
	private boolean to_remote;

	@CommandLine.Option(names = {"--from-remote"})
	private boolean from_remote;

	@CommandLine.ArgGroup(exclusive = false)
	private ProxySettings proxy;

	static class ProxySettings {
		@CommandLine.Option(names = {"--proxy-ip"}, required = true)
		private InetAddress proxy_ip;

		@CommandLine.Option(names = {"--proxy-port"}, required = true)
		private int proxy_port;
	}

	private final GitLabReleases gitLabReleases;
	private final Tasks tasksData;
	private final Commands commands;
	private final OSInterface osInterface;
	
	UpdateCommand(GitLabReleases gitLabReleases, Tasks tasks, Commands commands, OSInterface osInterface) {
		this.gitLabReleases = gitLabReleases;
		this.tasksData = tasks;
		this.commands = commands;
		this.osInterface = osInterface;
	}

	private boolean updateToVersion(String version, Proxy proxy) {
		try {
			boolean updated = gitLabReleases.updateToRelease(version, proxy);
			
			if (updated) {
				System.out.println("Updated to version '" + version + "'");
				System.out.println();
				System.out.println("Press any key to shutdown. Please restart with the new version.");

				// force a restart
				System.in.read();

				return true;
			}
			else {
				System.out.println("Version '" + version + "' not found on GitLab");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to update to version '" + version + "'");
		}

		return false;
	}

	@Override
	public void run() {
		Proxy proxy = Proxy.NO_PROXY;

		boolean updatedToNewRelease = false;

		if (this.proxy != null) {
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.proxy.proxy_ip, this.proxy.proxy_port));
		}

		List<String> versions;
		try {
			versions = gitLabReleases.getVersions(proxy);
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
			updatedToNewRelease = updateToVersion(versions.get(versions.size() - 1), proxy);
		}
		else if (tasks) {
			List<Task> taskList = new ArrayList<>(tasksData.getAllTasks());

			taskList.sort(Comparator.comparingLong(o -> o.id));

			for (Task task : taskList) {
				String list = tasksData.findListForTask(task.id).getFullPath();
				tasksData.getWriter().writeTask(task, "git-data/tasks" + list + "/" + task.id + ".txt");
			}
			
			String currentVersion = "Unknown";
			
			try {
				currentVersion = osInterface.getVersion();
			}
			catch (IOException ignored) {
			}

			try (DataOutputStream output = osInterface.createOutputStream("git-data/task-data-version.txt")) {
				output.write(currentVersion.getBytes());
			}
			catch (IOException e) {
				e.printStackTrace(System.out);
			}

			osInterface.runGitCommand("git add .", false);
			osInterface.runGitCommand("git commit -m \"Updating task files to version '" + currentVersion + "'\"", false);

			tasksData.load(new TaskLoader(tasksData, new TaskReader(osInterface), osInterface), commands);

			System.out.println("Updated all tasks.");
		}
		else if (release != null) {
			updatedToNewRelease = updateToVersion(release, proxy);
		}
		else if (to_remote) {
			osInterface.runGitCommand("git push", false);

			System.out.println("Pushed changes to remote");
		}
		else if (from_remote) {
			osInterface.runGitCommand("git pull", false);

			tasksData.load(new TaskLoader(tasksData, new TaskReader(osInterface), osInterface), commands);

			System.out.println("Pulled changes from remote");
		}
		else {
			System.out.println("Invalid command.");
		}
		System.out.println();

		if (updatedToNewRelease) {
			osInterface.exit();
		}
	}
}
