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
		
		writeTask(newTask);
		
		addAndCommit(newTask, "Adding task");
		
		return newTask;
	}
	
	private void writeTask(Task task) {
		writer.writeTask(task, "git-data/" + task.id + ".txt");
	}
	
	private void addAndCommit(Task task, final String comment) {
		osInterface.runGitCommand(new GitCommand("git add " + task.id + ".txt"));
		osInterface.runGitCommand(new GitCommand("git commit -m \"" + comment + " " + task.toString().replace("\"", "\\\"") + "\""));
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
			
			writeTask(newActiveTask);
			addAndCommit(newActiveTask, "Starting task");
			
			return newActiveTask;
		}
		throw new RuntimeException("com.andrewauclair.todo.Task " + id + " was not found.");
	}
	
	Task stopTask() {
		Task activeTask = getActiveTask();
		activeTaskID = -1;
		
		Task stoppedTask = activeTask.stop();
		tasks.remove(activeTask);
		tasks.add(stoppedTask);
		
		return stoppedTask;
	}
	
	Task finishTask() {
		Optional<Task> first = tasks.stream()
				.filter(task -> task.id == activeTaskID)
				.findFirst();
		
		if (first.isPresent()) {
			activeTaskID = -1;
			
			Task activeTask = first.get();
			
			Task finishedTask = activeTask.finish();
			
			tasks.remove(activeTask);
			tasks.add(finishedTask);
			
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
