// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.build;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.task.*;

import java.util.ArrayList;
import java.util.List;

public final class TaskBuilder {
	private final long id;
	private final List<TaskTimes> taskTimes = new ArrayList<>();
	private String task;
	private TaskState state;
	private boolean recurring;

	public TaskBuilder(long id) {
		this.id = id;
	}

	public TaskBuilder(Task task) {
		id = task.id;
		this.task = task.task;
		state = task.state;
		taskTimes.addAll(task.getAllTimes());
		recurring = task.isRecurring();
	}

	public TaskBuilder withName(String name) {
		if (state == TaskState.Finished) {
			throw new TaskException("Task " + id + " cannot be renamed because it has been finished.");
		}
		task = name;
		return this;
	}

	public TaskBuilder withState(TaskState state) {
		if (this.state == TaskState.Finished && state != TaskState.Finished) {
			taskTimes.remove(taskTimes.size() - 1);
		}
		this.state = state;
		return this;
	}

	public TaskBuilder withTime(TaskTimes time) {
		taskTimes.add(time);
		return this;
	}

	public TaskBuilder withRecurring(boolean recurring) {
		this.recurring = recurring;
		return this;
	}

	public Task start(long start, Tasks tasks) {
		taskTimes.add(new TaskTimes(start, new TaskFinder(tasks).getProjectForTask(new ExistingID(tasks, id)), new TaskFinder(tasks).getFeatureForTask(new ExistingID(tasks, id))));
		state = TaskState.Active;
		return build();
	}

	public Task build() {
		return new Task(id, task, state, taskTimes, recurring);
	}

	public Task finish(long stop) {
		if (state == TaskState.Active) {
			addStopTime(stop);
		}
		state = TaskState.Finished;

		// finish time
		taskTimes.add(new TaskTimes(stop));

		return build();
	}

	private void addStopTime(long stop) {
		TaskTimes lastTime = taskTimes.remove(taskTimes.size() - 1);

		TaskTimes stopTime = new TaskTimes(lastTime.start, stop, lastTime.project, lastTime.feature);
		taskTimes.add(stopTime);
	}

	public Task stop(long stop) {
		addStopTime(stop);
		state = TaskState.Inactive;
		return build();
	}
}
