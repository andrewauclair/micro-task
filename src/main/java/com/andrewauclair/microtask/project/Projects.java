// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.task.Tasks;

import java.util.ArrayList;
import java.util.List;

public class Projects {
	private final List<Project> projects = new ArrayList<>();
	private final Tasks tasks;

	private Project activeProject = null;

	public Projects(Tasks tasks) {
		this.tasks = tasks;
	}

	public boolean hasProject(String name) {
		return projects.stream()
				.anyMatch(project -> project.getName().equals(name));
	}

	public Project createProject(String name) {
		Project project = new Project(tasks, name);
		projects.add(project);

		return project;
	}

	public Project getProject(String name) {
		return projects.stream()
				.filter(project -> project.getName().equals(name))
				.findFirst().get();
	}

	public void setActiveProject(String name) {
		activeProject = getProject(name);
	}

	public Project getActiveProject() {
		return activeProject;
	}
}
