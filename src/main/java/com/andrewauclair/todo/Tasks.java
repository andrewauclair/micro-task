// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.git.GitCommand;
import com.andrewauclair.todo.os.OSInterface;

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
	private final OSInterface osInterface;

	// this constructor should only be used in the tests
	Tasks() {
		// create an empty writer
		writer = new TaskWriter(new FileCreator() {
			@Override
			public OutputStream createOutputStream(String fileName) {
				return new ByteArrayOutputStream();
			}
		});
		osInterface = new OSInterface() {
			@Override
			public boolean runGitCommand(GitCommand command) {
				return true;
			}
		};
	}

	public Tasks(TaskWriter writer, OSInterface osInterface) {
		this.writer = writer;
		this.osInterface = osInterface;
	}

	Task addTask(String task) {
		Task newTask = new Task(startingID++, task, Task.TaskState.Inactive);
		tasks.add(newTask);

		writer.writeTask(newTask, "git-data/" + newTask.id + ".txt");

		osInterface.runGitCommand(new GitCommand("git add " + newTask.id + ".txt"));
		osInterface.runGitCommand(new GitCommand("git commit -m \"Adding task " + newTask.toString().replace("\"", "\\\"") + "\""));

		return newTask;
	}
	
	Task startTask(int id) {
		Optional<Task> first = tasks.stream()
				.filter(task -> task.id == id)
				.findFirst();

		if (first.isPresent()) {
			activeTaskID = first.get().id;
			Task activeTask = first.get();
			
			Task newActiveTask = activeTask.activate();
			tasks.remove(activeTask);
			tasks.add(newActiveTask);
			
			return newActiveTask;
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
			Task activeTask = first.get();
			
			Task finishedTask = activeTask.finish();
			
			return finishedTask;
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
