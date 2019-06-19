// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class Tasks {
	private int startingID = 0;
	
	private int activeTaskID = -1;
	
	private final List<Task> tasks = new ArrayList<>();
	private final TaskWriter writer;

	// this constructor should only be used in the tests
	Tasks() {
		// create an empty writer
		writer = new TaskWriter(new FileCreator() {
			@Override
			public OutputStream createOutputStream(String fileName) {
				return new ByteArrayOutputStream();
			}
		});
	}

	public Tasks(TaskWriter writer) {
		this.writer = writer;
	}

	Task addTask(String task) {
		Task newTask = new Task(startingID++, task);
		tasks.add(newTask);

		writer.writeTask(newTask, "git-data/" + newTask.id + ".txt");

		return newTask;
	}
	
	Task startTask(int id) {
		Optional<Task> first = tasks.stream()
				.filter(task -> task.id == id)
				.findFirst();

		if (first.isPresent()) {
			activeTaskID = first.get().id;
			return first.get();
		}
		throw new RuntimeException("com.andrewauclair.todo.Task " + id + " was not found.");
	}
	
	Task stopTask() {
		Task activeTask = getActiveTask();
		activeTaskID = -1;
		return activeTask;
	}
	
	Task finishTask() {
		Optional<Task> first = tasks.stream()
				.filter(task -> task.id == activeTaskID)
				.findFirst();
		
		tasks.removeIf(task -> task.id == activeTaskID);
		
		if (first.isPresent()) {
			return first.get();
		}
		throw new RuntimeException("No active task.");
	}
	
	Task getActiveTask() {
		Optional<Task> first = tasks.stream()
				.filter(task -> task.id == activeTaskID)
				.findFirst();
		
		if (first.isPresent()) {
			return first.get();
		}
		throw new RuntimeException("No active task.");
	}
	
	List<Task> getTasks() {
		return tasks;
	}
}
