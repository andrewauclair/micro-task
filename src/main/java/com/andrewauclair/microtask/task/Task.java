// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import java.util.*;

public final class Task {
	private final long id; // TODO it would be great if this was an instance of ExistingID

	private final FullTaskID fullID; // set once when task is created and never changes
	private RelativeTaskID shortID = RelativeTaskID.NO_SHORT_ID;

	public final String task;
	public final TaskState state;
	public final long addTime;
	public final long finishTime;
	public final List<TaskTimes> startStopTimes;
	public final boolean recurring;
	public final long dueTime;

	public final List<String> tags;

	public Task(long id, String task, TaskState state, long addTime, long finishTime, List<TaskTimes> startStopTimes, boolean recurring, long dueTime, List<String> tags) {
		this.id = id;

		this.fullID = new FullTaskID(id);

		this.task = task;
		this.state = state;
		this.addTime = addTime;
		this.finishTime = finishTime;
		this.startStopTimes = Collections.unmodifiableList(startStopTimes);
		this.recurring = recurring;
		this.dueTime = dueTime;
		this.tags = Collections.unmodifiableList(tags);
	}

	public long ID() {
		return id;
	}

	public FullTaskID fullID() {
		return fullID;
	}

	public RelativeTaskID shortID() {
		return shortID;
	}

	public void setShortID(RelativeTaskID shortID) {
		this.shortID = shortID;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, fullID, task, state, addTime, finishTime, startStopTimes, recurring, dueTime, tags);
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
				Objects.equals(fullID, otherTask.fullID) &&
				Objects.equals(task, otherTask.task) &&
				state == otherTask.state &&
				addTime == otherTask.addTime &&
				finishTime == otherTask.finishTime &&
				Objects.equals(startStopTimes, otherTask.startStopTimes) &&
				recurring == otherTask.recurring &&
				dueTime == otherTask.dueTime &&
				Objects.equals(tags, otherTask.tags);
	}

	@Override
	public String toString() {
		return "Task{" +
				"id=" + id +
				", task='" + task + '\'' +
				", state=" + state +
				", addTime=" + addTime +
				", finishTime=" + (finishTime == TaskTimes.TIME_NOT_SET ? "None" : finishTime) +
				", startStopTimes=" + startStopTimes +
				", recurring=" + recurring +
				", due=" + dueTime +
				", tags=" + tags +
				'}';
	}

	public String description() {
		return id + " - '" + task + "'";
	}
}
