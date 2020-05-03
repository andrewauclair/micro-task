// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;

import java.util.*;

public class Feature {
	private final Tasks tasks;
	private final String name;
	private final Feature parent;

	private final List<ExistingListName> lists = new ArrayList<>();
	private final List<ExistingGroupName> groups = new ArrayList<>();

	public Feature(Tasks tasks, String name, Feature parent) {
		this.tasks = tasks;
		this.name = name;
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public Feature getParent() {
		return parent;
	}

	public void addList(ExistingListName listName) {
		lists.add(listName);
	}

	public void addGroup(ExistingGroupName groupName) {
		groups.add(groupName);
	}

	public List<Task> getTasks() {
		Set<Task> tasks = new HashSet<>();
		lists.forEach(list -> tasks.addAll(this.tasks.getList(list).getTasks()));
		groups.forEach(group -> tasks.addAll(this.tasks.getGroup(group).getTasks()));

		return new ArrayList<>(tasks);
	}
}
