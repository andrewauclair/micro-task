// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.update;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.Tasks;

import java.util.Optional;

import static com.andrewauclair.microtask.task.ActiveContext.NO_ACTIVE_TASK;

public class TaskStateUpdater {
	private final Tasks tasks;
	private final Projects projects;
	private final OSInterface osInterface;

	public TaskStateUpdater(Tasks tasks, Projects projects, OSInterface osInterface) {
		this.tasks = tasks;
		this.projects = projects;
		this.osInterface = osInterface;
	}

	public Task startTask(ExistingID id, boolean finishActive) {
		Task currentTask = tasks.getListForTask(id).getTask(id);

		if (currentTask.state == TaskState.Finished) {
			throw new TaskException("Task has already been finished.");
		}

		if (tasks.getActiveTaskID() == currentTask.ID().get().ID()) {
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

		tasks.setActiveTaskID(currentTask.ID().get().ID());
		tasks.setCurrentList(tasks.getActiveTaskList());
		tasks.setCurrentGroup(tasks.getActiveTaskList().parentGroupName());

		long startTime = osInterface.currentSeconds();

		if (lastTask.isPresent()) {
			int size = lastTask.get().startStopTimes.size();
			startTime = lastTask.get().startStopTimes.get(size - 1).stop;
		}

		return tasks.getList(tasks.getActiveTaskList()).startTask(new ExistingID(tasks.idValidator(), tasks.getActiveTaskID()), startTime, tasks, projects);
	}

	public Task stopTask() {
		Task stoppedTask = tasks.getList(tasks.getActiveTaskList()).stopTask(new ExistingID(tasks.idValidator(), tasks.getActiveTaskID()));

		tasks.setActiveTaskID(NO_ACTIVE_TASK);

		return stoppedTask;
	}

	public Task finishTask(ExistingID id) {
		Task task = tasks.getListForTask(id).finishTask(id);

		if (id.get().ID() == tasks.getActiveTaskID()) {
			tasks.setActiveTaskID(NO_ACTIVE_TASK);
		}

		return task;
	}
}
