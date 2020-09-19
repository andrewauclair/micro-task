// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.TaskException;

import java.util.Objects;

public class NewProject {
	private final String name;

	public NewProject(Projects projects, String name) {
		this.name = name;
		if (projects.hasProject(name)) {
			throw new TaskException("Project '" + name + "' already exists.");
		}
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof NewProject that)) {
			return false;
		}
		return Objects.equals(getName(), that.getName());
	}

	@Override
	public final int hashCode() {
		return Objects.hash(getName());
	}
}
