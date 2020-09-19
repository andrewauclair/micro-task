// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.update;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.build.TaskBuilder;
import com.andrewauclair.microtask.task.list.name.ExistingListName;

public class TaskRecurringUpdater {
	private final Tasks tasks;
	private final OSInterface osInterface;

	public TaskRecurringUpdater(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	public Task updateRecurring(ExistingID id, boolean recurring) {
		Task optionalTask = tasks.getTask(id);

		if (optionalTask.state == TaskState.Finished) {
			throw new TaskException("Cannot set task " + id.get() + " recurring state. The task has been finished.");
		}

		Task task = new TaskBuilder(optionalTask)
				.withRecurring(recurring)
				.build();

		String list = tasks.findListForTask(new ExistingID(tasks, task.id)).getFullPath();
		tasks.replaceTask(new ExistingListName(tasks, list), optionalTask, task);

		String file = "git-data/tasks" + list + "/" + task.id + ".txt";
		tasks.getWriter().writeTask(task, file);

		osInterface.gitCommit("Set recurring for task " + task.id + " to " + recurring);

		return task;
	}
}
