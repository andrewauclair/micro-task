// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.update;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.Tasks;

import java.util.Optional;

import static com.andrewauclair.microtask.task.Tasks.NO_ACTIVE_TASK;

public class TaskStateUpdater {
	private final Tasks tasks;
	private final OSInterface osInterface;

	public TaskStateUpdater(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	public Task startTask(ExistingID id, boolean finishActive) {
		Task currentTask = tasks.getListForTask(id).getTask(id);

		if (currentTask.state == TaskState.Finished) {
			throw new TaskException("Task has already been finished.");
		}

		if (tasks.getActiveTaskID() == currentTask.id) {
			throw new TaskException("Task is already active.");
		}

		Optional<Task> lastTask = Optional.empty();

		if (tasks.getActiveTaskID() != NO_ACTIVE_TASK) {
			if (finishActive) {
				lastTask = Optional.of(tasks.finishTask());
			}
			else {
				lastTask = Optional.of(tasks.stopTask());
			}
		}

		tasks.setActiveTaskID(currentTask.id);
		tasks.setActiveList(tasks.getActiveTaskList());
		tasks.setActiveGroup(tasks.getActiveTaskList().parentGroupName());

		long startTime = osInterface.currentSeconds();

		if (lastTask.isPresent()) {
			int size = lastTask.get().getStartStopTimes().size();
			startTime = lastTask.get().getStartStopTimes().get(size - 1).stop;
		}

		return tasks.getList(tasks.getActiveTaskList()).startTask(new ExistingID(tasks, tasks.getActiveTaskID()), startTime, tasks);
	}

	public Task stopTask() {
		Task stoppedTask = tasks.getList(tasks.getActiveTaskList()).stopTask(new ExistingID(tasks, tasks.getActiveTaskID()));

		tasks.setActiveTaskID(NO_ACTIVE_TASK);

		return stoppedTask;
	}

	public Task finishTask(ExistingID id) {
		Task task = tasks.getListForTask(id).finishTask(id);

		if (id.get() == tasks.getActiveTaskID()) {
			tasks.setActiveTaskID(NO_ACTIVE_TASK);
		}

		return task;
	}
}
