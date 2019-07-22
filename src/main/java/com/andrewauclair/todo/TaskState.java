// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

public enum TaskState {
	Inactive(0, "Inactive"),
	Active(1, "Active"),
	Finished(2, "Finished");

	final int value;
	final String name;

	TaskState(int value, String name) {
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
