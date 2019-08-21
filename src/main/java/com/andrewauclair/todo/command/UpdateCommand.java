// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.os.GitLabReleases;
import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class UpdateCommand extends Command {
	private static final int MAX_DISPLAYED_VERSIONS = 5;
	
	private final GitLabReleases gitLabReleases;
	private final Tasks tasks;
	private final OSInterface osInterface;
	
	public UpdateCommand(GitLabReleases gitLabReleases, Tasks tasks, OSInterface osInterface) {
		this.gitLabReleases = gitLabReleases;
		this.tasks = tasks;
		this.osInterface = osInterface;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		String[] s = command.split(" ");
		
		String arg = s[1];
		
		List<String> versions;
		try {
			versions = gitLabReleases.getVersions();
		}
		catch (IOException e) {
			e.printStackTrace();
			output.println("Failed to get releases from GitLab");
			output.println();
			return;
		}
		
		switch (arg) {
		case "-r":
		case "--releases":
			output.println("Releases found on GitLab");
			output.println();
			
			int toDisplay = Math.min(versions.size(), MAX_DISPLAYED_VERSIONS);
			
			if (versions.size() > 5) {
				output.println((versions.size() - 5) + " older releases");
				output.println();
			}
			
			for (int i = versions.size() - toDisplay; i < versions.size(); i++) {
				if (i + 1 == versions.size()) {
					output.print(ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN);
				}
				output.print(versions.get(i));
				if (i + 1 == versions.size()) {
					output.print(ConsoleColors.ANSI_RESET);
				}
				output.println();
			}
			break;
		case "-l":
		case "--latest":
			updateToVersion(output, versions.get(versions.size() - 1));
			break;
		case "--tasks":
			List<Task> taskList = new ArrayList<>(tasks.getAllTasks());
			
			taskList.sort(Comparator.comparingLong(o -> o.id));
			
			for (Task task : taskList) {
				String list = tasks.findListForTask(task.id);
				tasks.getWriter().writeTask(task, "git-data/tasks" + list + "/" + task.id + ".txt");
				osInterface.runGitCommand("git add tasks" + list + "/" + task.id + ".txt");
			}
			
			osInterface.runGitCommand("git commit -m \"Updating task files.\"");
			
			output.println("Updated all tasks.");
			break;
		default:
			updateToVersion(output, arg);
			break;
		}
		output.println();
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
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(
				node("update",
						node("-r", "--releases"),
						node("-l", "--latest"),
						node("--tasks")
				)
		);
	}
}
