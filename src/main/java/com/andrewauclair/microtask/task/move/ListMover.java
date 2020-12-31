// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.move;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.TaskList;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;

public class ListMover {
	private final Tasks tasks;
	private final OSInterface osInterface;

	public ListMover(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	public void moveList(ExistingListName list, ExistingGroupName group) {
		TaskList currentList = tasks.getList(list);
		TaskList newList = tasks.getGroupForList(list).moveList(currentList, tasks.getGroup(group.absoluteName()), System.out, osInterface);

		if (tasks.getCurrentList().equals(list)) {
			tasks.setCurrentList(new ExistingListName(tasks, newList.getFullPath()));
		}
	}
}
