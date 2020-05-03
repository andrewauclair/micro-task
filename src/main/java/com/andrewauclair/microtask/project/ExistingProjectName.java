// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.TaskException;

public class ExistingProjectName {
	private final String name;

	public ExistingProjectName(Projects projects, String name) {
		this.name = name;
		if (!projects.hasProject(name)) {
			throw new TaskException("Project '" + name + "' doesn't exist.");
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
