// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.os.OSInterface;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Task {
	public final long id;
	public final String task;
	public final TaskState state;
	private final List<TaskTimes> taskTimes;
	private final boolean recurring;
	
	public Task(long id, String task, TaskState state, List<TaskTimes> times) {
		this.id = id;
		this.task = task;
		this.state = state;
		taskTimes = Collections.unmodifiableList(times);
		recurring = false;
	}
	
	public Task(long id, String task, TaskState state, List<TaskTimes> times, boolean recurring) {
		this.id = id;
		this.task = task;
		this.state = state;
		taskTimes = Collections.unmodifiableList(times);
		this.recurring = recurring;
	}

	public List<TaskTimes> getTimes() {
		return taskTimes;
	}

	public List<TaskTimes> getStartStopTimes() {
		return taskTimes.subList(1, taskTimes.size());
	}
	
	public long getElapsedTime(OSInterface osInterface) {
		long total = 0;
		for (TaskTimes time : getStartStopTimes()) {
			if (time.stop != TaskTimes.TIME_NOT_SET) {
				total += time.stop - time.start;
			}
			else {
				total += osInterface.currentSeconds() - time.start;
			}
		}
		return total;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, task, state, taskTimes, recurring);
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
				Objects.equals(taskTimes, otherTask.taskTimes) &&
				recurring == otherTask.recurring;
	}

	@Override
	public String toString() {
		return "Task{" +
				"id=" + id +
				", task='" + task + '\'' +
				", state=" + state +
				", taskTimes=" + taskTimes +
				", recurring=" + recurring +
				'}';
	}
	
	public String description() {
		return id + " - '" + task + "'";
	}

	public boolean isRecurring() {
		return recurring;
	}
}
