// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.project.ExistingFeature;
import com.andrewauclair.microtask.project.ExistingMilestone;
import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.andrewauclair.microtask.task.TaskGroup.ROOT_PATH;

public class ActiveContext {
	public static final int NO_ACTIVE_TASK = -1;

	private long activeTaskID = NO_ACTIVE_TASK;
	private ExistingListName activeList = null;
	private ExistingGroupName activeGroup = null;
	private ExistingProject activeProject;
	private ExistingFeature activeFeature;
	private ExistingMilestone activeMilestone;
	private List<String> activeTags = Collections.emptyList();

	private ExistingListName currentList;
	private ExistingGroupName currentGroup;

	public ActiveContext(Tasks tasks) {
		currentGroup = new ExistingGroupName(tasks, ROOT_PATH);
	}

	public void setActiveTaskID(long taskID) {
		activeTaskID = taskID;
	}

	public long getActiveTaskID() {
		return activeTaskID;
	}

	public void setActiveList(ExistingListName list) {
		activeList = list;
	}

	public void setNoActiveList() {
		activeList = null;
	}

	public Optional<ExistingListName> getActiveList() {
		return Optional.ofNullable(activeList);
	}

	public void setActiveGroup(ExistingGroupName group) {
		activeGroup = group;
	}

	public void setNoActiveGroup() {
		activeGroup = null;
	}

	public Optional<ExistingGroupName> getActiveGroup() {
		return Optional.ofNullable(activeGroup);
	}

	public void setActiveProject(ExistingProject project) {
		activeProject = project;
	}

	public void setNoActiveProject() {
		activeProject = null;
	}

	public Optional<ExistingProject> getActiveProject() {
		return Optional.ofNullable(activeProject);
	}

	public void setActiveFeature(ExistingFeature feature) {
		activeFeature = feature;
	}

	public void setNoActiveFeature() {
		activeFeature = null;
	}

	public Optional<ExistingFeature> getActiveFeature() {
		return Optional.ofNullable(activeFeature);
	}

	public void setActiveMilestone(ExistingMilestone milestone) {
		activeMilestone = milestone;
	}

	public void setNoActiveMiletone() {
		activeMilestone = null;
	}

	public Optional<ExistingMilestone> getActiveMilestone() {
		return Optional.ofNullable(activeMilestone);
	}

	public void setActiveTags(List<String> tags) {
		activeTags = tags;
	}

	public void setNoActiveTags() {
		activeTags = Collections.emptyList();
	}

	public List<String> getActiveTags() {
		return Collections.unmodifiableList(activeTags);
	}

	public void setCurrentList(ExistingListName list) {
		currentList = list;
	}

	public ExistingListName getCurrentList() {
		return currentList;
	}

	public void setCurrentGroup(ExistingGroupName group) {
		currentGroup = group;
	}

	public ExistingGroupName getCurrentGroup() {
		return currentGroup;
	}
}
