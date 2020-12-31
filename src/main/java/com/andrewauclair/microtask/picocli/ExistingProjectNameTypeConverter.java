// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.picocli;

import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.project.Projects;
import picocli.CommandLine;

public class ExistingProjectNameTypeConverter implements CommandLine.ITypeConverter<ExistingProject> {
	private final Projects projects;

	public ExistingProjectNameTypeConverter(Projects projects) {
		this.projects = projects;
	}

	@Override
	public ExistingProject convert(String value) {
		try {
			return new ExistingProject(projects, value);
		}
		catch (RuntimeException e) {
			throw new CommandLine.TypeConversionException(e.getMessage());
		}
	}
}
