// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.util.Collections;
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
	
	private final long start;
	private final long stop;
	
	final TaskTimes times;
	
	Task(int id, String task, TaskState state) {
		this.id = id;
		this.task = task;
		this.state = state;
		this.start = 0;
		this.stop = 0;
		times = new TaskTimes();
	}
	
	Task(int id, String task, TaskState state, TaskTimes.Times times) {
		this.id = id;
		this.task = task;
		this.state = state;
		this.start = times.start;
		this.stop = times.stop;
		this.times = new TaskTimes(Collections.singletonList(times));
	}
	
	Task activate(long start) {
		return new Task(id, task, TaskState.Active, new TaskTimes.Times(start));
	}
	
	Task finish(long stop) {
		return new Task(id, task, TaskState.Finished, new TaskTimes.Times(times.asList().get(0).start, stop));
	}
	
	Task stop(long stop) {
		return new Task(id, task, TaskState.Inactive, new TaskTimes.Times(times.asList().get(0).start, stop));
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
				stop == otherTask.stop &&
				Objects.equals(times, otherTask.times);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, task, state, start, stop, times);
	}
	
	// TODO make a description method and have the toString print all the info, this will help in tests that fail
	@Override
	public String toString() {
		return id + " - \"" + task + "\"";
	}
}
