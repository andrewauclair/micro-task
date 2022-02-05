// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.jline;

import com.andrewauclair.microtask.project.Project;
import com.andrewauclair.microtask.project.Projects;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ProjectCompleter extends ArrayList<String> {
	public ProjectCompleter(Projects projects) {
		super(projects.getAllProjects().stream()
				.map(Project::getName)
				.collect(Collectors.toList()));
	}
}
