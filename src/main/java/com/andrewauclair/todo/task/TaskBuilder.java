// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import java.util.ArrayList;
import java.util.List;

public class TaskBuilder {
	private final long id;
	private String task;
	private TaskState state;
	private final List<TaskTimes> taskTimes;
	private boolean recurring;
	
	public TaskBuilder(Task task) {
		id = task.id;
		this.task = task.task;
		state = task.state;
		taskTimes = new ArrayList<>(task.getAllTimes());
		recurring = task.isRecurring();
	}

	TaskBuilder withState(TaskState state) {
		if (this.state == TaskState.Finished && state != TaskState.Finished) {
			taskTimes.remove(taskTimes.size() - 1);
		}
		this.state = state;
		return this;
	}

	TaskBuilder withRecurring(boolean recurring) {
		this.recurring = recurring;
		return this;
	}
	
	Task start(long start, Tasks tasks) {
		taskTimes.add(new TaskTimes(start, tasks.getProjectForTask(id), tasks.getFeatureForTask(id)));
		state = TaskState.Active;
		return build();
	}
	
	Task finish(long stop) {
		if (state == TaskState.Active) {
			addStopTime(stop);
		}
		state = TaskState.Finished;
		
		// finish time
		taskTimes.add(new TaskTimes(stop));
		
		return build();
	}
	
	Task stop(long stop) {
		addStopTime(stop);
		state = TaskState.Inactive;
		return build();
	}
	
	public Task rename(String name) {
		task = name;
		return build();
	}
	
	private void addStopTime(long stop) {
		TaskTimes lastTime = taskTimes.remove(taskTimes.size() - 1);

		TaskTimes stopTime = new TaskTimes(lastTime.start, stop, lastTime.project, lastTime.feature);
		taskTimes.add(stopTime);
	}

	Task build() {
		return new Task(id, task, state, taskTimes, recurring);
	}
}
