// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.util.Objects;

final class Task {
	final int id;
	final String task;
	
	Task(int id, String task) {
		this.id = id;
		this.task = task;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Task task1 = (Task) o;
		return id == task1.id &&
				Objects.equals(task, task1.task);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, task);
	}
	
	@Override
	public String toString() {
		return id + " - \"" + task + "\"";
	}
}
