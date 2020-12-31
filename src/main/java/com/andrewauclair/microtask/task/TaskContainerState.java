// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

public enum TaskContainerState {
	InProgress("InProgress"),
	Finished("Finished");

	private final String name;

	TaskContainerState(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
