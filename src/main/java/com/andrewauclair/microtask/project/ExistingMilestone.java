// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.TaskException;

import java.util.Objects;

public final class ExistingMilestone {
	private final String name;

	public ExistingMilestone(Project project, String name) {
		if (!project.hasMilestone(name)) {
			throw new TaskException("Milestone '" + name + "' does not exist on project '" + project.getName() + "'");
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
		final ExistingMilestone milestone = (ExistingMilestone) o;
		return Objects.equals(name, milestone.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
