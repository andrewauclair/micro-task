// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class Feature {
	private final OSInterface osInterface;
	private final Project project;
	private final Tasks tasks;
	private final String name;
	private final Feature parent;

	private final List<ExistingListName> lists = new ArrayList<>();
	private final List<ExistingGroupName> groups = new ArrayList<>();

	public Feature(OSInterface osInterface, Project project, Tasks tasks, String name, Feature parent) {
		this.osInterface = osInterface;
		this.project = project;
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

	public boolean containsList(ExistingListName list) {
		for (final ExistingGroupName group : groups) {
			if (tasks.getGroup(group).containsListAbsolute(list.absoluteName())) {
				return true;
			}
		}
		return lists.stream().anyMatch(list::equals);
	}

	public boolean containsGroup(ExistingGroupName group) {
		return groups.stream().anyMatch(group::equals);
	}

	public List<Task> getTasks() {
		Set<Task> tasks = new HashSet<>();
		lists.forEach(list -> tasks.addAll(this.tasks.getList(list).getTasks()));
		groups.forEach(group -> tasks.addAll(this.tasks.getGroup(group).getTasks()));

		return new ArrayList<>(tasks);
	}

	void save() {
		try (PrintStream outputStream = new PrintStream(osInterface.createOutputStream("git-data/tasks/projects/" + project.getName() + "/" + getName() + "/feature.txt"))) {
			outputStream.println("name " + getName());
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}
}
