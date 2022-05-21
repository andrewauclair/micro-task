// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.TaskException;

import java.util.Objects;

public final class NewFeature {
	private final String name;

	public NewFeature(Project project, String name) {
		if (project.hasFeature(name)) {
			throw new TaskException("Feature '" + name + "' already exists on project '" + project.getName() + "'");
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
		if (!(o instanceof NewFeature)) {
			return false;
		}
		// TODO Pattern matching instanceof (JDK 16+), had to remove --enable-preview for now
		NewFeature that = (NewFeature) o;

		return Objects.equals(getName(), that.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
