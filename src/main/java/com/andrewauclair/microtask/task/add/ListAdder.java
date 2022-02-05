// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.add;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.group.name.NewTaskGroupName;
import com.andrewauclair.microtask.task.list.TaskListFileWriter;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;

public class ListAdder {
	private final Tasks tasks;
	private final TaskWriter writer;
	private final OSInterface osInterface;

	public ListAdder(Tasks tasks, TaskWriter writer, OSInterface osInterface) {
		this.tasks = tasks;
		this.writer = writer;
		this.osInterface = osInterface;
	}

	public void addList(NewTaskListName listName, boolean createFiles) {
		TaskGroup group;

		String absoluteList = listName.absoluteName();

		String groupName = listName.parentGroupName().absoluteName();

		// create any groups in the path that don't exist
		tasks.createGroup(new TaskGroupName(tasks, listName.parentGroupName().absoluteName()){}, createFiles);

		group = tasks.getGroup(groupName);

		if (group.getState() == TaskContainerState.Finished && createFiles) {
			throw new TaskException("List '" + absoluteList + "' cannot be created because group '" + group.getFullPath() + "' has been finished.");
		}

		TaskList newList = new TaskList(listName.shortName(), group, osInterface, writer, TaskContainerState.InProgress);

		group.addChild(newList);

		if (createFiles) {
			osInterface.createFolder("git-data/tasks" + newList.getFullPath());

			new TaskListFileWriter(newList, osInterface).write();

			osInterface.gitCommit("Created list '" + newList.getFullPath() + "'");
		}
	}
}
