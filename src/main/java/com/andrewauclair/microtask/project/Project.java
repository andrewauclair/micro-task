// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.task.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Project {
	private final Tasks tasks;
	private final String name;

	private final List<Feature> features = new ArrayList<>();

	public Project(Tasks tasks, String name) {
		this.tasks = tasks;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addFeature(String feature, String parent) {
		// TODO Throw exception if parent task doesn't exist
		// TODO Check if parent task is null
		Feature featureParent = null;
		String parentName = "";
		if (parent != null) {
			featureParent = getFeature(parent).get();
			parentName = featureParent.getName();
		}
		String name = feature;
		if (!parentName.isEmpty()) {
			name = parentName + "/" + feature;
		}
		features.add(new Feature(tasks, name, featureParent));
	}

	public Optional<Feature> getFeature(String feature) {
		for (final Feature existingFeature : features) {
			if (existingFeature.getName().equals(feature)) {
				return Optional.of(existingFeature);
			}
		}
		return Optional.empty();
	}

	public boolean hasFeature(String feature) {
		return features.stream()
				.anyMatch(existingFeature -> existingFeature.getName().equals(feature));
	}

	public int getFeatureCount() {
		return features.size();
	}

	public int getTaskCount() {
		int total = 0;

		for (final Feature feature : features) {
			total += feature.getTasks().size();
		}
		return total;
	}

	public int getFinishedTaskCount() {
		int total = 0;

		for (final Feature feature : features) {
			total += feature.getTasks().stream()
					.filter(task -> task.state == TaskState.Finished)
					.count();
		}

		return total;
	}
}
