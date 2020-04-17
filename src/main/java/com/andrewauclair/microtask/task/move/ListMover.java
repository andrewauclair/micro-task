// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.move;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.TaskList;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingTaskListName;

public class ListMover {
	private final Tasks tasks;
	private final OSInterface osInterface;

	public ListMover(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	public void moveList(ExistingTaskListName list, ExistingTaskGroupName group) {
		TaskList currentList = tasks.getList(list);
		TaskList newList = tasks.getGroupForList(list).moveList(currentList, tasks.getGroup(group.absoluteName()), System.out, osInterface);

		if (tasks.getActiveList().equals(list)) {
			tasks.setActiveList(new ExistingTaskListName(tasks, newList.getFullPath()));
		}
	}
}
