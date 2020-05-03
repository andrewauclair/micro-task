// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.picocli;

import com.andrewauclair.microtask.project.ExistingProjectName;
import com.andrewauclair.microtask.project.Projects;
import picocli.CommandLine;

public class ExistingProjectNameTypeConverter implements CommandLine.ITypeConverter<ExistingProjectName> {
	private final Projects projects;

	public ExistingProjectNameTypeConverter(Projects projects) {
		this.projects = projects;
	}

	@Override
	public ExistingProjectName convert(String value) {
		try {
			return new ExistingProjectName(projects, value);
		}
		catch (RuntimeException e) {
			throw new CommandLine.TypeConversionException(e.getMessage());
		}
	}
}
