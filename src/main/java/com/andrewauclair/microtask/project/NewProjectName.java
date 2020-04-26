// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.TaskException;

public class NewProjectName {
	private final Projects projects;
	private final String name;

	public NewProjectName(Projects projects, String name) {
		this.projects = projects;
		this.name = name;
		if (projects.hasProject(name)) {
			throw new TaskException("Project '" + name + "' already exists");
		}
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}
}
