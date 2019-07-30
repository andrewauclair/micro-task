// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Task;
import com.andrewauclair.todo.Tasks;
import com.andrewauclair.todo.os.GitLabReleases;
import org.jline.builtins.Completers;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class UpdateCommand extends Command {
	private final GitLabReleases gitLabReleases;
	private final Tasks tasks;

	public UpdateCommand(GitLabReleases gitLabReleases, Tasks tasks) {
		this.gitLabReleases = gitLabReleases;
		this.tasks = tasks;
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
		
		if (arg.equals("-r") || arg.equals("--releases")) {
			output.println("Releases found on GitLab");
			output.println();
			versions.forEach(output::println);
		}
		else if (arg.equals("-l") || arg.equals("--latest")) {
			updateToVersion(output, versions.get(versions.size() - 1));
		}
		else if (arg.equals("--tasks")) {
			List<Task> taskList = new ArrayList<>(tasks.getAllTasks());

			taskList.sort(Comparator.comparingLong(o -> o.id));

			for (Task task : taskList) {
				String list = tasks.findListForTask(task.id);
				tasks.writer.writeTask(task, "git-data/tasks/" + list + "/" + task.id + ".txt");
				tasks.osInterface.runGitCommand("git add tasks/" + list + "/" + task.id + ".txt");
			}

			tasks.osInterface.runGitCommand("git commit -m \"Updating task files.\"");

			output.println("Updated all tasks.");
		}
		else {
			updateToVersion(output, arg);
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
