// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.move;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.TaskGroup;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;

public class GroupMover {
	private final Tasks tasks;
	private final OSInterface osInterface;

	public GroupMover(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	public void moveGroup(ExistingGroupName group, ExistingGroupName destGroup) {
		TaskGroup groupToMove = tasks.getGroup(group);

		TaskGroup activeGroup = tasks.getCurrentGroup();

		TaskGroup newGroup = tasks.getGroup(groupToMove.getParent()).moveGroup(groupToMove, tasks.getGroup(destGroup), System.out, osInterface);

		if (activeGroup.getFullPath().equals(groupToMove.getFullPath())) {
			tasks.setCurrentGroup(new ExistingGroupName(tasks, newGroup.getFullPath()));
		}
	}
}
