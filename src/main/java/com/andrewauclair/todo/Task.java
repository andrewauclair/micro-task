// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class Task {
	final int id;
	final String task;
	final TaskState state;
	private final List<TaskTimes> taskTimes;

	Task(int id, String task) {
		this.id = id;
		this.task = task;
		this.state = TaskState.Inactive;
		taskTimes = Collections.emptyList();
	}

	Task(int id, String task, TaskState state, TaskTimes times) {
		this.id = id;
		this.task = task;
		this.state = state;
		taskTimes = Collections.singletonList(times);
	}

	Task(int id, String task, TaskState state, List<TaskTimes> times) {
		this.id = id;
		this.task = task;
		this.state = state;
		taskTimes = Collections.unmodifiableList(times);
	}

	Task activate(long start) {
		List<TaskTimes> times = new ArrayList<>(taskTimes);
		times.add(new TaskTimes(start));
		return new Task(id, task, TaskState.Active, times);
	}

	Task finish(long stop) {
		List<TaskTimes> times = addStopTime(stop);
		return new Task(id, task, TaskState.Finished, times);
	}
	
	Task stop(long stop) {
		List<TaskTimes> times = addStopTime(stop);
		return new Task(id, task, TaskState.Inactive, times);
	}
	
	private List<TaskTimes> addStopTime(long stop) {
		List<TaskTimes> times = new ArrayList<>(taskTimes);
		TaskTimes lastTime = times.remove(times.size() - 1);
		TaskTimes stopTime = new TaskTimes(lastTime.start, stop);
		times.add(stopTime);
		return times;
	}

	List<TaskTimes> getTimes() {
		return taskTimes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Task otherTask = (Task) o;

		return id == otherTask.id &&
				Objects.equals(task, otherTask.task) &&
				state == otherTask.state &&
				Objects.equals(taskTimes, otherTask.taskTimes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, task, state, taskTimes);
	}
	
	@Override
	public String toString() {
		return "Task{" +
				"id=" + id +
				", task='" + task + '\'' +
				", state=" + state +
				", taskTimes=" + taskTimes +
				'}';
	}
	
	String description() {
		return id + " - \"" + task + "\"";
	}
	
	enum TaskState {
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
}
