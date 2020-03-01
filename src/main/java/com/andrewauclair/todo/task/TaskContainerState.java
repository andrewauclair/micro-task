// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

public enum TaskContainerState {
	InProgress(0, "InProgress"),
	Active(1, "Active"),
	Finished(2, "Finished");

	private final int value;
	private final String name;

	TaskContainerState(int value, String name) {
		this.value = value;
		this.name = name;
	}

	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return name;
	}
}
