// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.TaskException;

import java.util.Objects;

// suppressing warning because it isn't valid here. The ExistingFeature(String) constructor is private, there for we can't make a proper matching record.
public final class ExistingFeature {
	private final String name;

	private ExistingFeature(String name) {
		this.name = name;
	}

	public static ExistingFeature tryCreate(Project project, String name) {
		if (!project.hasFeature(name)) {
			throw new TaskException("Feature '" + name + "' does not exist on project '" + project.getName() + "'");
		}
		return new ExistingFeature(name);
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
		if (!(o instanceof ExistingFeature)) {
			return false;
		}
		// TODO Pattern matching instanceof (JDK 16+), had to remove --enable-preview for now
		ExistingFeature that = (ExistingFeature) o;

		return Objects.equals(getName(), that.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
