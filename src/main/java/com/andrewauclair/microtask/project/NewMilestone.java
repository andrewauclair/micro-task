// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.TaskException;

import java.util.Objects;

public final class NewMilestone {
	private final String name;

	public NewMilestone(Project project, String name) {
		if (project.hasMilestone(name)) {
			throw new TaskException("Milestone '" + name + "' already exists on project '" + project.getName() + "'");
		}
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final NewMilestone that = (NewMilestone) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
