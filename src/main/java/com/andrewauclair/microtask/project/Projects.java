// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.group.name.NewTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Projects {
	private final List<Project> projects = new ArrayList<>();
	private final Tasks tasks;
	private final OSInterface osInterface;

	private Project activeProject = null;

	public Projects(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	public List<Project> getAllProjects() {
		return Collections.unmodifiableList(projects);
	}

	public boolean hasProject(String name) {
		return projects.stream()
				.anyMatch(project -> project.getName().equals(name));
	}

	private String projectToGroup(String name) {
		return "/projects/" + name + "/";
	}

	public Project createProject(NewProject projectName, boolean save) {
		TaskGroupFinder finder = new TaskGroupFinder(tasks);

		String groupName = projectToGroup(projectName.getName());

		if (!finder.hasGroupPath(new TaskGroupName(tasks, groupName){})) {
			tasks.createGroup(new NewTaskGroupName(tasks, groupName), save);
		}

		ExistingGroupName group = new ExistingGroupName(tasks, groupName);

		Project project = new Project(osInterface, tasks, group, projectName.getName());
		projects.add(project);

		if (save) {
			tasks.addList(new NewTaskListName(tasks, group.absoluteName() + "general"), true); // TODO This needs to only happen if we're saving because when we load from the files we will be loading a general list and should not create it

			Task planning = tasks.addTask("Planning", new ExistingListName(tasks, group.absoluteName() + "general"));

			tasks.setRecurring(new ExistingID(tasks, planning.id), true);

			save();
		}

		return project;
	}

	private void save() {
		for (final Project project : projects) {
			try (PrintStream outputStream = new PrintStream(osInterface.createOutputStream("git-data/tasks" + project.getGroup().getFullPath() + "project.txt"))) {
				outputStream.println("name " + project.getName());
			}
			catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}
	}

	public Project getProject(ExistingProject name) {
		return projects.stream()
				.filter(project -> project.getName().equals(name.getName()))
				.findFirst().get();
	}

	public Project getProjectFromFeature(ExistingFeature feature) {
		return projects.stream()
				.filter(project -> project.hasFeature(feature.getName()))
				.findFirst().get();
	}

	public Project getProjectFromMilestone(ExistingMilestone milestone) {
		return projects.stream()
				.filter(project -> project.hasMilestone(milestone.getName()))
				.findFirst().get();
	}
	public void setActiveProject(ExistingProject name) {
		activeProject = getProject(name);
	}

	public Project getActiveProject() {
		return activeProject;
	}

	// TODO Not sure we really need this now that we have DataLoader and we're doing it in parallel with group loading
	public void load() {
		TaskGroupFinder finder = new TaskGroupFinder(tasks);

		if (finder.hasGroupPath(new TaskGroupName(tasks, "/projects/"){})) {
			TaskGroup projectGroup = tasks.getGroup(new ExistingGroupName(tasks, "/projects/"));

			for (final TaskContainer child : projectGroup.getChildren()) {
				if (child instanceof TaskGroup group) {
					if (osInterface.fileExists("git-data/tasks" + group.getFullPath() + "project.txt")) {
						createProject(new NewProject(this, group.getName()), false);
					}
				}
			}
		}
	}

	public String getProjectForList(TaskList list) {
		for (final Project project : projects) {
			if (project.getGroup().containsListAbsolute(list.getFullPath())) {
				return project.getName();
			}
		}
		return "";
	}

	public String getFeatureForList(TaskList list) {
		for (final Project project : projects) {
			if (project.getGroup().containsListAbsolute(list.getFullPath())) {
				for (final Feature feature : project.getFeatures()) {
					if (feature.containsList(new ExistingListName(tasks, list.getFullPath()))) {
						return feature.getName();
					}
				}
			}
		}
		return "";
	}
}
