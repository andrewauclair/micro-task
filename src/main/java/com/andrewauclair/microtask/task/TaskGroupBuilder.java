// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

public class TaskGroupBuilder {
	private String name;
	private TaskGroup parent = null;
	private TaskContainerState state = TaskContainerState.InProgress;

	private String project = "";
	private String feature = "";

	public static TaskGroup createRootGroup() {
		return new TaskGroup(TaskGroup.ROOT_PATH, null, "", "", TaskContainerState.InProgress);
	}

	public TaskGroupBuilder(TaskGroup group) {
		this.name = group.getName();
	}

	public TaskGroup build() {
		return new TaskGroup(name, parent, project, feature, state);
	}
}
