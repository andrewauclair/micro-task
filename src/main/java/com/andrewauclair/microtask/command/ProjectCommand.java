// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.project.ExistingProjectName;
import com.andrewauclair.microtask.project.Project;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingTaskListName;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_BOLD;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_PURPLE;

@Command(name = "project")
public class ProjectCommand implements Runnable {
	private final Tasks tasks;
	private final Projects projects;
	private final LocalSettings localSettings;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"-n", "--name"})
	private ExistingProjectName name;

	@Option(names = {"--feature"})
	private String feature;

	@Option(names = {"--add-list"}, completionCandidates = ListCompleter.class)
	private ExistingTaskListName list;

	@Option(names = {"--add-group"}, completionCandidates = GroupCompleter.class)
	private ExistingTaskGroupName group;

	@Option(names = {"--add-feature"})
	private String newFeature;

	@Option(names = {"--progress"})
	private boolean progress;

	public ProjectCommand(Tasks tasks, Projects projects, LocalSettings localSettings, OSInterface osInterface) {
		this.tasks = tasks;
		this.projects = projects;
		this.localSettings = localSettings;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		if (list != null) {
//			TaskList list = tasks.getListByName(this.list);

			// TODO Throw exception if the project already contains the list
			projects.getProject(name.getName()).getFeature(feature).get().addList(list);

			System.out.println("Added list '" + list + "' to project '" + name + "'");
		}
		else if (group != null) {
//			TaskGroup group = tasks.getGroup(this.group);

			// TODO Throw exception if the project already contains the group
			projects.getProject(name.getName()).getFeature(feature).get().addGroup(group);

			System.out.println("Added group '" + group + "' to project '" + name + "'");
		}
		else if (newFeature != null) {
			projects.getProject(name.getName()).addFeature(newFeature, null);

			System.out.println("Added feature '" + newFeature + "' to project '" + name + "'");
		}
		else if (progress) {
			System.out.println("Project progress for 'test'");
			System.out.println();

			Project project = projects.getProject(name.getName());

			int taskCount = project.getTaskCount();
			int finishedTaskCount = project.getFinishedTaskCount();
			int taskPercentComplete = (int) ((finishedTaskCount / (double)taskCount) * 100);

			String taskProgress = "";
			for (int i = 0; i < taskPercentComplete / 10; i++) {
				taskProgress += "=";
			}
			for (int i = 0; i < 10 - (taskPercentComplete / 10); i++) {
				taskProgress += " ";
			}

			System.out.println(String.format("Features  0 / %d [          ] 0%%", project.getFeatureCount()));
			System.out.println(String.format("Tasks     %d / %d [%s] %d%%", finishedTaskCount, taskCount, taskProgress, taskPercentComplete));

		}
		System.out.println();
	}
}
