// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.picocli;

import com.andrewauclair.microtask.project.NewProject;
import com.andrewauclair.microtask.project.Projects;
import picocli.CommandLine;

public class NewProjectNameTypeConverter implements CommandLine.ITypeConverter<NewProject> {
	private final Projects projects;

	public NewProjectNameTypeConverter(Projects projects) {
		this.projects = projects;
	}

	@Override
	public NewProject convert(String value) {
		try {
			return new NewProject(projects, value);
		}
		catch (RuntimeException e) {
			throw new CommandLine.TypeConversionException(e.getMessage());
		}
	}
}
