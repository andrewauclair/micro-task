// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.task.TaskContainer;
import com.andrewauclair.microtask.task.TaskGroup;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;

import java.util.ArrayList;
import java.util.List;

public class Project {
	private final Tasks tasks;
	private final ExistingTaskGroupName group;

	private final List<Feature> features = new ArrayList<>();

	public Project(Tasks tasks, ExistingTaskGroupName group) {
		this.tasks = tasks;
		this.group = group;
	}

	public String getName() {
		return group.shortName();
	}

	public int getFeatureCount() {
		return getFeatureCountForGroup(tasks.getGroup(group)) + 1;
	}

	private int getFeatureCountForGroup(TaskGroup group) {
		int count = 0;
		for (final TaskContainer child : group.getChildren()) {
			count++;
			if (child instanceof TaskGroup childGroup) {
				count += getFeatureCountForGroup(childGroup);
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
}
