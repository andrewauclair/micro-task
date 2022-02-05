// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.TaskException;

import java.util.Objects;

public class ExistingProject {
	private final String name;

	public ExistingProject(Projects projects, String name) {
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

	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof ExistingProject that)) {
			return false;
		}
		return Objects.equals(getName(), that.getName());
	}

	@Override
	public final int hashCode() {
		return Objects.hash(getName());
	}
}
