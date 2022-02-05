// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.Tasks;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Milestone {
	private final Tasks tasks;
	private final Project project;
	private final OSInterface osInterface;
	private final String name;
	private final List<ExistingFeature> features = new ArrayList<>();
	private final List<ExistingID> taskIDs = new ArrayList<>();

	public Milestone(Tasks tasks, Project project, OSInterface osInterface, String name) {
		this.tasks = tasks;
		this.project = project;
		this.osInterface = osInterface;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public List<ExistingFeature> getFeatures() {
		return Collections.unmodifiableList(features);
	}

	public void addFeature(ExistingFeature feature) {
		features.add(feature);
	}

	public void removeFeature(ExistingFeature feature) {
		features.removeIf(feat -> feat.equals(feature));
	}

	public List<ExistingID> getTasks() {
		List<ExistingID> IDs = new ArrayList<>();

		for (final ExistingFeature feature : features) {
			IDs.addAll(project.getFeature(feature).getTasks().stream()
					.map(task -> new ExistingID(tasks, task.id))
					.collect(Collectors.toList()));
		}

		IDs.addAll(taskIDs);

		return IDs;
	}

	public void addTask(ExistingID taskID) {
		taskIDs.add(taskID);
	}

	public void removeTask(ExistingID taskID) {
		taskIDs.removeIf(t -> t.equals(taskID));
	}

	public void save() {
		try (PrintStream outputStream = new PrintStream(osInterface.createOutputStream("git-data/tasks/projects/" + project.getName() + "/milestone-" + name + ".txt"))) {
			outputStream.println("name " + name);

			for (final ExistingFeature feature : features) {
				outputStream.println("feature " + feature.getName());
			}

			for (final ExistingID task : taskIDs) {
				outputStream.println("task " + task.get());
			}
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}
}
