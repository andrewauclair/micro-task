// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.TaskException;

import java.util.Objects;

public final class ExistingFeature {
	private final String name;

	public ExistingFeature(Project project, String name) {
		if (!project.hasFeature(name)) {
			throw new TaskException("Feature '" + name + "' does not exist on project '" + project.getName() + "'");
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
		if (!(o instanceof ExistingFeature that)) {
			return false;
		}
		return Objects.equals(getName(), that.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
