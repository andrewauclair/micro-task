// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.picocli;

import com.andrewauclair.microtask.project.NewProjectName;
import com.andrewauclair.microtask.project.Projects;
import picocli.CommandLine;

public class NewProjectNameTypeConverter implements CommandLine.ITypeConverter<NewProjectName> {
	private final Projects projects;

	public NewProjectNameTypeConverter(Projects projects) {
		this.projects = projects;
	}

	@Override
	public NewProjectName convert(String value) throws Exception {
		try {
			return new NewProjectName(projects, value);
		}
		catch (RuntimeException e) {
			throw new CommandLine.TypeConversionException(e.getMessage());
		}
	}
}
