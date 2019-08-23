// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Task {
	public final long id;
	public final String task;
	public final TaskState state;
	private final List<TaskTimes> taskTimes;
	private final long issue;
	private final String project;
	private final String feature;
	
	public Task(long id, String task, TaskState state, List<TaskTimes> times) {
		this.id = id;
		this.task = task;
		this.state = state;
		taskTimes = Collections.unmodifiableList(times);
		issue = -1;
		project = "";
		feature = "";
	}

	public Task(long id, String task, TaskState state, List<TaskTimes> times, long issue, String project) {
		this(id, task, state, times, issue, project, "");
	}

	public Task(long id, String task, TaskState state, List<TaskTimes> times, long issue, String project, String feature) {
		this.id = id;
		this.task = task;
		this.state = state;
		taskTimes = Collections.unmodifiableList(times);
		this.issue = issue;
		this.project = project;
		this.feature = feature;
	}

	public List<TaskTimes> getTimes() {
		return taskTimes;
	}

	public List<TaskTimes> getStartStopTimes() {
		return taskTimes.subList(1, taskTimes.size());
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, task, state, taskTimes, issue, project, feature);
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
				Objects.equals(project, otherTask.project) &&
				Objects.equals(feature, otherTask.feature);
	}

	@Override
	public String toString() {
		return "Task{" +
				"id=" + id +
				", task='" + task + '\'' +
				", state=" + state +
				", taskTimes=" + taskTimes +
				", issue=" + issue +
				", project='" + project + '\'' +
				", feature='" + feature + '\'' +
				'}';
	}
	
	public String description() {
		return id + " - '" + task + "'";
	}
	
	public long getIssue() {
		return issue;
	}

	public String getProject() {
		return project;
	}

	public String getFeature() {
		return feature;
	}
}
