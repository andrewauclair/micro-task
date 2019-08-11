// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import java.util.ArrayList;
import java.util.List;

class TaskBuilder {
	private final long id;
	private String task;
	private TaskState state;
	private final List<TaskTimes> taskTimes;
	private long issue;
	private String charge;

	TaskBuilder(Task task) {
		id = task.id;
		this.task = task.task;
		state = task.state;
		taskTimes = new ArrayList<>(task.getTimes());
		issue = task.getIssue();
		charge = task.getCharge();
	}

	TaskBuilder withIssue(long issue) {
		this.issue = issue;
		return this;
	}

	TaskBuilder withCharge(String charge) {
		this.charge = charge;
		return this;
	}
	
	Task activate(long start) {
		taskTimes.add(new TaskTimes(start));
		state = TaskState.Active;
		return build();
	}
	
	Task finish(long stop) {
		if (state == TaskState.Active) {
			addStopTime(stop);
		}
		state = TaskState.Finished;
		return build();
	}
	
	Task stop(long stop) {
		addStopTime(stop);
		state = TaskState.Inactive;
		return build();
	}
	
	Task rename(String name) {
		task = name;
		return build();
	}
	
	private void addStopTime(long stop) {
		TaskTimes lastTime = taskTimes.remove(taskTimes.size() - 1);
		
		TaskTimes stopTime = new TaskTimes(lastTime.start, stop);
		taskTimes.add(stopTime);
	}

	Task build() {
		return new Task(id, task, state, taskTimes, issue, charge);
	}
}
