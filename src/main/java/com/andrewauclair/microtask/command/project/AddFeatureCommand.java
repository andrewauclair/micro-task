// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.project;

import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.project.NewFeature;
import com.andrewauclair.microtask.project.Project;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;
import picocli.CommandLine;

@CommandLine.Command(name = "feature")
public class AddFeatureCommand implements Runnable {
	private final Tasks tasks;
	private final Projects projects;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Parameters(index = "0", description = "The project to add the feature to.")
	private ExistingProject project;

	@CommandLine.Parameters(index = "1", description = "The feature to add.")
	private String feature;

	public AddFeatureCommand(Tasks tasks, Projects projects) {
		this.tasks = tasks;
		this.projects = projects;
	}

	private boolean hasList() {
		TaskListFinder finder = new TaskListFinder(tasks);
		return finder.hasList(new TaskListName(tasks, "/projects/" + project.getName() + "/" + feature) {});
	}

	private boolean hasGroup() {
		TaskGroupFinder finder = new TaskGroupFinder(tasks);
		return finder.hasGroupPath(new TaskGroupName(tasks, "/projects/" + project.getName() + "/" + feature + "/"){});
	}

	@Override
	public void run() {
		Project project = projects.getProject(this.project);

		if (!hasList() && !hasGroup()) {
			tasks.addList(new NewTaskListName(tasks, "/projects/" + project.getName() + "/" + feature), true);
		}

		project.addFeature(new NewFeature(project, feature), true);

		System.out.println("Created feature '" + feature + "' for project '" + project.getName() + "'");
		System.out.println();
	}
}
