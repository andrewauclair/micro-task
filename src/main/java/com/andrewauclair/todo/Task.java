// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Task {
	public final long id;
	public final String task;
	public final TaskState state;
	private final List<TaskTimes> taskTimes;
	private final long issue;
	private final String charge;
	
	Task(long id, String task) {
		this(id, task, TaskState.Inactive, Collections.emptyList());
	}

	Task(long id, String task, TaskState state, List<TaskTimes> times) {
		this.id = id;
		this.task = task;
		this.state = state;
		taskTimes = Collections.unmodifiableList(times);
		issue = -1;
		charge = "";
	}
	
	Task(long id, String task, TaskState state, List<TaskTimes> times, long issue, String charge) {
		this.id = id;
		this.task = task;
		this.state = state;
		taskTimes = Collections.unmodifiableList(times);
		this.issue = issue;
		this.charge = charge;
	}
	
	public List<TaskTimes> getTimes() {
		return taskTimes;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, task, state, taskTimes, issue, charge);
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
				issue == otherTask.issue &&
				Objects.equals(charge, otherTask.charge);
	}

	@Override
	public String toString() {
		return "Task{" +
				"id=" + id +
				", task='" + task + '\'' +
				", state=" + state +
				", taskTimes=" + taskTimes +
				", issue=" + issue +
				", charge='" + charge + '\'' +
				'}';
	}
	
	public String description() {
		return id + " - '" + task + "'";
	}
	
	public long getIssue() {
		return issue;
	}
	
	public String getCharge() {
		return charge;
	}
}
