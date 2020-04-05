// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

public enum TaskState {
	Inactive("Inactive"),
	Active("Active"),
	Finished("Finished");

	private final String name;

	TaskState(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
