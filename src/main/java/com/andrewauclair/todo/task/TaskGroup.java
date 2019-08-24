// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import java.util.*;

public final class TaskGroup implements TaskContainer {
	private final String name;
	private final String fullPath;
	private final String parent;
	
	private final List<TaskContainer> children = new ArrayList<>();
	
	public TaskGroup(String name) {
		this.name = name;
		fullPath = name;
		parent = null;
	}

	// name is relative and the parent is the absolute path of the parent
	TaskGroup(String name, String parent) {
		this.name = name;
		this.parent = parent;
		
		if (parent.equals("/")) {
			fullPath = "/" + name;
		}
		else {
			fullPath = parent + "/" + name;
		}
	}
	
	public String getName() {
		return name;
	}

	@Override
	public String getFullPath() {
		return fullPath;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, fullPath, parent, children);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TaskGroup taskGroup = (TaskGroup) o;
		return Objects.equals(name, taskGroup.name) &&
				Objects.equals(fullPath, taskGroup.fullPath) &&
				Objects.equals(parent, taskGroup.parent) &&
				Objects.equals(children, taskGroup.children);
	}

	public TaskGroup rename(String newName) {
		TaskGroup group = new TaskGroup(newName, parent);
		group.children.addAll(children);
		return group;
	}

	public String getParent() {
		return parent;
	}
	
	void addChild(TaskContainer child) {
		children.add(child);
	}
	
	void removeChild(TaskContainer child) {
		children.remove(child);
	}
	
	boolean containsListAbsolute(String name) {
		return children.stream()
				.filter(child -> child instanceof TaskList)
				.map(child -> (TaskList) child)
				.anyMatch(list -> list.getFullPath().equals(name));
	}

	// finds lists by their absolute name
	Optional<TaskList> getListAbsolute(String path) {
		return children.stream()
				.filter(child -> child instanceof TaskList)
				.map(child -> (TaskList) child)
				.filter(list -> list.getFullPath().equals(path))
				.findFirst();
	}

	Optional<TaskGroup> getGroupAbsolute(String path) {
		for (TaskContainer child : children) {
			if (child instanceof TaskGroup) {
				TaskGroup group = (TaskGroup) child;

				Optional<TaskGroup> result = group.getGroupAbsolute(path);

				if (result.isPresent()) {
					return result;
				}
			}
		}
		if (getFullPath().equals(path)) {
			return Optional.of(this);
		}
		return Optional.empty();
	}

	public List<TaskContainer> getChildren() {
		return Collections.unmodifiableList(children);
	}

	@Override
	public String toString() {
		return "TaskGroup{" +
				"name='" + name + '\'' +
				", fullPath='" + fullPath + '\'' +
				", parent=" + (parent == null ? "" : parent) +
				", children=" + children +
				'}';
	}
	
	boolean containsGroup(TaskGroup newGroup) {
		return children.stream()
				.filter(child -> child instanceof TaskGroup)
				.map((child -> (TaskGroup) child))
				.anyMatch(group -> group.getFullPath().equals(newGroup.getFullPath()));
	}

	Optional<String> findListForTask(long id) {
		for (TaskContainer child : getChildren()) {
			if (child instanceof TaskList) {
				TaskList list = (TaskList) child;

				if (list.containsTask(id)) {
					return Optional.of(list.getFullPath());
				}
			}
			else {
				Optional<String> list = ((TaskGroup) child).findListForTask(id);

				if (list.isPresent()) {
					return list;
				}
			}
		}
		return Optional.empty();
	}
}
