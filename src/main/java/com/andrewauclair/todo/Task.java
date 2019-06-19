// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.lang.annotation.IncompleteAnnotationException;
import java.util.Objects;

final class Task {
	enum TaskState {
		Inactive(0, "Inactive"),
		Active(1, "Active"),
		Finished(2, "Finished");
		
		int value;
		String name;
		
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
	
	final int id;
	final String task;
	final TaskState state;
	
	Task(int id, String task, TaskState state) {
		this.id = id;
		this.task = task;
		this.state = state;
	}
	
	Task activate() {
		return new Task(id, task, TaskState.Active);
	}
	
	Task finish() {
		return new Task(id, task, TaskState.Finished);
	}
	
	Task stop() {
		return new Task(id, task, TaskState.Inactive);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Task task1 = (Task) o;
		return id == task1.id &&
				Objects.equals(task, task1.task) &&
				state == task1.state;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, task, state);
	}
	
	// TODO make a description method and have the toString print all the info, this will help in tests that fail
	@Override
	public String toString() {
		return id + " - \"" + task + "\"";
	}
}
