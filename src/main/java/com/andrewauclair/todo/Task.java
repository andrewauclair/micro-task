// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
	
	final TaskTimes times;
	
	Task(int id, String task) {
		this.id = id;
		this.task = task;
		this.state = TaskState.Inactive;
		times = new TaskTimes();
	}
	
	Task(int id, String task, TaskState state, TaskTimes.Times times) {
		this.id = id;
		this.task = task;
		this.state = state;
		this.times = new TaskTimes(Collections.singletonList(times));
	}
	
	Task(int id, String task, TaskState state, List<TaskTimes.Times> times) {
		this.id = id;
		this.task = task;
		this.state = state;
		this.times = new TaskTimes(times);
	}
	
	Task activate(long start) {
		List<TaskTimes.Times> times = new ArrayList<>(this.times.asList());
		times.add(new TaskTimes.Times(start));
		return new Task(id, task, TaskState.Active, times);
	}
	
	Task finish(long stop) {
		List<TaskTimes.Times> times = new ArrayList<>(this.times.asList());
		TaskTimes.Times lastTime = times.remove(times.size() - 1);
		TaskTimes.Times stopTime = new TaskTimes.Times(lastTime.start, stop);
		times.add(stopTime);
		return new Task(id, task, TaskState.Finished, times);
	}
	
	Task stop(long stop) {
		List<TaskTimes.Times> times = new ArrayList<>(this.times.asList());
		TaskTimes.Times lastTime = times.remove(times.size() - 1);
		TaskTimes.Times stopTime = new TaskTimes.Times(lastTime.start, stop);
		times.add(stopTime);
		return new Task(id, task, TaskState.Inactive, times);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Task otherTask = (Task) o;
		
		return id == otherTask.id &&
				Objects.equals(task, otherTask.task) &&
				state == otherTask.state &&
				Objects.equals(times, otherTask.times);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, task, state, times);
	}
	
	// TODO make a description method and have the toString print all the info, this will help in tests that fail
	@Override
	public String toString() {
		return id + " - \"" + task + "\"";
	}
}
