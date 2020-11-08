// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.build;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.*;

import java.util.ArrayList;
import java.util.List;

public final class TaskBuilder {
	private final long id;
	private String task;
	private TaskState state;
	private long addTime;
	private long finishTime = TaskTimes.TIME_NOT_SET;
	private final List<TaskTimes> startStopTimes = new ArrayList<>();
	private boolean recurring;
	private long dueTime;
	private final List<String> tags = new ArrayList<>();

	public TaskBuilder(long id) {
		this.id = id;
	}

	public TaskBuilder(Task task) {
		id = task.id;
		this.task = task.task;
		state = task.state;
		addTime = task.addTime;
		finishTime = task.finishTime;
		startStopTimes.addAll(task.startStopTimes);
		recurring = task.recurring;
		dueTime = task.dueTime;
		tags.addAll(task.tags);
	}

	public TaskBuilder withTask(String name) {
		if (state == TaskState.Finished) {
			throw new TaskException("Task " + id + " cannot be renamed because it has been finished.");
		}
		task = name;
		return this;
	}

	public TaskBuilder withState(TaskState state) {
		if (this.state == TaskState.Finished && state != TaskState.Finished) {
//			startStopTimes.remove(startStopTimes.size() - 1);
			finishTime = TaskTimes.TIME_NOT_SET;
		}
		this.state = state;
		return this;
	}

	public TaskBuilder withAddTime(long addTime) {
		this.addTime = addTime;
		return this;
	}

	public TaskBuilder withFinishTime(long finishTime) {
		this.finishTime = finishTime;
		return this;
	}

	public TaskBuilder withStartStopTime(TaskTimes time) {
		startStopTimes.add(time);
		return this;
	}

	public TaskBuilder withRecurring(boolean recurring) {
		this.recurring = recurring;
		return this;
	}

	public TaskBuilder withDueTime(long dueTime) {
		this.dueTime = dueTime;
		return this;
	}

	public TaskBuilder clearTags() {
		tags.clear();
		return this;
	}

	public TaskBuilder withTag(String tag) {
		tags.add(tag);
		return this;
	}

	public Task start(long start, Tasks tasks, Projects projects) {
		ExistingID existingID = new ExistingID(tasks, id);
		TaskList list = new TaskFinder(tasks).findListForTask(existingID);
		String project = projects.getProjectForList(list);
		String feature = projects.getFeatureForList(list);
		startStopTimes.add(new TaskTimes(start, project, feature));
		state = TaskState.Active;
		return build();
	}

	public Task build() {
		return new Task(id, task, state, addTime, finishTime, startStopTimes, recurring, dueTime, tags);
	}

	public Task finish(long finishTime) {
		if (state == TaskState.Active) {
			addStopTime(finishTime);
		}
		state = TaskState.Finished;

		// finish time
		this.finishTime = finishTime;

		return build();
	}

	private void addStopTime(long stop) {
		TaskTimes lastTime = startStopTimes.remove(startStopTimes.size() - 1);

		TaskTimes stopTime = new TaskTimes(lastTime.start, stop, lastTime.project, lastTime.feature);
		startStopTimes.add(stopTime);
	}

	public Task stop(long stop) {
		addStopTime(stop);
		state = TaskState.Inactive;
		return build();
	}
}
