// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

public class TaskGroupBuilder {
	private final String name;
	private final TaskGroup parent = null;
	private final TaskContainerState state = TaskContainerState.InProgress;
	private final String timeCategory = ""; // TODO implement

	public static TaskGroup createRootGroup() {
		return new TaskGroup(TaskGroup.ROOT_PATH, null, TaskContainerState.InProgress, "");
	}

	public TaskGroupBuilder(TaskGroup group) {
		this.name = group.getName();
	}

	public TaskGroup build() {
		return new TaskGroup(name, parent, state, timeCategory);
	}
}
