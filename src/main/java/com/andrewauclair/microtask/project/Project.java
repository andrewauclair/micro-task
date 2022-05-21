// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Project {
	private final OSInterface osInterface;

	private final Tasks tasks;
	private final ExistingGroupName group;
	private final String name;

	private final List<Feature> features = new ArrayList<>();
	private final List<Milestone> milestones = new ArrayList<>();

	public Project(OSInterface osInterface, Tasks tasks, ExistingGroupName group, String name) {
		this.osInterface = osInterface;
		this.tasks = tasks;
		this.group = group;
		this.name = name;
	}

	public TaskGroup getGroup() {
		return tasks.getGroup(group);
	}

	public String getName() {
		return group.shortName();
	}

	public void addFeature(NewFeature feature, boolean save) {
		Feature newFeature = new Feature(osInterface, this, tasks, feature.getName(), null);
		features.add(newFeature);

		if (getGroup().containsListAbsolute(getGroup().getFullPath() + feature.getName())) {
			newFeature.addList(new ExistingListName(tasks, getGroup().getFullPath() + feature.getName()));
		}

		if (save) {
			newFeature.save();
		}
	}

	public int getFeatureCount() {
		return getFeatureCountForGroup(tasks.getGroup(group));
	}

	private int getFeatureCountForGroup(TaskGroup group) {
		int count = 0;
		for (final TaskContainer child : group.getChildren()) {
			count++;
			if (child instanceof TaskGroup) {
				// TODO Pattern matching instanceof (JDK 16+), had to remove --enable-preview for now
				TaskGroup childGroup = (TaskGroup) child;

				count += getFeatureCountForGroup(childGroup);
			}
		}
		return count;
	}

	public long getFinishedFeatureCount() {
		return getFinishedFeatureCountForGroup(tasks.getGroup(group));
	}

	private long getFinishedFeatureCountForGroup(TaskGroup group) {
		int count = 0;
		for (final TaskContainer child : group.getChildren()) {
			if (child.getState() == TaskContainerState.Finished) {
				count++;
			}
			if (child instanceof TaskGroup) {
				// TODO Pattern matching instanceof (JDK 16+), had to remove --enable-preview for now
				TaskGroup childGroup = (TaskGroup) child;

				count += getFinishedFeatureCountForGroup(childGroup);
			}
		}
		return count;
	}

	public long getTaskCount() {
		return tasks.getGroup(group).getTasks().size();
	}

	public long getFinishedTaskCount() {
		return tasks.getGroup(group).getTasks().stream()
				.filter(task -> task.state == TaskState.Finished)
				.count();
	}

	public boolean hasFeature(String feature) {
		return features.stream().anyMatch(feat -> Objects.equals(feat.getName(), feature));
	}

	public void addMilestone(NewMilestone milestone, boolean save) {
		Milestone ms = new Milestone(tasks, this, osInterface, milestone.getName());
		milestones.add(ms);

		if (save) {
			ms.save();
		}
	}

	public boolean hasMilestone(String milestone) {
		return milestones.stream().anyMatch(ms -> ms.getName().equals(milestone));
	}

	public Milestone getMilestone(ExistingMilestone milestone) {
		for (final Milestone ms : milestones) {
			if (ms.getName().equals(milestone.getName())) {
				return ms;
			}
		}
		return null;
	}

	List<Feature> getFeatures() {
		return Collections.unmodifiableList(features);
	}

	public Feature getFeature(ExistingFeature name) {
		for (final Feature feature : features) {
			if (feature.getName().equals(name.getName())) {
				return feature;
			}
		}
		return null;
	}
}
