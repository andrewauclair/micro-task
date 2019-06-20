// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

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
	
	final long start;
	final long stop;
	
	Task(int id, String task, TaskState state) {
		this.id = id;
		this.task = task;
		this.state = state;
		this.start = 0;
		this.stop = 0;
	}
	
	Task(int id, String task, TaskState state, long start, long stop) {
		this.id = id;
		this.task = task;
		this.state = state;
		this.start = start;
		this.stop = stop;
	}
	
	Task activate(long start) {
		return new Task(id, task, TaskState.Active, start, 0);
	}
	
	Task finish(long stop) {
		return new Task(id, task, TaskState.Finished, start, stop);
	}
	
	Task stop(long stop) {
		return new Task(id, task, TaskState.Inactive, start, stop);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Task otherTask = (Task) o;
		return id == otherTask.id &&
				Objects.equals(task, otherTask.task) &&
				state == otherTask.state &&
				start == otherTask.start &&
				stop == otherTask.stop;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, task, state, start, stop);
	}
	
	// TODO make a description method and have the toString print all the info, this will help in tests that fail
	@Override
	public String toString() {
		return id + " - \"" + task + "\"";
	}
}
