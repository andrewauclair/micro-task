// Copyright (C) 2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.schedule;

import com.andrewauclair.microtask.project.Project;
import com.sun.source.tree.UsesTree;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Schedule {
	private final Map<Project, Integer> percents = new HashMap<>();

	public void scheduleProject(Project project, int percent) {
		percents.put(project, percent);
	}

	public boolean hasProject(String project) {
		return percents.keySet().stream()
				.anyMatch(prj -> project.equals(prj.getName()));
	}

	public int projectPercent(String project) {
		Optional<Map.Entry<Project, Integer>> first = percents.entrySet().stream()
				.filter(set -> project.equals(set.getKey().getName()))
				.findFirst();

		if (first.isPresent()) {
			return first.get().getValue();
		}
		return 0;
	}
}
